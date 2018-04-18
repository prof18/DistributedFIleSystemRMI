package fs.actions;

import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimerTask;

public class MapUpdateTask extends TimerTask {

    private ArrayList<NetNodeLocation> nodeList;
    private String fileID;
    private Collection<NetNodeLocation> nodeSet;

    public MapUpdateTask(String fileID, Collection<NetNodeLocation> nodeSet, ArrayList<NetNodeLocation> nodeList){
        this.fileID = fileID;
        this.nodeSet = nodeSet;
        this.nodeList = nodeList;
    }
    @Override
    public void run() {
        Registry registry;
        try {
            for (NetNodeLocation netNode: nodeSet) {
                registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
                NetNode node = (NetNode) registry.lookup(netNode.toUrl());
                node.updateFileList(fileID, nodeList);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
