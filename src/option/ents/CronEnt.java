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
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import prim.service.ServiceFactory;
import warehouse.OptionsKeeper;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.ControllerMethod;
import warehouse.controllerStructure.ControllerService;
import warehouse.controllerStructure.StructureController;
import warehouse.cron.CronObject;
import warehouse.cron.CronSingleton;
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
public class CronEnt extends OptionAbstract {

  private String str = "";


  private CronEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "cronEnt";
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static CronEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new CronEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;
    try{
      CronSingleton ck = CronSingleton.getInstanceNew(app);
      String servName= MyString.getString(params.get("servName"));
      if(action.equals("add")&&!servName.equals("")){
        Integer coNew=ck.setCronObject();
        CronObject cobj=ck.getCronObject(coNew);
        cobj.setServiceName(servName);
        ck.SaveCollectionInFile();
      }else if(action.equals("delete")&&!servName.equals("")){
        for(CronObject co:ck.getCronlist()){
          if(co.getServiceName().equals(servName)){
            ck.getCronlist().remove(co);
          }
        }
        ck.SaveCollectionInFile();
      }
      AbsEnt date=rd.table("","",null);
      Map<AbsEnt,String> mp1=new HashMap();
      mp1.put(rd.combo(getServiceMap(),null, "servName"), "");
      rd.tr(date, rd.rightForm(true, object, "add", null, mp1, "Добавить", rd.getRenderConstant().ADD_IMGPH, false));
      for(CronObject co:ck.getCronlist()){
        Map<AbsEnt,String> mp=new HashMap();
        mp.put(rd.hiddenInput("servName", co.getServiceName()), "");
        rd.tr(date, co.getServiceName(),rd.rightForm(true, object, "delete", null, mp, "Удалить", rd.getRenderConstant().DEL_IMGPH, false));
      }      
      str+=date.render();
    } catch (Exception e) {
      MyString.getStackExeption(e);
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
  
}
