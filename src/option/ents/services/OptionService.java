/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.services;

import java.util.ArrayList;
import java.util.List;
import prim.AbstractApplication;
import prim.model.Model;
import prim.model.ModelFactory;
import prim.modelStructure.Structure;
import prim.select.Select;
import prim.select.Table;
import prim.select.TableSelectFactory;
import warehouse.modelKeeper.ModelStructureKeeper;

/**
 *
 * @author User
 */
public class OptionService {

  protected AbstractApplication app;
  protected List<String> errors = new ArrayList();

  public OptionService(AbstractApplication app) {
    this.app = app;
  }

  public List<String> getErrors() {
    return errors;
  }

  protected Table getTable(String modelAlias) throws Exception {
    TableSelectFactory tf = new TableSelectFactory(app);
    Table table = tf.getTable(modelAlias);
    return table;
  }

  protected Select getSelect(Table... tb) throws Exception {
    TableSelectFactory tf = new TableSelectFactory(app);
    return tf.getSelect(tb);
  }

  protected Model getModel(String tableAlias) throws Exception {
    ModelFactory modelFactory = new ModelFactory(app);
    return modelFactory.getModel(tableAlias);
  }
  
  
  
}
