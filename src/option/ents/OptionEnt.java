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
import web.fabric.AbsEnt;

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
  public String render() throws Exception{
    AbsEnt base=rd.div("","");
    base.setValue("Вызван объект"+ object);
    return base.render();
    
  }

  @Override
  public Boolean run() throws Exception{
    return true;
  }

  
}
