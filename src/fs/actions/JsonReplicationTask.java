package fs.actions;


import fs.objects.structure.FSTreeNode;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;

public class JsonReplicationTask extends TimerTask implements Serializable {

    private NetNodeLocation netNode;
    private FSTreeNode directory;

    public JsonReplicationTask(NetNodeLocation node,  FSTreeNode directory) {
        netNode = node;
        this.directory = directory;
    }

    @Override
    public void run() {
        System.out.println("JsonReplicationTask nel nodo " + netNode.toUrl());
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            System.out.println("Node URL: " + netNode.toUrl());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            node.updateUI(directory);

            /*if (node.saveFileReplica(repWr)) {

            } else {
                System.out.println("File non replicato");
            }*/
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
