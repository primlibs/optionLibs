/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import option.Creator;
import option.objects.PairController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.prim.core.AbstractApplication;
import com.prim.core.db.ExecutorFabric;
import com.prim.core.db.QueryExecutor;
import com.prim.support.MyString;
import com.prim.support.primXml;
import com.prim.support.FileExecutor;
import com.prim.core.service.ServiceFactory;
import com.prim.core.warehouse.OptionsKeeper;
import com.prim.core.warehouse.WarehouseSingleton;
import com.prim.core.warehouse.controllerStructure.ControllerKeeper;
import com.prim.core.warehouse.controllerStructure.ControllerMethod;
import com.prim.core.warehouse.controllerStructure.ControllerService;
import com.prim.core.warehouse.controllerStructure.StructureController;
import com.prim.core.warehouse.cron.CronObject;
import com.prim.core.warehouse.cron.CronSingleton;
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
 * @author кот
 */
public class DumpEnt extends OptionAbstract {

  public static String message = "";
  private String str = "";

  private DumpEnt(AbstractApplication app, Render rd, String action, String specAction) {
    message = "";

    this.object = Creator.DUMP_OBJECT_NAME;
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static DumpEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new DumpEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;
    if (action.equals("getFile")) {
      if (MyString.NotNull(params.get("fileName"))) {
        File fl = new File(app.getDumpPath() + "/" + params.get("fileName"));
        if (fl.exists()) {
          FileExecutor fe = new FileExecutor(fl);
          fileContent = fe.readBytes();
          fileName = MyString.getString(params.get("fileName"));
        }
      }
    } else if (action.equals("getLastDump")) {
      Calendar cl = Calendar.getInstance();
      int year = cl.get(Calendar.YEAR);
      int month = cl.get(Calendar.MONTH);
      int day = cl.get(Calendar.DAY_OF_MONTH);
      OptionsKeeper ok = app.getKeeper().getOptionKeeper();
      backup.Backup bb = backup.Backup.getInstance();
      bb.setDbOpts(ok.getDbName(), ok.getDbUser(), ok.getDbPass());
      bb.setArhiveName(year + "_" + month + "_" + day + ".tar.bz2");
      //bb.setDumpDirectoryName(ok.getDumpPath(), year + "_" + month + "_" + day);
      bb.setDumpDirectoryName(ok.getDumpPath());
      bb.setSqlDumpName("dump.sql");
      bb.addConfigFile(ok.getAppUserDataConfigPath(), "pair.xml");
      bb.addConfigFile(ok.getAppUserDataConfigPath(), "systemModel.xml");
      bb.addConfigFile("/usr/local/" + app.getAppName(), "config.xml");
      str += ok.getAppUserDataConfigPath();
      bb.createBackup();
      str += "<br/>" + message + "<br/>";
      if (bb.getError().isEmpty()) {
        str += "дамп создан";
      } else {
        str += bb.getError();
      }
      //развернуть из дампа
    } else if (action.equals("uploadFile") && MyString.NotNull(params.get("fileName"))) {
      str += "vwe";
    } else if (action.equals("uploadFile")) {
      uploadFile();
    } else if (action.equals("fromDump")) {
      OptionsKeeper ok = app.getKeeper().getOptionKeeper();

      if (killBase(app.getConnection(), ok.getDbName()) == true) {
        backup.Backup bb = backup.Backup.getInstance();
        bb.setDbOpts(ok.getDbName(), ok.getDbUser(), ok.getDbPass());
        bb.setArhiveName(MyString.getString(params.get("fileName")));
        //bb.setDumpDirectoryName(ok.getDumpPath(), ok.getDumpPath());
        bb.setDumpDirectoryName(ok.getDumpPath());
        bb.setSqlDumpName("dump.sql");
        bb.addConfigFile(ok.getAppUserDataConfigPath(), "pair.xml");
        bb.addConfigFile(ok.getAppUserDataConfigPath(), "systemModel.xml");
        bb.loadDump();
        if (bb.getError().isEmpty()) {
          str += "дамп создан";
        } else {
          str += bb.getError();
        }
      }

    } else {

      Map<String, Object> mp1 = new HashMap<String, Object>();
      HrefOptionInterface ho1 = rd.getHrefOption();
      ho1.setObject(object);
      ho1.setAction("getLastDump");
      ho1.setNoValidateRights();
      ho1.setName("Получить последний дамп");
      AbsEnt hr1 = rd.href(new HashMap(), ho1);
      str += hr1.render();

      str += uploadFileForm();

      String dumpPth = app.getDumpPath();
      String list[] = new File(dumpPth).list();
      AbsEnt table = rd.table("", "", "");
      table.setId("dumptb");
      rd.trTh(table, "Название", "");
      if (list != null) {
        for (int i = 0; i < list.length; i++) {
          File fl = new File(dumpPth + "/" + list[i]);
          if (!fl.isDirectory()) {
            Map<String, Object> mp = new HashMap<String, Object>();
            HrefOptionInterface ho = rd.getHrefOption();
            ho.setObject(object);
            ho.setAction("getFile");
            ho.setName(list[i]);
            ho.setTitle("Скачать");
            ho.setNoValidateRights();
            mp.put("fileName", list[i]);
            mp.put("getFile", "1");
            AbsEnt hr = rd.href(mp, ho);

            Map<String, Object> mp3 = new HashMap<String, Object>();
            HrefOptionInterface ho3 = rd.getHrefOption();
            ho3.setObject(object);
            ho3.setAction("fromDump");
            ho3.setName("Развернуть");
            ho3.setTitle("Развернуть");
            ho3.setNoValidateRights();
            mp3.put("fileName", list[i]);
            AbsEnt hr3 = rd.href(mp3, ho3);

            rd.tr(table, hr, hr3);
          }
        }
      }
      str += table.render();

      str += "<script type='text/javascript'>$(document).ready(function()  {"
              + "        $(\"#dumptb\").tablesorter(); "
              + "    } "
              + ");</script>";

    }
    return status;
  }

