/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.modelEnts;

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
public class ChangeStructure extends ModelEnt {

  private String str = "";
  TreeMap<String, Object> relationsMap = new TreeMap<String, Object>();
  List<String> errors = new ArrayList();

  public ChangeStructure(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {

    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    String structureAlias = (params.get("structureAlias") != null ? params.get("structureAlias").toString().trim() : "");

    str += ("<a href='AllStructure'>Перейти к списку всех моделей</a></br>");
    str += ("<a href='OneStructure?structureAlias=" + structureAlias + "'>Перейти к модели " + structureAlias + "</a>");
    str += ("<h1>Изменить параметры модели " + structureAlias + "</h1>");

    if (!errors.isEmpty()) {
      str += (errors);
    }

    if (structureAlias != null) {
      if (mss.hasStructure(structureAlias)) {
        Structure struct = mss.getStructure(structureAlias);

        LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
        hs.put(rd.checkBox("fileWork", struct.isFileWork(), null), "Работа с файлами");

        AbsEnt form = rd.verticalForm(hs, "Обновить", "images/refresh.png");
        form.setAttribute(EnumAttrType.action, "");
        str += (form.render());
      }
    }

    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    String structureAlias = (params.get("structureAlias") != null ? params.get("structureAlias").toString().trim() : "");
    String fileWork = (params.get("fileWork") != null ? params.get("fileWork").toString() : "");

    if (params.get("submit") != null) {
      ModelStructureManager manager = new ModelStructureManager(app);
      status = manager.changeStructure(structureAlias, fileWork);
      errors = manager.getErrors();
      // добавить поле и сохранить
      if (status == true) {

        isRedirect = true;
        redirectObject = Creator.MODEL_OBJECT_NAME;
        redirectAction = "OneStructure";
        redirectParams.put("structureAlias", structureAlias);

      }
    }

    return status;
  }
}
