/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.modelEnts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import option.Creator;
import option.ents.ModelEnt;
import option.objects.Valids;
import prim.AbstractApplication;
import prim.filterValidator.entity.ValidatorAbstract;
import prim.libs.MyString;
import prim.modelStructure.Field;
import prim.modelStructure.Structure;
import prim.modelStructure.Unique;
import warehouse.modelKeeper.ModelStructureKeeper;
import warehouse.modelKeeper.ModelStructureManager;
import web.Render;
import web.fabric.AbsEnt;
import web.fabric.EnumAttrType;

/**
 *
 * @author User
 */
public class AddValidator extends ModelEnt {

  private String str = "";
  TreeMap<String, Object> relationsMap = new TreeMap<String, Object>();
  List<String> errors = new ArrayList();

  public AddValidator(AbstractApplication app, Render rd, String action, String specAction) {
    super(app, rd, action, specAction);
  }

  @Override
  public String render() throws Exception {

       try {

      String structureAlias = (params.get("structureAlias") != null ? params.get("structureAlias").toString().trim() : "");
      String fieldAlias = (params.get("fieldAlias") != null ? params.get("fieldAlias").toString().trim() : "");

      Structure struct = null;
      Field field = null;
      String content = "";
      ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();

      if (structureAlias != null & fieldAlias != null) {
        if (mss.hasStructure(structureAlias)) {
          struct = mss.getStructure(structureAlias);
          field = struct.getField(fieldAlias);
        }
      }

      if (field != null) {

        LinkedHashMap<String, Object> combo = new LinkedHashMap<String, Object>();
        for (Valids validator : getValidarors()) {
          combo.put(validator.getName(), validator.getAlias());
        }

        LinkedHashMap<AbsEnt, String> hs = new LinkedHashMap<AbsEnt, String>();
        hs.put(rd.combo(combo, null, "validatorName"), "Валидатор");
        hs.put(rd.hiddenInput("action", action), "");
        hs.put(rd.hiddenInput("object", object), "");
        AbsEnt form = rd.verticalForm(hs, "Изменить", "images/ok.png");
        form.setAttribute(EnumAttrType.action, "/option");
        content = form.render();
      } else {
        content = "Не найдено поля структуры с таким именем";
      }

      str += ("<a href='AllStructure'>Перейти к списку всех моделей</a></br>");
      str += ("<a href='OneStructure?structureAlias=" + structureAlias + "'>Перейти к модели " + structureAlias + "</a>");
      str += ("<h1>Добавить валидатор к полю</h1>");
      str += ("<h2>Модель: " + structureAlias + ", поле: " + fieldAlias + "</h2>");
      if (!errors.isEmpty()) {
        str += (errors);
      }

      str += (content);

    } catch (Exception e) {
      str += MyString.getStackExeption(e);
    }
    
    return str;
  }

  @Override
  public Boolean run() throws Exception {
    boolean status = true;

    String structureAlias = (params.get("structureAlias") != null ? params.get("structureAlias").toString().trim() : "");
    String fieldAlias = (params.get("fieldAlias") != null ? params.get("fieldAlias").toString().trim() : "");

    Structure struct = null;
    Field field = null;
    ModelStructureKeeper mss = app.getKeeper().getModelStructureKeeper();
    if (structureAlias != null & fieldAlias != null) {
      if (mss.hasStructure(structureAlias)) {
        struct = mss.getStructure(structureAlias);
        field = struct.getField(fieldAlias);
      }
    }

    if (field != null) {

      // добавить валидатор
      if (params.get("submit") != null) {
        String validatorName = (params.get("validatorName") != null ? params.get("validatorName").toString().trim() : "");
        ModelStructureManager manager = new ModelStructureManager(app);
        int idx = manager.addValidator(struct, fieldAlias, validatorName);
        if (idx > -1) {
          isRedirect = true;
          redirectObject = Creator.MODEL_OBJECT_NAME;
          redirectAction = "ChangeValidator";
          redirectParams.put("structureAlias", structureAlias);
          redirectParams.put("fieldAlias", fieldAlias);
          redirectParams.put("validatorId", idx);
        } else {
          errors.addAll(manager.getErrors());
        }
      }
    }

    return status;
  }
  
   private  ArrayList<Valids> getValidarors() {
    ArrayList<Valids> res = new ArrayList<Valids>();
    res.add(new Valids("DateCompareValidator", "DateCompareValidator"));
    res.add(new Valids("DateFormatValidator", "DateFormatValidator"));
    res.add(new Valids("DateToFormatFilter", "DateToFormatFilter"));
    res.add(new Valids("DecimalFilter", "DecimalFilter"));
    res.add(new Valids("DecimalValidator", "DecimalValidator"));
    res.add(new Valids("DigitsFilter", "DigitsFilter"));
    res.add(new Valids("DigitsValidator", "DigitsValidator"));
    res.add(new Valids("NumToWordsFilter", "NumToWordsFilter"));
    res.add(new Valids("PhoneFilter", "PhoneFilter"));
    res.add(new Valids("QuantityValidator", "QuantityValidator"));
    res.add(new Valids("StringLenghtValidator", "StringLenghtValidator"));
    res.add(new Valids("StringRusValidator", "StringRusValidator"));
    res.add(new Valids("ValidatorAbstract", "ValidatorAbstract"));
    res.add(new Valids("WorkingHoursFilter", "WorkingHoursFilter"));
    res.add(new Valids("PfFilter", "PfFilter"));
    res.add(new Valids("InnValidator", "InnValidator"));
    res.add(new Valids("MailValidator", "MailValidator"));
    return res;
  }
  
}
