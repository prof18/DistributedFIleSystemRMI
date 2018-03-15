package mediator_fs_net;

import fs.actions.FlatServiceImpl;
import fs.actions.object.CacheFileWrapper;
import net.objects.NetNodeImpl;

import java.rmi.RemoteException;


public class MediatorFsNet {
    private NetNodeImpl node;
    private FlatServiceImpl service;

    public MediatorFsNet() {

    }

    public void addNetService(NetNodeImpl node1) {
        node = node1;
    }

    public void addService(FlatServiceImpl service1) {
        service = service1;
    }

    public CacheFileWrapper getFile(String UFID) {
            return node.getFile(UFID);

    }
}
