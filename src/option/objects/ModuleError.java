/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.objects;

import java.util.ArrayList;

/**
 *
 * @author Кот
 */
public class ModuleError {

  private ArrayList<String> errors = new ArrayList<String>();
  private ArrayList<String> messages = new ArrayList<String>();

  public ArrayList<String> getErrors() {
    return errors;
  }

  public ArrayList<String> getMessages() {
    return messages;
  }

  public void setError(String err) {
    this.errors.add(err);
  }

  public void setMessage(String msg) {
    this.messages.add(msg);
  }
}
