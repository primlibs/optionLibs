/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option;

import java.util.Map;
import prim.AbstractApplication;
import web.Render;

/**
 *
 * @author кот
 */
public interface Renderrable {
  public String render();
  public String getRedirectObject();
  public String getRedirectAction();
  public String getRedirectSpecAction();
  public Boolean run();
  public Boolean isRedirect();
  public Map<String, Object> getRedirectParams();
  public void setApplication(AbstractApplication app);
  public void setRender(Render rd);
  public void setParams(Map<String, Object> prms);
}
