/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.ents;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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
import prim.AbstractApplication;
import prim.libs.MyString;
import prim.libs.primXml;
import prim.model.FileExecutor;
import prim.service.ServiceFactory;
import warehouse.OptionsKeeper;
import warehouse.WarehouseSingleton;
import warehouse.controllerStructure.ControllerKeeper;
import warehouse.controllerStructure.ControllerMethod;
import warehouse.controllerStructure.ControllerService;
import warehouse.controllerStructure.StructureController;
import warehouse.cron.CronObject;
import warehouse.cron.CronSingleton;
import warehouse.pair.PairKeeper;
import web.HrefOptionInterface;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;
import web.pair.Pair;
import web.pair.PairObject;
import web.pair.Sequence;

/**
 *
 * @author кот
 */
public class DumpEnt extends OptionAbstract {

    private String str = "";

    private DumpEnt(AbstractApplication app, Render rd, String action, String specAction) {
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
            bb.setDumpDirectoryName(ok.getDumpPath(), year + "_" + month + "_" + day);
            bb.setSqlDumpName("dump_" + year + "_" + month + "_" + day + ".sql");
            bb.addConfigFile(ok.getAppUserDataConfigPath(), "pair.xml");
            bb.addConfigFile(ok.getAppUserDataConfigPath(), "systemModel.xml");
            bb.createBackup();
            if (bb.getError().isEmpty()) {
                str += "дамп создан";
            } else {
                str += bb.getError();
            }
        //развернуть из дампа
        }else if (action.equals("fromDump")){
            
        } else {

            Map<String, Object> mp1 = new HashMap<String, Object>();
            HrefOptionInterface ho1 = rd.getHrefOption();
            ho1.setObject(object);
            ho1.setAction("getLastDump");
            ho1.setNoValidateRights();
            ho1.setName("Получить последний дамп");
            AbsEnt hr1 = rd.href(new HashMap(), ho1);
            str += hr1.render();
            
            
            Map<String, Object> mp2 = new HashMap<String, Object>();
            HrefOptionInterface ho2 = rd.getHrefOption();
            ho1.setObject(object);
            ho1.setAction("fromDump");
            ho1.setNoValidateRights();
            ho1.setName("Развернуть из дампа");
            AbsEnt hr2 = rd.href(new HashMap(), ho2);
            str += hr2.render();
            

            String dumpPth = app.getDumpPath();
            String list[] = new File(dumpPth).list();
            AbsEnt table = rd.table("", "", "");
            table.setId("dumptb");
            rd.trTh(table, "Название","");
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
                        AbsEnt hr3 = rd.href(mp, ho);
 
                        rd.tr(table, hr,hr3);
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
    
    
}
