/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.Creator;
import option.ents.modelEnts.AddStructure;
import option.ents.modelEnts.AddValidator;
import option.ents.modelEnts.AllStructure;
import option.ents.modelEnts.ChangeStructure;
import option.ents.modelEnts.ChangeValidator;
import option.ents.modelEnts.OneStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import prim.modelStructure.Structure;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.StructureController;
import warehouse.modelKeeper.ModelStructureKeeper;
import warehouse.pair.PairKeeper;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.pair.Pair;

/**
 *
 * @author кот
 */
public class DownloadEnt extends OptionAbstract {

  private String content = "";
  private final String CONTROLLER_SPECACTION = "getControllers";
  private final String MODEL_SPECACTION = "getModels";
  private final String PAIR_SPECACTION = "getPairs";

  protected DownloadEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = Creator.DOWNLOAD_OBJECT_NAME;
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static DownloadEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new DownloadEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return content;
  }

  // главный метод  ------------------------------------------------------------------------------------------
  
  /**
   * 
   * @return
   * @throws Exception 
   */
  @Override
  public Boolean run() throws Exception {
    if (specAction.equals(CONTROLLER_SPECACTION)) {
      getControllersFile();
      return true;
    } else if (specAction.equals(MODEL_SPECACTION)) {
      getModelsFile();
      return true;
    }else if (specAction.equals(PAIR_SPECACTION)) {
      getPairsFile();
      return true;
    }
    content += show();
    return true;
  }

  // матоды отображения ----------------------------------------------------------------------------------------------------------
  
  /**
   * 
   * @return 
   */
  private String show() {
    String str = "";
    try {
      // форма выбора контроллеров
      str += controllersForm();
      // форма выбора пар
      str += pairForm();
      // форма выбора моделей
      str += modelsForm();
    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    }
    return str;
  }

  private String controllersForm() throws Exception {
    ControllerKeeper ck = app.getKeeper().getControllerKeeper();
    ck.setDataFromBase();
    Map<String, StructureController> controllers = ck.getControllers();
    Map<String, Object> controllersNames = new TreeMap();
    for (String name : controllers.keySet()) {
      controllersNames.put(name, name);
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.multipleCombo(controllersNames, null, "controllers", 10), "Контроллеры");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", CONTROLLER_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Получить файл контроллеров", "images/ok.png");
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  private String modelsForm() throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    Map<String, Structure> structureMap = mss.getStructureMap();
    Map<String, Object> modelNames = new TreeMap();
    for (String name : structureMap.keySet()) {
      modelNames.put(name, name);
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.multipleCombo(modelNames, null, "models", 10), "Модели");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", MODEL_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Получить файл моделей", "images/ok.png");
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  private String pairForm() throws Exception {
    WarehouseSingleton.getInstance().getNewKeeper(app);
    PairKeeper ps = app.getKeeper().getPairKeeper();
    List<Pair> allPairs = ps.getPair().getAllPairsClone();
    Map<String, Object> pairNames = new TreeMap();
    for (Pair pair : allPairs) {
      String name = pair.getObject() + ":" + pair.getAction();
      pairNames.put(name, name);
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.combo(pairNames, null, "pairName"), "Пары");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", PAIR_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Получить файл пар", "images/file.png");
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  // методы получения данных -----------------------------------------------------------------------------------------------------------
  
  /**
   * 
   */
  private void execute() {
    // получить файл с контроллерами
    // получить файл с парами
    // получить файл с моделями
  }

  private void getControllersFile() throws Exception {
    ControllerKeeper ck = app.getKeeper().getControllerKeeper();
    ck.setDataFromBase();
    Map<String, StructureController> controllers = ck.getControllers();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);

    // найти структуры по переданным именам
    // составить из них файл xml
    // каждая структура вовращает себя в xml
    String[] controllersNames = getArray("controllers");
    for (String name : controllersNames) {
      if (controllers.containsKey(name)) {
        Element controllerElement = primXml.createEmptyElement(doc, root, StructureController.ELEMENT_NAME);
        StructureController sc = controllers.get(name);
        sc.getSelfInXml(doc, controllerElement);
      }
    }

    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = "controllers.xml";
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
        modelStructure.getSelfInXml(doc, modelElement);
      }
    }

    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = "models.xml";
  }

  private void getPairsFile() throws Exception {

    WarehouseSingleton.getInstance().getNewKeeper(app);
    PairKeeper ps = app.getKeeper().getPairKeeper();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);

    Object name = params.get("pairName");
    if (name != null) {

      String[] str = name.toString().split(":");
      if (str.length == 2) {
        String object = str[0];
        String action = str[1];

        Pair pair = ps.searchOnePair(object, action);

        pair.getSelfInXml(doc, root);
      }
    }
    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = "pairs.xml";
  }

 
  // загрузить контроллеры
  // разобрать файл xml
  // для каждого элемента создать структуру контроллера
  // добавить её в Keeper
  // сохранить контроллер с этим именем
}
