package mediator_fs_net;

import fs.actions.FSStructure;
import fs.actions.JsonReplicationTask;
import fs.actions.ReplicationTask;
import fs.actions.ReplicationWrapper;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WrapperFileServiceUtil;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class MediatorFsNet {
    private NetNode node;
    private FileService service;
    private FSStructure fsStructure;

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
        System.out.println("entrato nel mediator alla ricerca del file : " + UFID);
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
        System.out.println("entrato in mediator -> getFileFromFS");
        return service.getFileAndAttribute(UFID);
    }

    /**
     * This method is used to call the method replaceFileFromFS in order to replace an edit file
     * from an instance of the interface NetNode
     *
     * @param list is a list of file to replace
     */
    public void replaceFile(ArrayList<WritingCacheFileWrapper> list) {
        System.out.println("[MEDIATOR] entrato in replaceFile");
        System.out.println("file da modificare " + list.size());
        try {
            node.replaceFileFromFS(list);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public FSStructure getFsStructure() {
        return fsStructure;
    }


    public NetNode getNode() {
        return node;
    }

    public void jsonReplicaton(String json) {

        System.out.println("Json replication.");
        try {
            for (NetNodeLocation nnl : node.getHashMap().values()) {
                new JsonReplicationTask(nnl, node, json).run();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    /*public boolean fileReplication(ReplicationWrapper file){ //Probabile che sia da sistemare
        System.out.println("[MEDIATOR] entrato in fileReplication");
        boolean check = false;

        do {
            try {
                check = node.saveFileReplica(file);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }while(!check);

        return check;
    }*/
}