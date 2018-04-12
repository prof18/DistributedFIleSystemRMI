package fs.actions;


import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;

public class RemoveTask extends TimerTask {

    private String fileID;
    private NetNodeLocation netNode;

    public RemoveTask(String fileID, NetNodeLocation node){
        this.fileID = fileID;
        netNode = node;
    }
    @Override
    public void run() {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            node.getMediator().getFsStructure().getTree().removeOneFile(fileID);
            node.getMediator().getFsStructure().getTree().removeOneFile(fileID+".attr");
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
