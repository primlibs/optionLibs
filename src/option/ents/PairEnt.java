/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.objects.PairController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.prim.core.AbstractApplication;
import com.prim.support.MyString;
import com.prim.support.primXml;
import com.prim.core.service.ServiceFactory;
import com.prim.core.warehouse.WarehouseSingleton;
import com.prim.core.warehouse.controllerStructure.ControllerKeeper;
import com.prim.core.warehouse.controllerStructure.StructureController;
import com.prim.core.warehouse.pair.PairKeeper;
import com.prim.web.FormOptionInterface;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;
import com.prim.core.pair.Pair;
import com.prim.core.pair.PairObject;
import com.prim.core.pair.Sequence;

/**
 *
 * @author кот
 */
public class PairEnt extends OptionAbstract {

  // все пары, для вывода в форме
  private TreeMap<String, Object> allPairsToString;
  private List<Pair> allPairs = new ArrayList<Pair>();
  // все контроллеры и методы
  private TreeMap<String, Object> controllers;
  private TreeMap<String, Object> rendersMethods = new TreeMap<String, Object>();
  // активные пары, то есть те, которые нужно отобразить в развернутом виде
  private ArrayList<Pair> activePairs;
  private String formAction = "";
  private String str = "";
  private final String DOWNLOAD_FILE_SPECACTION = "downloadPairFiles";
  private final String UPLOAD_FILE_SPECACTION = "uploadPairFiles";
  private final String UPLOAD_MAIN_FILE_SPECACTION = "uploadMainPairFiles";
  private final String CHECK_SPECACTION = "checkPairs";
  private List<String> errors = new ArrayList();

  private PairEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "pairEnt";
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static PairEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new PairEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;
    activePairs = new ArrayList<Pair>();
    allPairsToString = new TreeMap<String, Object>();
    controllers = new TreeMap<String, Object>();
    ArrayList<String> errorList = new ArrayList<String>();

    rendersMethods.put("0", "--");
    rendersMethods.putAll(getRenders());

    controllers.put("0", "не выбрано");
    controllers.putAll(getControllers());

    // вернуть файл пар
    if (specAction.equals(DOWNLOAD_FILE_SPECACTION)) {
      getPairsFile();
      return true;
    }

    // загрузить файл пар
    if (specAction.equals(UPLOAD_FILE_SPECACTION)) {
      uploadPairsFile();
      redirectObject = object;
      redirectAction = action;
      isRedirect = true;
      return true;
    }

    if (specAction.equals(UPLOAD_MAIN_FILE_SPECACTION)) {
      uploadMainPairFile();
      if (errors.isEmpty()) {
        redirectObject = object;
        redirectAction = action;
        isRedirect = true;
        return true;
      }
    }

    //перегружаем пары на случай изменений
    WarehouseSingleton.getInstance().getNewKeeper(app);
    PairKeeper ps = app.getKeeper().getPairKeeper();
    Pair pair = ps.getPair();

    if (specAction.equals(CHECK_SPECACTION)) {
      List<String> incorrect = checkIncorrectPairs(ps);
      str += showCheckResult(incorrect);
      return true;
    }

