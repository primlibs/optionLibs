/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import option.objects.PairController;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.service.ServiceFactory;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.StructureController;
import warehouse.pair.PairKeeper;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.pair.Pair;
import web.pair.PairObject;
import web.pair.Sequence;

/**
 *
 * @author кот
 */
public class PairEnt extends OptionAbstract {

  // все рендеры, для вывода в форме
  private TreeMap<String, Object> renders;
  // все пары, для вывода в форме
  private TreeMap<String, Object> allPairsToString;
  private List<Pair> allPairs = new ArrayList<Pair>();
  // все контроллеры и методы
  private TreeMap<String, Object> controllers;
  public static PrintWriter out2;
  private TreeMap<String, Object> rendersMethods = new TreeMap<String, Object>();
  // активные пары, то есть те, которые нужно отобразить в развернутом виде
  private ArrayList<Pair> activePairs;
  private String formAction = "";
  private String str = "";

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
    renders = new TreeMap<String, Object>();
    controllers = new TreeMap<String, Object>();
    ArrayList<String> errors = new ArrayList<String>();


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
          errors.add("Ошибка при вызове метода контроллера");
          errors.add(MyString.getStackExeption(e));
        }
        if (status == false) {
          errors.addAll(cnt.getErrors());
        }
      }

      rendersMethods.put("0", "--");
      rendersMethods.putAll(getRenders());

      //перегружаем пары на случай изменений
      WarehouseSingleton.getInstance().getNewKeeper(app);
      PairKeeper ps = app.getKeeper().getPairKeeper();
      Pair pair = ps.getPair();

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
      ControllerKeeper cs = app.getKeeper().getControllerKeeper();
      controllers.put("0", "не выбрано");

      for (String controllerName : cs.getControllers().keySet()) {
        StructureController clr = cs.getControllers().get(controllerName);
        if (params.get("pairObject") != null && controllerName.equals(params.get("pairObject").toString())) {
          for (String methodName : clr.getControllersMethods().keySet()) {
            String controllerMethod = controllerName + ":" + methodName;
            controllers.put(controllerMethod, controllerMethod);
          }
        }
      }

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

        if (!errors.isEmpty()) {
          content += "<div class='errors'>" + errors + "</div>";
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
      str += searchForm();

      str += (content);
      str += (ps.getErrors());
      str += (PairObject.message);
    } catch (Exception e) {
      MyString.getStackExeption(e);
    }
    return status;
  }

  private String showLink(Pair pair) throws Exception {
    Map<String, Object> linkParams = new HashMap();
    linkParams.put("pairAction", pair.getAction());
    linkParams.put("pairObject", pair.getObject());
    return href(object, action, "", pair.getObject() + ":" + pair.getAction(), linkParams) + "</br>";
  }

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


    String display = "";
    if (activePairs.contains(pair)) {
      display = "";
    } else {
      display = "style='display:none;'";
    }

    // основной вывод пары
    str += "<div class='pair_show' id='pair_show" + fullName + "' " + display + "'>";

    // START SEQUENCE
    str += "<div class='seq'>";

    // заголовок sequence
    str += "<div class='seq_head'>";
    str += "<font class='display_link' onclick=\"hide('seq_show" + fullName + "');\">[Показать SEQUENCE]</font>";
    str += "</div>";

    String displaySeq = "";

    if (params.get("objectName") != null && params.get("actionName") != null && params.get("objectName").toString().equals(object)
            && params.get("actionName").toString().equals(action) && params.get("seq") != null) {
      displaySeq = "";
    } else {
      displaySeq = "style='display:none;'";
    }

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

    for (String seqName : pair.getSequenceClone().keySet()) {
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
}
