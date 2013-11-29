/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option;

import option.ents.OptionAbstract;
import java.util.HashMap;
import java.util.Map;
import prim.AbstractApplication;
import prim.libs.MyString;
import web.Render;

/**
 *
 * @author кот
 */
public final class Creator {
  AbstractApplication app;
  String object="";
  String action="";
  String specAction="";
  Map<String, Object> innerRequest=new HashMap();
  Render rd;
  
  protected Boolean redirect=false;
  protected String redirectObject="";
  protected String redirectAction="";
  protected String redirectSpecAction = "";
  protected Map<String, Object> redirectParams=new HashMap<String, Object>();
  protected byte[] fileContent;
  protected String fileName;
  
  public static final String OPTION_OBJECT_NAME="optionEnt";
  public static final String CONTROLLER_OBJECT_NAME="controllerEnt";
  public static final String PAIR_OBJECT_NAME="pairEnt";
  public static final String MODEL_OBJECT_NAME="modelEnt";
  public static final String CRON_OBJECT_NAME="cronEnt";

  public Boolean isRedirect() {
    return redirect;
  }

  public String getFileName() {
    return fileName;
  }

  public String getRedirectObject() {
    return redirectObject;
  }

  public String getRedirectAction() {
    return redirectAction;
  }

  public Map<String, Object> getRedirectParams() {
    return redirectParams;
  }

  public String getRedirectSpecAction() {
    return redirectSpecAction;
  }
  
  
  
  private Creator(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest,Render rd){
    this.app=app;
    this.action=action;
    this.object=object;
    this.specAction=specAction;
    this.innerRequest=innerRequest;
    this.rd=rd;
  }
  
  public static Creator getInstance(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest, Render rd){
    return new Creator(app,object,action,specAction,innerRequest,rd);
  }
  
   public String run(){
    try{
    //маршрутизация
    String result="";
    Renderrable rbl= getPath(app,object,action,specAction,innerRequest,rd);
    rbl.setParams(innerRequest);
    //выполнение
    rbl.run();
    this.fileContent = rbl.getFileContent();
    this.fileName = rbl.getFileName();
    //переадресация
    if(rbl.isRedirect()){
      redirect = true;
      this.redirectAction = rbl.getRedirectAction();
      this.redirectObject = rbl.getRedirectObject();
      this.redirectParams = rbl.getRedirectParams();  
      this.redirectSpecAction = rbl.getRedirectSpecAction();
    }else{
      result=rbl.render();
    }
    //ответ
    return result;
    }catch(Exception ex){
      return MyString.getStackExeption(ex);
    }
  }
   
   public byte[] getFileContent() {
     return fileContent;
   }
  
  private Renderrable getPath(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest, Render rd){
    if(object.equals(MODEL_OBJECT_NAME)){
      return OptionAbstract.getModel(app, rd,action,specAction);
    }else if(object.equals(PAIR_OBJECT_NAME)){
      return OptionAbstract.getPair(app, rd,action,specAction);
    }else if(object.equals(CONTROLLER_OBJECT_NAME)){
      return OptionAbstract.getController(app, rd,action,specAction);
    }else if(object.equals(CRON_OBJECT_NAME)){
      return OptionAbstract.getCron(app, rd,action,specAction);
    }else{
      return OptionAbstract.getOption(app, rd,action,specAction);
    }
  }
  
}
