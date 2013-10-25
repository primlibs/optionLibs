/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option;

/**
 *
 * @author кот
 */
public class Creator {
  private Creator(){
    
  }
  
  
  public static Creator getInstance(){
    return new Creator();
  }
  
  public String run(){
    String result="";
    return result;
  }
  
  
}
