package mediator_fs_net;

import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import net.objects.interfaces.NetNode;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This class is a sort of mediator design patter, in order to handle the communication between the
 * NetNode and FileService Implementation
 */

public class MediatorFsNet {
    private NetNode node;
    private FileService service;

    public MediatorFsNet() {

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

    /**
     * This method is used to call the method replaceFileFromFS in order to replace an edit file
     * from an instance of the interface NetNode
     *
     * @param list is a list of file to replace
     */
    public void replaceFile(ArrayList<WritingCacheFileWrapper> list) {
        
        try {
            node.replaceFileFromFS(list);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
