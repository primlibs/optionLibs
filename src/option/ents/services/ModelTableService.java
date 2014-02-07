/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.services;

import com.csvreader.CsvWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.prim.core.AbstractApplication;
import com.prim.support.FormatDate;
import com.prim.support.MyString;
import com.prim.core.model.DinamicModel;
import com.prim.core.model.Model;
import com.prim.core.modelStructure.Field;
import com.prim.core.modelStructure.Structure;
import com.prim.core.select.AgrTypes;
import com.prim.core.select.OrdTypes;
import com.prim.core.select.Select;
import com.prim.core.select.Table;
import com.prim.core.select.TableSelectFactory;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;

/**
 *
 * @author Pavel Rice
 */
public class ModelTableService extends OptionService {

  // количество записей на одну страницу в отображении списка
  private final Integer RECORDS_OF_PAGE = 20;

  public ModelTableService(AbstractApplication app) {
    super(app);
  }

  /**
   * получить структуру по имени
   *
   * @param structureName
   * @return
   * @throws Exception
   */
  public Structure getStructure(String structureName) throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    return mss.getStructure(structureName);
  }

  /**
   * получить количество страниц
   *
   * @param modelNameObject
   * @param id
   * @return
   * @throws Exception
   */
  public int getCountPages(Object modelNameObject, Object id) throws Exception {
    if (MyString.NotNull(modelNameObject)) {
      String modelName = modelNameObject.toString();
      return getCountPages(modelName, RECORDS_OF_PAGE, id);
    } else {
      errors.add("Ошибка: не передано имя модели");
      return 0;
    }
  }

  /**
   * получить номер страницы, на которой должна выводится модель с заданным ИД
   *
   * @param modelName
   * @param id
   * @return
   * @throws Exception
   */
  public int getPageById(String modelName, Object id) throws Exception {
    if (MyString.NotNull(id)) {
      // получить список всех моделей
      Select sel = getSelect(modelName, false, null, null, null, null);
      boolean ok = sel.executeSelect(app.getConnection());
      if (ok) {
        Structure struct = getStructure(modelName);
        String primaryName = struct.getPrimaryAlias();
        List<DinamicModel> dmList = sel.getDinamicList();
        Integer pos = -1;
        // найти среди них ИД
        // определить его позицию
        for (DinamicModel dm : dmList) {
          if (dm.get(primaryName) != null && dm.get(primaryName).toString().equals(id.toString())) {
            pos = dmList.indexOf(dm) + 1;
            break;
          }
        }
        if (pos > 1) {
          // разделить позицию на количество записей на странице
          // округлить до целого в большую сторону
          double page = Math.ceil(pos.doubleValue() / RECORDS_OF_PAGE.doubleValue());
          return (int) page;
        } else {
          errors.add("Модель с заданным Ид не найдена");
        }
      } else {
        errors.addAll(sel.getError());
      }
    } else {
      errors.add("не передан ИД модели");
    }
    return -1;
  }

  /**
   * возвращает список структур для всех моделей
   *
   * @return
   * @throws Exception
   */
  public Map<String, Structure> getModelList() throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    TreeMap<String, Structure> structureMap = new TreeMap<String, Structure>(mss.getStructureMap());
    return structureMap;
  }

  /**
   * возвращает данные для одной модели
   *
   * @param modelNameObject
   * @param pageObject
   * @param id
   * @return
   * @throws Exception
   */
  public List<DinamicModel> getOneModelData(Object modelNameObject, Object pageObject, Object sortableColumn, Object id) throws Exception {
    if (MyString.NotNull(modelNameObject)) {
      String modelName = modelNameObject.toString();
      Structure struct = getStructure(modelName);
      int page = getPage(pageObject);
      int start = (page - 1) * RECORDS_OF_PAGE;
      Select sel = getSelect(modelName, false, start, RECORDS_OF_PAGE, sortableColumn, id);
      boolean ok = sel.executeSelect(app.getConnection());
      if (!ok) {
        errors.addAll(sel.getError());
      }
      return sel.getDinamicList();
    } else {
      errors.add("Ошибка: не передано имя модели");
    }
    return new ArrayList();
  }

  public byte[] getCsvFile(String modelName) throws Exception {
    if (MyString.NotNull(modelName)) {
      Structure struct = getStructure(modelName);
      Select select = getSelect(modelName, false, null, null, null, null);
      if (select.executeSelect(app.getConnection())) {
        // создать writer
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CsvWriter csvWriter = new CsvWriter(baos, ',', Charset.forName("UTF-8"));
        try {
          // отсортировать колонкиы
          Field primaryField = getPrimaryField(struct);
          Map<String, Field> notSystemFields = getNotSystemFields(struct);
          Map<String, Field> systemFields = getSystemFields(struct);
          Map<String, Field> fields = new LinkedHashMap();
          fields.put(primaryField.getAlias(), primaryField);
          fields.putAll(notSystemFields);
          fields.putAll(systemFields);
          // записать заголовки
          for (String fieldName : fields.keySet()) {
            csvWriter.write(fieldName);
          }
          csvWriter.endRecord();
          // записать все остальные данные
          for (DinamicModel model : select.getDinamicList()) {
            for (String fieldName : fields.keySet()) {
              String value = (model.get(fieldName) != null ? model.get(fieldName).toString() : "");
              csvWriter.write(value);
            }
            csvWriter.endRecord();
          }
        } finally {
          csvWriter.close();
          baos.close();
        }
        return baos.toByteArray();
      } else {
        errors.addAll(select.getError());
      }
    } else {
      errors.add("Ошибка: не передано имя модели");
    }
    return new byte[0];
  }

  private Map<String, Field> getNotSystemFields(Structure struct) throws Exception {
    Map<String, Field> notSystemFields = new TreeMap();
    Map<String, Field> fields = struct.getCloneFields();
    for (String name : fields.keySet()) {
      Field field = fields.get(name);
      if (field.isEditable()) {
        notSystemFields.put(name, field);
      }
    }
    return notSystemFields;
  }

  private Map<String, Field> getSystemFields(Structure struct) throws Exception {
    Map<String, Field> systemFields = new TreeMap();
    Map<String, Field> fields = struct.getCloneFields();
    for (String name : fields.keySet()) {
      Field field = fields.get(name);
      if (!field.isEditable() && !name.equals(struct.getPrimaryAlias())) {
        systemFields.put(name, field);
      }
    }
    return systemFields;
  }

  private Field getPrimaryField(Structure struct) throws Exception {
    return struct.getCloneFields().get(struct.getPrimaryAlias());
  }

  /**
   * добавить модель
   *
   * @param params
   * @param modelName
   * @throws Exception
   */
  public void addModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      Model model = getModel(modelName);
      model.set(params);
      model.set("insert_date", FormatDate.getCurrentDateInMysql());
      model.set("update_date", FormatDate.getCurrentDateInMysql());
      model.set("insert_user_id", app.getRightsObject().getUserId());
      model.set("update_user_id", app.getRightsObject().getUserId());
      if (model.getPrimary() != null && !"".equals(model.getPrimary())) {
        errors.add("Обнаружен первичный ключ " + model.getPrimaryAlias() + model.getPrimary());
      } else {
        boolean ok = model.save();
        if (!ok) {
          errors.addAll(model.getError());
        }
      }
    } else {
      errors.add("Ошибка: нельзя изменять данные системных моделей");
    }
  }

  /**
   * изменить модель
   *
   * @param params
   * @param modelName
   * @throws Exception
   */
  public void changeModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      Model model = getModel(modelName);
      model.set(params);
      model.set("update_date", FormatDate.getCurrentDateInMysql());
      model.set("update_user_id", app.getRightsObject().getUserId());
      if (model.getPrimary() == null || "".equals(model.getPrimary())) {
        errors.add("Не обнаружен первичный ключ " + model.getPrimaryAlias() + model.getPrimary());
      } else {
        boolean ok = model.save();
        if (!ok) {
          errors.addAll(model.getError());
        }
      }
    } else {
      errors.add("Ошибка: нельзя изменять данные системных моделей");
    }
  }

  /**
   * закрыть модель
   *
   * @param params
   * @param modelName
   * @throws Exception
   */
  public void closeModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      Model model = getModel(modelName);
      model.set(params);
      if (model.findByPrimary() == false) {
        errors.add("Не обнаружен первичный ключ " + model.getPrimaryAlias() + model.getPrimary());
      } else {
        model.set("delete_date", FormatDate.getCurrentDateInMysql());
        boolean ok = model.save();
        if (!ok) {
          errors.addAll(model.getError());
        }
      }
    } else {
      errors.add("Ошибка: нельзя изменять данные системных моделей");
    }
  }

  private int getCountPages(String modelName, int recordsOfPage, Object id) throws Exception {
    int count = 0;
    Select countSel = getSelect(modelName, true, null, null, null, id);
    boolean ok = countSel.executeSelect(app.getConnection());
    if (!ok) {
      errors.addAll(countSel.getError());
    }
    List<DinamicModel> dinamicList = countSel.getDinamicList();
    if (!dinamicList.isEmpty()) {
      count = Integer.parseInt(dinamicList.get(0).get("count").toString());
    }
    double countDouble = count;
    double recordsOfPageDouble = recordsOfPage;
    double countPagesDouble = Math.ceil(countDouble / recordsOfPageDouble);
    return (int) countPagesDouble;
  }

  private Select getSelect(String tableName, boolean isCountSelect, Integer start, Integer recordsOfPage, Object sortableColumn, Object id) throws Exception {
    Table table = getTable(tableName);
    Structure struct = getStructure(tableName);
    Select sel = getSelect();
    if (isCountSelect) {
      sel.select(table.getPrimary(), "count", AgrTypes.COUNT);
    } else {
      sel.select(table);
    }
    sel.from(table);
    sel.and(table.getPrimary().isNotNull());
    if (id != null && !id.toString().isEmpty()) {
      sel.and(table.getPrimary().eq(id));
    }

    if (MyString.NotNull(sortableColumn)) {
      sel.order(table.get(sortableColumn.toString()), OrdTypes.ASC);
    } else {
      if (!struct.isSystem()) {
        sel.order(table.get("delete_date"), OrdTypes.ASC);
        sel.order(table.get("insert_date"), OrdTypes.DESC);
      }
    }
    if (!isCountSelect) {
      if (start != null) {
        sel.setLimitFrom(start);
      }
      if (recordsOfPage != null) {
        sel.setLimitRange(recordsOfPage);
      }
    }

    return sel;

  }

  private int getPage(Object objectPage) {
    int page = 1;
    if (objectPage != null) {
      try {
        page = Integer.parseInt(objectPage.toString());
      } catch (Exception e) {
      }
    }
    if (page < 1) {
      page = 1;
    }
    return page;
  }
}
