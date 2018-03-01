package fileSystem;

import com.google.gson.Gson;
import utils.Constants;
import utils.PropertiesHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.UUID;


public class FsOperation {

    private String rootPath;
    private DirectoryTree root = null;
    private File dirFile = null;

    private static FsOperation INSTANCE = null;

    public static FsOperation getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FsOperation();
        return INSTANCE;
    }

    private FsOperation() {
        rootPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
    }

    public void setRoot(DirectoryTree rootDir){
        if (dirFile != null){
            root = rootDir;
        }
    }

    public void setDirFile(File dirFile) {
        this.dirFile = dirFile;
    }

    public void createDirectory(DirectoryTree currentDir, String dirName){
        DirectoryTree newDir = new DirectoryTree(UUID.randomUUID().toString(), dirName, null);
        newDir.setParent(currentDir);
        currentDir.addChild(newDir);

        BufferedReader br;
        FileWriter fw;

        try{
            br = new BufferedReader(new FileReader(dirFile));
            if (br.readLine() != null) {
                System.out.println("No errors, and directory file is empty");
                fw = new FileWriter(dirFile, true);
                Gson gson = new Gson();
                gson.toJson(newDir, fw);

                fw.close();
            } else {
                System.out.println("nessuna radice");
            }

        }catch(java.io.IOException e){
            e.printStackTrace();
            System.out.println("Problema metodo setRoot classe FsOperation");
            System.exit(-1);
        }

    }

    public void deleteDirectory(DirectoryTree currDir, String dirName){
        DirectoryTree remDir = currDir.getChild(dirName);
        remDir.removeParent();
        updateFsFile();
    }

    /*public void mv(String nodeName, String path){
        String[] pathA = path.split("/");
        DirectoryTree file = currentDir.getChild(nodeName);
        currentDir = getRootFromHere();
        for (int i = 0; i < pathA.length; i++) {
            currentDir = currentDir.getChild(pathA[i]);
        }
        file.setParent(currentDir);
    }

    public void removeFile(){
       currentDir.setFile(null);
    }*/

    public DirectoryTree cd(String path){
        DirectoryTree currentDir = root;
        String cmd = path.substring(0, 1);
        String dirName = "";
        if(path.length() > 3){
            dirName = path.substring(3, path.length()-1);
        }

        switch (cmd){
            case "..": return currentDir;

            case "cd": {
                if (currentDir.hasChild(currentDir) && currentDir.hasChild(dirName)){
                    currentDir = currentDir.getChild(dirName);
                }
            }

            case "up": currentDir = currentDir.getParent();
        }

        return currentDir;
    }

    /*private DirectoryTree getRootFromHere(){
        if(currentDir.isRoot()) {
            return currentDir;
        }
        return currentDir.getRoot();
    }*/

    private void updateFsFile(){
       /* if (dirFile != null){
            BufferedReader br;
            FileWriter fw;

            try{
                br = new BufferedReader(new FileReader(dirFile));
                if (br.readLine() != null) {
                    System.out.println("No errors, and directory file is empty");
                    fw = new FileWriter(dirFile, false);
                    Gson gson = new Gson();
                    gson.toJson(newDir, fw);

                    fw.close();
                } else {
                    System.out.println("nessuna radice");
                }

            }catch(java.io.IOException e){
                e.printStackTrace();
                System.out.println("Problema metodo setRoot classe FsOperation");
                System.exit(-1);
            }

        }*/
    }
}
