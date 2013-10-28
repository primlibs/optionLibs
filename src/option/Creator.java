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
  
  
  public static final String OPTION_OBJECT_NAME="optionEnt";
  public static final String CONTROLLER_OBJECT_NAME="controlletEnt";
  public static final String PAIR_OBJECT_NAME="pairEnt";
  public static final String MODEL_OBJECT_NAME="modelEnt";
  
  
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
    //переадресация
    if(rbl.isRedirect()){
      this.object=rbl.getRedirectObject();
      this.action=rbl.getRedirectAction();
      this.specAction=rbl.getRedirectSpecAction();
      this.innerRequest=rbl.getRedirectParams();
      result=run();
    }else{
      result=rbl.render();
    }
    //ответ
    return result;
    }catch(Exception ex){
      return MyString.getStackExeption(ex);
    }
  }
  
  private Renderrable getPath(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest, Render rd){
    if(object.equals(MODEL_OBJECT_NAME)){
      return OptionAbstract.getModel(app, rd,action,specAction);
    }else if(object.equals(PAIR_OBJECT_NAME)){
      return OptionAbstract.getPair(app, rd,action,specAction);
    }else if(object.equals(CONTROLLER_OBJECT_NAME)){
      return OptionAbstract.getController(app, rd,action,specAction);
    }else{
      return OptionAbstract.getOption(app, rd,action,specAction);
    }
  }
  
}
