/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import option.Creator;
import option.ents.renders.ModelTableRender;
import option.ents.services.ModelTableService;
import com.prim.core.AbstractApplication;
import com.prim.support.MyString;
import com.prim.core.model.DinamicModel;
import com.prim.core.modelStructure.Structure;
import com.prim.web.Render;

/**
 *
 * @author Pavel Rice
 */
public class ModelTableEnt extends OptionAbstract {

  public static PrintWriter out2;
  private String str = "";
  private List<String> errors = new ArrayList();
  ModelTableService service;
  ModelTableRender render;
  public final static String ACTION_TABLE = "modelTable";
  public final static String ACTION_ONE_MODEL = "oneModel";
  public final static String SPECACTION_ADD = "addModel";
  public final static String SPECACTION_CHANGE = "changeModel";
  public final static String SPECACTION_CLOSE = "deleteModel";
  public final static String SPECACTION_DOWNLOAD_CSV = "downloadCsv";
  // параметры name и page получают такие значения, для того чтобы не было случайных совпадений названий этих параметров с названиями параметров моделей
  public final static String PARAMETER_NAME = "name_12343394844";
  public final static String PARAMETER_PAGE = "page_12355433444";
  public final static String PARAMETER_COLUMN = "column_12355433444";

  private ModelTableEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = Creator.MODELTABLE_OBJECT_NAME;
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);

    this.service = new ModelTableService(app);
    this.render = new ModelTableRender(rd, this.object, this.action);
  }

  static ModelTableEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new ModelTableEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    if (action.equals(ACTION_TABLE)) {
      return modelTable();
    } else if (action.equals(ACTION_ONE_MODEL)) {
      return oneModel();
    }

    return status;
  }

  /**
   * таблица с моделями
   */
  private boolean modelTable() throws Exception {
    Map<String, Structure> serviceMap = service.getModelList();
    str = render.renderModelList(serviceMap, service.getErrors());
    return true;
  }

  
  /**
   * просмотр одной модели
   */
  private boolean oneModel() throws Exception {
    if (MyString.NotNull(params.get(PARAMETER_NAME))) {
      String modelName = params.get(PARAMETER_NAME).toString();

      if (specAction.equals(SPECACTION_ADD)) {
        service.addModel(params, modelName);
        if (service.getErrors().isEmpty()) {
          isRedirect = true;
          redirectObject = object;
          redirectAction = action;
          redirectSpecAction = "";
          redirectParams.put(PARAMETER_NAME, params.get(PARAMETER_NAME));
          return true;
        }
      } else if (specAction.equals(SPECACTION_CHANGE)) {
        service.changeModel(params, modelName);
        if (service.getErrors().isEmpty()) {
          isRedirect = true;
          redirectObject = object;
          redirectAction = action;
          redirectSpecAction = "";
          redirectParams.put(PARAMETER_NAME, params.get(PARAMETER_NAME));
          return true;
        }
      } else if (specAction.equals(SPECACTION_CLOSE)) {
        service.closeModel(params, modelName);
        if (service.getErrors().isEmpty()) {
          isRedirect = true;
          redirectObject = object;
          redirectAction = action;
          redirectSpecAction = "";
          redirectParams.put(PARAMETER_NAME, params.get(PARAMETER_NAME));
          return true;
        }
      } else if (specAction.equals(SPECACTION_DOWNLOAD_CSV)) {
        byte[] bytes = service.getCsvFile(modelName);
        if (service.getErrors().isEmpty()) {
          fileContent = bytes;
          fileName = modelName + ".csv";
          return true;
        } 
      }

      Object page;
      int countPages = 0;
      List<DinamicModel> modelList = new ArrayList();
      Structure structure = service.getStructure(modelName);
      // если передан ИД
      if (MyString.NotNull(params.get("id"))) {
        // получить номер страницы, который соотв. этому ИД
        page = service.getPageById(modelName, params.get("id"));
        errors.addAll(service.getErrors());
      } else {
        page = params.get(PARAMETER_PAGE);
      }
      // получить список моделей
      if (errors.isEmpty()) {
        modelList = service.getOneModelData(modelName, page, params.get(PARAMETER_COLUMN), null);
        // получить структуру, соотв. модели
        errors.addAll(service.getErrors());
        if (errors.isEmpty()) {
          // количество страниц
          countPages = service.getCountPages(modelName, null);
          errors.addAll(service.getErrors());
        }
      }
      render.setRequestParams(params);
      str += render.renderOneModel(modelList, errors, structure, countPages, page, params.get(PARAMETER_COLUMN));
    } else {
      errors.add("Ошибка: не передано имя модели");
      str += errors;
      return false;
    }
    return true;
  }
}
