/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.modelEnts;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import option.Creator;
import option.ents.ModelEnt;
import option.objects.ModuleError;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.modelStructure.Field;
import prim.modelStructure.Structure;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.ControllerMethod;
import warehouse.controllerStructure.ControllerOrigin;
import warehouse.controllerStructure.ControllerService;
import warehouse.controllerStructure.ServiceParameter;
import warehouse.modelKeeper.ModelStructureKeeper;
import warehouse.pair.PairKeeper;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.pair.Pair;
import web.pair.PairObject;
import web.pair.Sequence;
import web.pair.SequenceObject;

/**
 *
 * @author User
 */
public class AllStructure extends ModelEnt {
  
  public AllStructure(AbstractApplication app,Render rd,String action,String specAction) {
    super(app, rd, action, specAction);
  }
  
  @Override
  public String render() throws Exception{
    String str = "";
      try {

      if (params.get("modelName") != null && !"".equals(params.get("modelName")) && specAction.equals("modulate")) {
        String modelName = params.get("modelName").toString();

        ModuleError result;
        str += "</br>Модуляция контроллера........";
        result = modulateController(modelName);
        for (String res : result.getMessages()) {
          str += "</br> " + res;
        }
        if (result.getErrors().isEmpty()) {
          str += "</br><b>Контроллер смоделирован</b>";
          str += "</br>Модуляция пар........";
          result = modulatePairs(modelName, null);
          for (String res : result.getMessages()) {
            str += "</br> " + res;
          }
          if (result.getErrors().isEmpty()) {
            str += "</br><b>Пары смоделированы</b>";
          }
        } else {
          for (String res : result.getErrors()) {
            str += "</br> " + res;
          }
        }
        str += "</br> ";
      }

      str += "<br/>" + href(Creator.MODEL_OBJECT_NAME, "AddStructure", "", "Добавить новый тип данных", new HashMap()) + "<br/>";
      str += "<h1>Модели данных</h1>";

      ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

      TreeMap<String, Structure> structureMap = new TreeMap<String, Structure>(mss.getStructureMap());
      ArrayList<String> systemMap = new ArrayList<String>();
      // показать список

      str += "<table border=1>";
      str += "<tr><td>Название</td><td>Полей</td><td>Первичный ключ</td><td>Работа с файлами</td><td>связи</td></tr>";
      for (String name : structureMap.keySet()) {
        Structure structure = structureMap.get(name);
        if (!structure.isSystem()) {
          Map<String, Object> oneStructureParams = new HashMap();
          oneStructureParams.put("structureAlias", name);
          str += "<tr><td>" + href(Creator.MODEL_OBJECT_NAME, "OneStructure", "", name, oneStructureParams) + "</td>";
          str += "<td>" + (structure.getCloneFields().size() - 8) + "</td>";
          str += "<td>" + structure.getPrimaryAlias() + "</td>";
          str += "<td>";
          if (structure.isFileWork() != false) {
            str += "Да";
          }
          str += "</td>";
          str += "<td>";
          for (Field fi : structure.getCloneFields().values()) {
            if (fi.getRelations() != null) {
              str += fi.getAlias() + "-" + fi.getRelations() + ";";
            }
          }
          str += "</td>";
          str += "<td>";
          
          LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
          hs.put(rd.hiddenInput("object", object), "");
          hs.put(rd.hiddenInput("action", action), "");
          hs.put(rd.hiddenInput("specAction", "modulate"), "");
          AbsEnt form = rd.verticalForm(hs, name, "images/refresh.png");
          form.addEnt(rd.hiddenInput("modelName", name));
          str += form.render();


          str += "</td></tr>";
        } else {
          systemMap.add(name);
        }
      }
      str += "</table>";

      str += "<br/><br/><b>Системные:</b>";
      str += "<table>";
      for (String name : systemMap) {
        Map<String, Object> oneStructureParams = new HashMap();
        oneStructureParams.put("structureAlias", name);
        str += "<tr><td>" + href(Creator.MODEL_OBJECT_NAME, "OneStructure", "", name, oneStructureParams) + "</td><td>";

        AbsEnt form = rd.form("./AllStructure?action=modulate");
        form.addEnt(rd.formSubmit("Смоделировать приложение", "images/refresh.png"));
        form.addEnt(rd.hiddenInput("modelName", name));
        form.setAttribute(EnumAttrType.action, "");
        str += form.render();
        str += "</td></tr>";
      }
      str += "</table>";

    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    } 
      return str;
  }
 
  
  
  @Override
  public Boolean run() throws Exception {
    return true;
  }
  
   
  private ModuleError modulateController(String modelName) {
    ModuleError result = new ModuleError();
    ArrayList<String> banParams = new ArrayList<String>();
    banParams.add("update_user_id");
    banParams.add("insert_user_id");
    banParams.add("delete_user_id");
    banParams.add("update_date");
    banParams.add("insert_date");
    banParams.add("delete_date");
    banParams.add("user_data_type_id");
    try {
      //ModelStructureKeeper mss = ModelStructureKeeper.getInstance(SettingOptions.set("project"));
      ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
      if (mss.hasStructure(modelName) != true) {
        throw new Exception("Модели с переданным именем не существует");
      }
      Structure modelStructure = mss.getStructure(modelName);
      //ControllerKeeper cs = ControllerKeeper.getInstance(SettingOptions.set("project"));
      ControllerKeeper cs = app.getKeeper().getControllerKeeper();
      cs.setDataFromBase();

      cs.addController(modelName);
      ControllerService css;
      ServiceParameter cp;

      //Добавить метод поиска
      String method = "search";
      if (!cs.hasMethod(modelName, method)) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Просмотр списка");
        cm.setDescription("Просмотр списка");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("findActive");
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер поиска");
        }
        result.setMessage("Создан метод поиска для контроллера " + modelName);
      }

