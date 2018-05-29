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


/**
 * This class contains all method used for the replication of created or deleted files and file system changes.
 */

public class ReplicationMethods {

    private static ReplicationMethods INSTANCE = null;

    private ReplicationMethods() {
    }

    public static ReplicationMethods getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ReplicationMethods();
        return INSTANCE;
    }

    /**
     * This method is used to replicate file in a selected node.
     *
     * @param netNodeLoc node where file is replicated
     * @param repWr wrapper with file data to be replicated
     * @param nNode netNode for update the replication file-node list
     */
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
                    netNodeLoc.addOccupiedSpace((int) repWr.getAttribute().getFileLength());
                    nNode.nodeFileAssociation(repWr.getUFID(), netNodeLoc);
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


    /**
     * This method is used to replicate file system structure in all connected nodes
     *
     * @param netNode node where file system JSON is replicated
     * @param root Root of the file system
     */
    public void jsonReplication(NetNodeLocation netNode, FSTreeNode root) {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            node.updateUI(root);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used to delete local replicated files in the connected nodes
     *
     * @param  fileID file UFID
     * @param netNode node where file is local saved
     * @param treeFileDirectory Directory to delete
     */
    public boolean deleteFile(String fileID, NetNodeLocation netNode, FSTreeNode treeFileDirectory, long fileSize) {
        boolean deleteState = false;
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());

            String directoryUFID = null;
            if (treeFileDirectory != null){
                directoryUFID = treeFileDirectory.getUFID();
            }

            if (node.deleteFile(fileID, directoryUFID, fileSize)) {
                deleteState = true;
                System.out.println("Replication Deleting successful");
            } else {
                System.out.println("Replication Deleting failed");
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        return deleteState;
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
