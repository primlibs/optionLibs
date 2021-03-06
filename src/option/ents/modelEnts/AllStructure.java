/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.modelEnts;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.Creator;
import option.ents.ModelEnt;
import option.objects.ModuleError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.prim.core.AbstractApplication;
import com.prim.support.MyString;
import com.prim.support.primXml;
import com.prim.core.modelStructure.Field;
import com.prim.core.modelStructure.Structure;
import com.prim.core.modelStructure.StructureFabric;
import com.prim.core.warehouse.WarehouseSingleton;
import com.prim.core.warehouse.controllerStructure.ControllerKeeper;
import com.prim.core.warehouse.controllerStructure.ControllerMethod;
import com.prim.core.warehouse.controllerStructure.ControllerOrigin;
import com.prim.core.warehouse.controllerStructure.ControllerService;
import com.prim.core.warehouse.controllerStructure.ServiceParameter;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;
import com.prim.core.warehouse.pair.PairKeeper;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;
import com.prim.core.pair.Pair;
import com.prim.core.pair.PairObject;
import com.prim.core.pair.Sequence;
import com.prim.core.pair.SequenceObject;
import com.prim.core.representation.Xml;
import com.prim.support.enums.DataTypes;

/**
 *
 * @author User
 */
public class AllStructure extends ModelEnt {

  private final String DOWNLOAD_FILE_SPECACTION = "downloadModelFiles";
  private final String UPLOAD_FILE_SPECACTION = "uploadModelFiles";
  private final String CHECK_SPECACTION = "checkModels";
  private String content = "";
  private List<String> errors = new ArrayList();

  public AllStructure(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return content;
  }

  @Override
  public Boolean run() throws Exception {
    try {

      // вернуть файл моделей
      if (specAction.equals(DOWNLOAD_FILE_SPECACTION)) {
        getModelsFile();
        return true;
      }

      // загрузить файл моделей
      if (specAction.equals(UPLOAD_FILE_SPECACTION)) {
        uploadModelsFile();
        redirectObject = "modelEnt";
        redirectAction = "AllStructure";
        isRedirect = true;
        return true;
      }

      ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

      TreeMap<String, Structure> structureMap = new TreeMap<String, Structure>(mss.getStructureMap());
      ArrayList<String> systemMap = new ArrayList<String>();

      if (specAction.equals(CHECK_SPECACTION)) {
        List<String> incorrect = checkIncorrectModels(mss);
        content += showCheckResult(incorrect);
        return true;
      }

      if (params.get("modelName") != null && !"".equals(params.get("modelName")) && 
              (specAction.equals("modulate") || specAction.equals("modulateNew"))) {
        String modelName = params.get("modelName").toString();

        content += errors;

        ModuleError result;
        content += "</br>Модуляция контроллера........";
        result = modulateController(modelName);
        for (String res : result.getMessages()) {
          content += "</br> " + res;
        }
        if (result.getErrors().isEmpty()) {
          content += "</br><b>Контроллер смоделирован</b>";
          content += "</br>Модуляция пар........";
          
          if (specAction.equals("modulate")) {
            result = modulatePairs(modelName, null);
          } else if (specAction.equals("modulateNew")) {
            result = modulateNewPairs(modelName);
          }
          
          for (String res : result.getMessages()) {
            content += "</br> " + res;
          }
          if (result.getErrors().isEmpty()) {
            content += "</br><b>Пары смоделированы</b>";
          }
        } else {
          for (String res : result.getErrors()) {
            content += "</br> " + res;
          }
        }
        content += "</br> ";
      }

      content += "<br/>" + href(Creator.MODEL_OBJECT_NAME, "AddStructure", "", "Добавить новый тип данных", new HashMap()) + "<br/>";
      content += checkForm();
      content += "<h1>Модели данных</h1>";


      // показать список

      content += "<table border=1>";
      content += "<tr><td>Название</td><td>Полей</td><td>Первичный ключ</td><td>Работа с файлами</td><td>связи</td></tr>";
      for (String name : structureMap.keySet()) {
        Structure structure = structureMap.get(name);
        if (!structure.isSystem()) {
          Map<String, Object> oneStructureParams = new HashMap();
          oneStructureParams.put("structureAlias", name);
          content += "<tr><td>" + href(Creator.MODEL_OBJECT_NAME, "OneStructure", "", name, oneStructureParams) + "</td>";
          content += "<td>" + (structure.getCloneFields().size() - 8) + "</td>";
          content += "<td>" + structure.getPrimaryAlias() + "</td>";
          content += "<td>";
          if (structure.isFileWork() != false) {
            content += "Да";
          }
          content += "</td>";
          content += "<td>";
          for (Field fi : structure.getCloneFields().values()) {
            if (fi.getRelations() != null) {
              content += fi.getAlias() + "-" + fi.getRelations() + ";";
            }
          }
          content += "</td>";
          content += "<td>";

          LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
          hs.put(rd.hiddenInput("object", object), "");
          hs.put(rd.hiddenInput("action", action), "");
          hs.put(rd.hiddenInput("specAction", "modulate"), "");
          AbsEnt form = rd.verticalForm(hs, name, "images/refresh.png");
          form.addEnt(rd.hiddenInput("modelName", name));
          content += form.render();
          content += "</td>";
          
          LinkedHashMap<AbsEnt, String> hsNew = new LinkedHashMap<AbsEnt, String>();
          hsNew.put(rd.hiddenInput("object", object), "");
          hsNew.put(rd.hiddenInput("action", action), "");
          hsNew.put(rd.hiddenInput("specAction", "modulateNew"), "");
          AbsEnt formNew = rd.verticalForm(hsNew, name + " NEW MODULATE", null);
          formNew.addEnt(rd.hiddenInput("modelName", name));
          content += "<td>" +  formNew.render() + "<td/>";

          content += "</tr>";
        } else {
          systemMap.add(name);
        }
      }
      content += "</table>";

      content += "<br/><br/><b>Системные:</b>";
      content += "<table>";
      for (String name : systemMap) {
        Map<String, Object> oneStructureParams = new HashMap();
        oneStructureParams.put("structureAlias", name);
        content += "<tr><td>" + href(Creator.MODEL_OBJECT_NAME, "OneStructure", "", name, oneStructureParams) + "</td><td>";
        AbsEnt form = rd.form(false);
        form.addEnt(rd.formSubmit("Смоделировать приложение", "images/refresh.png"));
        form.addEnt(rd.hiddenInput("modelName", name));
        form.addEnt(rd.hiddenInput("object", object));
        form.addEnt(rd.hiddenInput("action", action));
        form.addEnt(rd.hiddenInput("specAction", "modulate"));
        form.setAttribute(EnumAttrType.action, "");
        content += form.render();
        content += "</td></tr>";
      }
      content += "</table>";

      content += downloadForm(mss);
      content += uploadForm();

    } catch (Exception e) {
      content += MyString.getStackExeption(e);
    }
    return true;
  }

