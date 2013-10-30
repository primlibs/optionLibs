/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.StructureController;
import web.Render;
import web.fabric.AbsEnt;

/**
 *
 * @author кот
 */
public class DownloadEnt extends OptionAbstract {

  private String content = "";
  private final String CONTROLLER_SPECACTION = "getControllers";

  protected DownloadEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "modelEnt";
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

  // контроллер ------------------------------------------------------------------------------------------
  @Override
  public Boolean run() throws Exception {
    if (specAction.equals(CONTROLLER_SPECACTION)) {
      getControllersFile();
      return true;
    }
    content += show();
    return true;
  }

  // рендер ----------------------------------------------------------------------------------------------
  private String show() {
    String str = "";
    try {
      // форма выбора контроллеров
      str += controllersForm();
      // форма выбора пар
      // форма выбора моделей
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
    return form.render();
  }

  // модель -----------------------------------------------------------------------------------------------------------
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
    Document doc=db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);
    
    // найти структуры по переданным именам
    // составить из них файл xml
    // каждая структура вовращает себя в xml
    String[] controllersNames = getArray("controllers");
    for (String name: controllersNames) {
      if (controllers.containsKey(name)) {
        Element controllerElement = primXml.createEmptyElement(doc, root, StructureController.ELEMENT_NAME);
        StructureController sc = controllers.get(name);
        sc.getSelfInXml(doc, controllerElement);
      }
    }

    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = "controllers.xml";
  }
  
  protected final String[] getArray(String paramName) {
    if (params.get(paramName) != null) {
      try {
        String[] array = (String[]) params.get(paramName);
        return array;
      } catch (Exception e) {
        String param = params.get(paramName).toString();
        String[] array = {param};
        return array;
      }
    }
    String[] array = new String[0];
    return array;
  }
  
  // загрузить контроллеры
  // разобрать файл xml
  // для каждого элемента создать структуру контроллера
  // добавить её в Keeper
  // сохранить контроллер с этим именем
  
}
