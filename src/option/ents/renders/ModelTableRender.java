/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.renders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import option.Creator;
import option.ents.ModelTableEnt;
import prim.model.DinamicModel;
import prim.modelStructure.Field;
import prim.modelStructure.Structure;
import web.FormOptionInterface;
import web.Render;
import web.fabric.AbsEnt;

/**
 *
 * @author Pavel Rice
 */
public class ModelTableRender extends OptionRender {

  public ModelTableRender(Render render, String object, String action) {
    super(render, object, action);
  }

  public String renderOneModel(List<DinamicModel> dmList, List<String> errors, Structure structure, int countPages, Object pageObject) throws Exception {
    // получить список моделей
    // получить структуру
    String str = "";
    str += errors + "<br/>";

    // отсортировать поля - сначала ИД, потом тип, потом остальные, потом системные поля
    Map<String, Field> fieldsMap = new LinkedHashMap();
    fieldsMap.put(structure.getPrimaryAlias(), structure.getCloneFields().get(structure.getPrimaryAlias()));
    fieldsMap.put("user_data_type_id", structure.getCloneFields().get("user_data_type_id"));
    fieldsMap.putAll(getNotSystemFields(structure));
    fieldsMap.putAll(getSystemFields(structure));

    // вывести форму добавления
    str += addForm(fieldsMap, structure);

    // вывести таблицу
    str += table(dmList, fieldsMap, structure);

    // вывести пагинатор
    Map<String, Object> params = new HashMap();
    params.put(ModelTableEnt.NAME_PARAMETER, structure.getTableAlias());
    int page = 1;
    if (pageObject != null) {
      page = Integer.parseInt(pageObject.toString());
    }
    str += paginator(page, countPages, object, action, params, ModelTableEnt.PAGE_PARAMETER).render();

    return str;
  }

  private String table(List<DinamicModel> modelList, Map<String, Field> fieldsMap, Structure struct) throws Exception {
    // вывести таблицу
    AbsEnt table = rd.table("1", "5", "");

    // вывести строку заголовков
    // ИД
    // тип
    // заголовки для всех изменяемых полей
    // ячейка для формы удаления

    /*
     AbsEnt trHead = rd.getFabric().get("tr");
     table.addEnt(trHead);
     for (String fieldName: fieldsMap.keySet()) {
     rd.td(trHead, fieldName);
     }
     */

    // вывести саму таблицу
    // для каждой модели
    for (DinamicModel model : modelList) {
      AbsEnt tr = rd.getFabric().get("tr");
      table.addEnt(tr);
      // вывести ячейку с ИД
      String primaryName = struct.getPrimaryAlias();
      rd.td(tr, model.get(primaryName));

      // вывести ячейку с формой изменения
      FormOptionInterface fo = rd.getFormOption();
      fo.setAction(ModelTableEnt.ONE_MODEL_ACTION);
      fo.setObject(object);
      fo.setSpecAction(ModelTableEnt.CHANGE_SPECACTION);
      fo.setNoValidateRights();
      fo.setHorisontal(true);
      fo.setTitle("Изменить модель");
      Map<AbsEnt, String> inner = new LinkedHashMap();
      inner.put(rd.hiddenInput(primaryName, model.get(primaryName)), "");
      inner.put(rd.hiddenInput(ModelTableEnt.NAME_PARAMETER, struct.getTableAlias()), "");
      // для каждого поля
      for (String fieldName : fieldsMap.keySet()) {
        // добавить в форму поле
        Field field = fieldsMap.get(fieldName);
        if (field.isEditable()) {
          String type = field.getType();
          if (type.equalsIgnoreCase("data")) {
            inner.put(rd.dateTimeInput(fieldName, model.get(fieldName), fieldName), "");
          } else if (type.equalsIgnoreCase("text")) {
            inner.put(rd.textArea(fieldName, model.get(fieldName), fieldName), "");
          } else {
            inner.put(rd.textInput(fieldName, model.get(fieldName), fieldName), "");
          }
        }
      }
      AbsEnt changeForm = rd.rightForm(inner, fo);
      rd.td(tr, changeForm);

      // вывести системные поля
      for (String fieldName : fieldsMap.keySet()) {
        if (!fieldName.equals(primaryName)) {
          Field field = fieldsMap.get(fieldName);
          if (!field.isEditable() && !fieldName.equals(primaryName)) {
            rd.td(tr, model.get(fieldName));
          }
        }
      }
      
      // вывести форму закрытия модели
      FormOptionInterface closeFo = rd.getFormOption();
      closeFo.setAction(ModelTableEnt.ONE_MODEL_ACTION);
      closeFo.setObject(object);
      closeFo.setSpecAction(ModelTableEnt.CLOSE_SPECACTION);
      closeFo.setNoValidateRights();
      closeFo.setHorisontal(true);
      closeFo.setTitle("Закрыть модель");
      Map<AbsEnt, String> closeInner = new LinkedHashMap();
      closeInner.put(rd.hiddenInput(primaryName, model.get(primaryName)), "");
      closeInner.put(rd.hiddenInput(ModelTableEnt.NAME_PARAMETER, struct.getTableAlias()), "");
      AbsEnt closeForm = rd.rightForm(closeInner, closeFo);
      rd.td(tr, closeForm);
    }
    return table.render();
  }

  private String addForm(Map<String, Field> fieldsMap, Structure struct) throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    // получить список полей таблицы
    // для каждого поля
    for (String fieldName : fieldsMap.keySet()) {
      Field field = fieldsMap.get(fieldName);
      // если это не ИД и не системное поле
      if (field.isEditable()) {
        // добавить в форму поле
        String type = field.getType();
        // если это поле типа дата
        if (type.equalsIgnoreCase("datetime")) {
          inner.put(rd.dateTimeInput(fieldName, requestParams.get(fieldName), fieldName), "");
        } else if (type.equalsIgnoreCase("text")) {
          inner.put(rd.textArea(fieldName, requestParams.get(fieldName), fieldName), "");
        } else {
          inner.put(rd.textInput(fieldName, requestParams.get(fieldName), fieldName), "");
        }
      }
    }
    inner.put(rd.hiddenInput(ModelTableEnt.NAME_PARAMETER, struct.getTableAlias()), "");
    FormOptionInterface fo = rd.getFormOption();
    fo.setAction(ModelTableEnt.ONE_MODEL_ACTION);
    fo.setObject(object);
    fo.setSpecAction(ModelTableEnt.ADD_SPECACTION);
    fo.setNoValidateRights();
    fo.setHorisontal(true);
    fo.setTitle("Добавить модель");
    AbsEnt addForm = rd.rightForm(inner, fo);
    return addForm.render();
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
      if (!field.isEditable() && !name.equals(struct.getPrimaryAlias()) && !name.equals("user_data_type_id")) {
        systemFields.put(name, field);
      }
    }
    return systemFields;
  }

  public String renderModelTable(Map<String, Structure> structureMap, List<String> errors) throws Exception {
    String content = "";
    content += errors + "<br/>";
    for (String name : structureMap.keySet()) {
      Structure structure = structureMap.get(name);
      if (!structure.isSystem()) {
        Map<String, Object> oneStructureParams = new HashMap();
        oneStructureParams.put(ModelTableEnt.NAME_PARAMETER, name);
        content += href(Creator.MODELTABLE_OBJECT_NAME, ModelTableEnt.ONE_MODEL_ACTION, "", name, oneStructureParams);
        content += "<br/>";
      }
    }
    return content;
  }
}
