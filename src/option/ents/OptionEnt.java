/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import option.ents.OptionAbstract;
import java.util.Map;
import prim.AbstractApplication;
import prim.libs.MyString;
import web.Render;

/**
 *
 * @author кот
 */
class OptionEnt extends OptionAbstract{
  
  private OptionEnt(AbstractApplication app,Render rd,String action,String specAction){
    this.object="optionEnt";
    setApplication(app);
    setRender(rd);
    this.action= MyString.getString(action);
    this.specAction= MyString.getString(specAction);
  }

  static OptionEnt getInstance(AbstractApplication app,Render rd,String action,String specAction){
    return new OptionEnt(app, rd,action,specAction);
  }
  
  @Override
  public String render() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Boolean run() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  
}