  // методы получения данных -----------------------------------------------------------------------------------------------------------
  /**
   * проверка пар на наличие пар с некорректными значениями
   */
  private List<String> checkIncorrectModels(ModelStructureKeeper mss) throws Exception {
    List<String> incorrect = new ArrayList();
    // получить список моделей
    Map<String, Structure> structureMap = mss.getStructureMap();
    for (String modelName : structureMap.keySet()) {
      // для каждой модели
      // получить список полей
      Structure struct = structureMap.get(modelName);
      Map<String, Field> fields = struct.getCloneFields();
      // для каждого поля
      for (String fieldName : fields.keySet()) {
        Field field = fields.get(fieldName);
        // если есть связь с моделью, которой нет в списке
        String relation = field.getRelations();
        // то добавить в список некорректных
        if (relation != null && !relation.trim().isEmpty() && !structureMap.containsKey(relation.trim())) {
          incorrect.add("Модель: " + modelName + ", поле:" + fieldName + ", зависимость: " + relation);
        }
      }
    }
    return incorrect;
  }

  /**
   * получить файл моделей
   *
   * @throws Exception
   */
  private void getModelsFile() throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

    Map<String, Structure> models = mss.getStructureMap();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);

    // найти структуры по переданным именам
    // составить из них файл xml
    // каждая структура вовращает себя в xml
    String[] modelsNames = getArray("models");
    for (String name : modelsNames) {
      if (models.containsKey(name)) {
        Element modelElement = primXml.createEmptyElement(doc, root, Structure.ELEMENT_NAME);
        Structure modelStructure = models.get(name);
        //modelStructure.getSelfInXml(doc, modelElement);
        Xml.structureToXml(doc, modelElement, modelStructure);
      }
    }

    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = "models.xml";
  }

  /**
   * загрузить файлы моделей
   *
   * @throws Exception
   */
  private void uploadModelsFile() throws Exception {
    // получить элементы из файла xml
    Map<String, String> filesMap = (HashMap<String, String>) params.get("_FILEARRAY_");
    if (filesMap.size() > 0) {
      File file = null;
      Document doc = null;
      for (String path : filesMap.keySet()) {
        file = new File(path);
      }
      if (file != null) {
        try {
          doc = primXml.getDocumentByFile(file);
        } catch (Exception e) {
          errors.add("Файл не является файлом XML или имеет неправильную структуру");
        }
        if (doc != null) {
          
          NodeList list = doc.getChildNodes();
          Element root = (Element) list.item(0);
          NodeList modelsNodeList = root.getElementsByTagName(Structure.ELEMENT_NAME);

          // получить все модели из ModelStructureManager
          ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

          Map<String, Structure> models = mss.getStructureMap();
          // для каждого элемента
          for (int i = 0; i < modelsNodeList.getLength(); i++) {
            Element modelElement = (Element) modelsNodeList.item(i);
            // создать модель
            //Structure newStructure = StructureFabric.getStructureFromXml(modelElement);
            Structure newStructure = Xml.structureFromXml(modelElement);
            if (!newStructure.isSystem()) {
              String name = newStructure.getName();
              // если модели с таким именем нет в списке
              if (!models.containsKey(name)) {
                // добавить
                /*
                app.getConnection().prepareStatement("insert into user_data_types (name, active_from, struct_text) values (?, now(), ?)");
                if (!app.equals(mss.getApplication())) {
                  throw new Exception(app.getConnection().isClosed() + " " + mss.getApplication().getConnection().isClosed() + " ");
                }
                */
                mss.addStructure(name, newStructure);
                
              } else {
                // если модель с таким именем уже есть
                // если поставлена галочка заменять
                Structure oldStructure = models.get(name);
                if (!oldStructure.isSystem()) {
                  if (params.get("replace") != null) {
                    // то обновить
                    mss.updateStructure(name, newStructure);
                  }
                }
              }
              refreshWarehouseSingleton();
            }
          }
        }
      }
    }
  }

  /**
   * создать контроллеры
   *
   * @param modelName
   * @return
   */
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
        /*
        cp = css.addInnerParams(modelStructure.getPrimaryAlias());
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias(modelStructure.getPrimaryAlias());
        */
        cp = css.addInnerParams(modelStructure.getPrimaryAlias());
        cp.setAlias(modelStructure.getPrimaryAlias());
        cp.setOrigin(ControllerOrigin.Request);
        setParameterSettings(cp, modelStructure.getField(modelStructure.getPrimaryAlias()));
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
            setParameterSettings(cp, fi);
          }
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addOuterParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Input);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
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
            setParameterSettings(cp, fi);
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
            setParameterSettings(cp, fi);
          }
        }
        cp = css.addInnerParams("cId");
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias("cId");
        cp.setDataType(DataTypes.CHAR); 

        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер изменения");
        }
        result.setMessage("Создан метод удаления для контроллера " + modelName);
      }

      //метод просмотра файлов
      method = "showAllFiles";
      if (!cs.hasMethod(modelName, method) && modelStructure != null && modelStructure.isFileWork() == true) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Просмотр списка файлов");
        cm.setDescription("Просмотр списка файлов");
        css = cm.addControllerService();
        css.setServiceName("files");
        css.setServiceAction("showFilesModel");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias("model_id");
            setParameterSettings(cp, fi);
          }
        }
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер просмотра файлов");
        }
        result.setMessage("Создан метод просмотра файлов для контроллера " + modelName);
      }
      //метод добавления файлов
      method = "addFiles";
      if (!cs.hasMethod(modelName, method) && modelStructure != null && modelStructure.isFileWork() == true) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Добавление файлов");
        cm.setDescription("Добавление файлов");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("saveFiles");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
            cp = css.addOuterParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Input);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
          }
        }
        cp = css.addInnerParams("_FILEARRAY_");
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias("_FILEARRAY_");
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер сохранения файлов");
        }
        result.setMessage("Создан метод сохранения файлов для контроллера " + modelName);
      }

      //метод удаления файла
      method = "delFile";
      if (!cs.hasMethod(modelName, method) && modelStructure != null && modelStructure.isFileWork() == true) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Удаление файлов");
        cm.setDescription("Удаление файлов");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("deleteFile");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
            cp = css.addOuterParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Input);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
          }
        }
        cp = css.addInnerParams("file_id");
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias("file_id");
        cp.setDataType(DataTypes.INT);
        cp.setMandatory(true);
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер удаления файлов");
        }
        result.setMessage("Создан метод удаления файлов для контроллера " + modelName);
      }
      //метод прочтения файла
      method = "getFile";
      if (!cs.hasMethod(modelName, method) && modelStructure != null && modelStructure.isFileWork() == true) {
        ControllerMethod cm = new ControllerMethod();
        cm.setAlias("Получение файлов");
        cm.setDescription("Получение файлов");
        css = cm.addControllerService();
        css.setServiceName(modelName);
        css.setServiceAction("getFile");
        for (Field fi : modelStructure.getCloneFields().values()) {
          if (fi.getAlias().equals(modelStructure.getPrimaryAlias())) {
            cp = css.addInnerParams(fi.getAlias());
            cp.setOrigin(ControllerOrigin.Request);
            cp.setAlias(fi.getAlias());
            setParameterSettings(cp, fi);
          }
        }
        cp = css.addInnerParams("file_id");
        cp.setOrigin(ControllerOrigin.Request);
        cp.setAlias("file_id");
        cp.setDataType(DataTypes.INT);
        cp.setMandatory(true);
        cs.addMethod(modelName, method, cm);
        if (cs.saveController(modelName) != true) {
          throw new Exception("Не удалось сохранить контроллер получения файлов");
        }
        result.setMessage("Создан метод получения файлов для контроллера " + modelName);
      }

    } catch (Exception e) {
      result.setError(e.getMessage());
    }
    return result;
  }

  private void setParameterSettings(ServiceParameter parameter, Field field) {
    parameter.setMandatory(field.isMandatory());
    parameter.setDataType(field.getType());
  }
  
  private ModuleError modulateNewPairs(String modelName) throws Exception {
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
      Pair searchPair = PairObject.getInstance(modelName, method, false, new HashMap(), null, pair, true, modelName + ":search");
      pair.addPair(searchPair);
    }

    method = "change";
    if (!ps.containsPair(modelName, method)) {
      Pair changePair = PairObject.getInstance(modelName, method, false, new HashMap(), null, pair, true, modelName + ":change");
      pair.addPair(changePair);
    }

    method = "delete";
    if (!ps.containsPair(modelName, method)) {
      Pair deletePair = PairObject.getInstance(modelName, method, false, new HashMap(), null, pair, true, modelName + ":delete");
      pair.addPair(deletePair);
    }

    method = "add";
    if (!ps.containsPair(modelName, method)) {
      Pair addPair = PairObject.getInstance(modelName, method, false, new HashMap(), null, pair, true, modelName + ":add");
      pair.addPair(addPair);
    }

    method = "showOne";
    if (!ps.containsPair(modelName, method)) {
      Pair showPair = PairObject.getInstance(modelName, method, false, new HashMap(), null, pair, true, modelName + ":showOne");
      pair.addPair(showPair);
    }

    if (ModelStructure.isFileWork() == true) {
        
    }

    ps.SaveCollectionInFile();
    return result;
  }
  
  /**
   * создать пары
   *
   * @param modelName
   * @param pairName
   * @return
   * @throws Exception
   */
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
      Pair searchPair = PairObject.getInstance(modelName, method, false, seq, null, pair, false, "");
      pair.addPair(searchPair);
    }

    method = "change";
    if (!ps.containsPair(modelName, method)) {
      Pair changePair = PairObject.getInstance(modelName, method, false, null, null, pair, false, "");
      Sequence changeSeq = SequenceObject.getInstance("default", modelName, "showOne", modelName + ":" + "renderChangeEntityForm", modelName + ":" + "renderChangeEntityForm", null, null, null, null);
      changePair.setSequence(changeSeq);
      changeSeq = SequenceObject.getInstance("change", modelName, "change", null, modelName + ":" + "renderChangeEntityForm", modelName + ":search", null, null, null);
      changePair.setSequence(changeSeq);
      pair.addPair(changePair);
    }

    method = "delete";
    if (!ps.containsPair(modelName, method)) {
      Pair deletePair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
      Sequence deleteSeq = SequenceObject.getInstance("default", modelName, method, null, null, modelName + ":search", modelName + ":search", null, null);
      deletePair.setSequence(deleteSeq);
      pair.addPair(deletePair);
    }

    method = "add";
    if (!ps.containsPair(modelName, method)) {
      Pair addPair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");

      Sequence defSeq = SequenceObject.getInstance("default", null, null, modelName + ":renderAddEntityForm", modelName + ":renderAddEntityForm", null, null, null, null);
      addPair.setSequence(defSeq);
      Sequence addSeq = SequenceObject.getInstance("add", modelName, method, null, modelName + ":renderAddEntityForm", modelName + ":search", null, null, null);

      addPair.setSequence(addSeq);
      pair.addPair(addPair);
    }

    method = "showOne";
    if (!ps.containsPair(modelName, method)) {
      Pair showPair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
      Sequence showSeq = SequenceObject.getInstance("default", modelName, method, modelName + ":" + "renderOneEntity", modelName + ":" + "renderOneEntity", null, null, null, null);
      showPair.setSequence(showSeq);
      pair.addPair(showPair);
    }

    if (ModelStructure.isFileWork() == true) {
      //создание пары
      //метод просмотра файлов
      method = "showAllFiles";
      if (!ps.containsPair(modelName, method)) {
        Pair showAllFiles = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
        showAllFiles.setDef(false);
        Sequence showSeq = SequenceObject.getInstance("default", modelName, method, modelName + ":" + method, modelName + ":" + method, null, null, null, null);;
        showAllFiles.setSequence(showSeq);
        pair.addPair(showAllFiles);
      }
      //метод добавления файлов
      method = "addFiles";
      if (!ps.containsPair(modelName, method)) {
        Pair addFiles = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
        Sequence showSeq = SequenceObject.getInstance("default", modelName, method, modelName + ":" + method, modelName + ":" + method, null, null, null, null);;
        addFiles.setSequence(showSeq);
        
        Sequence addSeq = SequenceObject.getInstance("addFiles", modelName, method, null, modelName + ":" + method, modelName + ":search", null, null, null);;
        addFiles.setSequence(addSeq);
        
        pair.addPair(addFiles);
      }
      
      //метод удаления файла
      method = "delFile";
      if (!ps.containsPair(modelName, method)) {
        Pair deletePair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
        Sequence deleteSeq = SequenceObject.getInstance("default", modelName, method, null, null, modelName + ":search", modelName + ":search", null, null);;
        deletePair.setSequence(deleteSeq);
        pair.addPair(deletePair);
      }
      //метод прочтения файла
      method = "getFile";
      if (!ps.containsPair(modelName, method)) {
        Pair readFilePair = PairObject.getInstance(modelName, method, Boolean.FALSE, null, null, pair, false, "");
        Sequence seq = SequenceObject.getInstance("default", modelName, method, null, null, modelName + ":search", modelName + ":search", null, null);;
        readFilePair.setSequence(seq);
        pair.addPair(readFilePair);
      }

    }

    ps.SaveCollectionInFile();
    return result;
  }

  // методы отображения ----------------------------------------------------------------------------------------------------------
  /**
   * форма для скачивания файла
   *
   * @return
   * @throws Exception
   */
  private String downloadForm(ModelStructureKeeper mss) throws Exception {
    Map<String, Structure> structureMap = mss.getStructureMap();
    Map<String, Object> modelNames = new TreeMap();
    for (String name : structureMap.keySet()) {
      Structure struct = structureMap.get(name);
      if (!struct.isSystem()) {
        modelNames.put(name, name);
      }
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.multipleCombo(modelNames, null, "models", 10), "Модели");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", DOWNLOAD_FILE_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Получить файл моделей", "images/ok.png");
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * форма для загрузки
   *
   * @return
   * @throws Exception
   */
  private String uploadForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.fileInput("file", null, "Выберите файл"), "");
    inner.put(rd.checkBox("replace", null), "Заменять существующие");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", UPLOAD_FILE_SPECACTION), "");
    AbsEnt form = rd.horizontalForm(inner, "Загрузить файл моделей", null, true, null);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * форма - проверить пары
   *
   * @return
   */
  private String checkForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", CHECK_SPECACTION), "");
    AbsEnt form = rd.horizontalForm(inner, "Проверить модели", null);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * вывести результат проверки контроллеров
   *
   * @return
   */
  private String showCheckResult(List<String> incorrect) {
    // вывести список
    String str = "Список моделей, в которых найдены завивисмости с моделями, отсутствующими в системе <br/><br/>";
    if (incorrect.isEmpty()) {
      str += "Несоответствий не найдено";
    }
    for (String s : incorrect) {
      str += s + "<br/>";
    }
    return str;
  }
}
