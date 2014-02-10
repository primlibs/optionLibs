/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.objects;

import com.prim.web.HrefOptionInterface;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;
import com.prim.web.objects.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rice Pavel
 */
public class ViewFunctions {
  
  private ViewFunctions() { }
  
   public static String paginator3000(Render rd, int page, int countPages, String object, String action, Map<String, Object> params, String nameParameterPage, String paginatorId) throws Exception {
    List<Parameter> paramsList = new ArrayList();
    for (String name : params.keySet()) {
      paramsList.add(new Parameter(name, params.get(name)));
    }
    return paginator3000(rd, page, countPages, object, action, paramsList, nameParameterPage, paginatorId);
  }

  public static String paginator3000(Render rd, int page, int countPages, String object, String action, List<Parameter> params, String nameParameterPage, String paginatorId) throws Exception {
    HrefOptionInterface ho = rd.getHrefOption();
    ho.setAction(action);
    ho.setObject(object);
    ho.setSpecAction("");
    ho.setNoValidateRights();
    AbsEnt link = rd.href(params, ho);
    String href = link.getAttribute(EnumAttrType.href);
    return "<div class=\"paginator\" id=\"" + paginatorId + "\"></div>"
            + "<script type=\"text/javascript\">"
            + "paginator_example = new Paginator("
            + "'" + paginatorId + "',"
            + countPages + ","
            + "10,"
            + page + ","
            + "'" + href + "',"
            + "'" + nameParameterPage + "'"
            + ");"
            + "</script>";
  }
  
}
