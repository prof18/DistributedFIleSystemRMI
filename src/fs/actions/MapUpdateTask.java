package fs.actions;

import fs.actions.object.ListFileWrapper;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

//task per la modifica dei flag di scrittura
public class MapUpdateTask extends TimerTask {

    private ListFileWrapper listFileWrapper;
    private String fileID;
    private Collection<NetNodeLocation> nodeSet;

    public MapUpdateTask(String fileID, Collection<NetNodeLocation> nodeSet, ListFileWrapper listFileWrapper) {
        this.fileID = fileID;
        this.nodeSet = nodeSet;
        this.listFileWrapper=listFileWrapper;
    }

    @Override
    public void run() {
        System.out.println("MAP UPDATE TASK");
        Registry registry;
        try {
            for (NetNodeLocation netNode : nodeSet) {
                registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
                NetNode node = (NetNode) registry.lookup(netNode.toUrl());
                node.updateFileNodeList(fileID, listFileWrapper);
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
