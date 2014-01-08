/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import com.csvreader.CsvReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.objects.CsvUploader;
import option.objects.PairController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import prim.model.FileExecutor;
import prim.modelStructure.Structure;
import prim.service.ServiceFactory;
import warehouse.OptionsKeeper;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.StructureController;
import warehouse.modelKeeper.ModelStructureKeeper;
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
  public final static String FILE_LIST_ACTION = "fileList";
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
      if (specAction.equals(UPLOAD_DATA_SPECACTION)) {
        uploadData();
      }
      str += errors + "<br/>";
      str += showOneFile();
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
      str += uploadFileForm();
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
  private String uploadFileForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.fileInput("file", null, "Выберите файл"), "");
    FormOptionInterface fo = rd.getFormOption();
    fo.setFormToUploadFiles(true);
    fo.setNoValidateRights();
    fo.setTitle("Загрузить файл CSV");
    fo.setAction(action);
    fo.setObject(object);
    fo.setSpecAction(UPLOAD_FILE_SPECACTION);
    AbsEnt form = rd.rightForm(inner, fo);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
  }

  /**
   * показать список файлов
   *
   * @return
   * @throws Exception
   */
  private String fileList() throws Exception {
    // получить директорию, где хранятся файлы
    String result = "";
    String fileDir = getFileDir();
    // если директория не равна null
    if (fileDir != null) {
      // показать все файлы
      String list[] = new File(fileDir).list();
      if (list != null) {
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
      }
      return result;
    } else {
      errors.add("Ошибка: не задано свойство DumpPath");
      return "";
    }
  }

  /**
   * форма загрузки данных в БД
   *
   * @param headers
   * @return
   * @throws Exception
   */
  private String uploadDataForm(String[] headers) throws Exception {
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    Map<String, Structure> structureMap = mss.getStructureMap();
    Map<String, Object> fieldNamesMap = new LinkedHashMap();
    fieldNamesMap.put("", "не выбрано");
    for (String modelName : structureMap.keySet()) {
      Structure struct = structureMap.get(modelName);
      // получить все несистемные модели
      if (!struct.isSystem()) {
        Set<String> fieldNamesSet = struct.getFieldsNames();
        // для каждой модели получить поля, кроме системных
        for (String fieldName : fieldNamesSet) {
          // каждое поле добавить в массив в формате "название модели":"название поля"
          String fullName = modelName + ":" + fieldName;
          fieldNamesMap.put(fullName, fullName);
        }
      }
    }
    // для каждого заголовка - выбор поля модели
    FormOptionInterface fo = rd.getFormOption();
    Map<AbsEnt, String> inner = new LinkedHashMap();
    int i = 0;
    for (String header : headers) {
      inner.put(rd.combo(fieldNamesMap, "", "column_" + i), header);
      i++;
    }
    fo.setAction(ONE_FILE_ACTION);
    fo.setObject(object);
    fo.setSpecAction(UPLOAD_DATA_SPECACTION);
    fo.setNoValidateRights();
    fo.setHorisontal(false);
    fo.setTitle("Загрузить данные в БД");
    AbsEnt uploadDataForm = rd.rightForm(inner, fo);
    return uploadDataForm.render();
  }

  /**
   * показать один файл
   *
   * @return
   */
  private String showOneFile() throws Exception {
    String result = "";
    if (params.get("fileName") != null) {
      String fileName = params.get("fileName").toString();
      // прочитать содержимое файла
      CsvReader reader = new CsvReader(new FileInputStream(new File(getFileDir() + "/" + fileName)), Charset.forName("UTF-8"));
      reader.setDelimiter(',');
      reader.readHeaders();
      String[] headers = reader.getHeaders();

      // вывести форму
      result += uploadDataForm(headers);

      AbsEnt table = rd.table("1", "5", "0");
      // вывести заголовки
      AbsEnt trHeader = rd.getFabric().get("tr");
      table.addEnt(trHeader);
      for (String header : headers) {
        rd.td(trHeader, header);
      }
      // вывести каждую строку файла
      while (reader.readRecord()) {
        int columnCount = reader.getColumnCount();
        AbsEnt tr = rd.getFabric().get("tr");
        table.addEnt(tr);
        for (int i = 0; i <= columnCount; i++) {
          rd.td(tr, reader.get(i));
        }
      }
      result += table.render();
      return result;
    } else {
      errors.add("Не передано название файла");
    }
    return "";
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
      String fileName = null;
      for (String path : filesMap.keySet()) {
        file = new File(path);
        fileName = filesMap.get(path);
      }
      if (file != null) {
        // проверить, CSV ли это
        String extension = fileName.substring(fileName.lastIndexOf("."));
        errors.add(fileName + " " + extension);
        if (!extension.equals(".csv")) {
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
          boolean ok = fileExec.move(path);
          if (ok) {
            ok = fileExec.rename(fileName);
          }
          if (!ok) {
            errors.add("Ошибка при сохранении файла: " + fileExec.getErrors());
            return false;
          }
        } else {
          errors.add("Ошибка: не задано свойство DumpPath");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * получить название директории с файлами
   *
   * @return
   * @throws Exception
   */
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
  private void uploadData() throws Exception {
    if (params.get("fileName") != null) {

      String fileName = params.get("fileName").toString();
      File csv = new File(getFileDir() + "/" + fileName);
      CsvUploader uploader = new CsvUploader(app, csv, params);
      boolean ok = uploader.uploadData();
      if (!ok) {
        errors.addAll(uploader.getErrors());
      }
    }
  }
}
