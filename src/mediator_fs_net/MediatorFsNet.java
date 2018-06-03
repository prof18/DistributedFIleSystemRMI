package mediator_fs_net;

import fs.actions.FSStructure;
import fs.actions.ReplicationMethods;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.Constants;
import utils.PropertiesHelper;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;


public class MediatorFsNet {
    private static MediatorFsNet INSTANCE = null;
    private NetNode node;
    private FileService service;
    private FSStructure fsStructure;

    public MediatorFsNet() {
        PropertiesHelper.getInstance();
        setFsStructure();
    }

    public static MediatorFsNet getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MediatorFsNet();
        }

        return INSTANCE;
    }

    /**
     * This method add the NetNode to the mediator instance
     *
     * @param node1 is the NetNode to add to the mediator
     */
    public void addNetService(NetNode node1) {
        node = node1;
    }

    /**
     * This method add the FileService to the mediator instance
     *
     * @param service1 is the FileService to add to the mediator
     */
    public void addService(FileService service1) {
        service = service1;
    }


    public void setFsStructure() {
        this.fsStructure = FSStructure.getInstance();
    }


    /**
     * This method is used to call the method the getFileOtherHost of the interface NetNode
     *
     * @param UFID is the unique name of the requested file
     * @return return a wrapper that contains the file and its attribute
     */
    public CacheFileWrapper getFile(String UFID) {
        try {
            return node.getFileOtherHost(UFID);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[GET-FILE] Unable to get the file");
        }
        return null;
    }

    /**
     * This method is used to call the method the getFileAndAttribute of the interface NetNode
     *
     * @param UFID is the unique name of the requested file
     * @return return a wrapper that contains the file and its attribute
     */
    public CacheFileWrapper getFileFromFS(String UFID) {
        return service.getFileAndAttribute(UFID);
    }


    public FSStructure getFsStructure() {
        if (fsStructure == null) {
            fsStructure = FSStructure.getInstance();
        }
        return fsStructure;
    }


    public NetNode getNode() {
        return node;
    }

    /**
     * This method is used to call the method jsonReplication of the class ReplicationMethods for update the file system structure in all connected nodes
     *
     * @param treeRoot is the file system root
     */
    public void jsonReplication(FSTreeNode treeRoot) {
        try {
            HashMap<Integer, NetNodeLocation> tmpHashMap = new HashMap<>(node.getHashMap());

            tmpHashMap.remove((node.getOwnIp() + node.getOwnPort()).hashCode());
            for (NetNodeLocation nnl : tmpHashMap.values()) {
                ReplicationMethods.getInstance().jsonReplication(nnl, treeRoot);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[JSON-REPLICATION] Unable to update the structure in all connected nodes");
        }
    }

    /**
     * This method is used to call the method updateModels of the class MainUI for update the UI and the tree structure of the file system
     *
     * @param treeRoot is the file system root
     */
    public void updateJson(FSTreeNode treeRoot) {
        MainUI.updateModels(treeRoot, false);
    }

    /**
     * This method is used to delete a local file
     *
     * @param UFID                  file UFID
     * @param treeFileDirectoryUFID is the directory where the file is saved
     */
    public void removeFileFromTree(String UFID, String treeFileDirectoryUFID) {
        FSTreeNode root = FSStructure.getInstance().getTree();
        root.findNodeByUFID(root, treeFileDirectoryUFID).removeOneFile(UFID);
        FSStructure.getInstance().generateJson(root);
        updateJson(root);
    }

    /**
     * This method is used to delete all file contained in a directory in all connected nodes
     *
     * @param files list of files to delete
     */
    public void deleteDirectoryFiles(ArrayList<FileWrapper> files) {
        HashMap<String, ListFileWrapper> fileNodeList;
        try {
            fileNodeList = node.getFileNodeList();

            for (FileWrapper file : files) {
                ArrayList<NetNodeLocation> fileLocations = fileNodeList.get(file.getUFID()).getLocations();

                for (NetNodeLocation fileLocation : fileLocations) {
                    if (fileLocation.toUrl().compareTo(node.getOwnLocation().toUrl()) != 0) {
                        boolean fileDelete = ReplicationMethods.getInstance().deleteFile(file.getUFID(), fileLocation, null, file.getAttribute().getFileLength());
                        if (fileDelete) {
                            fileLocation.reduceOccupiedSpace((int) file.getAttribute().getFileLength());
                        }
                    } else {
                        String localFilePath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
                        File localFile = new File(localFilePath + file.getUFID());
                        File localAttribute = new File(localFilePath + file.getUFID() + ".attr");
                        boolean deleteLocalFile = localFile.delete();
                        boolean deleteAttrFile = localAttribute.delete();
                        if (deleteLocalFile && deleteAttrFile) {
                            MediatorFsNet.getInstance().getNode().getOwnLocation().reduceOccupiedSpace((int) file.getAttribute().getFileLength());
                        }
                    }
                }
                fileNodeList.remove(file.getUFID());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[DELETE-DIRECTORY-FILES] Unable to delete all the files inside a directory in all the nodes");
        }


    }

    public void updateCache(String fileID){
        System.out.println("cache invalidation for file : "+fileID);
        service.removeFromCache(fileID);
    }
}