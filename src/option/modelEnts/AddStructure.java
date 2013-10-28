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
public class AddStructure extends ModelEnt {

  private String str = "";
  TreeMap<String, Object> relationsMap = new TreeMap<String, Object>();
  private ArrayList<String> errors = new ArrayList<String>();

  public AddStructure(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    String alias = (params.get("alias") != null ? params.get("alias").toString().trim() : null);
    String primaryName = (params.get("primaryName") != null ? params.get("primaryName").toString().trim() : null);
    String fileWork = (params.get("fileWork") != null ? params.get("fileWork").toString() : "");
    
     try {
      
      str += href(Creator.MODEL_OBJECT_NAME, "AllStructure", "", "Перейти к списку всех моделей", new HashMap());
      str += "<h1>Добавить новый тип данных</h1>";

      if (!errors.isEmpty()) {
        str += errors;
      }

      LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
      hs.put(rd.textInput("alias", alias, "Алиас модели"), "Алиас модели");
      hs.put(rd.textInput("primaryName", primaryName, "Название первичного ключа"), "Название первичного ключа");
      hs.put(rd.checkBox("fileWork", fileWork), "Разрешена ли работа с файлами");
      AbsEnt form = rd.verticalForm(hs, "Добавить", "images/add.png");
      form.setAttribute(EnumAttrType.action, "");
      str += form.render();

    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    }
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;
    // добавить новую структуру модели
    if (params.get("submit") != null) {

      String alias = (params.get("alias") != null ? params.get("alias").toString().trim() : null);
      String primaryName = (params.get("primaryName") != null ? params.get("primaryName").toString().trim() : null);
      String fileWork = (params.get("fileWork") != null ? params.get("fileWork").toString() : "");

      ModelStructureManager manager = new ModelStructureManager(app);
      status = manager.addStructure(alias, primaryName, fileWork);

      errors.addAll(manager.getErrors());
      if (status == true) {
        redirectAction = "AllStructure";
        redirectObject = Creator.MODEL_OBJECT_NAME;
        isRedirect = true;
      }
    }
    return status;
  }
}
