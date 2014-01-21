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
import web.HrefOptionInterface;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.fabric.entities.Txt;

/**
 *
 * @author Pavel Rice
 */
public class ModelTableRender extends OptionRender {

  public ModelTableRender(Render render, String object, String action) {
    super(render, object, action);
  }

  /**
   * вывести данные одной модели
   * @param dmList
   * @param errors
   * @param structure
   * @param countPages
   * @param pageObject
   * @return
   * @throws Exception 
   */
  public String renderOneModel(List<DinamicModel> dmList, List<String> errors, Structure structure, int countPages, Object pageObject) throws Exception {

    String str = "";
    str += "<h2>Данные по модели " + structure.getTableAlias() + "</h2>";
    str += errors + "<br/>";
    Map<String, Field> fieldsMap = structure.getCloneFields();

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
  
  /**
   * вывести список всех моделей
   * @param structureMap
   * @param errors
   * @return
   * @throws Exception 
   */
   public String renderModelList(Map<String, Structure> structureMap, List<String> errors) throws Exception {
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

  private String table(List<DinamicModel> modelList, Map<String, Field> fieldsMap, Structure struct) throws Exception {
    AbsEnt table = rd.table("1", "5", "");
    table.addAttribute(EnumAttrType.style, " border-collapse: collapse; ");

    Field primaryField = getPrimaryField(struct);
    Map<String, Field> notSystemFields = getNotSystemFields(struct);
    Map<String, Field> systemFields = getSystemFields(struct);


    AbsEnt trHead = rd.getFabric().get("tr");
    table.addEnt(trHead);
    // ИД
    rd.td(trHead, primaryField.getAlias());
    // несистемные поля
    for (String fieldName : notSystemFields.keySet()) {
      rd.td(trHead, fieldName);
    }
    // форма изменения
    rd.td(trHead, "");
    // системные поля
    for (String fieldName : systemFields.keySet()) {
      rd.td(trHead, fieldName);
    }
    // форма закрытия
    rd.td(trHead, "");


    for (DinamicModel model : modelList) {
      AbsEnt tr = rd.getFabric().get("tr");
      table.addEnt(tr);
      // вывести ячейку с ИД
      String primaryName = struct.getPrimaryAlias();
      rd.td(tr, model.get(primaryName));

      // вывести форму изменения
      String formId = "changeForm" + model.get(primaryName);
      for (String fieldName : notSystemFields.keySet()) {
        // добавить в форму поле
        Field field = notSystemFields.get(fieldName);
        AbsEnt formElement;
        String type = field.getType();
        if (type.equalsIgnoreCase("datetime")) {
          formElement = rd.dateTimeInput(fieldName, model.get(fieldName), fieldName);
        } else if (type.equalsIgnoreCase("text")) {
          formElement = rd.textArea(fieldName, model.get(fieldName), fieldName);
        } else {
          formElement = rd.textInput(fieldName, model.get(fieldName), fieldName);
        }
        formElement.setAttribute(EnumAttrType.form, formId);
        AbsEnt div = rd.div("", "");
        div.addEnt(formElement);
        if (field.getRelations() != null && !field.getRelations().isEmpty()) {
          HrefOptionInterface ho = rd.getHrefOption();
          ho.setObject(object);
          ho.setAction(ModelTableEnt.ONE_MODEL_ACTION);
          ho.setName(field.getRelations());
          ho.setNoValidateRights();
          Map<String, Object> linkParams = new HashMap();
          linkParams.put(ModelTableEnt.NAME_PARAMETER, field.getRelations());
          linkParams.put("id", model.get(fieldName));
          AbsEnt link = rd.href(linkParams, ho);
          div.addEnt(link);
        }
        
        rd.td(tr, div);
      }

      // форма изменения
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
      AbsEnt changeForm = rd.rightForm(inner, fo).setAttribute(EnumAttrType.id, formId);
      rd.td(tr, changeForm);

      // вывести системные поля
      for (String fieldName : systemFields.keySet()) {
        if (!fieldName.equals(primaryName)) {
          Field field = systemFields.get(fieldName);
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
    Map<String, Field> notSystemFields = getNotSystemFields(struct);
    Map<AbsEnt, String> inner = new LinkedHashMap();
    for (String fieldName : notSystemFields.keySet()) {
      Field field = fieldsMap.get(fieldName);
      if (field.isEditable()) {
        String type = field.getType();
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
      if (!field.isEditable() && !name.equals(struct.getPrimaryAlias())) {
        systemFields.put(name, field);
      }
    }
    return systemFields;
  }

  private Field getPrimaryField(Structure struct) throws Exception {
    return struct.getCloneFields().get(struct.getPrimaryAlias());
  }

 
}