    try {

      // если послана форма
      if (params.get("submit") != null) {
        // вызвать метод контроллера
        status = false;
        PairController cnt = new PairController(app);
        String methodName = params.get("method") != null ? params.get("method").toString() : "";
        str += (methodName);
        try {
          Method[] methods = cnt.getClass().getDeclaredMethods();
          for (Method method : methods) {
            if (method.getName().equals(methodName)) {
              status = (Boolean) method.invoke(cnt, params);
              break;
            }
          }
          str += (status);
          str += (cnt.getErrors());
        } catch (Exception e) {
          errorList.add("Ошибка при вызове метода контроллера");
          errorList.add(MyString.getStackExeption(e));
        }
        if (status == false) {
          errorList.addAll(cnt.getErrors());
        }
      }





      // получить список всех пар
      allPairsToString.put("0", "не выбрано");
      allPairs = ps.getAllPairs();
      if (ps.getPair() != null) {
        String n = ps.getPair().getObject() + ":" + ps.getPair().getAction();
        allPairsToString.put(n, n);
      }
      for (Pair p : ps.getAllPairs()) {
        String n = p.getObject() + ":" + p.getAction();
        allPairsToString.put(n, n);
      }

      // получить список всех контроллеров и методов
      //ControllerKeeper cs = ControllerKeeper.getInstance(SettingOptions.set("project"));


      String content = "";
      if (pair != null) {

        // определить список активных пар
        // то есть тех, которые нужно показать в развернутом виде
        String pairObject = params.get("objectName") != null ? params.get("objectName").toString() : "";
        String pairAction = params.get("actionName") != null ? params.get("actionName").toString() : "";
        if (pairObject != null && pairAction != null) {
          Pair active = ps.getPair().searchOne(pairObject, pairAction);
          if (active != null) {
            activePairs.add(active);
            activePairs.addAll(active.getAllParentСlone());
          }
        }

        if (!errorList.isEmpty()) {
          content += "<div class='errors'>" + errorList + "</div>";
        }

        if (params.get("pairObject") != null && !params.get("pairObject").toString().equals("")
                && params.get("pairAction") != null && !params.get("pairAction").toString().equals("")) {
          // если определены параметры, то показать одну пару

          List<Pair> all = ps.getPair().getAllPairsClone();
          for (Pair p : all) {
            if (p.getObject().equals(params.get("pairObject").toString())
                    && p.getAction().equals(params.get("pairAction").toString())) {
              pair = p;
              break;
            }
          }

          content += href(object, action, "", "Посмотреть все", new HashMap());
          content += detailRenderPair(pair);
        } else if (params.get("search") != null) {
          // если задан запрос на поиск
          String searchObject = params.get("searchObject") != null ? params.get("searchObject").toString().trim() : "";
          String searchAction = params.get("searchAction") != null ? params.get("searchAction").toString().trim() : "";
          List<Pair> newPairs = new ArrayList();
          List<Pair> all = ps.getPair().getAllPairsClone();
          if (!searchObject.isEmpty() || !searchAction.isEmpty()) {
            for (Pair p : all) {
              if (searchObject != null && p.getObject().equals(searchObject)) {
                newPairs.add(p);
              } else if (searchAction != null && p.getAction().equals(searchAction)) {
                newPairs.add(p);
              }
            }
          } else {
            newPairs = all;
          }
          TreeMap<String, Pair> pairsMap = new TreeMap<String, Pair>();
          for (Pair innerPair : newPairs) {
            pairsMap.put(innerPair.getObject() + ":" + innerPair.getAction(), innerPair);
          }
          for (Pair p : pairsMap.values()) {
            content += showLink(p);
          }
        } else {
          // иначе, показать всё
          content += renderPair(pair);
        }
      } else {
        content += "Главная пара не найдена";
      }

      String title = "Pairs";
      if (params.get("pairObject") != null && !params.get("pairObject").toString().equals("")
              && params.get("pairAction") != null && !params.get("pairAction").toString().equals("")) {
        title = "Pair " + params.get("pairObject") + ":" + params.get("pairAction");
      }

      str += ("</link><script type='text/javascript' src='./script.js'></script>");

      // форма поиска
      errorList.addAll(errors);
      str += errorList;
      str += searchForm();
      str += checkForm();
      str += uploadForm();
      str += "<br/>";
      str += uploadMainPairForm();
      str += "<br/>";

      str += (content);
      str += (ps.getErrors());
      str += (PairObject.message);



    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    }
    return status;
  }

  // методы получения данных --------------------------------------------------------------------------------------------------------
  /**
   * получить файл с парами
   *
   * @throws Exception
   */
  private void getPairsFile() throws Exception {

    WarehouseSingleton.getInstance().getNewKeeper(app);
    PairKeeper ps = app.getKeeper().getPairKeeper();

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();
    Element root = doc.createElement("root");
    doc.appendChild(root);

    String pairName = "";
    if (params.get("pairObject") != null && params.get("pairAction") != null) {

      String pairObject = params.get("pairObject").toString();
      String pairAction = params.get("pairAction").toString();
      pairName = pairObject + "_" + pairAction + "_";

      Pair pair = ps.searchOnePair(pairObject, pairAction);

      pair.getSelfInXml(doc, root);

    }
    fileContent = primXml.documentToString(doc).getBytes("UTF-8");
    fileName = pairName + "pairs.xml";
  }

  /**
   * получить список рендеров. Формат списка: ключ - объект : метод. Значение -
   * то же.
   *
   * @return
   * @throws Exception
   */
  private TreeMap<String, Object> getRenders() throws Exception {
    Collection<String> classes;
    classes = ServiceFactory.scan(app.getRenderPath());
    TreeMap<String, Object> servicesMap = new TreeMap<String, Object>();
    HashMap<String, ArrayList<String>> hs = new HashMap<String, ArrayList<String>>();
    for (String clName : classes) {
      Class cls = Class.forName("renders.entities." + clName);
      ArrayList<String> al = new ArrayList<String>();
      hs.put(clName, al);
      Method[] m = cls.getMethods();
      for (Method mm : m) {
        al.add(mm.getName());
      }
    }

    ArrayList<String> checkList = new ArrayList<String>();
    Class cls = Class.forName("renders.BaseRender");
    Method[] m = cls.getMethods();
    for (Method mm : m) {
      if (!mm.getName().equals("renderOneEntity")
              && !mm.getName().equals("renderAddEntityForm")
              && !mm.getName().equals("renderChangeEntityForm")
              && !mm.getName().equals("renderEntityList")
              && !mm.getName().equals("addFiles")
              && !mm.getName().equals("showAllFiles")) {
        checkList.add(mm.getName());
      }
    }

    for (String str : hs.keySet()) {
      for (String str2 : hs.get(str)) {
        if (!checkList.contains(str2)) {
          servicesMap.put(str + ":" + str2, str + ":" + str2);
        }
      }
    }
    return servicesMap;
  }

  /**
   * получить список контроллеров. Формат списка: ключ - объект : метод.
   * Значение - то же. Возвращаются не все контроллеры, а только те, которые
   * подходят к данной паре
   *
   * @return
   * @throws Exception
   */
  private TreeMap<String, Object> getControllers() throws Exception {
    TreeMap<String, Object> map = new TreeMap();
    ControllerKeeper cs = app.getKeeper().getControllerKeeper();

    for (String controllerName : cs.getControllers().keySet()) {
      StructureController clr = cs.getControllers().get(controllerName);
      if (params.get("pairObject") != null && controllerName.equals(params.get("pairObject").toString())) {
        for (String methodName : clr.getControllersMethods().keySet()) {
          String controllerMethod = controllerName + ":" + methodName;
          map.put(controllerMethod, controllerMethod);
        }
      }
    }
    return map;
  }

  /**
   * получить список всех контроллеров
   *
   * @return
   * @throws Exception
   */
  private TreeMap<String, Object> getAllControllers() throws Exception {
    TreeMap<String, Object> map = new TreeMap();
    ControllerKeeper cs = app.getKeeper().getControllerKeeper();

    for (String controllerName : cs.getControllers().keySet()) {
      StructureController clr = cs.getControllers().get(controllerName);
      for (String methodName : clr.getControllersMethods().keySet()) {
        String controllerMethod = controllerName + ":" + methodName;
        map.put(controllerMethod, controllerMethod);
      }
    }
    return map;
  }

  /**
   * загрузить файл с парами
   *
   * @throws Exception
   */
  private void uploadPairsFile() throws Exception {
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

          Pair newPair = PairObject.getPairFromXml(root);
          String pairObject = newPair.getObject();
          String pairAction = newPair.getAction();
          PairKeeper pk = app.getKeeper().getPairKeeper();

          // если пары с таким именем нет в списке
          if (!pk.containsPair(pairObject, pairAction)) {
            // добавить
            checkChildrenPairs(pk, newPair);
            pk.getPair().addPair(newPair);
          } else {
            // если пара с таким именем уже есть
            // если поставлена галочка заменять
            if (params.get("replace") != null) {
              // то обновить
              pk.removePair(pairObject, pairAction);
              checkChildrenPairs(pk, newPair);
              pk.getPair().addPair(newPair);
            }
          }
          pk.SaveCollectionInFile();
          refreshWarehouseSingleton();
        }
      }
    }
  }

  /**
   * загрузить файл с главной парой
   *
   * @throws Exception
   */
  private void uploadMainPairFile() throws Exception {
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

          Pair newPair = PairObject.getPairFromXml(root);
          String pairObject = newPair.getObject();
          String pairAction = newPair.getAction();
          PairKeeper pk = app.getKeeper().getPairKeeper();

          if (pairObject.equals("app") && pairAction.equals("show")) {
            pk.setPair(newPair);
            str += "11111";
            pk.SaveCollectionInFile();
            refreshWarehouseSingleton();
          } else {
            errors.add("Ошибка: загружена не та пара! Надо загрузить пру с именем app:show");
          }
        }
      } else {
        errors.add("файл не передан");
      }
    } else {
      errors.add("файл не передан");
    }
  }

  /**
   * проверка дочерних пар новой пары, есть ли такая пара в singleton
   *
   * @param pk
   * @param newPair
   */
  private void checkChildrenPairs(PairKeeper pk, Pair newPair) {
    List<Pair> pairs = newPair.getAllPairsClone();
    for (Pair p : pairs) {
      checkPair(pk, newPair, p.getObject(), p.getAction());
    }
  }

  /**
   * проверить, есть ли уже пара с таким action и object
   */
  private void checkPair(PairKeeper pk, Pair newPair, String pairObject, String pairAction) {
    // если есть такая пара
    if (pk.containsPair(pairObject, pairAction)) {
      // если поставлена галочка заменить
      if (params.get("replace") != null) {
        // то удалить пару из singleton
        pk.removePair(pairObject, pairAction);
      } else {
        // иначе
        // удалить пару из новой пары
        newPair.removePair(pairObject, pairAction);
      }
    }
  }

  /**
   * проверка пар на наличие пар с некорректными значениями
   */
  private List<String> checkIncorrectPairs(PairKeeper pk) throws Exception {
    List<String> incorrect = new ArrayList();
    // получить список рендеров 
    // получить список контроллеров
    // получить список всех пар
    List<Pair> pairList = pk.getAllPairs();
    TreeMap<String, Pair> pairMap = new TreeMap();
    for (Pair p : pairList) {
      pairMap.put(p.getObject() + ":" + p.getAction(), p);
    }
    // каждую пару
    for (Pair p : pairMap.values()) {
      // проверить
      // если не соответствует
      // добавить в список
      String pairName = p.getObject() + ":" + p.getAction();
      Map<String, Sequence> seqMap = p.getSequenceClone();
      for (String seqName : seqMap.keySet()) {
        Sequence seq = seqMap.get(seqName);
        String methodName = seq.getAppMethodName();
        String objectName = seq.getAppObjectName();
        String controllerName = "";
        if (!objectName.isEmpty() && !methodName.isEmpty()) {
          controllerName = objectName + ":" + methodName;
        }
        String trueRender = seq.getTrueRender();
        String falseRender = seq.getFalseRender();
        TreeMap<String, Object> controllerMap = getAllControllers();
        if (!controllerName.isEmpty() && !controllerMap.containsKey(controllerName)) {
          incorrect.add("Пара: " + pairName + ", Sequence: " + seqName + ", контроллер: " + controllerName);
        }
        if (!trueRender.isEmpty() && !rendersMethods.containsKey(trueRender)) {
          incorrect.add("Пара: " + pairName + ", Sequence: " + seqName + ", trueRender: " + trueRender);
        }
        if (!falseRender.isEmpty() && !rendersMethods.containsKey(falseRender)) {
          incorrect.add("Пара: " + pairName + ", Sequence: " + seqName + ", falseRender: " + falseRender);
        }
      }
    }
    return incorrect;
  }

  // методы отображения ------------------------------------------------------------------------------------------------------------
  /**
   * форма для загрузки файла
   *
   * @param pairAction
   * @param pairObject
   * @return
   * @throws Exception
   */
  private String fileForm(String pairAction, String pairObject) throws Exception {
    WarehouseSingleton.getInstance().getNewKeeper(app);
    PairKeeper ps = app.getKeeper().getPairKeeper();
    List<Pair> allPairs = ps.getPair().getAllPairsClone();
    Map<String, Object> pairNames = new TreeMap();
    for (Pair pair : allPairs) {
      String name = pair.getObject() + ":" + pair.getAction();
      pairNames.put(name, name);
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.hiddenInput("pairAction", pairAction), "");
    inner.put(rd.hiddenInput("pairObject", pairObject), "");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", DOWNLOAD_FILE_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Скачать файл", null);
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
    AbsEnt form = rd.horizontalForm(inner, "Проверить пары", null);
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
    String str = "Список пар, в которых используются контроллеры или рендеры, отсутствующие в системе. <br/><br/>";
    if (incorrect.isEmpty()) {
      str += "Несоответствий не найдено";
    }
    for (String s : incorrect) {
      str += s + "<br/>";
    }
    return str;
  }

  /**
   * ссылка на пару
   *
   * @param pair
   * @return
   * @throws Exception
   */
  private String showLink(Pair pair) throws Exception {
    Map<String, Object> linkParams = new HashMap();
    linkParams.put("pairAction", pair.getAction());
    linkParams.put("pairObject", pair.getObject());
    return href(object, action, "", pair.getObject() + ":" + pair.getAction(), linkParams) + "</br>";
  }

  /**
   * форма поиска
   *
   * @return
   * @throws Exception
   */
  private String searchForm() throws Exception {
    Map<AbsEnt, String> hs = new LinkedHashMap();
    hs.put(rd.textInput("searchObject", params.get("searchObject"), "Object"), "");
    hs.put(rd.textInput("searchAction", params.get("searchAction"), "Action"), "");
    hs.put(rd.hiddenInput("search", "1"), "");
    hs.put(rd.hiddenInput("action", action), "");
    hs.put(rd.hiddenInput("object", object), "");
    AbsEnt form = rd.horizontalForm(hs, "Поиск", "images/ok.png");
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * краткий вывод пары
   *
   * @param pair
   * @return
   */
  private String renderPair(Pair pair) throws Exception {

    String pairAction = pair.getAction();
    String pairObject = pair.getObject();
    String fullName = pairAction + pairObject;
    String str = "";
    str += "<div class='pair'>";

    // заголовок пары
    str += "<div class='pair_head'>";
    str += "<table><tr>";
    str += "<td>Object: <b>" + pairObject + "</b> Action: <b>" + pairAction + "</b></td>";
    str += "<td>" + removePairForm(pair) + "</td>";
    str += "<td>" + fileForm(pairAction, pairObject) + "</td>";
    str += "<td><font class='display_link' onclick=\"hide('pair_show" + fullName + "');\">[Отображение]</font></td>";
    if (pair.getDef()) {
      str += " Default ";
    }
    str += "</tr></table>";
    str += "</div>";


    String display = "";
    if (activePairs.contains(pair)) {
      display = "";
    } else {
      display = "style='display:none;'";
    }

    // основной вывод пары
    str += "<div class='pair_show' id='pair_show" + fullName + "' " + display + "'>";

    str += "<div class='inner_pairs'>";

    Map<String, Object> linkParams = new HashMap();
    linkParams.put("pairObject", pair.getObject());
    linkParams.put("pairAction", pair.getAction());
    str += href(object, action, "", "Показать подробно", linkParams);
    // вывод вложенных пар

    TreeMap<String, Pair> pairsMap = new TreeMap<String, Pair>();
    for (Pair innerPair : pair.getPairsClone()) {
      pairsMap.put(innerPair.getObject() + ":" + innerPair.getAction(), innerPair);
    }

    for (Pair innerPair : pairsMap.values()) {
      str += renderPair(innerPair);
    }
    str += "</div>";

    str += "</div>";

    str += "</div>";
    return str;
  }

  /**
   * детальный вывод пары
   *
   * @param pair
   * @return
   */
  private String detailRenderPair(Pair pair) throws Exception {

    String action = pair.getAction();
    String object = pair.getObject();
    String fullName = action + object;
    String str = "";
    str += "<div class='pair'>";

    // заголовок пары
    str += "<div class='pair_head'>";
    str += "<table><tr>";
    str += "<td>Object: <b>" + object + "</b> Action: <b>" + action + "</b></td>";
    str += "<td>" + removePairForm(pair) + "</td>";
    str += "<td><font class='display_link' onclick=\"hide('pair_show" + fullName + "');\">[Отображение]</font></td>";
    str += "</tr></table>";
    str += "</div>";

    // определяем, показывать ли пару в развернутом виде
    String display = "";
    if (activePairs.contains(pair)) {
      display = "";
    } else {
      display = "style='display:none;'";
    }

    // определяем, показывать ли sequence в развернутом виде
    String displaySeq = "";
    if (params.get("objectName") != null && params.get("actionName") != null && params.get("objectName").toString().equals(object)
            && params.get("actionName").toString().equals(action) && params.get("seq") != null) {
      displaySeq = "";
      display = "";
    } else {
      displaySeq = "style='display:none;'";
    }

    // основной вывод пары
    str += "<div class='pair_show' id='pair_show" + fullName + "' " + display + "'>";

    // START SEQUENCE
    str += "<div class='seq'>";

    // заголовок sequence
    str += "<div class='seq_head'>";
    str += "<font class='display_link' onclick=\"hide('seq_show" + fullName + "');\">[Показать SEQUENCE]</font>";
    str += "</div>";

    // основная часть sequence
    str += "<div class='seq_show' id='seq_show" + fullName + "' " + displaySeq + " >";

    // форма добавления новой sequence
    str += "<div class='seq_add_form'>";
    str += addSeqForm(object, action);
    str += "</div>";

    // все sequence
    str += "<div class='seq_all'>";

    // сначала показать Sequence по умолчанию
    Sequence defSeq = pair.getSequenceClone().get("default");
    if (defSeq != null) {
      str += "<div class='seq_one'>";
      str += changeSeqForm(pair, defSeq, "default");
      str += "</div>";
    }

    TreeMap<String, Sequence> seqMap = new TreeMap(pair.getSequenceClone());
    for (String seqName : seqMap.keySet()) {
      if (!seqName.equals("default")) {
        Sequence seq = pair.getSequenceClone().get(seqName);
        str += "<div class='seq_one'>";
        str += changeSeqForm(pair, seq, seqName);
        str += removeSeqForm(pair, seqName);
        str += "</div>";
      }
    }
    str += "</div>";
    str += "</div>";

    str += "</div>";
    // END SEQUENCE  

    str += "<div class='inner_pairs'>";

    // добавить вложенную пару
    str += "<div class='add_pair'>";
    str += "<p><font class='display_link' onclick=\"hide('add_pair_form" + fullName + "');\">[Добавить вложенную пару]</font></p>";
    str += "<div id='add_pair_form" + fullName + "' style='display:none;float:left;width:100%;' >";
    str += addPairForm(object, action);
    str += movePairForm(pair);
    str += "</div>";
    str += "</div>";

    str += changePairForm(pair);

    // вывод вложенных пар
    TreeMap<String, Pair> pairsMap = new TreeMap<String, Pair>();
    for (Pair innerPair : pair.getPairsClone()) {
      pairsMap.put(innerPair.getObject() + ":" + innerPair.getAction(), innerPair);
    }

    for (Pair innerPair : pairsMap.values()) {
      str += renderPair(innerPair);
    }
    str += "</div>";

    str += "</div>";

    str += "</div>";

    return str;
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
    AbsEnt form = rd.horizontalForm(inner, "Загрузить файл пары", null, true, null);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  private String uploadMainPairForm() throws Exception {
    FormOptionInterface fo = rd.getFormOption();
    fo.setAction(action);
    fo.setObject(object);
    fo.setSpecAction(UPLOAD_MAIN_FILE_SPECACTION);
    fo.setNoValidateRights();
    fo.setFormToUploadFiles(true);
    fo.setHorisontal(true);
    fo.setTitle("Загрузить главную пару");
    fo.setJsHandler("onsubmit=\"return confirm('Вы действительно хотите заменить главную пару?');\"");

    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.fileInput("file", null, "Выберите файл"), "");

    AbsEnt form = rd.rightForm(inner, fo);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * форма добавления пары
   *
   * @param object
   * @param action
   * @return
   * @throws Exception
   */
  private String addPairForm(String object, String action) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("newObject", "", "Object"), "Object");
    hs.put(rd.textInput("newAction", "", "Action"), "Action");
    hs.put(rd.checkBox("def", false, null), "Default");
    hs.put(rd.combo(controllers, "", "objectMethod"), "Метод контроллера");
    hs.put(rd.combo(rendersMethods, "", "trueRender"), "TRUE Render");
    hs.put(rd.combo(rendersMethods, "", "falseRender"), "FALSE Render");
    hs.put(rd.combo(allPairsToString, "", "trueRedirect"), "TRUE Redirect");
    hs.put(rd.combo(allPairsToString, "", "falseRedirect"), "FALSE Redirect");
    AbsEnt form = rd.verticalForm(hs, "Добавить пару", "images/add.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "addPair"));
    form.addEnt(rd.hiddenInput("objectName", object));
    form.addEnt(rd.hiddenInput("actionName", action));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();
  }

  /**
   * форма изменения пары
   *
   * @param pair
   * @return
   * @throws Exception
   */
  private String changePairForm(Pair pair) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.checkBox("def", Boolean.parseBoolean(MyString.getString(pair.getDef())), null), "Default");
    AbsEnt form = rd.verticalForm(hs, "Изменить", "images/change.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "changePair"));
    form.addEnt(rd.hiddenInput("objectName", pair.getObject()));
    form.addEnt(rd.hiddenInput("actionName", pair.getAction()));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();

  }

  /**
   * форма перемещения пары
   *
   * @param pair
   * @return
   * @throws Exception
   */
  private String movePairForm(Pair pair) throws Exception {
    List<Pair> pairs = allPairs;
    pairs.removeAll(pair.getAllParentСlone());
    pairs.remove(pair);
    TreeMap<String, Object> pairsMap = new TreeMap<String, Object>();
    for (Pair p : pairs) {
      if (!pair.containsPair(p.getObject(), p.getAction())) {
        pairsMap.put(p.getObject() + ":" + p.getAction(), p.getObject() + ":" + p.getAction());
      }
    }

    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(pairsMap, "", "move"), "Пары");
    AbsEnt form = rd.verticalForm(hs, "Переместить пару", "images/ok.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "movePair"));
    form.addEnt(rd.hiddenInput("objectName", pair.getObject()));
    form.addEnt(rd.hiddenInput("actionName", pair.getAction()));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();
  }

  /**
   * форма удаления пары
   *
   * @param pair
   * @return
   * @throws Exception
   */
  private String removePairForm(Pair pair) throws Exception {
    Pair parent = pair.getParent();
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    AbsEnt form = rd.verticalForm(hs, "Удалить", "images/delete.png");
    form.setJs("onclick=\"return confirmDelete();\"");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "removePair"));
    if (parent != null) {
      form.addEnt(rd.hiddenInput("objectName", parent.getObject()));
      form.addEnt(rd.hiddenInput("actionName", parent.getAction()));
    }
    form.addEnt(rd.hiddenInput("removeObject", pair.getObject()));
    form.addEnt(rd.hiddenInput("removeAction", pair.getAction()));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();

  }

  /**
   * форма добавления sequence
   *
   * @param object
   * @param action
   * @return
   * @throws Exception
   */
  private String addSeqForm(String object, String action) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("seqName", "", "Название Sequence"), "Название Sequence");
    hs.put(rd.combo(controllers, "", "objectMethod"), "Метод контроллера");
    hs.put(rd.combo(rendersMethods, "", "trueRender"), "TRUE Render");
    hs.put(rd.combo(rendersMethods, "", "falseRender"), "FALSE Render");
    hs.put(rd.combo(allPairsToString, "", "trueRedirect"), "TRUE Redirect");
    hs.put(rd.combo(allPairsToString, "", "falseRedirect"), "FALSE Redirect");
    hs.put(rd.textInput("trueParams", "", "TRUE Redirect params"), "TRUE Redirect params");
    hs.put(rd.textInput("falseParams", "", "FALSE Redirect params"), "FALSE Redirect params");
    AbsEnt form = rd.verticalForm(hs, "+ Sequence", "images/add.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "addSeq"));
    form.addEnt(rd.hiddenInput("seq", 1));
    form.addEnt(rd.hiddenInput("objectName", object));
    form.addEnt(rd.hiddenInput("actionName", action));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();
  }

  /**
   * форма изменения sequence
   *
   * @param pair
   * @param seq
   * @param seqName
   * @return
   * @throws Exception
   */
  private String changeSeqForm(Pair pair, Sequence seq, String seqName) throws Exception {
    String str = "";
    str = str + "Название Sequence: " + seqName;

    String objectMethod = null;
    if (seq.getAppObjectName() != null && seq.getAppMethodName() != null) {
      objectMethod = seq.getAppObjectName() + ":" + seq.getAppMethodName();
    }

    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(controllers, objectMethod, "objectMethod"), "Метод контроллера");
    hs.put(rd.combo(rendersMethods, seq.getTrueRender(), "trueRender"), "TRUE Render");
    hs.put(rd.combo(rendersMethods, seq.getFalseRender(), "falseRender"), "FALSE Render");
    hs.put(rd.combo(allPairsToString, seq.getTrueRedirect(), "trueRedirect"), "TRUE Redirect");
    hs.put(rd.combo(allPairsToString, seq.getFalseRedirect(), "falseRedirect"), "FALSE Redirect");
    hs.put(rd.textInput("trueParams", seq.getTrueRedirectParams(), "TRUE Redirect params"), "TRUE Redirect params");
    hs.put(rd.textInput("falseParams", seq.getFalseRedirectParams(), "FALSE Redirect params"), "FALSE Redirect params");
    AbsEnt form = rd.verticalForm(hs, "Изменить", "images/change.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("seqName", seqName));
    form.addEnt(rd.hiddenInput("method", "changeSeq"));
    form.addEnt(rd.hiddenInput("seq", 1));
    form.addEnt(rd.hiddenInput("objectName", pair.getObject()));
    form.addEnt(rd.hiddenInput("actionName", pair.getAction()));
    form.setAttribute(EnumAttrType.action, formAction);
    str += form.render();
    return str;
  }

  /**
   * форма удаления sequence
   *
   * @param pair
   * @param seqName
   * @return
   * @throws Exception
   */
  private String removeSeqForm(Pair pair, String seqName) throws Exception {
    Pair parent = pair.getParent();
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    AbsEnt form = rd.verticalForm(hs, "Удалить", "images/delete.png");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("method", "removeSeq"));
    form.addEnt(rd.hiddenInput("seq", "1"));
    form.addEnt(rd.hiddenInput("seqName", seqName));
    form.addEnt(rd.hiddenInput("objectName", pair.getObject()));
    form.addEnt(rd.hiddenInput("actionName", pair.getAction()));
    form.setAttribute(EnumAttrType.action, formAction);
    return form.render();
  }
}
