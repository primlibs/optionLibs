/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.util.ArrayList;
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
    class someEnt {

      public String val1 = "";
      public String val2 = "";
      public Object val3 = "";

      public someEnt(String val1, String val2, Object val3) {
        this.val1 = val1;
        this.val2 = val2;
        this.val3 = val3;
      }
    }
    OptionsKeeper os = app.getKeeper().getOptionKeeper();
    ArrayList<someEnt> ents = new ArrayList<someEnt>();
    ents.add(new someEnt("Путь к конфигам приложения", "appConfigPath", os.getAppConfigPath()));
    ents.add(new someEnt("Язык локали", "appLocale", os.getAppLocale()));
    ents.add(new someEnt("Путь к логам приложения", "appLogPath", os.getAppLogPath()));
    ents.add(new someEnt("Путьк юзерским настройкам", "appUserDataConfigPath", os.getAppUserDataConfigPath()));
    ents.add(new someEnt("Имя драйвера", "dbDriver", os.getDbDriver()));
    ents.add(new someEnt("Путь к драйверу", "dbDriverUri", os.getDbDriverUrl()));
    ents.add(new someEnt("кодировка ДБ", "dbEncoding", os.getDbEncoding()));
    ents.add(new someEnt("Хост дб", "dbHost", os.getDbHost()));
    ents.add(new someEnt("Имя дб", "dbName", os.getDbName()));
    ents.add(new someEnt("ДБ пароль", "dbPass", os.getDbPass()));
    ents.add(new someEnt("Пользователь", "dbUser", os.getDbUser()));
    ents.add(new someEnt("Путь к файлам", "filePath", os.getFilePath()));
    ents.add(new someEnt("Временная директория для загружаемых файлов", "uploadPath", os.getUploadPath()));
    ents.add(new someEnt("Путь к bi", "biPath", os.getBiPath()));
    ents.add(new someEnt("Путь к рендерам", "renderPath", os.getRenderPath()));
    ents.add(new someEnt("Время жизни сессии, в минутах", "sessionLifeTime", os.getSessionLifeTime()));
    ents.add(new someEnt("Максимальный размер загружаемых файлов в мб", "maxUploadSizeMB", os.getMaxUploadSizeMB()));
    ents.add(new someEnt("Путь к дампам БД", "dumpPath", os.getDumpPath()));
    ents.add(new someEnt("Email для получения служебных сообщений", "emailNotification", os.getEmailNotification()));


    AbstractWebFabric cf = rd.getFabric();
    AbsEnt form = rd.form("./option");
    AbsEnt table = cf.get("table");
    form.addEnt(table);
    for (someEnt se : ents) {
      AbsEnt tr = cf.get("tr");
      AbsEnt td1 = cf.get("td");
      AbsEnt td2 = cf.get("td");
      tr.addEnt(td1);
      tr.addEnt(td2);
      td2.addEnt(rd.textInput(se.val2, se.val3, se.val2));
      td1.setValue(se.val1);
      table.addEnt(tr);
    }
    AbsEnt tr = cf.get("tr");
    AbsEnt td1 = cf.get("td");
    AbsEnt td2 = cf.get("td");
    tr.addEnt(td1);
    tr.addEnt(td2);
    AbsEnt in = rd.submitInput("submit", "Отправить");  
    td1.addEnt(in);
    table.addEnt(tr);

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
