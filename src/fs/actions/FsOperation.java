package fs.actions;

import fs.objects.structure.TreeNode;
import fs.objects.structure.FileWrapper;
import utils.Constants;
import utils.PropertiesHelper;


import java.io.File;
import java.util.UUID;


public class FsOperation {

    private String rootPath;
    private TreeNode root = null; //directory root
    private File dirFile = null; // json file
    private TreeNode currentDir = root; //current UI directory

    private static FsOperation INSTANCE = null;

    public static FsOperation getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FsOperation();
        return INSTANCE;
    }

    private FsOperation() {
        rootPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
    }

    public void setRoot(TreeNode rootDir){
        if (dirFile != null){
            root = rootDir;
        }
    }

    public void setDirFile(File dirFile) {
        this.dirFile = dirFile;
    }

    public void createDirectory(String dirName){
        TreeNode newDir = new TreeNode(UUID.randomUUID().toString(), dirName, null, null, null);
        currentDir.addChild(newDir);
        newDir.setParent(currentDir);

        /*BufferedReader br;
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
        }*/

    }

    public void addFile(FileWrapper file){
        currentDir.addFile(file);
    }

    public void deleteDirectory(String dirName){
        TreeNode remDir = currentDir.getChild(dirName);
        if(remDir != null) {
            remDir.removeParent();
        }
        //updateFsFile();
    }

    /*public void mv(String nodeName, String path){
        String[] pathA = path.split("/");
        DirectoryTree file = currentDir.getChild(nodeName);
        currentDir = getRootFromHere();
        for (int i = 0; i < pathA.length; i++) {
            currentDir = currentDir.getChild(pathA[i]);
        }
        file.setParent(currentDir);
    }*/

    public void deleteAllFile(TreeNode currentDir){
       currentDir.setFile(null);
    }

    public void deleteFile(FileWrapper file){
        currentDir.removeOneFile(file.getFileName());
    }

    public void renameDirectory(String name, String newName){
        if(currentDir.getChild(name) != null){
            currentDir.getChild(name).setNameNode(newName);
        }
    }

    public void renameFile(String name, String newName){
        if(currentDir.getFile(name) != null){
            currentDir.getFile(name).setFileName(newName);
        }
    }

    public TreeNode cd(String path){
        String cmd = path.substring(0, 1);
        String dirName = "";
        if(path.length() > 3){
            dirName = path.substring(3, path.length()-1);
        }

        switch (cmd){
            case "..": currentDir = root;

            case "cd": {
                if (currentDir.hasChild(currentDir) && currentDir.hasChild(dirName)){
                    currentDir = currentDir.getChild(dirName);
                }else{
                    System.out.println("Directory not found");
                }
            }

            case "up": currentDir = currentDir.getParent();
        }

        return currentDir;
    }

    /*private void updateFsFile(){
        if (dirFile != null){
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

        }
    }*/
}
