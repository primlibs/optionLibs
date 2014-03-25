/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.File;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.prim.core.AbstractApplication;
import com.prim.support.MyString;
import com.prim.support.primXml;
import com.prim.core.modelStructure.Structure;
import com.prim.core.modelStructure.StructureFabric;
import com.prim.core.service.ServiceFactory;
import com.prim.support.enums.DataTypes;
import com.prim.core.warehouse.OptionsKeeper;
import com.prim.core.warehouse.controllerStructure.ControllerKeeper;
import com.prim.core.warehouse.controllerStructure.ControllerMethod;
import com.prim.core.warehouse.controllerStructure.ControllerOrigin;
import com.prim.core.warehouse.controllerStructure.ControllerService;
import com.prim.core.warehouse.controllerStructure.ServiceParameter;
import com.prim.core.warehouse.controllerStructure.StructureController;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;

/**
 *
 * @author кот
 */
public class ControllerEnt extends OptionAbstract {

  private List<String> errors = new ArrayList();
  private LinkedHashMap<String, Object> servicesMap;
  private final String DOWNLOAD_FILE_SPECACTION = "downloadControllerFiles";
  private final String UPLOAD_FILE_SPECACTION = "uploadControllerFiles";
  private final String CHECK_SPECACTION = "checkControllers";
  private String str = "";