      //Добавить метод просмотра сущности
      method = "showOne";
      if (!cs.hasMethod(modelName, method)) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Просмотр");
        cm.setDescription("Просмотр");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("searchById");
        cp = css.addInnerParams(modelStructure.getPrimaryAlias());
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias(modelStructure.getPrimaryAlias());
        cp = css.addInnerParams(modelStructure.getPrimaryAlias());
        cp.setAlias(modelStructure.getPrimaryAlias());
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер просмотра");
        }
        result.setMessage("Создан метод просмотра для контроллера " + modelName);
      }

      //Добавить метод добавления
      method = "add";
      if (!cs.hasMethod(modelName, method)) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Добавить");
        cm.setDescription("Добавить");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("saveModel");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (!fi.getAlias().equals(modelStructure.getPrimaryAlias()) && !banParams.contains(fi.getAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
          }
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addOuterParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Input);
            cp.setAlias(fi.getAlias());
          }
        }
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер добавления");
        }
        result.setMessage("Создан метод добавления для контроллера " + modelName);
      }

      //Добавить метод изменения
      method = "change";
      if (!cs.hasMethod(modelName, method)) {
        ControllerMethod cm = new ControllerMethod();

        cm.setAlias("Изменить");
        cm.setDescription("Изменить");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("updateModel");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (!banParams.contains(fi.getAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
          }
        }
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер изменения");
        }
        result.setMessage("Создан метод изменения для контроллера " + modelName);
      }

      //Добавить метод удаления
      method = "delete";
      if (!cs.hasMethod(modelName, method)) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Удалить");
        cm.setDescription("Удаление");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("closeModel");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
          }
        }
        cp = css.addInnerParams("cId");
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias("cId");

        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер изменения");
        }
        result.setMessage("Создан метод удаления для контроллера " + modelName);
      }

    } catch (Exception e) {
      result.setError(e.getMessage());
    }
    return result;
  }
  
  private ModuleError modulatePairs(String modelName, String pairName) throws Exception {
    ModuleError result = new ModuleError();

    PairKeeper ps = app.getKeeper().getPairKeeper();
    Pair pair = ps.getPair();
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

    Structure ModelStructure = mss.getStructure(modelName);
    if (ModelStructure == null) {
      throw new Exception("Модели с переданным именем не существует");
    }
    String method = "search";
    if (!ps.containsPair(modelName, method)) {
      Sequence searchSeq = SequenceObject.getInstance("default", modelName, method, modelName + ":" + "renderEntityList", modelName + ":" + "renderEntityList", null, null, null, null);
      Map<String, Sequence> seq = new HashMap();
      seq.put(searchSeq.getName(), searchSeq);
      Pair searchPair = PairObject.getInstance(modelName, method, false, seq, null, pair);
      pair.addPair(searchPair);
    }

    method = "change";
    if (!ps.containsPair(modelName, method)) {
      Pair changePair = PairObject.getInstance(modelName, method, false, null, null, pair);
      Sequence changeSeq = SequenceObject.getInstance("default", modelName, "showOne", modelName + ":" + "renderChangeEntityForm", modelName + ":" + "renderChangeEntityForm", null, null, null, null);
      changePair.setSequence(changeSeq);
      changeSeq = SequenceObject.getInstance("change", modelName, "change", null, modelName + ":" + "renderChangeEntityForm", modelName + ":search", null, null, null);
      changePair.setSequence(changeSeq);
      pair.addPair(changePair);
    }

    method = "delete";
    if (!ps.containsPair(modelName, method)) {
      Pair deletePair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair);
      Sequence deleteSeq = SequenceObject.getInstance("default", modelName, method, null, null, modelName + ":search", modelName + ":search", null, null);
      deletePair.setSequence(deleteSeq);
      pair.addPair(deletePair); 
    }



    method = "add";
    if (!ps.containsPair(modelName, method)) {
      Pair addPair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair);

      Sequence defSeq = SequenceObject.getInstance("default", null, null, modelName + ":renderAddEntityForm", modelName + ":renderAddEntityForm", null, null, null, null);
      addPair.setSequence(defSeq);
      Sequence addSeq = SequenceObject.getInstance("add", modelName, method, null, modelName + ":renderAddEntityForm", modelName + ":search", null,  null, null);

      addPair.setSequence(addSeq);
      pair.addPair(addPair);
    }



    method = "showOne";
    if (!ps.containsPair(modelName, method)) {
      Pair showPair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair);
      Sequence showSeq = SequenceObject.getInstance("default", modelName, method, modelName + ":" + "renderOneEntity", modelName + ":" + "renderOneEntity", modelName, pairName, pairName, pairName);
      showPair.setSequence(showSeq);
      pair.addPair(showPair);
    }


    ps.SaveCollectionInFile();
    return result;
  }
  
}
