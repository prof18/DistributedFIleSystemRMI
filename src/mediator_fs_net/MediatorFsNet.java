package mediator_fs_net;

import fs.actions.FSStructure;
import fs.actions.ReplicationMethods;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FSTreeNode;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.PropertiesHelper;

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
    public CacheFileWrapper getFile(String UFID) { //ricerca nella "rete" del file
        try {
            return node.getFileOtherHost(UFID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This method is used to call the method the getFileAndAttribute of the interface NetNode
     *
     * @param UFID is the unique name of the requested file
     * @return return a wrapper that contains the file and its attribute
     */
    public CacheFileWrapper getFilefromFS(String UFID) {
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

    public void jsonReplication(FSTreeNode treeRoot) {
        try {
            HashMap<Integer, NetNodeLocation> tmpHashMap = new HashMap<>(node.getHashMap());

            tmpHashMap.remove((node.getOwnIp() + node.getOwnPort()).hashCode());
            for (NetNodeLocation nnl : tmpHashMap.values()) {
                ReplicationMethods.getInstance().jsonReplication(nnl, treeRoot);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void updateJson(FSTreeNode treeRoot) {
        MainUI.updateModels(treeRoot, false);
    }

    public void removeFileFromTree(String UFID, String treeFileDirectoryUFID) {
        FSTreeNode root = FSStructure.getInstance().getTree();
        root.findNodeByUFID(root, treeFileDirectoryUFID).removeOneFile(UFID);
        FSStructure.getInstance().generateJson(root);
        updateJson(root);
    }
}