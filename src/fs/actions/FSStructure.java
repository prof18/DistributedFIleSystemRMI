package fs.actions;

import fs.objects.json.JsonFile;
import fs.objects.json.JsonFolder;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;
import utils.Constants;
import utils.GSONHelper;
import utils.PropertiesHelper;

import java.util.*;

/**
 * This class provides some actions to handle the internal structure of the file system
 */
public class FSStructure {

    private static FSStructure INSTANCE = null;
    private FSTreeNode tree;

    public static FSStructure getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FSStructure();
        return INSTANCE;
    }

    public FSTreeNode getTree() {
        if (tree.isRoot())
            return tree;
        else
            return tree.findRoot();
    }

    /**
     * This method save the structure of the FS as a JSON File
     * @param tree The structure of the FS
     */
    public void generateJson(FSTreeNode tree) {

        //update the tree reference
        this.tree = tree;

        Queue<FSTreeNode> queue = new LinkedList<>();
        queue.add(tree);

        HashMap<String, JsonFolder> folderMap = new HashMap<>();

        while (!queue.isEmpty()) {
            FSTreeNode node = queue.poll();
            JsonFolder jsonFolder = new JsonFolder();
            jsonFolder.setRoot(node.isRoot());
            jsonFolder.setUFID(node.getUFID());
            jsonFolder.setFolderName(node.getNameNode());
            jsonFolder.setLastEditTime(node.getLastEditTime());
            jsonFolder.setOwner(node.getOwner());
            if (!node.isRoot())
                jsonFolder.setParentUFID(node.getParent().getUFID());

            ArrayList<String> sons = new ArrayList<>();
            ArrayList<JsonFile> files = new ArrayList<>();
            //set root sons
            for (FSTreeNode child : node.getChildren()) {
                queue.add(child);
                sons.add(child.getUFID());
            }
            jsonFolder.setChildren(sons);
            for (FileWrapper fileWrapper : node.getFiles()) {
                JsonFile file = new JsonFile();
                file.setUFID(fileWrapper.getUFID());
                file.setFileName(fileWrapper.getFileName());
                file.setAttribute(fileWrapper.getAttribute());
                file.setPath(fileWrapper.getPath());
                files.add(file);
            }
            jsonFolder.setFiles(files);
            folderMap.put(jsonFolder.getUFID(), jsonFolder);
        }

        String json = GSONHelper.getInstance().foldersToJson(folderMap);
        PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, json);
    }

    /**
     * This method creates the internal FS structure starting from the JSON
     */
    public void generateTreeStructure() {

        String structureJson = PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
        if (structureJson != null) {
            //load the tree structure
            HashMap<String, JsonFolder> folderMap = GSONHelper.getInstance().jsonToFolders(structureJson);

            JsonFolder jsonFolderRoot = null;
            for (Map.Entry<String, JsonFolder> entry : folderMap.entrySet()) {

                JsonFolder folder = entry.getValue();
                if (folder.isRoot()) {
                    jsonFolderRoot = folder;
                    break;
                }
            }

            if (jsonFolderRoot != null) {

                Queue<FSTreeNode> queue = new LinkedList<>();

                //build the tree, starting from the root
                tree = new FSTreeNode();
                tree.setParent(null);
                tree.setUFID(jsonFolderRoot.getUFID());
                tree.setNameNode(jsonFolderRoot.getFolderName());
                tree.setLastEditTime(jsonFolderRoot.getLastEditTime());
                queue.add(tree);
                while (!queue.isEmpty()) {
                    FSTreeNode node = queue.remove();
                    //we need to set the children and the file
                    //Children
                    JsonFolder folder = folderMap.get(node.getUFID());
                    ArrayList<FSTreeNode> nodeChildren = new ArrayList<>();
                    if (folder.getChildren() != null) {
                        for (String childUFID : folder.getChildren()) {
                            JsonFolder childFolder = folderMap.get(childUFID);
                            FSTreeNode child = new FSTreeNode();
                            child.setUFID(childFolder.getUFID());
                            child.setNameNode(childFolder.getFolderName());
                            child.setParent(node);
                            child.setLastEditTime(childFolder.getLastEditTime());
                            nodeChildren.add(child);
                            queue.add(child);
                        }
                    }
                    node.setChildren(nodeChildren);

                    //Files
                    ArrayList<FileWrapper> files = new ArrayList<>();
                    ArrayList<JsonFile> jsonFiles = folder.getFiles();
                    if (jsonFiles != null) {
                        for (JsonFile jsonFile : jsonFiles) {
                            FileWrapper file = new FileWrapper();
                            file.setAttribute(jsonFile.getAttribute());
                            file.setUFID(jsonFile.getUFID());
                            file.setFileName(jsonFile.getFileName());
                            file.setPath(jsonFile.getPath());
                            files.add(file);
                        }
                    }
                    node.setFiles(files);
                }
            } else {
                System.out.println("[GENERATE TREE STRUCTURE] There wasn't a root in the JSON");
            }

        } else {

            //generate the tree with only the root
            tree = new FSTreeNode();
            tree.setParent(null);
            tree.setUFID("root");
            tree.setNameNode("root");
            tree.setChildren(new ArrayList<>());
            tree.setFiles(new ArrayList<>());
            tree.setLastEditTime(System.currentTimeMillis());

            generateJson(tree);
        }
    }
}
