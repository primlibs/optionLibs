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
import prim.AbstractApplication;
import prim.libs.FormatDate;
import prim.libs.MyString;
import prim.model.DinamicModel;
import prim.model.Model;
import prim.model.ModelFactory;
import prim.modelStructure.Field;
import prim.modelStructure.Structure;
import prim.select.Select;
import prim.select.Table;
import prim.select.TableSelectFactory;

/**
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

  public CsvUploader(AbstractApplication app, File csv, Map<String, Object> params) throws Exception {
    // класс получает параметры:
    // Application
    // файл, из которого нужно скопировать данные
    // набор параметров из запроса
    this.app = app;
    this.modelFactory = new ModelFactory(app);
    this.csv = csv;
    this.params = params;
    this.baos = new ByteArrayOutputStream();
    reader = new CsvReader(new FileInputStream(csv), Charset.forName("UTF-8"));
    this.writer = new CsvWriter(baos, reader.getDelimiter(), Charset.forName("UTF-8"));
  }

  public List<String> getErrors() {
    return errors;
  }

  // загрузить данные
  public boolean uploadData() throws Exception {
    // прочитать заголовки файла
    String[] headers = reader.getHeaders();
    // записать заголовки в файл csv
    writeHeaders(headers);
    // для каждой строки в файле csv
    int number = 0;
    while (reader.readRecord()) {
      String[] values = reader.getValues();
      // записать строку
      boolean ok = uploadRow(values, number);
      number++;
    }
    // записать всё в новый файл
    writeNewFile();
    return true;
  }

  private void writeNewFile() throws Exception {
    byte[] bytes = baos.toByteArray();
    //FileWriter fileWriter = new FileWriter(csv);
    //fileWriter.w
    FileOutputStream fos = new FileOutputStream(csv);
    fos.write(bytes);
  }

  // записать в файл заголовки
  private void writeHeaders(String[] headers) throws Exception {
    for (String header : headers) {
      writer.write(header);
    }
    writer.endRecord();
  }

  // загрузить данные из строки
  private boolean uploadRow(String[] values, int rowNumber) throws Exception {
    // очистить ошибки
    errors.clear();
    boolean ok = true;

    // сохранить данные в модели
    try {
      // получить набор заголовков, набор параметров, данные из строки
      // создать массив моделей
      Map<String, Model> models = new LinkedHashMap();
      // для каждой ячейки в строке
      int columnNumber = 0;
      for (String value : values) {
        // если передан такой параметр, то есть имя поля
        if (params.containsKey("column_" + columnNumber) && params.get("column_" + columnNumber) != null) {
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
              errors.add("Ошибка в строке " + rowNumber + ", в модели " + modelName + " отсутствует поле " + fieldName);
              ok = false;
              break;
            }
            Field field = struct.getFieldClone(fieldName);
            // если это поле ИД
            if (fieldName.equals(struct.getPrimaryAlias())) {
              // записать значение в модель в поле oldId
              model.set("old_id", value);
            } // иначе если это поле ссылка на другую модель
            else if (field.getRelations() != null && !field.getRelations().isEmpty()) {
              String relationTableName = field.getRelations();
              // найти в БД модель запись такого типа с таким oldId
              int relationId = searchModel(relationTableName, value);
              // если такая запись есть одна
              if (relationId > 0) {
                // ИД этой записи записать в модель в это поле
                model.set(fieldName, relationId);
              } else {
                // иначе если такой записи нет или больше одного
                // ошибка, выйти
                errors.add("Ошибка в строке " + rowNumber + ", в модели " + modelName + " ошибка при поиске связанной мдели для поля " + fieldName);
                ok = false;
                break;
              }
            } // иначе
            else {
              // записать значение в модель в это поле
              model.set(fieldName, value);
            }
          }
        }
        columnNumber++;
      }

      // записать модели
      if (ok) {
        // то сохранить модели и не записывать в новый файл
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
              errors.add("Ошибка в строке " + rowNumber + ", не удалось сохранить модель " + modelName);
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
      }
    } catch (Exception exc) {
      ok = false;
      errors.add(MyString.getStackExeption(exc));
    }
    
    // если статус false
    if (ok == false) {
      // то строку записать в новый файл + записать ошибки
      writeRow(values);
    }

    return ok;
  }

  // записать строку в новый файл XML
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
    // если модели нет
    // записать ошибку
    // если модели больше одной
    // записать ошибку
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
        // сохранить модель
        boolean ok = model.save();
        if (!ok) {
          errors.addAll(model.getError());
        }
      }
    }
    // вернуть значение ИД
    return model.getPrimary();
  }
}
