/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option;

import java.util.HashMap;
import java.util.Map;
import prim.AbstractApplication;
import web.Render;

/**
 *
 * @author кот
 */
public final class Creator {
  AbstractApplication app;
  String object="";
  String action="";
  Map<String, Object> innerRequest=new HashMap();
  Render rd;
  
  private Creator(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest,Render rd){
    this.app=app;
    this.action=action;
    this.object=object;
    this.action=specAction;
    this.innerRequest=innerRequest;
    this.rd=rd;
  }
  
  
  public static Creator getInstance(AbstractApplication app,String object,String action,String specAction,Map<String, Object> innerRequest, Render rd){
    return new Creator(app,object,action,specAction,innerRequest,rd);
  }
  
  public String run(){
    //маршрутизация
    //выполнение
    //переадресация
    //ответ
    String result="";
    return result;
  }
  
  
}
