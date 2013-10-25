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
public class PairEnt extends OptionAbstract{
  private PairEnt(AbstractApplication app,Render rd,String action,String specAction){
    this.object="pairEnt";
    setApplication(app);
    setRender(rd);
    this.action= MyString.getString(action);
    this.specAction= MyString.getString(specAction);
  }

  static PairEnt getInstance(AbstractApplication app,Render rd,String action,String specAction){
    return new PairEnt(app, rd,action,specAction);
  }

  @Override
  public String render()throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Boolean run() throws Exception{
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