  private String uploadFileForm() throws Exception {
    Map<AbsEnt, String> inner = new LinkedHashMap();
    inner.put(rd.fileInput("file", null, "Выберите файл"), "");
    FormOptionInterface fo = rd.getFormOption();
    fo.setFormToUploadFiles(true);
    fo.setNoValidateRights();
    fo.setTitle("Загрузить файл дампа");
    fo.setAction("uploadFile");
    fo.setObject(object);
    AbsEnt form = rd.rightForm(inner, fo);
    form.setAttribute(EnumAttrType.style, "");
    return form.render();
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

        if (!extension.equals(".bz2")) {
          str += "Недопустимый формат файла. Файл должен быть формата .tar.bz2 передан " + extension;
          return false;
        }
        // проверить, задано ли свойство OptionCeeper - путь к дампам БД
        if (app.getDumpPath() != null) {
          String path = app.getDumpPath();
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
            str += "Ошибка при сохранении файла: " + fileExec.getErrors();
            return false;
          }
        } else {
          str += "Ошибка: не задано свойство DumpPath";
          return false;
        }
      }
    }
    return true;
  }

  private boolean killBase(Connection cn, String dbname) {
    Boolean res = true;
    try {
      cn.setAutoCommit(false);
      String query = " drop database " + dbname + ";";
      QueryExecutor ex1 = ExecutorFabric.getExecutor(cn, query);
      ex1.update();
      if (!ex1.getError().isEmpty()) {
        str += ex1.getError();
        res = false;
      }
      if (res == true) {
        cn.commit();
        cn.setAutoCommit(true);
      }
    } catch (Exception exc) {
      str += MyString.getStackExeption(exc);
      res = false;
    }

    if (res) {
      try {
        String query = "create database " + dbname;
        Statement stat = cn.createStatement();
        stat.executeUpdate(query);
      } catch (Exception exc) {
        str += MyString.getStackExeption(exc);
        res = false;
      }
    }

    return res;
  }
  /*
   private Boolean killTable(Connection cn, String dbname) {
   Boolean res = true;

   QueryExecutor ex = ExecutorFabric.getExecutor(cn, " show tables;");
   try {
   cn.setAutoCommit(false);
   ex.select();
   if (ex.getError().isEmpty()) {
   List<Map<String, Object>> resList = ex.getResultList();
   for (Map<String, Object> mp : resList) {
   String query = " drop table " + mp.get("Tables_in_" + dbname) + ";";
   QueryExecutor ex1 = ExecutorFabric.getExecutor(cn, query);
   ex1.update();
   if (!ex1.getError().isEmpty()) {
   str += ex1.getError();
   res = false;
   }
   }
   } else {
   str += ex.getError();
   res = false;
   }
   if (res == true) {
   cn.commit();
   cn.setAutoCommit(true);
   }
   } catch (Exception exc) {
   str += MyString.getStackExeption(exc);
   res = false;
   }
   return res;
   }
   */
}
