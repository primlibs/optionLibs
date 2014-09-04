/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.objects;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.prim.core.AbstractApplication;
import com.prim.core.warehouse.pair.PairKeeper;
import com.prim.core.pair.Pair;
import com.prim.core.pair.PairObject;
import com.prim.core.pair.Sequence;
import com.prim.core.pair.SequenceObject;

/**
 *
 * @author Новый профиль
 */
public class PairController {

  private ArrayList<String> errors = new ArrayList<String>();
  private PairKeeper ps;

  public PairController(AbstractApplication app) throws Exception {
    ps = app.getKeeper().getPairKeeper();
  }

  public ArrayList<String> getErrors() {
    return errors;
  }

  public boolean addNewPair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String newPairObject = params.get("newPairObject");
    String newPairAction = params.get("newPairAction");
    String webController = params.get("webController");
    String pairObject = params.get("pairObject");
    String pairAction = params.get("pairAction");
    if (checkParams(newPairObject, newPairAction, webController, pairObject, pairAction)) {
      Pair pair = ps.searchOnePair(pairObject, pairAction);
      if (pair != null) {
        if (!ps.containsPair(newPairObject, newPairAction)) {
          
          Sequence s = SequenceObject.getInstance("default", "", "", "", "", "", "", "", "");
          Map<String, Sequence> seqMap = new HashMap();
          seqMap.put(s.getName(), s);
          
          pair.addPair(PairObject.getInstance(newPairObject, newPairAction, false, seqMap, null, pair, true, webController));
          
          status = ps.SaveCollectionInFile();
        } else {
          errors.add("Пара с именем " + newPairObject + " и методом " + newPairAction + " уже существует");
        }
      }
    }
    return status;
  }
  
  

  // добавить пару
  public boolean addPair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);

    String object = params.get("objectName");
    String action = params.get("actionName");
    String newObject = params.get("newObject");
    String newAction = params.get("newAction");
    String def = params.get("def");

    String objectMethod = params.get("objectMethod");
    String trueRender = params.get("trueRender");
    String falseRender = params.get("falseRender");
    String trueRedirect = params.get("trueRedirect");
    String falseRedirect = params.get("falseRedirect");

    if (checkParams(object, action, newObject, newAction, objectMethod,
            trueRender, falseRender, trueRedirect, falseRedirect)) {
      Pair pair = ps.searchOnePair(object, action);
      if (pair != null) {
        if (!ps.containsPair(newObject, newAction)) {
          trueRender = (!trueRender.equals("0") ? trueRender : null);
          falseRender = (!falseRender.equals("0") ? falseRender : null);
          trueRedirect = (!trueRedirect.equals("0") ? trueRedirect : null);
          falseRedirect = (!falseRedirect.equals("0") ? falseRedirect : null);

          String objectName = null;
          String methodName = null;
          String[] str = objectMethod.split(":");
          if (str.length == 2) {
            objectName = str[0];
            methodName = str[1];
          }

          Sequence s = SequenceObject.getInstance("default", objectName, methodName, trueRender, falseRender, trueRedirect, falseRedirect, trueRedirect, falseRedirect);
          Map<String, Sequence> seqMap = new HashMap();
          seqMap.put(s.getName(), s);
          pair.addPair(PairObject.getInstance(newObject, newAction, (def != null ? true : false), seqMap, null, pair, false, ""));

          status = ps.SaveCollectionInFile();
        } else {
          errors.add("Пара с именем " + newObject + " и методом " + newAction + " уже существует");
        }
      }
    }
    return status;
  }

  // изменить пару
  public boolean changePair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("objectName");
    String action = params.get("actionName");
    String def = params.get("def");
    if (checkParams(object, action)) {
      Pair pair = ps.searchOnePair(object, action);

      if (pair != null) {
        pair.setDef(def != null ? true : false);
        status = ps.SaveCollectionInFile();
      }
    }
    return status;
  }
  
  // изменить новую пару
  public boolean changeNewPair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("objectName");
    String action = params.get("actionName");
    String def = params.get("def");
    String webController = params.get("webController");
    if (checkParams(object, action)) {
      Pair pair = ps.searchOnePair(object, action);

      if (pair != null) {
        pair.setDef(def != null ? true : false);
        pair.setControllerName(webController);
        status = ps.SaveCollectionInFile();
      }
    }
    return status;
  }

  public boolean movePair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("objectName");
    String action = params.get("actionName");
    String move = params.get("move");

    if (checkParams(object, action, move)) {

      String moveObject = null;
      String moveAction = null;

      String[] str = move.split(":");
      if (str.length == 2) {
        moveObject = str[0];
        moveAction = str[1];
      }
      if (moveObject != null & moveAction != null) {
        Pair movePair = ps.searchOnePair(moveObject, moveAction);
        Pair pair = ps.searchOnePair(object, action);
        List<Pair> parents = pair.getAllParentСlone();
        if (!parents.contains(movePair) && !(pair == movePair)) {
          ps.removePair(moveObject, moveAction);
          pair.addPair(movePair);
        }
        status = ps.SaveCollectionInFile();
      }
    }
    return status;
  }

  // удалить пару
  public boolean removePair(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("removeObject");
    String action = params.get("removeAction");
    if (checkParams(object, action)) {
      ps.removePair(object, action);
      status = ps.SaveCollectionInFile();
      errors.addAll(ps.getErrors());
    }
    return status;
  }

  // добавить sequence
  public boolean addSeq(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("objectName");
    String action = params.get("actionName");
    String seqName = params.get("seqName").trim();
    String objectMethod = params.get("objectMethod");
    String trueRender = params.get("trueRender");
    String falseRender = params.get("falseRender");
    String trueRedirect = params.get("trueRedirect");
    String falseRedirect = params.get("falseRedirect");
    String trueParams = params.get("trueParams");
    String falseParams = params.get("falseParams");

    // добавить sequence
    if (checkParams(object, action, seqName, objectMethod,
            trueRender, falseRender, trueRedirect, falseRedirect)) {
      Pair pair = ps.searchOnePair(object, action);
      if (pair != null) {
        if (!pair.containsSequence(seqName)) {

          trueRender = (!trueRender.equals("0") ? trueRender : null);
          falseRender = (!falseRender.equals("0") ? falseRender : null);
          trueRedirect = (!trueRedirect.equals("0") ? trueRedirect : null);
          falseRedirect = (!falseRedirect.equals("0") ? falseRedirect : null);

          String objectName = null;
          String methodName = null;
          String[] str = objectMethod.split(":");
          if (str.length == 2) {
            objectName = str[0];
            methodName = str[1];
          }

          Sequence s = SequenceObject.getInstance(seqName, objectName, methodName, trueRender, falseRender, trueRedirect, falseRedirect, trueParams, falseParams);
          pair.setSequence(s);
          status = ps.SaveCollectionInFile();
        } else {
          errors.add("Sequence с именем " + seqName + " уже существует в этой паре");
        }
      }
    }
    return status;
  }

  /**
   * изменить параметры Sequence
   *
   * @param request
   * @return
   * @throws Exception
   */
  public boolean changeSeq(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);

    String object = params.get("objectName");
    String action = params.get("actionName");
    String seqName = params.get("seqName");
    String objectMethod = params.get("objectMethod");
    String trueRender = params.get("trueRender");
    String falseRender = params.get("falseRender");
    String trueRedirect = params.get("trueRedirect");
    String falseRedirect = params.get("falseRedirect");
    String trueParams = params.get("trueParams");
    String falseParams = params.get("falseParams");

    // изменить sequence
    if (checkParams(seqName, objectMethod,
            trueRender, falseRender, trueRedirect, falseRedirect)) {
      Pair pair = ps.searchOnePair(object, action);
      if (pair != null) {
        Sequence s = pair.getSequence(seqName);
        if (s != null) {

          trueRender = (!trueRender.equals("0") ? trueRender : null);
          falseRender = (!falseRender.equals("0") ? falseRender : null);
          trueRedirect = (!trueRedirect.equals("0") ? trueRedirect : null);
          falseRedirect = (!falseRedirect.equals("0") ? falseRedirect : null);

          String objectName = null;
          String methodName = null;
          String[] str = objectMethod.split(":");
          if (str.length == 2) {
            objectName = str[0];
            methodName = str[1];
          }
          pair.setSequence(SequenceObject.getInstance(seqName, objectName, methodName, trueRender, falseRender, trueRedirect, falseRedirect, trueParams, falseParams));
          status = ps.SaveCollectionInFile();
        }
      }
    }

    return status;
  }

  // удалить Sequence
  public boolean removeSeq(Map<String, Object> request) throws Exception {
    boolean status = false;
    HashMap<String, String> params = getRequestParams(request);
    String object = params.get("objectName");
    String action = params.get("actionName");
    String seqName = params.get("seqName");

    if (checkParams(object, action, seqName)) {
      Pair pair = ps.searchOnePair(object, action);
      if (pair != null) {
        pair.removeSequence(seqName);
        status = ps.SaveCollectionInFile();
      }
    }

    return status;
  }

  private HashMap<String, String> getRequestParams(Map<String, Object> request) {
    HashMap<String, String> params = new HashMap<String, String>();
    for (String name : request.keySet()) {
      Object value = request.get(name);
      if (value != null) {
        params.put(name, value.toString());
      }
    }
    return params;
  }

  private boolean checkParams(String... names) {
    Boolean res = true;
    for (String param : names) {
      if (res == true && param != null && !"".equals(param)) {
        res = true;
      } else {
        res = false;
      }
    }
    return res;
  }
}
