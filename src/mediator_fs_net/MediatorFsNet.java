package mediator_fs_net;

import fs.actions.FlatServiceImpl;
import fs.actions.interfaces.FlatService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FileWrapper;
import net.objects.interfaces.NetNode;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;


public class MediatorFsNet {
    private NetNode node;
    private FlatService service;

    public MediatorFsNet() {

    }

    public void addNetService(NetNode node1) {
        node = node1;
    }

    public void addService(FlatService service1) {
        service = service1;
    }

    public CacheFileWrapper getFile(String UFID) { //ricerca nella "rete" del file
        System.out.println("entrato nel mediator alla ricerca del file : "+UFID);
        try {
            return node.getFileOtherHost(UFID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CacheFileWrapper getFilefromFS(String UFID){
        System.out.println("entrato in mediator -> getFileFromFS");
        return service.getFileAndAttribute(UFID);
    }

    public void replaceFile(ArrayList<WritingCacheFileWrapper> list){
        System.out.println("[MEDIATOR] entrato in replaceFile");
        System.out.println("file da modificare "+list.size());
        try {
            node.replaceFileFromFS(list);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*public void fileReplication(FileWrapper file){ //Probabile che sia da sistemare
        System.out.println("[MEDIATOR] entrato in fileReplication");
        boolean check;
        do {
            check = node.saveFileReplica(file);
        }while(!check);

    }*/
}
