/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import prim.AbstractApplication;
import prim.libs.FormatDate;
import prim.libs.MyString;
import prim.model.DinamicModel;
import prim.model.Model;
import prim.modelStructure.Structure;
import prim.select.AgrTypes;
import prim.select.OrdTypes;
import prim.select.Select;
import prim.select.Table;
import prim.select.TableSelectFactory;
import warehouse.modelKeeper.ModelStructureKeeper;

/**
 *
 * @author Pavel Rice
 */
public class ModelTableService extends OptionService {

  // количество записей на одну страницу в отображении списка
  private final int recordsOfPage = 20;

  public ModelTableService(AbstractApplication app) {
    super(app);
  }

  public Structure getStructure(String structureName) throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    return mss.getStructure(structureName);
  }

  public int getCountPages(Object modelNameObject, Object id) throws Exception {
    if (MyString.NotNull(modelNameObject)) {
      String modelName = modelNameObject.toString();
      return getCountPages(modelName, recordsOfPage, id);
    } else {
      errors.add("Ошибка: не передано имя модели");
      return 0;
    }
  }

  public Map<String, Structure> getModelList() throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    TreeMap<String, Structure> structureMap = new TreeMap<String, Structure>(mss.getStructureMap());
    return structureMap;
  }

  public List<DinamicModel> getOneModelData(Object modelNameObject, Object pageObject, Object id) throws Exception {
    // проверить входные параметры
    if (MyString.NotNull(modelNameObject)) {
      // получить список данных для этих параметров
      String modelName = modelNameObject.toString();
      Structure struct = getStructure(modelName);
      if (!struct.isSystem()) {
        int page = getPage(pageObject);
        // старт
        int start = (page - 1) * recordsOfPage;

        Select sel = getSelect(modelName, false, start, recordsOfPage, id);
        boolean ok = sel.executeSelect(app.getConnection());
        if (!ok) {
          errors.addAll(sel.getError());
        }
        return sel.getDinamicList();
      } else {
        errors.add("Ошибка: нельзя изменять данные системных моделей");
      }
    } else {
      errors.add("Ошибка: не передано имя модели");
    }
    return new ArrayList();
  }

  private int getCountPages(String modelName, int recordsOfPage, Object id) throws Exception {
    int count = 0;
    Select countSel = getSelect(modelName, true, null, null, id);
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

  private Select getSelect(String tableName, boolean isCountSelect, Integer start, Integer recordsOfPage, Object id) throws Exception {
    Table table = getTable(tableName);
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

    sel.order(table.get("delete_date"), OrdTypes.ASC);
    sel.order(table.get("insert_date"), OrdTypes.DESC);
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

  public void addModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      // создать модель
      Model model = getModel(modelName);
      // присвоить ей все параметры
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

  public void changeModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      // создать модель
      Model model = getModel(modelName);
      // присвоить ей все параметры
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

  public void closeModel(Map<String, Object> params, String modelName) throws Exception {
    Structure struct = getStructure(modelName);
    if (!struct.isSystem()) {
      // создать модель
      Model model = getModel(modelName);
      // присвоить ей все параметры
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
  
}
