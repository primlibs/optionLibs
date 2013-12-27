/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import prim.libs.MyString;
import web.objects.Parameter;

/**
 *
 * @author кот
 */
public final class Backup {
    //список полных путей к конфигурационным файлам

    private List<Parameter> configFiles = new ArrayList();
    private List<String> error = new ArrayList();
    private String sqlDumpName = "";
    private String dumpDirName = "";
    private String archiveName = "";

    private Backup() {
    }

    public static Backup getInstance() {
        return new Backup();
    }

    /**
     * Добавить имя файла и путь к нему в список
     *
     * @param
     */
    public void setConfigFile(String path, String name) {
        if (MyString.NotNull(name, path)) {
            Boolean contain = false;
            for (Parameter pr : configFiles) {
                if (pr.getName().equals(path) && pr.getValue().equals(name)) {
                    contain = true;
                }
            }
            if (!contain) {
                configFiles.add(new Parameter(path, name));
            }
        } else {
            error.add("Имя конфигурационного файла или путь к нему не передан");
        }
    }

    /**
     * Вернуть клон списка конфигурационных файлов где имя параметра - это путь, а
     * значение - имя файла
     * @return
     */
    public List<Parameter> getConfigFiles() {
        List<Parameter> result = new ArrayList();
        for (Parameter pp : configFiles) {
            result.add(new Parameter(pp.getName(), pp.getValue()));
        }
        return result;
    }

    
    /**
     * установить имя дампа бд
     * @param name 
     */
    public void setSqlDumpName(String name) {
        if (MyString.NotNull(name)) {
            sqlDumpName = name;
        } else {
            error.add("Имя дампа не было передано");
        }
    }
    
     /**
     * установить путь до директории дампа
     * @param name 
     */
    public void setDumpDirectoryName(String path) {
        if (MyString.NotNull(path)) {
            dumpDirName = path;
        } else {
            error.add("Директория для дампа не было передано");
        }
    }
    
     /**
     * установить имя архива
     * @param name 
     */
    public void setArhiveName(String name) {
        if (MyString.NotNull(name)) {
            archiveName = name;
        } else {
            error.add("Имя архива не было передано");
        }
    }
}
