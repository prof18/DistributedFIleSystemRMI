package mediator_fs_net;

import fs.actions.FSStructure;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WrapperFileServiceUtil;
import fs.actions.object.WritingCacheFileWrapper;
import net.objects.interfaces.NetNode;

import java.rmi.RemoteException;
import java.util.ArrayList;


public class MediatorFsNet {
    private NetNode node;
    private FileService service;
    private FSStructure fsStructure;
    private WrapperFileServiceUtil wfsu;

    public MediatorFsNet() {

    }

    public void addNetService(NetNode node1) {
        node = node1;
    }

    public void addService(FileService service1) {
        service = service1;
    }

    public void setFsStructure() {
        this.fsStructure = FSStructure.getInstance();
    }

    public void setWrapperFileServiceUtil(WrapperFileServiceUtil wfsu){
        this.wfsu = wfsu;
    }

    public CacheFileWrapper getFile(String UFID) {
        System.out.println("entrato nel mediator alla ricerca del file : "+UFID);
        try {
            return node.getFileOtherHost(UFID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FSStructure getFsStructure() {
        return fsStructure;
    }

    public WrapperFileServiceUtil getWrapperFileServiceUtil() {
        return wfsu;
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
}
