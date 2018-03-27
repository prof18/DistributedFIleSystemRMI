package fs.actions;

import fs.actions.interfaces.DirectoryService;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;
import utils.Constants;
import utils.PropertiesHelper;

import javax.swing.*;
import java.util.ArrayList;
import java.util.UUID;

/**
 * We have to do two things: update the json and create new file in the PC FS
 */
public class DirectoryServiceImpl implements DirectoryService {

    //A reference to the File System in the host PC
    private String hostFSPath;
/*    private FSTreeNode root = null; //directory root
    private File dirFile = null; // json file
    private FSTreeNode currentDir = root; //current UI directory*/

    private static DirectoryServiceImpl INSTANCE = null;

    public static DirectoryServiceImpl getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DirectoryServiceImpl();
        return INSTANCE;
    }

    private DirectoryServiceImpl() {
        hostFSPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
    }

    @Override
    public void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback) {

        //FSTreeNode tree = FSStructure.getInstance().getTree();
        FSTreeNode node = new FSTreeNode();
        node.setParent(currentNode);
        node.setNameNode(dirName);
        //TODO; change UUID
        node.setUFID(UUID.randomUUID().toString());
        node.setChildren(new ArrayList<>());
        node.setFiles(new ArrayList<>());
        currentNode.addChild(node);
        //long editTime = System.currentTimeMillis();
        node.setLastEditTime(System.currentTimeMillis());
        node.updateAncestorTime();

        callback.onItemChanged(currentNode);
    }

    @Override
    public void renameDirectory(FSTreeNode nodeToRename, String newName, NewItemCallback callback) {
        nodeToRename.setNameNode(newName);
        nodeToRename.setLastEditTime(System.currentTimeMillis());
        nodeToRename.updateAncestorTime();
        callback.onItemChanged(nodeToRename);
    }

    @Override
    public void deleteDirectory(FSTreeNode nodeToDelete, NewItemCallback callback) {

        if (!nodeToDelete.getFiles().isEmpty() || !nodeToDelete.getChildren().isEmpty()) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                    "The folder is not empty. Would you like to delete all?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // Saving code here
                System.out.println("Delete");
                FSTreeNode parent = nodeToDelete.getParent();
                nodeToDelete.setLastEditTime(System.currentTimeMillis());
                nodeToDelete.updateAncestorTime();
                parent.getChildren().remove(nodeToDelete);
                callback.onItemChanged(parent);
            }
        } else {
            FSTreeNode parent = nodeToDelete.getParent();
            nodeToDelete.setLastEditTime(System.currentTimeMillis());
            nodeToDelete.updateAncestorTime();
            parent.getChildren().remove(nodeToDelete);
            callback.onItemChanged(parent);
        }

    }

    @Override
    public String lookup(String dir, String name) {
        return null;
    }

    @Override
    public void addName(FSTreeNode currentNode, String name, String fileID) {
        FileWrapper wrapper = new FileWrapper();

    }

    @Override
    public void unName(String dir, String name) {

    }

    @Override
    public String getNames(String dir, String pattern) {
        return null;
    }

    /* public void setRoot(FSTreeNode rootDir){
        if (dirFile != null){
            root = rootDir;
        }
    }

    public void setDirFile(File dirFile) {
        this.dirFile = dirFile;
    }


    public void createDirectory(String dirName){
        FSTreeNode newDir = new FSTreeNode(UUID.randomUUID().toString(), dirName, null, null, null);
        currentDir.addChild(newDir);
        newDir.setParent(currentDir);

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
            System.out.println("Problema metodo setRoot classe DirectoryServiceImpl");
            System.exit(-1);
        }

    }

    public void addFile(FileWrapper file){
        currentDir.addFile(file);
    }

    public void deleteDirectory(String dirName){
        FSTreeNode remDir = currentDir.getChild(dirName);
        if(remDir != null) {
            remDir.removeParent();
        }
        //updateFsFile();
    }

    public void mv(String nodeName, String path){
        String[] pathA = path.split("/");
        DirectoryTree file = currentDir.getChild(nodeName);
        currentDir = getRootFromHere();
        for (int i = 0; i < pathA.length; i++) {
            currentDir = currentDir.getChild(pathA[i]);
        }
        file.setParent(currentDir);
    }

    public void deleteAllFile(FSTreeNode currentDir) {
        currentDir.setFiles(null);
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

    public FSTreeNode cd(String path){
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

    private void updateFsFile(){
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
                System.out.println("Problema metodo setRoot classe DirectoryServiceImpl");
                System.exit(-1);
            }

        }
    }*/
}