  private ControllerEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "controllerEnt";
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static ControllerEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new ControllerEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;
    try {

      ControllerKeeper ck = app.getKeeper().getControllerKeeper();
      ck.setDataFromBase();


      // вернуть файл контроллеров
      if (specAction.equals(DOWNLOAD_FILE_SPECACTION)) {
        getControllersFile();
        return true;
      }

      // загрузить файл контроллеров
      if (specAction.equals(UPLOAD_FILE_SPECACTION)) {
        uploadControllersFile();
        redirectObject = object;
        redirectAction = action;
        isRedirect = true;
        return true;
      }

      servicesMap = getServiceMap();

      if (specAction.equals(CHECK_SPECACTION)) {
        List<String> incorrect = checkControllers(ck);
        str += showCheckResult(incorrect);
        return true;
      }

      /**
       * Добавление нового контроллера
       */
      ArrayList<String> reqAr = new ArrayList<String>();

      reqAr.add("addCntrl");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("alias");
      reqAr.add("descr");
      reqAr.add("metAlias");
      reqAr.add("metDescr");
      if (checkParam(reqAr) == true) {
        ck.addMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        StructureController cr = ck.getController(params.get("cntrlName").toString().trim());
        if (cr != null) {
          cr.setAlias(params.get("alias").toString().trim());
          cr.setDescription(params.get("descr").toString().trim());
          ControllerMethod cm = cr.getMethod(params.get("methName").toString().trim());
          if (cm != null) {
            cm.setAlias(params.get("metAlias").toString().trim());
            cm.setDescription(params.get("metDescr").toString().trim());
          }
          ck.saveController(params.get("cntrlName").toString().trim());
        }
        ck.setDataFromBase();
      }

      /**
       * добавление нового метода
       */
      reqAr = new ArrayList<String>();
      reqAr.add("addMeth");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("metAlias");
      reqAr.add("metDescr");
      if (checkParam(reqAr) == true) {
        ck.addMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        StructureController cr = ck.getController(params.get("cntrlName").toString().trim());
        if (cr != null) {
          ControllerMethod cm = cr.getMethod(params.get("methName").toString().trim());
          if (cm != null) {
            cm.setAlias(params.get("metAlias").toString().trim());
            cm.setDescription(params.get("metDescr").toString().trim());
          }
          Boolean res = ck.saveController(params.get("cntrlName").toString().trim());
        }
        ck.setDataFromBase();
      }

      /**
       * изменение метода
       */
      reqAr = new ArrayList<String>();
      reqAr.add("chngMethod");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("newMethName");
      reqAr.add("metAlias");
      reqAr.add("metDescr");
      if (checkParam(reqAr) == true) {
        String contrName = params.get("cntrlName").toString().trim();
        String oldmName = params.get("methName").toString().trim();
        String newmName = params.get("newMethName").toString().trim();
        String alim = params.get("metAlias").toString().trim();
        String desm = params.get("metDescr").toString().trim();
        String hid = null;
        if (params.get("hidden") != null) {
          hid = params.get("hidden").toString();
        }
        Boolean hidden = true;
        if (hid == null || hid.equals("")) {
          hidden = false;
        }
        boolean free = false;
        if (params.get("free") != null) {
          free = true;
        }
        StructureController crn = ck.getController(contrName);
        if (crn != null) {
          ControllerMethod cm = crn.getMethod(oldmName);
          if (cm != null) {
            if (oldmName.equals(newmName)) {
              cm.setAlias(alim);
              cm.setDescription(desm);
              cm.setHidden(hidden);
              cm.setFree(free);
            } else if (crn.getMethod(newmName) == null) {
              crn.deleteMethod(oldmName);
              crn.setMethod(newmName, cm);
              cm.setAlias(alim);
              cm.setDescription(desm);
              cm.setHidden(hidden);
              cm.setFree(free);
            }
            ck.saveController(contrName);
          }
          ck.setDataFromBase();
        }
      }

      /**
       * изменение контроллера
       */
      reqAr = new ArrayList<String>();
      reqAr.add("chngCntrl");
      reqAr.add("cntrlName");
      reqAr.add("newCntrlName");
      reqAr.add("alias");
      reqAr.add("description");
      if (checkParam(reqAr) == true) {
        String oldName = params.get("cntrlName").toString().trim();
        String newName = params.get("newCntrlName").toString().trim();
        String ali = params.get("alias").toString().trim();
        String des = params.get("description").toString().trim();
        StructureController cr = ck.getController(oldName);
        if (cr != null) {
          status = false;
          if (oldName.equals(newName)) {
            cr.setAlias(ali);
            cr.setDescription(des);
            status = ck.saveController(oldName);
          } else {
            ck.deleteController(oldName);
            Map<String, StructureController> csHs = ck.getControllers();
            csHs.put(newName, cr);
            cr.setAlias(ali);
            cr.setDescription(des);
            status = ck.saveController(newName);
          }
          str += (status);
        }
        ck.setDataFromBase();
      }


      /**
       * удаление контроллера
       */
      reqAr = new ArrayList<String>();
      reqAr.add("deleteCntrl");
      reqAr.add("cName");
      if (checkParam(reqAr) == true) {
        ck.deleteController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }


      /**
       * удаление метода
       */
      reqAr = new ArrayList<String>();
      reqAr.add("deleteMeth");
      reqAr.add("cName");
      reqAr.add("cAction");


      if (checkParam(reqAr) == true) {
        Boolean res = true;
        res = ck.deleteMethod(params.get("cName").toString(), params.get("cAction").toString().trim());

        res = ck.saveController(params.get("cName").toString().trim());

        ck.setDataFromBase();
      }


      /*
       * Добавить сервис
       */
      reqAr = new ArrayList<String>();
      reqAr.add("addServ");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("servName");
      if (checkParam(reqAr) == true) {

        String[] s = params.get("servName").toString().trim().split(":");
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        ControllerService css = cm.addControllerService();
        css.setServiceName(s[0]);
        css.setServiceAction(s[1]);
        ck.saveController(params.get("cntrlName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       * Изменить сервис
       */
      reqAr = new ArrayList<String>();
      reqAr.add("chngServ");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("servName");
      reqAr.add("index");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        ControllerService cServ = cm.getContollerService(Integer.parseInt(params.get("index").toString().trim()));
        if (cServ != null) {
          String[] s = params.get("servName").toString().trim().split(":");
          cServ.setServiceName(s[0]);
          cServ.setServiceAction(s[1]);
          ck.saveController(params.get("cntrlName").toString().trim());
          ck.setDataFromBase();
        }
      }

      /*
       Удалить сервис
       */
      reqAr = new ArrayList<String>();
      reqAr.add("delServMeth");
      reqAr.add("cName");
      reqAr.add("cAction");
      reqAr.add("servIndex");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cName").toString().trim(), params.get("cAction").toString().trim());
        Integer i = Integer.parseInt(params.get("servIndex").toString().trim());
        cm.getServiceList().remove(cm.getServiceList().get(i));
        ck.saveController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       Поднять сервис
       */
      reqAr = new ArrayList<String>();
      reqAr.add("upServMeth");
      reqAr.add("cName");
      reqAr.add("cAction");
      reqAr.add("servIndex");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cName").toString().trim(), params.get("cAction").toString().trim());
        Integer i = Integer.parseInt(params.get("servIndex").toString().trim());
        cm.up(i);
        ck.saveController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }


      /*
       Опустить сервис
       */
      reqAr = new ArrayList<String>();
      reqAr.add("downServMeth");
      reqAr.add("cName");
      reqAr.add("cAction");
      reqAr.add("servIndex");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cName").toString().trim(), params.get("cAction").toString().trim());
        Integer i = Integer.parseInt(params.get("servIndex").toString().trim());
        cm.down(i);
        ck.saveController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       Добавить параметр на входе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("addInnerParam");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("indexX");
      reqAr.add("paramName");
      reqAr.add("paramAlias");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        Integer i = Integer.parseInt(params.get("indexX").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        ServiceParameter cp = css.addInnerParams(params.get("paramName").toString().trim());
        cp.setAlias(params.get("paramAlias").toString().trim());
        cp.setOrigin(ControllerOrigin.Request);
        ck.saveController(params.get("cntrlName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       Удалить параметр на входе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("delInnerParam");
      reqAr.add("cName");
      reqAr.add("cAction");
      reqAr.add("servIndex");
      reqAr.add("paramName");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cName").toString().trim(), params.get("cAction").toString().trim());
        Integer i = Integer.parseInt(params.get("servIndex").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        css.deleteInnerParams(params.get("paramName").toString().trim());
        ck.saveController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       изменить настройки параметра на входе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("changeSourceInner");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("indexX");
      reqAr.add("paramName");
      reqAr.add("sourse");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        Integer i = Integer.parseInt(params.get("indexX").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        ServiceParameter cp = css.getInnerParams().get(params.get("paramName").toString().trim());
        String src = params.get("sourse").toString().trim();
        for (ControllerOrigin co : ControllerOrigin.values()) {
          if (src.equals(co.toString())) {
            cp.setOrigin(co);
          }
        }
        boolean mandatory = false;
        if (params.get("mandatory") != null) {
          mandatory = true;
        }
        cp.setMandatory(mandatory);
        boolean array = false;
        if (params.get("array") != null) {
          array = true;
        }
        cp.setArray(array);
        DataTypes dataType = DataTypes.CHAR;
        if (params.get("dataType") != null) {
          String dataTypeString = params.get("dataType").toString().trim();
          if (dataTypeString != null) {
            for (DataTypes type : DataTypes.values()) {
              if (dataTypeString.equalsIgnoreCase(type.toString())) {
                dataType = type;
                break;
              }
            }
          }
        }
        cp.setDataType(dataType);
        ck.saveController(params.get("cntrlName").toString().trim());
        ck.setDataFromBase();
      }


      /*
       Добавить параметр на выходе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("addOuterParam");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("indexX");
      reqAr.add("paramName");
      reqAr.add("paramAlias");
      if (checkParam(reqAr) == true) {

        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        Integer i = Integer.parseInt(params.get("indexX").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        ServiceParameter cp = css.addOuterParams(params.get("paramName").toString().trim());
        cp.setAlias(params.get("paramAlias").toString().trim());
        cp.setOrigin(ControllerOrigin.Input);
        ck.saveController(params.get("cntrlName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       Удалить параметр на выходе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("delOuterParam");
      reqAr.add("cName");
      reqAr.add("cAction");
      reqAr.add("servIndex");
      reqAr.add("paramName");
      if (checkParam(reqAr) == true) {

        ControllerMethod cm = ck.getOneControllerMethod(params.get("cName").toString().trim(), params.get("cAction").toString().trim());
        Integer i = Integer.parseInt(params.get("servIndex").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        css.deleteOuterParams(params.get("paramName").toString().trim());
        ck.saveController(params.get("cName").toString().trim());
        ck.setDataFromBase();
      }

      /*
       изменить параметр на вsходе
       */
      reqAr = new ArrayList<String>();
      reqAr.add("changeSourceOuter");
      reqAr.add("cntrlName");
      reqAr.add("methName");
      reqAr.add("indexX");
      reqAr.add("paramName");
      reqAr.add("sourse");
      if (checkParam(reqAr) == true) {
        ControllerMethod cm = ck.getOneControllerMethod(params.get("cntrlName").toString().trim(), params.get("methName").toString().trim());
        Integer i = Integer.parseInt(params.get("indexX").toString().trim());
        ControllerService css = cm.getServiceList().get(i);
        ServiceParameter cp = css.getOuterParams().get(params.get("paramName").toString().trim());
        String src = params.get("sourse").toString().trim();
        for (ControllerOrigin co : ControllerOrigin.values()) {
          if (src.equals(co.toString())) {
            cp.setOrigin(co);
          }
        }
        boolean mandatory = false;
        if (params.get("mandatory") != null) {
          mandatory = true;
        }
        cp.setMandatory(mandatory);
        boolean array = false;
        if (params.get("array") != null) {
          array = true;
        }
        cp.setArray(array);
        DataTypes dataType = DataTypes.CHAR;
        if (params.get("dataType") != null) {
          String dataTypeString = params.get("dataType").toString().trim();
          if (dataTypeString != null) {
            for (DataTypes type : DataTypes.values()) {
              if (dataTypeString.equalsIgnoreCase(type.toString())) {
                dataType = type;
                break;
              }
            }
          }
        }
        cp.setDataType(dataType);
        ck.saveController(params.get("cntrlName").toString().trim());
        ck.setDataFromBase();
      }

      String cName = (params.get("cntrlName") != null ? params.get("cntrlName").toString().trim() : null);
      if (cName == null) {
        cName = (params.get("cName") != null ? params.get("cName").toString().trim() : null);
      }
      String cAction = (params.get("methName") != null ? params.get("methName").toString().trim() : null);
      if (cAction == null) {
        cAction = (params.get("cAction") != null ? params.get("cAction").toString().trim() : null);
      }

      String name = params.get("name") != null ? params.get("name").toString() : null;
      String methodName = params.get("methodName") != null ? params.get("methodName").toString() : null;

      String title = "Servlet Controller";
      if (name != null) {
        title = "Cnt " + name;
      }

      str += errors;

      str += downloadForm();

      str += uploadForm();

      str += checkForm();

      str += (getAddControllerForm());

      // вывод списка контроллеров
      if (name == null) {
        TreeMap<String, StructureController> map = new TreeMap<String, StructureController>(ck.getControllers());
        for (String Name : map.keySet()) {
          str += (showLink(Name));
        }
        // вывод списка методов в контроллере

      } else if (name != null && methodName == null) {
        // вывод одного контроллера
        str += href(object, action, "", "Перейти к общему списку", new HashMap()) + "</br></br>";
        StructureController cr = ck.getController(name);
        str += ("<div class=controller_head id=" + name + " > ");

        Map<String, Object> linkParams = new HashMap();
        linkParams.put("cName", name);
        linkParams.put("deleteCntrl", "1");
        str += ("<div style='float:left;'>" + changeControllerForm(name, cr.getAlias(), cr.getDescription()) + "</div><div style='float:left;'>" + href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"") + "<font color=brown onclick=\"hide('" + name + "1');\" >Отображение</font></div>");
        str += ("</br>");
        str += ("</div>");
        String dispCnt;
        if (cName != null && name.equals(cName)) {
          dispCnt = "";
        } else {
          dispCnt = "style=\"display:none;\"";
        }
        str += ("<div class=controller_body id=" + name + "1 " + dispCnt + ">");
        str += (getAddMethodForm(name));
        str += ("<div class=controllerMethods>");

        TreeMap<String, ControllerMethod> methodsMap = new TreeMap(ck.getControllers().get(name).getControllersMethods());
        // вывод методов
        for (String method : methodsMap.keySet()) {
          Map<String, Object> methodParams = new HashMap();
          methodParams.put("name", name);
          methodParams.put("methodName", method);
          String href = href(object, action, "", method, methodParams) + "</br>";
          str += href;
        }
      } else if (name != null && methodName != null) {
        str += href(object, action, "", "Перейти к общему списку", new HashMap()) + "</br></br>";
        str += "<h2>Контроллер:" + name + ", метод:" + methodName + "</h2>";

        Map<String, Object> linkParams = new HashMap();

        // вывод метода
        TreeMap<String, ControllerMethod> methodsMap = new TreeMap(ck.getControllers().get(name).getControllersMethods());
        for (String action : methodsMap.keySet()) {
          if (action.equals(methodName)) {
            ControllerMethod cm = methodsMap.get(action);
            str += ("<div class=controllerMethodAll>");
            str += ("<div class=controllerMethod>");

            linkParams = new HashMap();
            linkParams.put("cName", name);
            linkParams.put("cAction", action);
            linkParams.put("deleteMeth", "1");

            str += ("<div style='float:left;'>" + changeMethodForm(name, action, cm.getAlias(), cm.getDescription(), cm.getHidden(), cm.isFree()) + "</div><div style='float:left;'> " + href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"") + "<font color=brown onclick=\"hide('" + name + action + "1');\" >Отображение</font></div>");
            str += ("</br>");
            str += ("<div style='clear:both;'>" + getAddServiceForm(name, action) + "</div>");
            str += ("</div>");
            String dispServ;
            if (cName != null && cAction != null && name.equals(cName) && action.equals(cAction)) {
              dispServ = "";
            } else {
              dispServ = "style=\"display:none;\"";
            }

            str += ("<div class=controllerServices id=" + name + action + "1 " + dispServ + ">");
            for (ControllerService clS : cm.getServiceList()) {
              str += ("<div class=controllerService>");
              str += ("<div class=service>");
              str += (changeServiceAction(clS.getServiceName(), clS.getServiceAction(), name, action, cm.getServiceList().indexOf(clS)));
              str += ("</br>");
              linkParams = new HashMap();
              linkParams.put("cName", name);
              linkParams.put("cAction", action);
              linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
              linkParams.put("delServMeth", "1");
              str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");

              linkParams = new HashMap();
              linkParams.put("cName", name);
              linkParams.put("cAction", action);
              linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
              linkParams.put("upServMeth", "1");
              str += href(object, action, "", "Поднять", linkParams);


              linkParams = new HashMap();
              linkParams.put("cName", name);
              linkParams.put("cAction", action);
              linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
              linkParams.put("downServMeth", "1");
              str += href(object, action, "", "Опустить", linkParams);

              str += ("</div>");
              str += ("<div class=params>");
              str += ("<div class=innerParams>");
              str += ("Параметры на входе:");
              str += ("<table>");
              str += ("<tr><td>Имя</td><td>Алиас</td><td>Источник</td><td>Действие</td></tr>");
              for (String innerName : clS.getInnerParams().keySet()) {
                ServiceParameter parameter = clS.getInnerParams().get(innerName);
                String dataTypeString = "";
                if (parameter.getDataType() != null) {
                  dataTypeString = parameter.getDataType().toString();
                }
                str += ("<tr>");
                str += ("<td>" + innerName + "</td>");
                str += ("<td>" + clS.getInnerParams().get(innerName).getAlias() + "</td>");
                str += ("<td>" + changeSourseInner(name, action, cm.getServiceList().indexOf(clS), innerName, clS.getInnerParams().get(innerName).getOrigin(), parameter.isMandatory(), dataTypeString, parameter.isArray()) + "</td>");
                str += ("<td>");

                linkParams = new HashMap();
                linkParams.put("cName", name);
                linkParams.put("cAction", action);
                linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
                linkParams.put("paramName", innerName);
                linkParams.put("delInnerParam", "1");
                str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");
                str += ("</td>");
                str += ("</tr>");
              }
              str += ("</table>");
              str += (getAddInnerParamsForm(name, action, cm.getServiceList().indexOf(clS)));
              str += ("</div>");
              str += ("<div class=outerParams>");
              str += ("Параметры на выходе:");
              str += ("<table>");
              str += ("<tr><td>Имя</td><td>Алиас</td><td>Источник</td><td>Действие</td></tr>");
              for (String outerName : clS.getOuterParams().keySet()) {
                ServiceParameter parameter = clS.getOuterParams().get(outerName);
                String dataTypeString = "";
                if (parameter.getDataType() != null) {
                  dataTypeString = parameter.getDataType().toString();
                }

                str += ("<tr>");
                str += ("<td>" + outerName + "</td>");
                str += ("<td>" + parameter.getAlias() + "</td>");
                str += ("<td>" + changeSourseOuter(name, action, cm.getServiceList().indexOf(clS), outerName, parameter.getOrigin(), parameter.isMandatory(), dataTypeString, parameter.isArray()) + "</td>");
                str += ("<td>");

                linkParams = new HashMap();
                linkParams.put("cName", name);
                linkParams.put("cAction", action);
                linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
                linkParams.put("paramName", outerName);
                linkParams.put("delOuterParam", "1");
                str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");

                str += ("</td>");
                str += ("</tr>");
              }
              str += ("</table>");
              str += (getAddOuterParamsForm(name, action, cm.getServiceList().indexOf(clS)));
              str += ("</div>");
              str += ("</div>");
              str += ("</div>");
            }
            str += ("</div>");
            str += ("</div>");
          }
        }

      }

      // вывод одного метода
      /*
       else {
       // вывод одного контроллера
       str += href(object, action, "", "Перейти к общему списку", new HashMap()) + "</br></br>";
       StructureController cr = ck.getController(name);
       str += ("<div class=controller_head id=" + name + " > ");

       Map<String, Object> linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("deleteCntrl", "1");
       str += ("<div style='float:left;'>" + changeControllerForm(name, cr.getAlias(), cr.getDescription()) + "</div><div style='float:left;'>" + href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"") + "<font color=brown onclick=\"hide('" + name + "1');\" >Отображение</font></div>");
       str += ("</br>");
       str += ("</div>");
       String dispCnt;
       if (cName != null && name.equals(cName)) {
       dispCnt = "";
       } else {
       dispCnt = "style=\"display:none;\"";
       }
       str += ("<div class=controller_body id=" + name + "1 " + dispCnt + ">");
       str += (getAddMethodForm(name));
       str += ("<div class=controllerMethods>");

       TreeMap<String, ControllerMethod> methodsMap = new TreeMap(ck.getControllers().get(name).getControllersMethods());
       // вывод методов
       for (String action : methodsMap.keySet()) {
       ControllerMethod cm = methodsMap.get(action);
       str += ("<div class=controllerMethodAll>");
       str += ("<div class=controllerMethod>");

       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("deleteMeth", "1");

       str += ("<div style='float:left;'>" + changeMethodForm(name, action, cm.getAlias(), cm.getDescription(), cm.getHidden()) + "</div><div style='float:left;'> " + href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"") + "<font color=brown onclick=\"hide('" + name + action + "1');\" >Отображение</font></div>");
       str += ("</br>");
       str += ("<div style='clear:both;'>" + getAddServiceForm(name, action) + "</div>");
       str += ("</div>");
       String dispServ;
       if (cName != null && cAction != null && name.equals(cName) && action.equals(cAction)) {
       dispServ = "";
       } else {
       dispServ = "style=\"display:none;\"";
       }

       str += ("<div class=controllerServices id=" + name + action + "1 " + dispServ + ">");
       for (ControllerService clS : cm.getServiceList()) {
       str += ("<div class=controllerService>");
       str += ("<div class=service>");
       str += (changeServiceAction(clS.getServiceName(), clS.getServiceAction(), name, action, cm.getServiceList().indexOf(clS)));
       str += ("</br>");
       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
       linkParams.put("delServMeth", "1");
       str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");

       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
       linkParams.put("upServMeth", "1");
       str += href(object, action, "", "Поднять", linkParams);


       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
       linkParams.put("downServMeth", "1");
       str += href(object, action, "", "Опустить", linkParams);

       str += ("</div>");
       str += ("<div class=params>");
       str += ("<div class=innerParams>");
       str += ("Параметры на входе:");
       str += ("<table>");
       str += ("<tr><td>Имя</td><td>Алиас</td><td>Источник</td><td>Действие</td></tr>");
       for (String innerName : clS.getInnerParams().keySet()) {
       str += ("<tr>");
       str += ("<td>" + innerName + "</td>");
       str += ("<td>" + clS.getInnerParams().get(innerName).getAlias() + "</td>");
       str += ("<td>" + changeSourseInner(name, action, cm.getServiceList().indexOf(clS), innerName, clS.getInnerParams().get(innerName).getOrigin()) + "</td>");
       str += ("<td>");

       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
       linkParams.put("paramName", innerName);
       linkParams.put("delInnerParam", "1");
       str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");
       str += ("</td>");
       str += ("</tr>");
       }
       str += ("</table>");
       str += (getAddInnerParamsForm(name, action, cm.getServiceList().indexOf(clS)));
       str += ("</div>");
       str += ("<div class=outerParams>");
       str += ("Параметры на выходе:");
       str += ("<table>");
       str += ("<tr><td>Имя</td><td>Алиас</td><td>Источник</td><td>Действие</td></tr>");
       for (String outerName : clS.getOuterParams().keySet()) {
       str += ("<tr>");
       str += ("<td>" + outerName + "</td>");
       str += ("<td>" + clS.getOuterParams().get(outerName).getAlias() + "</td>");
       str += ("<td>" + changeSourseOuter(name, action, cm.getServiceList().indexOf(clS), outerName, clS.getOuterParams().get(outerName).getOrigin()) + "</td>");
       str += ("<td>");

       linkParams = new HashMap();
       linkParams.put("cName", name);
       linkParams.put("cAction", action);
       linkParams.put("servIndex", cm.getServiceList().indexOf(clS));
       linkParams.put("paramName", outerName);
       linkParams.put("delOuterParam", "1");
       str += href(object, action, "", "Удалить", linkParams, "", "onclick=\"return confirmDelete();\"");

       str += ("</td>");
       str += ("</tr>");
       }
       str += ("</table>");
       str += (getAddOuterParamsForm(name, action, cm.getServiceList().indexOf(clS)));
       str += ("</div>");
       str += ("</div>");
       str += ("</div>");
       }
       str += ("</div>");
       str += ("</div>");
       }
       str += ("</div>");
       str += ("</div>");
       }
       */

      str += (ck.getErrors());



    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    }
    return status;
  }

  // методы получения данных ---------------------------------------------------------------------------------------------------------
  /**
   * получить список сервисов. Формат массива: ключи - имя сервиса : имя метода.
   * Значения - то же самое
   *
   * @return
   * @throws Exception
   */
  private LinkedHashMap<String, Object> getServiceMap() throws Exception {
    HashMap<String, ArrayList<String>> hs = new HashMap<String, ArrayList<String>>();

    Collection<String> classes;
    OptionsKeeper os = app.getKeeper().getOptionKeeper();
    classes = ServiceFactory.scan(os.getBiPath());
    for (String clName : classes) {
      Class cls = Class.forName("bi." + clName);
      ArrayList<String> al = new ArrayList<String>();
      hs.put(clName, al);
      Method[] m = cls.getMethods();
      for (Method mm : m) {
        al.add(mm.getName());
      }
    }

    ArrayList<String> checkList = new ArrayList<String>();
    checkList.add("wait");
    checkList.add("setRequest");
    checkList.add("getConnection");
    checkList.add("toString");
    checkList.add("equals");
    checkList.add("hashCode");
    checkList.add("getClass");
    checkList.add("notify");
    checkList.add("notifyAll");
    checkList.add("setStructure");
    checkList.add("getActionResult");
    checkList.add("setConnection");
    checkList.add("setField");
    checkList.add("setAuthorizedUserId");
    checkList.add("setRightsObject");
    checkList.add("getActionResult");

    LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
    for (String str : hs.keySet()) {
      for (String str2 : hs.get(str)) {
        if (!checkList.contains(str2)) {
          map.put(str + ":" + str2, str + ":" + str2);
        }
      }
    }
    return map;
  }

  /**
   * загрузка файла контроллеров
   *
   * @throws Exception
   */
  private void uploadControllersFile() throws Exception {
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
          NodeList controllersNodeList = root.getElementsByTagName(StructureController.ELEMENT_NAME);

          ControllerKeeper ck = app.getKeeper().getControllerKeeper();
          ck.setDataFromBase();

          Map<String, StructureController> controllers = ck.getControllers();
          // для каждого элемента
          for (int i = 0; i < controllersNodeList.getLength(); i++) {
            Element cntElement = (Element) controllersNodeList.item(i);
            // создать модель
            StructureController newCnt = StructureController.getFromXml(cntElement);
            String controllerName = newCnt.getName();
            // если модели с таким именем нет в списке
            if (!controllers.containsKey(controllerName)) {
              // добавить
              controllers.put(controllerName, newCnt);
              ck.saveController(controllerName);
            } else {
              // если контроллер с таким именем уже есть
              // если поставлена галочка заменять
              if (params.get("replace") != null) {
                // то обновить
                controllers.put(controllerName, newCnt);
                ck.saveController(controllerName);
              }
              // если поставлена галочка - добавлять методы
              if (params.get("add") != null) {
                // получить старый контроллер
                StructureController oldCnt = controllers.get(controllerName);
                // получить методы из нового контроллера
                Map<String, ControllerMethod> newMethods = newCnt.getControllersMethods();
                // для каждого метода из нового
                for (String newMethodName : newMethods.keySet()) {
                  // если такого метода нет в старом 
                  if (!oldCnt.getControllersMethods().containsKey(newMethodName)) {
                    // добавить
                    ControllerMethod newMethod = newMethods.get(newMethodName);
                    oldCnt.getControllersMethods().put(newMethodName, newMethod);
                  }
                }
                ck.saveController(controllerName);
              }

            }
            refreshWarehouseSingleton();
          }
        }
      }
    }
  }

  /**
   * вернуть файл контроллеров
   *
   * @throws Exception
   */
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
   * проверка контроллеров
   */
  private List<String> checkControllers(ControllerKeeper ck) {
    List<String> incorrect = new ArrayList();
    // список контроллеров и методов, где есть несоответствия
    // получить список всех контроллеров
    TreeMap<String, StructureController> controllers = new TreeMap(ck.getControllers());
    // получить список всех сервисов в приложении
    // проверить каждый метод контроллера - есть ли такой сервис. 
    for (String controllerName : controllers.keySet()) {
      StructureController cnt = controllers.get(controllerName);
      TreeMap<String, ControllerMethod> methods = new TreeMap(cnt.getControllersMethods());
      for (String methodName : methods.keySet()) {
        ControllerMethod method = methods.get(methodName);
        List<ControllerService> serviceList = method.getServiceList();
        for (ControllerService service : serviceList) {
          String serviceName = service.getServiceName();
          String serviceAction = service.getServiceAction();
          String fullName = serviceName + ":" + serviceAction;
          // если нет
          if (!servicesMap.containsKey(fullName)) {
            // добавить метод в список
            incorrect.add("Контроллер: " + controllerName + ", Метод контроллера: " + methodName + ", сервис: " + fullName);
          }
        }
      }
    }
    return incorrect;
  }

  /**
   * проверка, существуют ли все параметры с именами
   *
   * @param name
   * @return
   */
  Boolean checkParam(ArrayList<String> name) {
    Boolean res = true;
    for (String param : name) {
      if (res == true && params.get(param) != null && !"".equals(params.get(param).toString())) {
        res = true;
      } else {
        res = false;
      }
    }
    return res;
  }

  // методы вывода рендера ------------------------------------------------------------------------------------------------------------
  /**
   * форма - проверить контроллеры
   *
   * @return
   */
  private String checkForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", CHECK_SPECACTION), "");
    AbsEnt form = rd.horizontalForm(inner, "Проверить контроллеры", null);
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
    String str = "Список контроллеров, в которых используются сервисы, отсутствующие в системе. <br/><br/>";
    if (incorrect.isEmpty()) {
      str += "Несоответствий не найдено";
    }
    for (String s : incorrect) {
      str += s + "<br/>";
    }
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
    inner.put(rd.checkBox("add", null), "Добавлять методы к существующим");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", UPLOAD_FILE_SPECACTION), "");
    AbsEnt form = rd.horizontalForm(inner, "Загрузить файл контроллеров", null, true, null);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * форма для скачивания файлов
   *
   * @return
   * @throws Exception
   */
  private String downloadForm() throws Exception {
    ControllerKeeper ck = app.getKeeper().getControllerKeeper();
    ck.setDataFromBase();
    Map<String, StructureController> controllers = ck.getControllers();
    Map<String, Object> controllersNames = new TreeMap();
    for (String name : controllers.keySet()) {
      controllersNames.put(name, name);
    }
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.multipleCombo(controllersNames, null, "controllers", 5), "Контроллеры");
    inner.put(rd.hiddenInput("action", action), "");
    inner.put(rd.hiddenInput("object", object), "");
    inner.put(rd.hiddenInput("specAction", DOWNLOAD_FILE_SPECACTION), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    AbsEnt form = rd.horizontalForm(inner, "Скачать файл", null);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * ссылка на контроллер
   *
   * @param name
   * @return
   * @throws Exception
   */
  private String showLink(String name) throws Exception {
    Map<String, Object> linkParams = new HashMap();
    linkParams.put("name", name);
    return href(object, action, "", name, linkParams) + "</br>";
  }

  /**
   * форма добавления контроллера
   *
   * @return
   * @throws Exception
   */
  String getAddControllerForm() throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("cntrlName", "", "Название"), "");
    hs.put(rd.textInput("alias", "", "Русское"), "");
    hs.put(rd.textArea("descr", "", "Описание"), "");
    hs.put(rd.textInput("methName", "", "Метод"), "");
    hs.put(rd.textInput("metAlias", "", "Метод(рус)"), "");
    hs.put(rd.textArea("metDescr", "", "Описание метода"), "");
    AbsEnt form = rd.horizontalForm(hs, "Добавить контроллер", "images/add.png");
    form.setAttribute(EnumAttrType.action, "");
    form.setAttribute(EnumAttrType.style, "");
    form.addEnt(rd.hiddenInput("addCntrl", "addCntrl"));
    return form.render();
  }

  /**
   * форма добавления метода
   *
   * @param contr
   * @return
   * @throws Exception
   */
  String getAddMethodForm(String contr) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("methName", "", "Метод"), "Метод");
    hs.put(rd.textInput("metAlias", "", "Метод(рус)"), "Метод(рус)");
    hs.put(rd.textArea("metDescr", "", "Описание метода"), "Описание метода");
    AbsEnt form = rd.horizontalForm(hs, "Добавить метод", "images/add.png");
    form.addEnt(rd.hiddenInput("addMeth", "addMeth"));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма добавления сервиса
   *
   * @param contr
   * @param Action
   * @return
   * @throws Exception
   */
  String getAddServiceForm(String contr, String Action) throws Exception {
    TreeMap<String, Object> tree = new TreeMap<String, Object>(servicesMap);
    servicesMap = new LinkedHashMap<String, Object>(tree);
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(servicesMap, contr, "servName"), "Название");
    AbsEnt form = rd.horizontalForm(hs, "+", "images/add.png");
    form.addEnt(rd.hiddenInput("addServ", "addServ"));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.addEnt(rd.hiddenInput("methName", Action));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма добавления параметра inner
   *
   * @param contr
   * @param Action
   * @param Index
   * @return
   * @throws Exception
   */
  String getAddInnerParamsForm(String contr, String Action, Integer Index) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("paramName", "", "Параметр"), "");
    hs.put(rd.textInput("paramAlias", "", "Алиас"), "");
    AbsEnt form = rd.horizontalForm(hs, "+", "images/add.png");
    form.addEnt(rd.hiddenInput("addInnerParam", Index));
    form.addEnt(rd.hiddenInput("indexX", Index));
    form.addEnt(rd.hiddenInput("methName", Action));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма добавления параметра outer
   *
   * @param contr
   * @param Action
   * @param Index
   * @return
   * @throws Exception
   */
  String getAddOuterParamsForm(String contr, String Action, Integer Index) throws Exception {
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("paramName", "", "Параметр"), "");
    hs.put(rd.textInput("paramAlias", "", "Алиас"), "");
    AbsEnt form = rd.horizontalForm(hs, "+", "images/add.png");
    form.addEnt(rd.hiddenInput("addOuterParam", Index));
    form.addEnt(rd.hiddenInput("indexX", Index));
    form.addEnt(rd.hiddenInput("methName", Action));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма изменения источника параметра inner
   *
   * @param contr
   * @param Action
   * @param Index
   * @param paramName
   * @param srcCode
   * @return
   * @throws Exception
   */
  String changeSourseInner(String contr, String Action, Integer Index, String paramName, ControllerOrigin srcCode, boolean mandatory, String dataType, boolean array) throws Exception {
    String result = "";
    LinkedHashMap<String, Object> ls = new LinkedHashMap<String, Object>();
    ls.put(ControllerOrigin.Input.toString(), "INPUT");
    ls.put(ControllerOrigin.Request.toString(), "REQUEST");
    ls.put(ControllerOrigin.Session.toString(), "SESSION");
    ls.put(ControllerOrigin.ReqSession.toString(), "REQ->SESSION");
    ls.put(ControllerOrigin.SesRequest.toString(), "SES->REQUEST");


    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(ls, srcCode, "sourse"), "");
    hs.put(rd.checkBox("mandatory", mandatory, ""), "Обяз.");
    hs.put(rd.combo(DataTypes.all(), dataType, "dataType", true), "");
    hs.put(rd.checkBox("array", array, ""), "Массив");
    AbsEnt form = rd.horizontalForm(hs, "OK", "images/ok.png");
    form.addEnt(rd.hiddenInput("changeSourceInner", Index));
    form.addEnt(rd.hiddenInput("indexX", Index));
    form.addEnt(rd.hiddenInput("methName", Action));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.addEnt(rd.hiddenInput("paramName", paramName));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма изменения источника параметра outer
   *
   * @param contr
   * @param Action
   * @param Index
   * @param paramName
   * @param srcCode
   * @return
   * @throws Exception
   */
  String changeSourseOuter(String contr, String Action, Integer Index, String paramName, ControllerOrigin srcCode, boolean mandatory, String dataType, boolean array) throws Exception {
    String result = "";
    LinkedHashMap<String, Object> ls = new LinkedHashMap<String, Object>();
    ls.put(ControllerOrigin.Input.toString(), "INPUT");
    ls.put(ControllerOrigin.Session.toString(), "SESSION");


    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(ls, srcCode, "sourse"), "");
    hs.put(rd.checkBox("mandatory", mandatory, ""), "Обяз.");
    hs.put(rd.combo(DataTypes.all(), dataType, "dataType", true), "");
    hs.put(rd.checkBox("array", array, ""), "Массив");
    AbsEnt form = rd.horizontalForm(hs, "OK", "images/ok.png");
    form.addEnt(rd.hiddenInput("changeSourceOuter", Index));
    form.addEnt(rd.hiddenInput("indexX", Index));
    form.addEnt(rd.hiddenInput("methName", Action));
    form.addEnt(rd.hiddenInput("cntrlName", contr));
    form.addEnt(rd.hiddenInput("paramName", paramName));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   *
   * @param serviceName
   * @param serviceAction
   * @param cntrlName
   * @param cntrlAction
   * @param ind
   * @return
   * @throws Exception
   */
  String changeServiceAction(String serviceName, String serviceAction, String cntrlName, String cntrlAction, Integer ind) throws Exception {
    String result = "";

    TreeMap<String, Object> tree = new TreeMap<String, Object>(servicesMap);
    servicesMap = new LinkedHashMap<String, Object>(tree);
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.combo(servicesMap, serviceName + ":" + serviceAction, "servName"), "");
    AbsEnt form = rd.horizontalForm(hs, "OK", "images/ok.png");
    form.addEnt(rd.hiddenInput("chngServ", "chngServ"));
    form.addEnt(rd.hiddenInput("index", ind));
    form.addEnt(rd.hiddenInput("methName", cntrlAction));
    form.addEnt(rd.hiddenInput("cntrlName", cntrlName));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма изменения контроллера
   *
   * @param cntrlName
   * @param alias
   * @param description
   * @return
   * @throws Exception
   */
  String changeControllerForm(String cntrlName, String alias, String description) throws Exception {
    String result = "";
    if (alias == null || "".equals(alias)) {
      alias = "Описание";
    }
    if (description == null || "".equals(description)) {
      description = "Полное описание";
    }

    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("newCntrlName", cntrlName, "Контроллер"), "Контроллер: ");
    hs.put(rd.textInput("alias", alias, "Алиас(рус)"), "Алиас(рус): ");
    hs.put(rd.textArea("description", description, "Описание: "), "Описание: ");
    AbsEnt form = rd.horizontalForm(hs, "OK", "images/ok.png");
    form.addEnt(rd.hiddenInput("chngCntrl", "chngCntrl"));;
    form.addEnt(rd.hiddenInput("cntrlName", cntrlName));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }

  /**
   * форма изменения метода
   *
   * @param cntrlName
   * @param methName
   * @param alias
   * @param description
   * @param hidden
   * @return
   * @throws Exception
   */
  String changeMethodForm(String cntrlName, String methName, String alias, String description, Boolean hidden, boolean free) throws Exception {
    String result = "";
    LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
    hs.put(rd.textInput("newMethName", methName, "Метод"), "Метод: ");
    hs.put(rd.textInput("metAlias", alias, "Алиас"), "Алиас(рус): ");
    hs.put(rd.textArea("metDescr", description, "Описание: "), "Описание: ");
    hs.put(rd.checkBox("hidden", hidden, null), "Скрытый");
    hs.put(rd.checkBox("free", free, null), "Общедоступный");
    AbsEnt form = rd.horizontalForm(hs, "OK", "images/ok.png");
    form.addEnt(rd.hiddenInput("chngMethod", "chngMethod"));;
    form.addEnt(rd.hiddenInput("cntrlName", cntrlName));
    form.addEnt(rd.hiddenInput("methName", methName));
    form.setAttribute(EnumAttrType.action, "");
    return form.render();
  }
}
