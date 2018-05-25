package fs.actions;

import fs.actions.object.ListFileWrapper;
import fs.objects.structure.FSTreeNode;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Constants;
import utils.PropertiesHelper;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;

public class ReplicationMethods {

    private static ReplicationMethods INSTANCE = null;

    private ReplicationMethods() {
    }

    public static ReplicationMethods getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ReplicationMethods();
        return INSTANCE;
    }

    public void fileReplication(NetNodeLocation netNodeLoc, ReplicationWrapper repWr, NetNode nNode) {
        Registry registry;
        try {
            int it = 0;
            registry = LocateRegistry.getRegistry(netNodeLoc.getIp(), netNodeLoc.getPort());
            System.out.println("Node URL: " + netNodeLoc.toUrl());
            NetNode node = (NetNode) registry.lookup(netNodeLoc.toUrl());
            boolean rep;
            do {
                System.out.println(repWr.getUFID());
                rep = node.saveFileReplica(repWr);

                if (rep) {
                    nNode.nodeFileAssociation(repWr.getUFID(), netNodeLoc);
                    netNodeLoc.addOccupiedSpace((int) repWr.getAttribute().getFileLength());
                    System.out.println("Replication of: " + repWr.getUFID() + " done.");
                } else {
                    System.out.println("Replication of " + repWr.getUFID() + " failed.");
                    it++;
                }
            } while (!rep && it <= 10);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void jsonReplication(NetNodeLocation netNode, FSTreeNode directory) {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            node.updateUI(directory);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String fileID, NetNodeLocation netNode, FSTreeNode treeFileDirectory) {
        Registry registry;
        String filePath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());

            if (node.deleteFile(fileID, treeFileDirectory.getUFID())) {
                System.out.println("Replication Deleting successful");
            } else {
                System.out.println("Replication Deleting failed");
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void updateWritePermissionMap(String fileID, Collection<NetNodeLocation> nodeSet, ListFileWrapper listFileWrapper) {
        Registry registry;
        try {
            for (NetNodeLocation netNode : nodeSet) {
                registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
                NetNode node = (NetNode) registry.lookup(netNode.toUrl());
                node.updateWritePermissionMap(fileID, listFileWrapper);
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
