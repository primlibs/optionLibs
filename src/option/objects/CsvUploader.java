/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.objects;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.prim.core.AbstractApplication;
import com.prim.support.FormatDate;
import com.prim.support.MyString;
import com.prim.core.model.DinamicModel;
import com.prim.core.model.Model;
import com.prim.core.model.ModelFactory;
import com.prim.core.modelStructure.Field;
import com.prim.core.modelStructure.Structure;
import com.prim.core.select.Select;
import com.prim.core.select.Table;
import com.prim.core.select.TableSelectFactory;

/**
 * класс, который загружает данные из файла CSV в базу данных
 *
 * @author Pavel Rice
 */
public class CsvUploader {

  private AbstractApplication app;
  private ModelFactory modelFactory;
  private TableSelectFactory tableFactory;
  private File csv;
  private Map<String, Object> params;
  private List<String> errors = new ArrayList();
  private CsvWriter writer;
  private ByteArrayOutputStream baos;
  private CsvReader reader;
  private List<String> messages = new ArrayList();

  /**
   *
   * @param app объект Application
   * @param csv файл, из которого нужно загрузить данные
   * @param params параметры из запроса
   * @throws Exception
   */
  public CsvUploader(AbstractApplication app, File csv, Map<String, Object> params) throws Exception {
    this.app = app;
    this.modelFactory = new ModelFactory(app);
    this.tableFactory = new TableSelectFactory(app);
    this.csv = csv;
    this.params = params;
  }

  public List<String> getErrors() {
    return errors;
  }

  public List<String> getMessages() {
    return messages;
  }

  /**
   * загрузить данные из файла в БД
   *
   * Читается файл CSV. Каждая строка из файла сохраняется в БД. Если строка
   * успешно сохранена - она удаляется из файла. Если не удалось сохранить
   * строку - строка остается в файле + в отдельную ячейку записваются ошибки.
   *
   * @return
   * @throws Exception
   */
  public boolean uploadData() throws Exception {
    // данные должны быть прочтены из файла, а потом другие данные заново записаны в тот же файл.
    // реализовано так: данные читаются из файла, в это время новые данные записываются в массив байтов.
    // после завершения чтения файла, файл стирается, и в него записываются новые данные из массива байтов.
    try {
      this.baos = new ByteArrayOutputStream();
      reader = new CsvReader(new FileInputStream(csv), Charset.forName("UTF-8"));
      reader.setDelimiter(',');
      reader.readHeaders();
      this.writer = new CsvWriter(baos, reader.getDelimiter(), Charset.forName("UTF-8"));

      // прочитать заголовки файла
      String[] headers = reader.getHeaders();
      // записать заголовки в файл csv
      writeHeaders(headers);
      int number = 0;
      while (reader.readRecord()) {
        String[] values = reader.getValues();
        // загрузить данные из строки
        boolean ok = uploadRow(values, number);
        number++;
      }
      // записать всё в новый файл
      writeNewFile();
    } finally {
      if (writer != null) {
        writer.close();
      }
      if (reader != null) {
        reader.close();
      }
      if (baos != null) {
        baos.close();
      }
    }
    return true;
  }

  // записать данные заново в тот же файл
  private void writeNewFile() throws Exception {
    writer.close();
    byte[] bytes = baos.toByteArray();
    FileOutputStream fos = new FileOutputStream(csv);
    fos.write(bytes);
  }

  // записать заголовки во writer
  private void writeHeaders(String[] headers) throws Exception {
    for (String header : headers) {
      writer.write(header);
    }
    writer.write("errors");
    writer.endRecord();
  }

  /**
   * загрузить данные из строки файла в БД
   *
   * @param values
   * @param rowNumber
   * @return
   * @throws Exception
   */
  private boolean uploadRow(String[] values, int rowNumber) throws Exception {
    errors.clear();
    boolean ok = true;

    try {
      // создать массив моделей
      Map<String, Model> models = new LinkedHashMap();
      // сохранить данные в моделях
      ok = loadDataInModels(models, values, rowNumber);

      // сохранить модели
      if (ok) {
        ok = saveModels(models, rowNumber);
      }

    } catch (Exception exc) {
      ok = false;
      errors.add(MyString.getStackExeption(exc));
    }

    if (ok == false) {
      // строку записать в новый файл + записать ошибки
      writeRow(values);
    }

    return ok;
  }

  // загрузить данные в модели
  private boolean loadDataInModels(Map<String, Model> models, String[] values, int rowNumber) throws Exception {
    boolean ok = true;
    int columnNumber = 0;
    for (String value : values) {
      // если передан такой параметр, то есть имя поля
      if (params.containsKey("column_" + columnNumber) && params.get("column_" + columnNumber) != null && !value.isEmpty()) {
        // получить название модели и название поля
        String param = params.get("column_" + columnNumber).toString();
        String[] str = param.split(":");
        if (str.length == 2) {
          String modelName = str[0];
          String fieldName = str[1];
          // если нет модели такого типа
          if (!models.containsKey(modelName)) {
            // содать модель
            models.put(modelName, modelFactory.getModel(modelName));
          }
          // взять модель такого типа
          Model model = models.get(modelName);
          // получить это поле из структуры
          Structure struct = model.getStructure();
          Set<String> fieldNames = struct.getFieldsNames();
          // если в модели нет такого поля
          if (!fieldNames.contains(fieldName)) {
            // ошибка, выйти
            errors.add(" в модели " + modelName + " отсутствует поле " + fieldName);
            ok = false;
            break;
          }
          Field field = struct.getFieldClone(fieldName);
          // если это поле ИД
          boolean isRelation = field.getRelations() != null && !field.getRelations().isEmpty();
          if (fieldName.equals(struct.getPrimaryAlias())) {
            // записать значение в модель в поле oldId
            model.set("old_id", value);
          } // иначе если это поле ссылка на другую модель
          else if (isRelation && params.get("column_old_id_" + columnNumber) != null) {
            String relationTableName = field.getRelations();
            // найти в БД модель запись такого типа с таким oldId
            Integer relationId = searchModel(relationTableName, value);
            // если такая запись есть одна
            if (relationId > 0) {
              // ИД этой записи записать в модель в это поле
              model.set(fieldName, relationId);
            } else {
              // иначе если такой записи нет или больше одного
              // ошибка, выйти
              errors.add(" в модели " + modelName + " ошибка при поиске связанной мдели для поля " + fieldName);
              ok = false;
              break;
            }
          } // иначе
          else {
            // записать значение в модель в это поле
            if (value.isEmpty()) {
              value = null;
            }
            model.set(fieldName, value);
          }
        }
      }
      columnNumber++;
    }
    return ok;
  }

