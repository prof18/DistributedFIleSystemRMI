package fs.actions;

import fs.actions.interfaces.DirectoryService;
import fs.actions.interfaces.FileService;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileAttribute;
import fs.objects.structure.FileWrapper;
import mediator_fs_net.MediatorFsNet;
import utils.Constants;
import utils.PropertiesHelper;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * We have to do two things: update the json and create new file in the PC FS
 */
public class DirectoryServiceImpl implements DirectoryService {

    //A reference to the File System in the host PC
    private String hostFSPath;
    private FileService fileService;

    private static DirectoryServiceImpl INSTANCE = null;

    public static DirectoryServiceImpl getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DirectoryServiceImpl();
        return INSTANCE;
    }

    @Override
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    private DirectoryServiceImpl() {
        hostFSPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
    }

    @Override
    public void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback) {

        String host = "";
        try {
            host = MediatorFsNet.getInstance().getNode().getHostName();
        } catch (RemoteException e) {
            System.out.println("Hostname null during folder creation");
        }


        FSTreeNode treeRoot = FSStructure.getInstance().getTree().findRoot();
        FSTreeNode directoryParent = treeRoot.findNodeByUFID(treeRoot, currentNode.getUFID());
        FSTreeNode node = new FSTreeNode();
        node.setParent(directoryParent);
        node.setNameNode(dirName);
        node.setUFID(host + "_" + Date.from(Instant.now()).hashCode());
        node.setChildrens(new ArrayList<>());
        node.setFiles(new ArrayList<>());
        directoryParent.addChild(node);
        //long editTime = System.currentTimeMillis();
        node.setLastEditTime(System.currentTimeMillis());
        node.updateAncestorTime();

        System.out.println("Replicazione del json per l'albero dopo creazione file");
        FSStructure.getInstance().generateJson(treeRoot);
        treeRoot.setGson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        MediatorFsNet.getInstance().jsonReplicaton(treeRoot);

        callback.onItemChanged(directoryParent);
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

        FSTreeNode nodeToReturn = null;

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
                nodeToReturn = parent;
            }
        } else {
            FSTreeNode parent = nodeToDelete.getParent();
            nodeToDelete.setLastEditTime(System.currentTimeMillis());
            nodeToDelete.updateAncestorTime();
            parent.getChildren().remove(nodeToDelete);
            nodeToReturn = parent;
        }

        System.out.println("Replicazione del json per l'albero dopo eliminazione del file");
        FSTreeNode treeRoot = FSStructure.getInstance().getTree().findRoot();
        FSStructure.getInstance().generateJson(treeRoot);
        treeRoot.setGson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        MediatorFsNet.getInstance().jsonReplicaton(treeRoot);

        callback.onItemChanged(nodeToReturn);

    }

    @Override
    public void addName(FSTreeNode currentNode, String name, String fileID, NewItemCallback callback) {
        FileWrapper wrapper = new FileWrapper();
        //get attribute
        try {
            FileAttribute fileAttribute = fileService.getAttributes(fileID);
            wrapper.setAttribute(fileAttribute);
            wrapper.setFileName(name);
            wrapper.setUFID(fileID);
            String path = currentNode.getPath();
            if (path.charAt(path.length() - 1) != '/')
                wrapper.setPath(currentNode.getPath() + "/" + name);
            else
                wrapper.setPath(currentNode.getPath() + name);

            System.out.println("Replicazione del json per l'albero dopo creazione file");
            FSTreeNode treeRoot = FSStructure.getInstance().getTree().findRoot();
            FSTreeNode curNode = treeRoot.findNodeByUFID(treeRoot, currentNode.getUFID());
            curNode.addFiles(wrapper);

            FSStructure.getInstance().generateJson(treeRoot);
            treeRoot.setGson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            System.out.println("Gson: " + PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            MediatorFsNet.getInstance().jsonReplicaton(treeRoot);

            callback.onItemChanged(curNode);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
