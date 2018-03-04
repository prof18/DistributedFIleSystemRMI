package fs.actions;

import fs.objects.json.JsonFile;
import fs.objects.json.JsonFolder;
import fs.objects.structure.FileWrapper;
import fs.objects.structure.FSTreeNode;
import utils.Constants;
import utils.GSONHelper;
import utils.PropertiesHelper;

import java.util.*;

public class FSStructure {

    private static FSStructure INSTANCE = null;
    private static FSTreeNode tree;

    public static FSStructure getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FSStructure();
        return INSTANCE;
    }

    private FSStructure() {

    }

    //qui si genera la struttura ad albero del file system.
    //e rimane sempre istanziata cosi' quando serve e' pronta


    public FSTreeNode getTree() {
        return tree;
    }


    public void generateTreeStructure() {

        //create the tree from the Json or generate the tree with only a root
        String structureJson = PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
        if (structureJson != null) {
            //load the tree structure
            HashMap<String, JsonFolder> folderMap = GSONHelper.getInstance().jsonToFolders(structureJson);

            JsonFolder jsonFolderRoot = null;

            for (Map.Entry<String, JsonFolder> entry : folderMap.entrySet()) {

                JsonFolder folder = entry.getValue();
                if (folder.isRoot()) {
                    System.out.println("Root found");
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
                            nodeChildren.add(child);
                            queue.add(child);
                        }
                    }
                    node.setChildrens(nodeChildren);

                    //Files
                    ArrayList<FileWrapper> files = new ArrayList<>();
                    ArrayList<JsonFile> jsonFiles = folder.getFiles();
                    if (jsonFiles != null) {
                        for (JsonFile jsonFile : jsonFiles) {
                            FileWrapper file = new FileWrapper();
                            file.setAttribute(jsonFile.getAttribute());
                            file.setUFID(jsonFile.getUFID());
                            file.setFileName(jsonFile.getFileName());
                            files.add(file);
                        }
                    }
                    node.setFiles(files);
                }
            } else {
                System.err.println("There wasn't a root in the json");
            }

        } else {
            //generate the tree with only the root
            tree = new FSTreeNode();
            tree.setParent(null);
            tree.setUFID(UUID.randomUUID().toString());
            tree.setNameNode("root");
        }


    }


}
