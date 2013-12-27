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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
        this.object = "dumpEnt";
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
        } else {
            String dumpPth = app.getDumpPath();
            String list[] = new File(dumpPth).list();
            AbsEnt table = rd.table("", "", "");
            table.setId("dumptb");
            rd.trTh(table, "Название");
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
                    AbsEnt hr = rd.href(mp, ho);
                    rd.tr(table, hr);
                }
            }
            str = table.render();

            str += "<script type='text/javascript'>$(document).ready(function()  {"
                    + "        $(\"#dumptb\").tablesorter(); "
                    + "    } "
                    + ");</script>";

        }
        return status;
    }
}
