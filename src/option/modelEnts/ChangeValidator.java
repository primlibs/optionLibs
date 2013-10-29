/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.modelEnts;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import option.Creator;
import option.ents.ModelEnt;
import option.objects.Valids;
import prim.AbstractApplication;
import prim.filterValidator.entity.ValidatorAbstract;
import prim.libs.MyString;
import prim.modelStructure.Field;
import prim.modelStructure.Structure;
import prim.modelStructure.Unique;
import warehouse.modelKeeper.ModelStructureKeeper;
import warehouse.modelKeeper.ModelStructureManager;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;

/**
 *
 * @author User
 */
public class ChangeValidator extends ModelEnt {

  private String str = "";
  TreeMap<String, Object> relationsMap = new TreeMap<String, Object>();
  List<String> errors = new ArrayList();

  public ChangeValidator(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {

    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    ValidatorAbstract validator = null;
    Structure struct = null;
    Field field = null;
    String structureAlias = (params.get("structureAlias") != null ? params.get("structureAlias").toString().trim() : "");
    String fieldAlias = (params.get("fieldAlias") != null ? params.get("fieldAlias").toString().trim() : "");
    String validatorName = null;
    String validatorId = (params.get("validatorId") != null ? params.get("validatorId").toString().trim() : "");
    String content = "";

    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

    if (structureAlias != null & fieldAlias != null & validatorId != null) {
      if (mss.hasStructure(structureAlias)) {
        struct = mss.getStructure(structureAlias);
        if (struct.hasField(fieldAlias)) {
          field = struct.getField(fieldAlias);
          List<ValidatorAbstract> validators = field.getCloneValidatorList();

          validator = validators.get(Integer.parseInt(validatorId));
          validatorName = validator.getClass().toString();
          int dotPos = validatorName.lastIndexOf(".") + 1;
          validatorName = validatorName.substring(dotPos);
        }
      }
    }

    if (validator != null) {
      HashMap<String, Object> validatorParams = validator.getParameters();
      if (params.get("submit") != null) {
        for (String name : validatorParams.keySet()) {
          validatorParams.put(name, params.get(name));
        }
        ModelStructureManager manager = new ModelStructureManager(app);

        status = manager.changeValidator(structureAlias, fieldAlias, validatorId, validatorParams);
        if (status == true) {
          redirectAction = "OneStructure";
          redirectObject = Creator.MODEL_OBJECT_NAME;
          redirectParams.put("structureAlias", structureAlias);
          isRedirect = true;
        } else {
          errors.addAll(manager.getErrors());
        }
      }

      ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
      LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();

      for (String name : validatorParams.keySet()) {
        hs.put(rd.textInput(name, validatorParams.get(name), "Значение"), name);
      }

      AbsEnt form = rd.verticalForm(hs, "Сохранить изменения", "images/ok.png");
      form.setAttribute(EnumAttrType.action, "");
      content = form.render();

    } else {
      content = "Валидатор с таким именем не найден";
    }
    
    Map<String, Object> modelParams = new HashMap();
    modelParams.put("structureAlias", structureAlias);
    str += href(Creator.MODEL_OBJECT_NAME, "AllStructure", "", "Перейти к списку всех моделей", new HashMap()) + "</br>";
    str += href(Creator.MODEL_OBJECT_NAME, "OneStructure", "", "Перейти к модели " + structureAlias, modelParams) + "</br>";
    str += ("<h1>Изменение параметров валидатора</h1>");
    str += ("<h2>Модель: " + structureAlias + ", поле: " + fieldAlias + ", валидатор: " + validatorName + "</h2>");

    if (!errors.isEmpty()) {
      str += (errors);
    }
    str += (content);

    return status;
  }

}
