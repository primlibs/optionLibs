/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.objects.PairController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import prim.model.FileExecutor;
import prim.service.ServiceFactory;
import warehouse.OptionsKeeper;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.StructureController;
import warehouse.pair.PairKeeper;
import web.FormOptionInterface;
import web.HrefOptionInterface;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.pair.Pair;
import web.pair.PairObject;
import web.pair.Sequence;

/**
 *
 * @author Pavel Rice
 */
public class ImportEnt extends OptionAbstract {

  public static PrintWriter out2;
  private String str = "";
  private List<String> errors = new ArrayList();
  private final String FILE_LIST_ACTION = "fileList";
  private final String ONE_FILE_ACTION = "oneFile";
  private final String UPLOAD_FILE_SPECACTION = "uploadFile";
  private final String UPLOAD_DATA_SPECACTION = "uploadData";
  private final String FILE_PATH = "csvFiles";

  private ImportEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "importEnt";
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static ImportEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new ImportEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    // действие "один файл":
    if (action.equals(ONE_FILE_ACTION)) {
      // если дана команда на загрузку данных
      // то загрузить данные
      // если отображение одного файла
      // показать сообщения
      // показать ошибку
      // то отобразить данные из одного файла
      // показать форму для загрузки данных
      // return
    }

    if (action.equals(FILE_LIST_ACTION)) {
      // действие "список файлов":
      // если загружен файл
      if (specAction.equals(UPLOAD_FILE_SPECACTION)) {
        // то загрузить его
        uploadFile();
      }
      // если нет никакого действия
      // показать сообщения
      // показать ошибки
      str += errors + "</br>";
      // то показать форму загрузки файла
      str += uploadForm();
      // и показать список существующих файлов
      str += fileList();
      // return
    }

    return status;
  }

  // методы отображения ----------------------------------------------------------------------------------------------------------------
  /**
   * форма загрузки файла
   *
   * @return
   */
  private String uploadForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.fileInput("file", null, "Выберите файл"), "");
    FormOptionInterface fo = rd.getFormOption();
    fo.setNoValidateRights();
    fo.setTitle("Загрузить файл CSV");
    fo.setAction(action);
    fo.setObject(object);
    fo.setSpecAction(UPLOAD_FILE_SPECACTION);
    AbsEnt form = rd.rightForm(inner, fo);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  private String fileList() throws Exception {
    // получить директорию, где хранятся файлы
    String result = "";
    String fileDir = getFileDir();
    // если директория не равна null
    if (fileDir != null) {
      // показать все файлы
      String list[] = new File(fileDir).list();
      for (String fileName : list) {
        Map<String, Object> mp = new HashMap<String, Object>();
        HrefOptionInterface ho = rd.getHrefOption();
        ho.setObject(object);
        ho.setAction(ONE_FILE_ACTION);
        ho.setName(fileName);
        ho.setTitle("Просмотреть файл");
        ho.setNoValidateRights();
        mp.put("fileName", fileName);
        AbsEnt hr = rd.href(mp, ho);
        result += hr.render() + "<br/>";
      }
      return result;
    } else {
      errors.add("Ошибка: не задано свойство DumpPath");
      return "";
    }
  }

  // методы работы с данными ----------------------------------------------------------------------------------------------------------
  /**
   * загрузить файл
   *
   * @return
   */
  private boolean uploadFile() throws Exception {
    Map<String, String> filesMap = (HashMap<String, String>) params.get("_FILEARRAY_");
    // если загружен файл
    if (filesMap.size() > 0) {
      File file = null;
      for (String path : filesMap.keySet()) {
        file = new File(path);
      }
      if (file != null) {
        // проверить, CSV ли это
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf("."));
        if (!extension.equals("csv")) {
          errors.add("Недопустимый формат файла. Файл должен быть формата csv");
          return false;
        }
        // проверить, задано ли свойство OptionCeeper - путь к дампам БД
        if (getFileDir() != null) {
          String path = getFileDir();
          File folder = new File(path);
          // если не создана папка хранения файлов
          if (!folder.exists()) {
            // создать папку
            folder.mkdir();
          }
          // сохранить файл на диске
          FileExecutor fileExec = new FileExecutor(file);
          fileExec.move(path);
        } else {
          errors.add("Ошибка: не задано свойство DumpPath");
          return false;
        }
      }
    }
    return true;
  }

  private String getFileDir() throws Exception {
    OptionsKeeper os = OptionsKeeper.getInstance(app.getOptionSingletonPath());
    if (os.getDumpPath() != null) {
      String path = os.getDumpPath() + "/" + FILE_PATH;
      return path;
    }
    return null;
  }

  /**
   * загрузить данные
   */
  private void upload() {
    // 
  }
}
