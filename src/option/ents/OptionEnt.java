/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import option.ents.OptionAbstract;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import warehouse.OptionsKeeper;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.AbstractWebFabric;
import web.fabric.EnumAttrType;

/**
 *
 * @author кот
 */
class OptionEnt extends OptionAbstract {

  private OptionEnt(AbstractApplication app, Render rd, String action, String specAction) {
    this.object = "optionEnt";
    setApplication(app);
    setRender(rd);
    this.action = MyString.getString(action);
    this.specAction = MyString.getString(specAction);
  }

  static OptionEnt getInstance(AbstractApplication app, Render rd, String action, String specAction) {
    return new OptionEnt(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {
    OptionsKeeper os = app.getKeeper().getOptionKeeper();


    /*
    ents.add(new someEnt("Пользователь", "dbUser", os.getDbUser()));
    ents.add(new someEnt("Путь к файлам", "filePath", os.getFilePath()));
    ents.add(new someEnt("Временная директория для загружаемых файлов", "uploadPath", os.getUploadPath()));
    ents.add(new someEnt("Путь к bi", "biPath", os.getBiPath()));
    ents.add(new someEnt("Путь к рендерам", "renderPath", os.getRenderPath()));
    ents.add(new someEnt("Время жизни сессии, в минутах", "sessionLifeTime", os.getSessionLifeTime()));
    ents.add(new someEnt("Максимальный размер загружаемых файлов в мб", "maxUploadSizeMB", os.getMaxUploadSizeMB()));
    ents.add(new someEnt("Путь к дампам БД", "dumpPath", os.getDumpPath()));
    ents.add(new someEnt("Email для получения служебных сообщений", "emailNotification", os.getEmailNotification()));

* */
    Map <AbsEnt,String> inner= new LinkedHashMap<AbsEnt, String>();
    inner.put(rd.textInput("appConfigPath",  os.getAppConfigPath(), "Путь к конфигам приложения"), "Путь к конфигам приложения");
    inner.put(rd.textInput("appLocale",  os.getAppLocale(), "Язык локали"), "Язык локали");
    inner.put(rd.textInput("appLogPath",  os.getAppLogPath(), "Путь к логам приложения"), "Путь к логам приложения");
    inner.put(rd.textInput("appUserDataConfigPath",  os.getAppUserDataConfigPath(), "Путьк юзерским настройкам"), "Путьк юзерским настройкам");
    inner.put(rd.textInput("dbDriver",  os.getDbDriver(), "Имя драйвера"), "Имя драйвера");
    inner.put(rd.textInput("dbDriverUri",  os.getDbDriverUrl(), "Путь к драйверу"), "Путь к драйверу");
    inner.put(rd.textInput("dbEncoding",  os.getDbEncoding(), "кодировка БД"), "кодировка БД");
    inner.put(rd.textInput("dbHost",  os.getDbHost(), "Хост БД"), "Хост БД");
    inner.put(rd.textInput("dbName",  os.getDbName(), "Имя БД"), "Имя БД");
    inner.put(rd.textInput("dbPass",  os.getDbPass(), "БД пароль"), "БД пароль");
    inner.put(rd.textInput("dbUser",  os.getDbUser(), "Пользователь"), "Пользователь");
    inner.put(rd.textInput("filePath",  os.getFilePath(), "Путь к файлам"), "Путь к файлам");
    inner.put(rd.textInput("uploadPath",  os.getUploadPath(), "Временная директория для загружаемых файлов"), "Временная директория для загружаемых файлов");
    inner.put(rd.textInput("biPath",  os.getBiPath(), "Путь к bi"), "Путь к bi");
    inner.put(rd.textInput("renderPath",  os.getRenderPath(), "Путь к рендерам"), "Путь к рендерам");
    inner.put(rd.textInput("sessionLifeTime",  os.getSessionLifeTime(), "Время жизни сессии, в минутах"), "Время жизни сессии, в минутах");
    inner.put(rd.textInput("maxUploadSizeMB",  os.getMaxUploadSizeMB(), "Максимальный размер загружаемых файлов в мб"), "Максимальный размер загружаемых файлов в мб");
    inner.put(rd.textInput("dumpPath",  os.getDumpPath(), "Путь к дампам БД"), "Путь к дампам БД");
    inner.put(rd.textInput("emailNotification",  os.getEmailNotification(), "Email для получения служебных сообщений"), "Email для получения служебных сообщений");
    
    AbsEnt form=rd.rightForm(false,object,"refresh", null, inner, "Подтвердить изменения", rd.getRenderConstant().OK_IMGPH, false).setAttribute(EnumAttrType.action, "./option");
    return form.render() + "Ошибки:" + os.getError();
  }

  @Override
  public Boolean run() throws Exception {
    OptionsKeeper os = app.getKeeper().getOptionKeeper();

    if (action.equals("refresh")) {
      String appConfigPath = params.get("appConfigPath").toString();
      String appLocale = params.get("appLocale").toString();
      String appLogPath = params.get("appLogPath").toString();
      String appUserDataConfigPath = params.get("appUserDataConfigPath").toString();
      String dbDriver = params.get("dbDriver").toString();
      String dbDriverUri = params.get("dbDriverUri").toString();
      String dbEncoding = params.get("dbEncoding").toString();
      String dbHost = params.get("dbHost").toString();
      String dbName = params.get("dbName").toString();
      String dbPass = params.get("dbPass").toString();
      String dbUser = params.get("dbUser").toString();
      String filePath = params.get("filePath").toString();
      String biPath = params.get("biPath").toString();
      String renderPath = params.get("renderPath").toString();
      Integer maxUploadSizeMB = Integer.parseInt(params.get("maxUploadSizeMB").toString());
      Integer sessionLifeTime = Integer.parseInt(params.get("sessionLifeTime").toString());
      String uploadPath = params.get("uploadPath").toString();
      String dumpPath = params.get("dumpPath").toString();
      String emailNotification = params.get("emailNotification").toString();

      OptionsKeeper os1 = OptionsKeeper.getInstance(dbDriver, dbDriverUri, dbName, dbHost, dbUser,
              dbPass, dbEncoding, appLogPath, appLocale, appUserDataConfigPath,
              filePath, sessionLifeTime, biPath, dumpPath, emailNotification, renderPath,
              appConfigPath, maxUploadSizeMB, uploadPath);

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();
      Element root = doc.createElement("root");
      doc.appendChild(root);
      Element cmet = primXml.createEmptyElement(doc, root, "OptionsKeeper");
      os1.getSelfInXml(doc, cmet);


      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);
      StreamResult res = new StreamResult(params.get("appConfigPath").toString());
      transformer.transform(source, res);
    }
    return true;
  }
}
