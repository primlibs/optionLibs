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
    if(object.equals("modelEnt")){
      return OptionAbstract.getModel(app, rd,action,specAction);
    }else if(object.equals("pairEnt")){
      return OptionAbstract.getPair(app, rd,action,specAction);
    }else if(object.equals("controllerEnt")){
      return OptionAbstract.getController(app, rd,action,specAction);
    }else{
      return OptionAbstract.getOption(app, rd,action,specAction);
    }
  }
  
}
