/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.HashMap;
import java.util.Map;
import option.Renderrable;
import option.Renderrable;
import prim.AbstractApplication;
import prim.libs.MyString;
import web.Render;

/**
 *
 * @author кот
 */
public abstract class OptionAbstract implements Renderrable{
  AbstractApplication app;
  Map<String, Object> redirectParams=new HashMap<String, Object>();
  Map<String, Object> params=new HashMap<String, Object>();
  Render rd;
  Boolean isRedirect=false;
  String redirectObject="";
  String redirectAction="";
  String redirectSpecAction="";
  
  String action="";
  String specAction="";
  String object="";

  @Override
  public String getRedirectObject() {
    return redirectObject;
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
  
}
