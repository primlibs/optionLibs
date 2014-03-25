/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.modelEnts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import option.Creator;
import option.ents.ModelEnt;
import com.prim.core.AbstractApplication;
import com.prim.core.modelStructure.Field;
import com.prim.core.modelStructure.Structure;
import com.prim.core.modelStructure.Unique;
import com.prim.support.enums.DataTypes;
import com.prim.support.filterValidator.entity.ValidatorAbstract;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;
import com.prim.core.warehouse.modelKeeper.ModelStructureManager;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;

/**
 *
 * @author User
 */
public class OneStructure extends ModelEnt {

  private String str = "";
  TreeMap<String, Object> relationsMap = new TreeMap<String, Object>();

  public OneStructure(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    String structureAlias = getParameter("structureAlias");

    String title = "Servlet OneStructure";
    if (structureAlias != null) {
      title = "Struct " + structureAlias;
    }

    str += "<script src='./script.js'></script>";
    str += href(Creator.MODEL_OBJECT_NAME, "AllStructure", "", "Перейти к списку всех моделей", new HashMap());

    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    Map<String, Structure> structureMap = mss.getStructureMap();

    if (structureAlias != null) {

      Structure struct = structureMap.get(structureAlias);
      if (struct != null) {
        // вывод параметров модели
        str += "<h1>Структура модели " + struct.getTableAlias() + "</h1>";
        str += "Название таблицы: " + struct.getTableName() + "<br/>";
        str += "Алиас таблицы: " + struct.getTableAlias() + "<br/>";
        str += "Первичный ключ таблицы: " + struct.getPrimaryAlias() + "<br/>";
        str += "Разрешена ли работа с файлами: " + (struct.isFileWork() ? " да " : "нет") + "<br/>";
        if (!struct.isSystem()) {
          Field userDataTypeId = struct.getField("user_data_type_id");
          if (userDataTypeId != null) {
            str += "user_data_type_id: " + userDataTypeId.getDef();
          }
        }
        str += " <br/> <br/>";
        TreeMap<String, Field> map = new TreeMap(struct.getCloneFields());

        relationsMap = new TreeMap<String, Object>();
        for (String name : structureMap.keySet()) {
          Structure s = structureMap.get(name);
          relationsMap.put(name, name);
        }
        relationsMap.put("0", "Не выбрано");

        Map<String, Object> structureParams = new HashMap();
        structureParams.put("structureAlias", structureAlias);
        str += href(Creator.MODEL_OBJECT_NAME, "ChangeStructure", "", "Изменить параметры модели", structureParams, "margin-right:100px;", "");
        str += href(Creator.MODEL_OBJECT_NAME, action, "deleteStructure", "Удалить модель", structureParams, "margin-right:100px;", "onclick='return confirmDelete();'");
        str += "<div style='overflow:hidden;'>" + addFieldForm() + "</div>";
        str += "<h2>Поля модели</h2>";

        // вывод параметров полей
        for (Field field : map.values()) {

          LinkedHashMap<String, String> columns = new LinkedHashMap<String, String>();
          LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();

          str += "<hr/><h2>" + field.getAlias() + "</h2>";
          str += "<table border='1' cellpadding='5' style='border-collapse:collapse'>";

          str += "<tr>";
          str += "<td>Имя поля в таблице</td>";
          str += "<td>Название поля</td>";
          str += "<td>Тип поля</td>";
          str += "</tr>";

          str += "<tr>";
          str += "<td>" + field.getName() + "</td>";
          str += "<td>" + field.getAlias() + "</td>";
          str += "<td>" + field.getType() + "</td>";
          str += "</tr>";
          str += "</table>";

          if (field.isEditable()) {

            str += changeFieldForm(field);

            structureParams.put("fieldAlias", field.getAlias());
            str += href(Creator.MODEL_OBJECT_NAME, "AddValidator", "", "[Добавить фильтр либо валидатор]", structureParams, "margin-right:100px;", "");
            str += href(Creator.MODEL_OBJECT_NAME, action, "deleteField", "[Удалить поле]", structureParams, "margin-right:100px;", "onclick='return confirmDelete();'");
            str += href(Creator.MODEL_OBJECT_NAME, action, "deleteFieldWithData", "[Удалить поле вместе с данными]", structureParams, "margin-right:100px;", "onclick='return confirmDelete();'");

            str += "<br/><br/>Фильтры и валидаторы: <br/>";

            List<ValidatorAbstract> validators = field.getValidatorList();

            for (ValidatorAbstract validator : validators) {

              int idx = field.getValidatorList().indexOf(validator);
              String validatorName = validator.getClass().toString();
              int dotPos = validatorName.lastIndexOf(".") + 1;
              validatorName = validatorName.substring(dotPos);

              structureParams.put("validatorId", idx);
              String deleteLink = href(Creator.MODEL_OBJECT_NAME, action, "deleteValidator", "[удалить]", structureParams, "", "onclick='return confirmDelete();'");
              String changeLink = href(Creator.MODEL_OBJECT_NAME, "ChangeValidator", "", "[изменить параметры]", structureParams);
              String upLink = href(Creator.MODEL_OBJECT_NAME, action, "upValidator", "[поднять]", structureParams);
              String downLink = href(Creator.MODEL_OBJECT_NAME, action, "downValidator", "[опустить]", structureParams);

              String output = "";
              output += "<b>" + validatorName + "</b>" + deleteLink + changeLink + upLink + downLink;
              HashMap<String, Object> prm = validator.getParameters();
              for (String pName : prm.keySet()) {
                output += "</br>" + pName + " = " + prm.get(pName);
              }

              str += "<div class='validator'>" + output + "</div>";
            }

          } else {
            str += fieldInfo(field);
            List<ValidatorAbstract> validators = field.getValidatorList();
            for (ValidatorAbstract validator : validators) {
              String validatorName = validator.getClass().toString();
              int dotPos = validatorName.lastIndexOf(".") + 1;
              validatorName = validatorName.substring(dotPos);

              String output = "";
              output += "<b>" + validatorName + "</b>";
              HashMap<String, Object> prm = validator.getParameters();
              for (String pName : prm.keySet()) {
                output += "</br>" + pName + " = " + prm.get(pName);
              }

              str += "<div class='validator'>" + output + "</div>";
            }
          }

        }

        str += "<h2>Unique</h2>";
        str += "<div style='overflow:hidden;'>" + addUniqueForm() + "</div>";
        List<Unique> uList = struct.getUniqueList();
        for (Unique unique : uList) {
          str += "<div style='overflow:hidden;'>";
          str += changeUniqueForm(unique, uList.indexOf(unique));
          str += deleteUniqueForm(unique, uList.indexOf(unique));
          str += "</div>";
        }

      }
    }

    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    // изменить поле
    if (params.get("submit") != null && params.get("fieldName") != null) {
      ModelStructureManager manager = new ModelStructureManager(app);
      status = manager.changeField(getParameter("appName"), getParameter("mandatory"), getParameter("updatable"),
              getParameter("relations"), getParameter("structureAlias"), getParameter("fieldName"), getParameter("def"));
      if (status == true) {
        redirectAction = "OneStructure";
        redirectObject = Creator.MODEL_OBJECT_NAME;
        redirectParams.put("structureAlias", getParameter("structureAlias"));
        isRedirect = true;
      } else {
        str += manager.getErrors();
      }
    } else if (!specAction.isEmpty() && !specAction.equals("default")) {
      String structureAlias = getParameter("structureAlias");
      String fieldAlias = getParameter("fieldAlias");
      String validatorId = getParameter("validatorId");
      ModelStructureManager manager = new ModelStructureManager(app);

      // удаление валидатора
      if (specAction.equals("deleteValidator")) {
        manager.deleteValidator(structureAlias, fieldAlias, validatorId);
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // поднять валидатор
      if (specAction.equals("upValidator")) {
        manager.upValidator(structureAlias, fieldAlias, validatorId);
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // опустить валидатор
      if (specAction.equals("downValidator")) {
        manager.downValidator(structureAlias, fieldAlias, validatorId);
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // удалить поле
      if (specAction.equals("deleteField")) {
        manager.deleteField(structureAlias, fieldAlias);
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // удалить поле вместе с данными
      if (specAction.equals("deleteFieldWithData")) {
        manager.deleteFieldWithData(structureAlias, fieldAlias);
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // удалить структуру
      if (specAction.equals("deleteStructure")) {
        manager.deleteStructure(structureAlias);

        redirectAction = "AllStructure";

      }

      // добавить Unique
      if (specAction.equals("addUnique")) {
        manager.addUnique(structureAlias, getParameter("names"), getParameter("checkDeleted"));
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // изменить Unique
      if (specAction.equals("changeUnique")) {
        manager.changeUnique(structureAlias, getParameter("names"), getParameter("checkDeleted"), getParameter("index"));
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // удалить Unique
      if (specAction.equals("deleteUnique")) {
        manager.deleteUnique(structureAlias, getParameter("index"));
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      // добавить поле
      if (specAction.equals("addField")) {
        DataTypes dataType = DataTypes.getTypeByString(getParameter("type"));
        
        manager.addField(getParameter("alias"), getParameter("appName"), getParameter("mandatory"),
                getParameter("updatable"), dataType, getParameter("relations"),
                getParameter("structureAlias"), getParameter("def"));
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);
      }

      redirectObject = Creator.MODEL_OBJECT_NAME;
      isRedirect = true;
    }

    return status;
  }

  private String getParameter(String paramName) {
    String str = null;
    if (params.get(paramName) != null) {
      str = params.get(paramName).toString().trim();
    }
    return str;
  }

  private String addFieldForm() throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("alias", null, "Алиас поля"), "Алиас поля");
    hs.put(rd.textInput("appName", null, "Имя в приложении"), "Имя в приложении");
    hs.put(rd.checkBox("mandatory", false, null), "Является ли обязательным");
    hs.put(rd.checkBox("updatable", true, null), "Является ли обновляемым");
    LinkedHashMap<String, Object> typeMap = new LinkedHashMap<String, Object>();
    typeMap.put("int", "INTEGER");
    typeMap.put("char", "CHAR");
    typeMap.put("datetime", "DATETIME");
    typeMap.put("bool", "BOOL");
    typeMap.put("decimal", "DECIMAL");
    typeMap.put("text", "TEXT");
    hs.put(rd.combo(typeMap, null, "type"), "Тип");
    hs.put(rd.combo(relationsMap, null, "relations"), "Зависимость");
    hs.put(rd.textInput("def", null, "Значение по умолчанию"), "Значение по умолчанию");
    hs.put(rd.hiddenInput("specAction", "addField"), "");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    hs.put(rd.hiddenInput("structureAlias", params.get("structureAlias")), "");
    AbsEnt form = rd.verticalForm(hs, "Добавить", "images/add.png");
    form.setAttribute(EnumAttrType.action, rd.getBaseLinkPath());
    return form.render();
  }

  private String changeFieldForm(Field field) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("appName", field.getAppName(), "Имя в приложении"), "Имя в приложении");
    hs.put(rd.checkBox("mandatory", field.isMandatory(), null), "Обязательное");
    hs.put(rd.checkBox("updatable", field.isUpdatable(), null), "Обновляемое");
    hs.put(rd.combo(relationsMap, field.getRelations(), "relations"), "Зависимость");
    hs.put(rd.textInput("def", field.getDef(), "Значение по умолчанию"), "Значение по умолчанию");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    hs.put(rd.hiddenInput("structureAlias", params.get("structureAlias")), "");
    AbsEnt form = rd.horizontalForm(hs, "Отправить", "images/ok.png");
    form.addEnt(rd.hiddenInput("fieldName", field.getAlias()));
    form.setAttribute(EnumAttrType.action, rd.getBaseLinkPath());
    form.setAttribute(EnumAttrType.style, "float:none;");
    return form.render();
  }

  private String fieldInfo(Field field) throws Exception {
    String str = "";
    str += "Имя в приложении: " + field.getAppName() + "<br/>";
    String mandatory = (field.isMandatory() ? "да" : "нет");
    str += "Обязательное: " + mandatory + "<br/>";
    String updatable = (field.isUpdatable() ? "да" : "нет");
    str += "Обновляемое: " + updatable + "<br/>";
    str += "Зависимость: " + field.getRelations() + "<br/>";
    str += "Значение по умолчанию: " + field.getDef() + "<br/>";
    return str;
  }

  private String addUniqueForm() throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("names", null, "Список полей"), "");
    hs.put(rd.checkBox("checkDeleted", false, null), "");
    hs.put(rd.hiddenInput("specAction", "addUnique"), "");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    hs.put(rd.hiddenInput("structureAlias", params.get("structureAlias")), "");
    AbsEnt form = rd.horizontalForm(hs, "Добавить Unique", "images/add.png");
    form.setAttribute(EnumAttrType.action, rd.getBaseLinkPath());
    return form.render();
  }

  private String changeUniqueForm(Unique unique, int index) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    String names = "";
    for (String name : unique.getFieldNames()) {
      names += name + ", ";
    }
    hs.put(rd.textInput("names", names, "Список полей"), "");
    hs.put(rd.checkBox("checkDeleted", unique.isCheckDeleted(), null), "");
    hs.put(rd.hiddenInput("specAction", "changeUnique"), "");
    hs.put(rd.hiddenInput("index", index), "");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    hs.put(rd.hiddenInput("structureAlias", params.get("structureAlias")), "");
    AbsEnt form = rd.horizontalForm(hs, "Именить Unique", "images/change.png");
    form.setAttribute(EnumAttrType.action, rd.getBaseLinkPath());
    return form.render();
  }

  private String deleteUniqueForm(Unique unique, int index) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.hiddenInput("specAction", "deleteUnique"), "");
    hs.put(rd.hiddenInput("index", index), "");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    hs.put(rd.hiddenInput("structureAlias", params.get("structureAlias")), "");
    AbsEnt form = rd.horizontalForm(hs, "Удалить Unique", "images/delete.png");
    form.setAttribute(EnumAttrType.action, rd.getBaseLinkPath());
    return form.render();
  }
}
