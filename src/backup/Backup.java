/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package backup;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import prim.libs.EnumFileSearch;
import prim.libs.FileSearch;
import prim.libs.MyString;
import prim.model.FileExecutor;
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
    private String dumpPath = "";
    private String innerDir = "";
    private String archiveName = "";
    private String dbUser = "";
    private String dbPass = "";
    private String dbName = "";
    private String unzipDir = "";

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
    public void addConfigFile(String path, String name) {
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
     * Вернуть клон списка конфигурационных файлов где имя параметра - это путь,
     * а значение - имя файла
     *
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
     *
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
     *
     * @param name
     */
    public void setDumpDirectoryName(String dumpPath, String innerDir) {
        if (MyString.NotNull(dumpPath, innerDir)) {
            this.dumpPath = dumpPath;
            this.innerDir = innerDir;
        } else {
            error.add("Директория для дампа или локальной директории не было передано");
        }
    }

    /**
     * установить имя архива
     *
     * @param name
     */
    public void setArhiveName(String name) {
        if (MyString.NotNull(name)) {
            archiveName = name;
        } else {
            error.add("Имя архива не было передано");
        }
    }

    /**
     * установить имя архива
     *
     * @param name
     */
    public void setDbOpts(String dbName, String dbUser, String dbPass) {
        if (MyString.NotNull(dbName, dbUser, dbPass)) {
            this.dbName = dbName;
            this.dbUser = dbUser;
            this.dbPass = dbPass;
        } else {
            error.add("Настройки базы данных не были переданы");
        }
    }

    /**
     * создать директорию для бекапа и бекап
     */
    public void createBackup() {
        if (error.isEmpty()) {
            createDir();
        }
        if (error.isEmpty()) {
            makeBackupDb();
        }
        if (error.isEmpty()) {
            copyConfigFiles();
        }
        if (error.isEmpty()) {
            createArchive();
        }

    }

    private void createArchive() {
        try {
            File dir = new File(dumpPath);
            String command = "tar -cjvf " + archiveName + " " + dumpPath + "/" + innerDir;
            Process proc = Runtime.getRuntime().exec(command, null, dir);
            int processComplete = proc.waitFor();
            if (processComplete != 0) {
                error.add("не удалось создать архив");
            }
        } catch (Exception ex) {
            error.add("Ошибка при попытке создать архив");
            error.add(MyString.getStackExeption(ex));
        }
    }

    private void copyConfigFiles() {
        try {
            for (Parameter pm : configFiles) {
                String path = pm.getName() + "/" + pm.getValue();
                FileExecutor file = new FileExecutor(path);
                file.copy(dumpPath + "/" + innerDir);
            }
        } catch (Exception ex) {
            error.add("Ошибка при попытке скопировать системные файлы");
            error.add(MyString.getStackExeption(ex));
        }

    }

    public List<String> getError() {
        List<String> result = new ArrayList();
        for (String err : error) {
            result.add(err);
        }
        return result;
    }

    public Boolean loadDump() {
        try {
            if (error.isEmpty()) {
                unzip();
            }
            if (error.isEmpty()) {
                loadDumpBD();
            }
            if (error.isEmpty()) {
                loadConfigFiles();
            }
        } catch (Exception ex) {
            error.add(MyString.getStackExeption(ex));
        }
        if (error.isEmpty()) {
            return true;
        }
        return false;
    }    
    
    // залить дамп БД
    private void loadDumpBD() throws Exception {
        if (new File(unzipDir).exists()) {
            FileSearch fs = FileSearch.findInDir(unzipDir, sqlDumpName);
            if (fs.getResult().equals(EnumFileSearch.success)) {
                String newDumpPath = fs.getFilePath();
                String[] command = new String[]{"/bin/sh","mysql -u" + dbUser + " -p" + dbPass + " " + dbName + " --default-character-set=utf8 < " + newDumpPath};
                Process proc = Runtime.getRuntime().exec(command);
                int i = proc.waitFor();
                
                if (i != 0) {
                    throw new Exception("не удалось залить дамп БД "+command[1]+" !");
                }
            }else{
                error.add(MyString.getString(fs.getResult()));
            }
        } else {
            throw new Exception("не найдена директория дампа " + unzipDir);
        }
    }

    //залить файлы конфигов
    private void loadConfigFiles() throws Exception {
        if (new File(unzipDir).exists()) {
            List<Param> pms=new ArrayList();
            for(Parameter pa:configFiles){
                FileSearch fs = FileSearch.findInDir(unzipDir, MyString.getString(pa.getValue()));
                if(!fs.getResult().equals(EnumFileSearch.success)){
                    error.add("Не найден файл в дампе "+pa.getValue());
                }else{
                    pms.add(new Param(fs.getFilePath(), pa.getValue(), pa.getName()));
                }
            }
            if(error.isEmpty()){
                for(Param pm:pms){
                    String newFilePath = pm.newPath+"/" + pm.fileName;
                    String oldFilePath = pm.dumpPath;
                    String command = "mv " + oldFilePath + " " + newFilePath;
                    Process proc = Runtime.getRuntime().exec(command);
                    int i = proc.waitFor();
                    if (i != 0) {
                      throw new Exception("не удалось копировать файлы настроек "+command+" !");
                    }
                }
            }
        }else{
          error.add("не найдена директория дампа " + unzipDir);
        }
    }

    // распаковать архив
    private void unzip() throws Exception {
        File zip = new File(dumpPath + "/" + archiveName);
        String dirName = zip.getParent();
        String newDirName;
        if (!dirName.equals("/")) {
            newDirName = dirName + "/";
        } else {
            newDirName = dirName;
        }
        String command = "tar xvf " + zip + " -C " + newDirName;
        Process proc = Runtime.getRuntime().exec(command);
        int i =proc.waitFor();
        if (i != 0) {
              throw new Exception("не удалось разархивировать "+command+" !");
         }
        unzipDir = newDirName.substring(newDirName.length()-1);
    }

    private void createDir() {
        try {
            File dir = new File(dumpPath + "/" + innerDir);
            dir.mkdir();
        } catch (Exception ex) {
            error.add("Ошибка при попытке создать директорию");
            error.add(MyString.getStackExeption(ex));
        }
    }

    private void makeBackupDb() {
        try {
            if (!MyString.NotNull(dumpPath + "/" + innerDir, sqlDumpName)) {
                error.add("Не установлены путь или имя дампа бд");
            }
            if (MyString.NotNull(dbName, dbPass, dbUser) && error.isEmpty()) {
                String user = dbUser;
                String pass = dbPass;
                String database = dbName;
                String command = "mysqldump --skip-opt -u" + user + " -p" + pass + " -B --create-options " + database + " -r " + dumpPath + "/" + innerDir + "/" + sqlDumpName;
                Process proc = Runtime.getRuntime().exec(command);
                int processComplete = proc.waitFor();
                if (processComplete != 0) {
                    throw new Exception("не удалось сделать дамп БД " + "mysqldump --skip-opt -u" + user + " -p" + pass + " -B --create-options " + database + " -r " + dumpPath + "/" + innerDir + "/" + sqlDumpName);
                }
            } else {
                error.add("Параметры баз данных не установлены");
            }

        } catch (Exception ex) {
            error.add("Ошибка при попытке создать бекап базы данных");
            error.add(MyString.getStackExeption(ex));
        }
    }
    
    
    
    private class Param{
        private String dumpPath;
        private String fileName;
        private String newPath;
        private Param(String dumpPath,Object fileName,String newPath){
            this.dumpPath=dumpPath;
            this.fileName=MyString.getString(fileName);
            this.newPath=newPath;
        }
    }
}
