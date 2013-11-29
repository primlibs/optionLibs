/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.HashMap;
import java.util.Map;
import option.Creator;
import option.Renderrable;
import option.Renderrable;
import prim.AbstractApplication;
import prim.libs.MyString;
import warehouse.WarehouseSingleton;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;

/**
 *
 * @author кот
 */
public abstract class OptionAbstract implements Renderrable{
  protected AbstractApplication app;
  protected Map<String, Object> redirectParams=new HashMap<String, Object>();
  protected Map<String, Object> params=new HashMap<String, Object>();
  protected Render rd;
  protected Boolean isRedirect=false;
  protected String redirectObject="";
  protected String redirectAction="";
  protected String redirectSpecAction="";
  
  protected String action="";
  protected String specAction="";
  protected String object="";
  protected byte[] fileContent;
  protected String fileName;

  @Override
  public String getRedirectObject() {
    return redirectObject;
  }

  public String getFileName() {
    return fileName;
  }
  
  @Override
  public String getRedirectAction() {
    return redirectAction;
  }

  @Override
  public String getRedirectSpecAction() {
    return redirectSpecAction;
  }

  @Override
  public Boolean isRedirect() {
    return isRedirect;
  }

  @Override
  public Map<String, Object> getRedirectParams() {
    return redirectParams;
  }

  @Override
  public void setApplication(AbstractApplication app) {
    this.app=app;
  }

  @Override
  public void setRender(Render rd) {
    this.rd=rd;
  }

  @Override
  public void setParams(Map<String, Object> prms) {
    if(prms!=null){
      params=prms;
    }
  }
  
  public static Renderrable getOption(AbstractApplication app,Render rd,String action,String specAction){
    return OptionEnt.getInstance(app, rd, action, specAction);
  }
  
  public static Renderrable getModel(AbstractApplication app,Render rd,String action,String specAction){
    return ModelEnt.getInstance(app, rd, action, specAction);
  }
  
  public static Renderrable getController(AbstractApplication app,Render rd,String action,String specAction){
    return ControllerEnt.getInstance(app, rd, action, specAction);
  }
  
  public static Renderrable getPair(AbstractApplication app,Render rd,String action,String specAction){
    return PairEnt.getInstance(app, rd, action, specAction);
  }
  
  public static Renderrable getCron(AbstractApplication app,Render rd,String action,String specAction){
    return CronEnt.getInstance(app, rd, action, specAction);
  }
  
  
  public Boolean setRedirect(String object,String action,String specAction){
    if(MyString.NotNull(object)){
      isRedirect=true;
      redirectObject=object;
      redirectAction=action;
      redirectSpecAction=specAction;
      return isRedirect;
    }else{
      return false;
    }
  }
  
  protected String href(String object, String action, String specAction, String name, Map<String, Object> params) throws Exception {
    return rd.href(object, action, specAction, params, name, false).render();
  }
  
  protected String href(String object, String action, String specAction, String name, Map<String, Object> params, String style, String js) throws Exception {
    AbsEnt href = rd.href(object, action, specAction, params, name, false);
    if (style != null && !style.isEmpty()) {
      href.setAttribute(EnumAttrType.style, style);
    }
    if (js != null && !js.isEmpty()) {
      href.setJs(js);
    }
    return href.render();
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
  
  @Override
 public byte[] getFileContent() {
   return fileContent;
 }
  
  protected void refreshWarehouseSingleton() throws Exception {
    WarehouseSingleton.getInstance().getNewKeeper(app);
  }
  
}
