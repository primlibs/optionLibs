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
import com.prim.core.AbstractApplication;
import com.prim.support.MyString;
import com.prim.support.primXml;
import com.prim.support.FileExecutor;
import com.prim.core.modelStructure.Structure;
import com.prim.core.service.ServiceFactory;
import com.prim.core.warehouse.OptionsKeeper;
import com.prim.core.warehouse.WarehouseSingleton;
import com.prim.core.warehouse.controllerStructure.ControllerKeeper;
import com.prim.core.warehouse.controllerStructure.StructureController;
import com.prim.core.warehouse.modelKeeper.ModelStructureKeeper;
import com.prim.core.warehouse.pair.PairKeeper;
import com.prim.web.FormOptionInterface;
import com.prim.web.HrefOptionInterface;
import com.prim.web.Render;
import com.prim.web.fabric.AbsEnt;
import com.prim.web.fabric.EnumAttrType;
import com.prim.core.pair.Pair;
import com.prim.core.pair.PairObject;
import com.prim.core.pair.Sequence;

/**
 *
 * @author Pavel Rice
 */
public class ImportEnt extends OptionAbstract {

  private String str = "";
  private List<String> errors = new ArrayList();
  public final static String FILE_LIST_ACTION = "fileList";
  private final String ONE_FILE_ACTION = "oneFile";
  private final String DELETE_FILE_SPECACTION = "deleteFile";
  private final String UPLOAD_FILE_SPECACTION = "uploadFile";
  private final String UPLOAD_DATA_SPECACTION = "uploadData";
  private final String DOWNLOAD_FILE_SPECACTION = "downloadFile";
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

    // просмотр одного файла
    if (action.equals(ONE_FILE_ACTION)) {
      if (specAction.equals(UPLOAD_DATA_SPECACTION)) {
        // загрузить данные на сервер
        uploadData();
      }
      str += errors + "<br/>";
      str += showOneFile();
    }

    // просмотр списка файлов
    if (action.equals(FILE_LIST_ACTION)) {
      if (specAction.equals(UPLOAD_FILE_SPECACTION)) {
        uploadFile();
      } else if (specAction.equals(DELETE_FILE_SPECACTION)) {
        deleteFile();
      } else if (specAction.equals(DOWNLOAD_FILE_SPECACTION)) {
        downloadFile();
        return true;
      }
      str += errors + "<br/>";
      str += uploadFileForm();
      str += fileList();
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
          result += hr.render() + deleteFileForm(fileName) + downloadFileForm(fileName) + "<br/><br/>";
        }
      }
      return result;
    } else {
      errors.add("Ошибка: не задано свойство DumpPath");
      return "";
    }
  }

  /**
   * форма удаления файла
   *
   * @param fileName
   * @return
   * @throws Exception
   */
  private String deleteFileForm(String fileName) throws Exception {
    FormOptionInterface fo = rd.getFormOption();
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.hiddenInput("fileName", fileName), "");
    fo.setAction(FILE_LIST_ACTION);
    fo.setObject(object);
    fo.setSpecAction(DELETE_FILE_SPECACTION);
    fo.setNoValidateRights();
    fo.setHorisontal(false);
    fo.setTitle("Удалить файл");
    AbsEnt form = rd.rightForm(inner, fo);
    return form.render();
  }

  /**
   * форма скачивания файла
   *
   * @param fileName
   * @return
   * @throws Exception
   */
  private String downloadFileForm(String fileName) throws Exception {
    FormOptionInterface fo = rd.getFormOption();
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.hiddenInput("fileName", fileName), "");
    inner.put(rd.hiddenInput("getFile", "1"), "");
    fo.setAction(FILE_LIST_ACTION);
    fo.setObject(object);
    fo.setSpecAction(DOWNLOAD_FILE_SPECACTION);
    fo.setNoValidateRights();
    fo.setHorisontal(false);
    fo.setTitle("Скачать файл");
    AbsEnt form = rd.rightForm(inner, fo);
    return form.render();
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
    Map<String, Object> fieldNamesMap = new TreeMap();
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
    inner.put(rd.hiddenInput("fileName", params.get("fileName")), "");
    int i = 0;
    for (String header : headers) {
      inner.put(rd.combo(fieldNamesMap, params.get("column_" + i), "column_" + i), header);
      inner.put(rd.checkBox("column_old_id_" + i, params.get("column_old_id_" + i)), header + " найти old_id");
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
    CsvReader reader = null;
    try {
      if (params.get("fileName") != null) {
        String fileName = params.get("fileName").toString();
        // прочитать содержимое файла
        reader = new CsvReader(new FileInputStream(new File(getFileDir() + "/" + fileName)), Charset.forName("UTF-8"));
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
    } finally {
      if (reader != null) {
        reader.close();
      }
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
   * загрузить данные из файла в БД
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
      errors.addAll(uploader.getMessages());
    } else {
      errors.add("Ошибка: не передано имя файла");
    }
  }

  /**
   * удалить файл
   */
  private void deleteFile() throws Exception {
    if (params.get("fileName") != null) {
      String fileName = params.get("fileName").toString();
      File csv = new File(getFileDir() + "/" + fileName);
      csv.delete();
    } else {
      errors.add("Ошибка: не передано имя файла");
    }
  }

  /**
   * скачать файл
   */
  private void downloadFile() throws Exception {
    if (params.get("fileName") != null) {
      String name = params.get("fileName").toString();
      File csv = new File(getFileDir() + "/" + name);
      FileExecutor exec = new FileExecutor(csv);
      fileContent = exec.readBytes();
      fileName = name;
    } else {
      errors.add("Ошибка: не передано имя файла");
    }
  }
}
