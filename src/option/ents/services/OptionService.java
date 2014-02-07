/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.services;

import java.util.ArrayList;
import java.util.List;
import com.prim.core.AbstractApplication;
import com.prim.core.model.Model;
import com.prim.core.model.ModelFactory;
import com.prim.core.modelStructure.Structure;
import com.prim.core.select.Select;
import com.prim.core.select.Table;
import com.prim.core.select.TableSelectFactory;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;

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
