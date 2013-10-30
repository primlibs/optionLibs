/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import option.ents.modelEnts.AddStructure;
import option.ents.modelEnts.AddValidator;
import option.ents.modelEnts.AllStructure;
import option.ents.modelEnts.ChangeStructure;
import option.ents.modelEnts.ChangeValidator;
import option.ents.modelEnts.OneStructure;
import prim.AbstractApplication;
import prim.libs.MyString;
import web.Render;

/**
 *
 * @author кот
 */
public class ModelEnt extends OptionAbstract{
  
  protected ModelEnt(AbstractApplication app,Render rd,String action,String specAction){
    this.object="modelEnt";
    setApplication(app);
    setRender(rd);
    this.action= MyString.getString(action);
    this.specAction= MyString.getString(specAction);
  }

  static ModelEnt getInstance(AbstractApplication app,Render rd,String action,String specAction){
    if (action.equals("AllStructure")) {
      return new AllStructure(app, rd, action, specAction);
    } else if (action.equals("OneStructure")) {
      return new OneStructure(app, rd, action, specAction);
    } else if (action.equals("AddStructure")) {
      return new AddStructure(app, rd, action, specAction);
    } else if (action.equals("AddValidator")) {
      return new AddValidator(app, rd, action, specAction);
    } else if (action.equals("ChangeStructure")) {
      return new ChangeStructure(app, rd, action, specAction);
    } else if (action.equals("ChangeValidator")) {
      return new ChangeValidator(app, rd, action, specAction);
    }
    return new ModelEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception{
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Boolean run() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
