/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents.renders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.prim.web.HrefOptionInterface;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;
import com.prim.web.objects.Parameter;

/**
 *
 * @author Pavel Rice
 */
public class OptionRender {

  protected String action;
  protected String object;
  protected Render rd;
  protected Map<String, Object> requestParams = new HashMap();

  public void setRequestParams(Map<String, Object> requestParams) {
    this.requestParams = requestParams;
  }

  public OptionRender(Render render, String object, String action) {
    this.rd = render;
    this.object = object;
    this.action = action;
  }

  protected String href(String object, String action, String specAction, String name, Map<String, Object> params) throws Exception {
    return rd.href(object, action, specAction, params, name, false).render();
  }

  protected String href(String object, String action, String specAction, String name, Map<String, Object> params, String style, String js) throws Exception {
    AbsEnt href = rd.href(object, action, specAction, params, name, false);
    if (style != null && !style.isEmpty()) {
      href.setAttribute(EnumAttrType.style, style);
    }
    if (js != null && !js.isEmpty()) {
      href.setJs(js);
    }
    return href.render();
  }

  /**
   * создает пагинатор
   *
   * @param page активная страница
   * @param countPages количество страниц
   * @param object параметр object для каждой ссылки пагинатора
   * @param action параметр action для каждой ссылки пагинатора
   * @param params прочие параметры для каждой ссылки пагинатора
   * @return
   * @throws Exception
   */
  public AbsEnt paginator(int page, int countPages, String object, String action, Map<String, Object> params, String pageParameterName) throws Exception {
    List<Parameter> paramsList = new ArrayList();
    for (String name : params.keySet()) {
      paramsList.add(new Parameter(name, params.get(name)));
    }
    return paginator(page, countPages, object, action, paramsList, pageParameterName);
  }
  
  protected String paginator3000(int page, int countPages, String object, String action, Map<String, Object> params, String nameParameterPage, String paginatorId) throws Exception {
    List<Parameter> paramsList = new ArrayList();
    for (String name : params.keySet()) {
      paramsList.add(new Parameter(name, params.get(name)));
    }
    return paginator3000(page, countPages, object, action, paramsList, nameParameterPage, paginatorId);
  }

  protected String paginator3000(int page, int countPages, String object, String action, List<Parameter> params, String nameParameterPage, String paginatorId) throws Exception {
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

  /**
   * создает пагинатор
   *
   * @param page активная страница
   * @param countPages количество страниц
   * @param object параметр object для каждой ссылки пагинатора
   * @param action параметр action для каждой ссылки пагинатора
   * @param params прочие параметры для каждой ссылки пагинатора
   * @return
   * @throws Exception
   */
  public AbsEnt paginator(int page, int countPages, String object, String action, List<Parameter> params, String nameParameterPage) throws Exception {
    if (nameParameterPage == null || nameParameterPage.isEmpty()) {
      nameParameterPage = "page";
    }
    AbsEnt div1 = rd.getFabric().get("div");
    if (countPages > 1) {

      AbsEnt div = rd.getFabric().get("div");
      div.setAttribute(EnumAttrType.style, "height: 20px;");
      div1.addEnt(div);

      AbsEnt ul = rd.getFabric().get("ul").addAttribute(EnumAttrType.id, "pagination-digg");
      div1.addEnt(ul);

      Parameter pageParameter = null;
      // вывести ссылку на предыдущую
      // если номер равен 1 то выводить не ссылку, а просто надпись
      AbsEnt li = rd.getFabric().get("li");


      if (page == 1) {
        li.setCss("previous-off");
        li.setValue("<< Previous");
      } else {
        li.setCss("previous");
        pageParameter = new Parameter(nameParameterPage, page - 1);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName("<< Previous");
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, "<< Previous", false, null, true);
        li.addEnt(href);
      }
      ul.addEnt(li);

      // если разница между номером и первой больше двух
      // вывести номер первой и троеточие
      if (page > 3) {
        AbsEnt li1 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, "1");
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName("1");
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, "1", false, null, true);
        li1.addEnt(href);
        ul.addEnt(li1);

        AbsEnt li2 = rd.getFabric().get("li");
        li2.setValue("...");
        li2.setCss("previous-off");
        ul.addEnt(li2);
      }

      // вывести два предыдущих номера, если они есть
      if (page > 2) {
        AbsEnt li3 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, page - 2);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName(page - 2);
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, page - 2, false, null, true);
        li3.addEnt(href);
        ul.addEnt(li3);
      }

      if (page > 1) {
        AbsEnt li4 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, page - 1);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName(page - 1);
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, page - 1, false, null, true);
        li4.addEnt(href);
        ul.addEnt(li4);
      }
      // вывести текущий номер
      AbsEnt li5 = rd.getFabric().get("li");
      li5.setCss("active");
      li5.setValue(page);
      ul.addEnt(li5);
      // вывести два следующих номера, если они есть
      if (countPages - page > 0) {
        AbsEnt li6 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, page + 1);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName(page + 1);
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, page + 1, false, null, true);
        li6.addEnt(href);
        ul.addEnt(li6);
      }

      if (countPages - page > 1) {
        AbsEnt li7 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, page + 2);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName(page + 2);
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, page + 2, false, null, true);
        li7.addEnt(href);
        ul.addEnt(li7);
      }
      // если разница между номером и последней больше двух
      // вывести троеточие и номер последней
      if (countPages - page > 2) {
        AbsEnt li8 = rd.getFabric().get("li");
        li8.setValue("...");
        li8.setCss("previous-off");
        ul.addEnt(li8);

        AbsEnt li9 = rd.getFabric().get("li");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, countPages);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName(countPages);
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, countPages, false, null, true);
        li9.addEnt(href);
        ul.addEnt(li9);
      }
      // вывести ссылку на следующую
      // если номер равен последнему то выводить не ссылку, а просто надпись
      AbsEnt li10 = rd.getFabric().get("li");
      if (page == countPages) {
        li10.setCss("next-off");
        li10.setValue("Next >>");
      } else {
        li10.setCss("next");
        params.remove(pageParameter);
        pageParameter = new Parameter(nameParameterPage, page + 1);
        params.add(pageParameter);

        HrefOptionInterface ho = rd.getHrefOption();
        ho.setAction(action);
        ho.setObject(object);
        ho.setSpecAction("");
        ho.setName("Next >>");
        ho.setNoValidateRights();
        AbsEnt href = rd.href(params, ho);
        //AbsEnt href = rd.href(object, action, "", params, "Next >>", false, null, true);
        li10.addEnt(href);
      }
      ul.addEnt(li10);

    }
    return div1;
  }
}
