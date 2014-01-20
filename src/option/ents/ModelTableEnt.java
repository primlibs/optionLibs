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
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.model.DinamicModel;
import prim.modelStructure.Structure;
import web.Render;

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
  public final static String TABLE_ACTION = "modelTable";
  public final static String ONE_MODEL_ACTION = "oneModel";
  public final static String ADD_SPECACTION = "addModel";
  public final static String CHANGE_SPECACTION = "changeModel";
  public final static String CLOSE_SPECACTION = "deleteModel";
  // параметры name и page получают такие значения, для того чтобы не было случайных совпадений названий этих параметров с названиями параметров моделей
  public final static String NAME_PARAMETER = "12345670993name";
  public final static String PAGE_PARAMETER = "14324783933page";

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

    if (action.equals(TABLE_ACTION)) {
      return modelTable();
    } else if (action.equals(ONE_MODEL_ACTION)) {
      return oneModel();
    }

    return status;
  }

  /**
   * таблица с моделями
   */
  private boolean modelTable() throws Exception {
    Map<String, Structure> serviceMap = service.getModelList();
    str = render.renderModelTable(serviceMap, service.getErrors());
    return true;
  }

  /**
   * просмотр одной модели
   */
  private boolean oneModel() throws Exception {
    if (MyString.NotNull(params.get(NAME_PARAMETER))) {
      String modelName = params.get(NAME_PARAMETER).toString();
      // если specaction - добавить
      if (!specAction.isEmpty()) {
        if (specAction.equals(ADD_SPECACTION)) {
          // сохранить модель
          service.addModel(params, modelName);
          // если нет ошибок
        } else if (specAction.equals(CHANGE_SPECACTION)) {
          service.changeModel(params, modelName);
        } else if (specAction.equals(CLOSE_SPECACTION)) {
          service.closeModel(params, modelName);
        }
        if (service.getErrors().isEmpty()) {
          // перенаправить
          isRedirect = true;
          redirectObject = object;
          redirectAction = action;
          redirectSpecAction = "";
          return true;
          // return
        } 
      }

      // получить список моделей
      List<DinamicModel> modelList = service.getOneModelData(modelName, params.get(PAGE_PARAMETER));
      // получить структуру, соотв. модели
      Structure structure = service.getStructure(modelName);
      int countPages = service.getCountPages(modelName);
      errors.addAll(service.getErrors());
      render.setRequestParams(params);
      str += render.renderOneModel(modelList, errors, structure, countPages, params.get(PAGE_PARAMETER));
    } else {
      errors.add("Ошибка: не передано имя модели");
      str += errors;
      return false;
    }
    // передать в рендер
    return true;
  }
  // методы отображения ----------------------------------------------------------------------------------------------------------------
  // методы работы с данными ----------------------------------------------------------------------------------------------------------
}
