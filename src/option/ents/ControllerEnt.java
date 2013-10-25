/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import prim.AbstractApplication;
import prim.libs.MyString;
import web.Render;

/**
 *
 * @author кот
 */
public class ControllerEnt extends OptionAbstract{
  
   private ControllerEnt(AbstractApplication app,Render rd,String action,String specAction){
    this.object="controllerEnt";
    setApplication(app);
    setRender(rd);
    this.action= MyString.getString(action);
    this.specAction= MyString.getString(specAction);
  }

  static ControllerEnt getInstance(AbstractApplication app,Render rd,String action,String specAction){
    return new ControllerEnt(app, rd,action,specAction);
  }

  @Override
  public String render() throws Exception{
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Boolean run() throws Exception{
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