  // сохранить модели
  private boolean saveModels(Map<String, Model> models, int rowNumber) throws Exception {
    boolean ok = true;
    // открыть транзакцию
    Connection conn = app.getConnection();
    conn.setAutoCommit(false);
    // записывать модели по одной
    try {
      for (String modelName : models.keySet()) {
        Model model = models.get(modelName);
        Object id = saveModel(model, models);
        // если не удалось записать
        if (id == null) {
          // то прервать запись
          ok = false;
          break;
        }
      }
    } catch (Exception exc) {
      ok = false;
      errors.add(MyString.getStackExeption(exc));
    }
    if (ok) {
      // подтвердить транзакцию
      conn.commit();
    } else {
      // откатить транзакцию
      conn.rollback();
    }
    return ok;
  }

  // записать строку в writer
  private void writeRow(String[] values) throws Exception {
    for (String value : values) {
      writer.write(value);
    }
    writer.write(errors.toString());
    writer.endRecord();
  }

  // найти в БД запись с заданными типом и oldId
  private int searchModel(String tableName, String oldId) throws Exception {
    Table table = tableFactory.getTable(tableName);
    Select sel = tableFactory.getSelect();
    sel.select(table);
    sel.from(table);
    sel.and(table.get("old_id").eq(oldId));

    boolean ok = sel.executeSelect(app.getConnection());
    if (!ok) {
      errors.addAll(sel.getError());
      return -1;
    }
    // если модель одна 
    int size = sel.getDinamicList().size();
    if (size == 1) {
      // вернуть её ИД
      Model model = modelFactory.getModel(tableName);
      String primaryName = model.getPrimaryAlias();
      DinamicModel dinamicModel = sel.getDinamicList().get(0);
      return Integer.parseInt(dinamicModel.get(primaryName).toString());
    }
    // записать ошибку
    if (size == 0) {
      errors.add(" связанная модель не найдена ");
    } else if (size > 1) {
      errors.add(" связанных моделей больше одной ");
    }
    return -1;
  }

  // сохранить одну модель
  private Object saveModel(Model model, Map<String, Model> models) throws Exception {
    // если ИД модели не задан
    if (model.getPrimary() == null) {
      // для каждого поля модели
      Map<String, Field> fields = model.getStructure().getCloneFields();
      for (String fieldName : fields.keySet()) {
        Field field = fields.get(fieldName);
        // если есть зависимость от другой модели и если поле равно null
        boolean isRelation = field.getRelations() != null && !field.getRelations().isEmpty();
        if (isRelation && model.get(fieldName) == null) {
          // найти эту модель среди заданных
          String relationName = field.getRelations();
          // если модель найдена
          if (models.containsKey(relationName)) {
            // получить её ИД
            Model relationModel = models.get(relationName);
            Object id = relationModel.getPrimary();
            // если ИД нету
            if (id == null) {
              // сохранить её
              // получить ИД сохраненной модели
              // присвоить его этому полю
              id = saveModel(relationModel, models);
            }
            model.set(fieldName, id);
          }
        }
      }
      // если значения системных полей не установлены
      // установить их
      if (model.get("insert_date") == null) {
        model.set("insert_date", FormatDate.getCurrentDateInMysql());
      }
      if (model.get("update_date") == null) {
        model.set("update_date", FormatDate.getCurrentDateInMysql());
      }
      if (model.get("insert_user_id") == null) {
        model.set("insert_user_id", app.getRightsObject().getUserId());
      }
      if (model.get("update_user_id") == null) {
        model.set("update_user_id", app.getRightsObject().getUserId());
      }
      // произвести поиск по old_id
      boolean ok = checkOldId(model);
      if (ok) {
        // сохранить модель
        ok = model.save();
      }
      if (!ok) {
        errors.addAll(model.getError());
      }

    }
    // вернуть значение ИД
    return model.getPrimary();
  }

  /**
   * проверить наличие старого ИД
   *
   * @param model
   * @return
   * @throws Exception
   */
  private boolean checkOldId(Model model) throws Exception {
    if (model.get("old_id") != null) {
      Object oldId = model.get("old_id");
      String tableName = model.getStructure().getTableAlias();
      Table table = tableFactory.getTable(tableName);
      Select sel = tableFactory.getSelect();
      sel.select(table);
      sel.from(table);
      sel.and(table.get("old_id").eq(oldId));
      boolean ok = sel.executeSelect(app.getConnection());
      if (!ok) {
        errors.addAll(sel.getError());
        return false;
      }
      int size = sel.getDinamicList().size();
      if (size > 0) {
        errors.add("Найден такой же old_id для модели " + tableName);
        return false;
      }
    }
    return true;
  }
}
