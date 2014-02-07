/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package option.objects;

import java.util.ArrayList;
import com.prim.support.MyString;

/**
 *
 * @author пользователь
 */
public class Valids {
  private String name= "";
  private String alias= "";



  public String getName() {
    return name;
  }

  public String getAlias() {
    return alias;
  }

  
  public Valids(String name,String alias){
    if(MyString.NotNull(name)){
      this.name=name;
    }
    if(MyString.NotNull(name)){
      this.alias=alias;
    }    
  }
  
}
