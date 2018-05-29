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

        FSTreeNode node = new FSTreeNode();
        node.setParent(currentNode);
        node.setNameNode(dirName);
        node.setUFID(host + "_" + Date.from(Instant.now()).hashCode());
        node.setChildrens(new ArrayList<>());
        node.setFiles(new ArrayList<>());
        currentNode.addChild(node);
        node.setLastEditTime(System.currentTimeMillis());
        node.updateAncestorTime();
        node.setOwner(PropertiesHelper.getInstance().loadConfig(Constants.USERNAME_CONFIG));

        FSStructure.getInstance().generateJson(currentNode.findRoot());
        String json = PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
        currentNode.findRoot().setJson(json);
        MediatorFsNet.getInstance().jsonReplication(currentNode.findRoot());

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

        FSTreeNode nodeToReturn;

        if (!nodeToDelete.getFiles().isEmpty() || !nodeToDelete.getChildren().isEmpty()) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                    "The folder is not empty. Would you like to delete all?", "Warning", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // Saving code here
                ArrayList<FileWrapper> allFilesUFID = nodeToDelete.getAllFilesWhenDeleteDirectory();
                if (allFilesUFID != null) {
                    MediatorFsNet.getInstance().deleteDirectoryFiles(allFilesUFID);
                }
                FSTreeNode parent = nodeToDelete.getParent();
                nodeToDelete.setLastEditTime(System.currentTimeMillis());
                nodeToDelete.updateAncestorTime();
                parent.getChildren().remove(nodeToDelete);
                nodeToReturn = parent;
            } else {
                return;
            }
        } else {
            FSTreeNode parent = nodeToDelete.getParent();
            nodeToDelete.setLastEditTime(System.currentTimeMillis());
            nodeToDelete.updateAncestorTime();
            parent.getChildren().remove(nodeToDelete);
            nodeToReturn = parent;
        }

        FSTreeNode treeRoot = nodeToReturn.findRoot();
        FSStructure.getInstance().generateJson(treeRoot);
        String json = PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
        treeRoot.setJson(json);
        MediatorFsNet.getInstance().jsonReplication(treeRoot);

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

            currentNode.addFiles(wrapper);

            FSStructure.getInstance().generateJson(currentNode.findRoot());
            currentNode.findRoot().setJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            MediatorFsNet.getInstance().jsonReplication(currentNode.findRoot());

            callback.onItemChanged(currentNode);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
