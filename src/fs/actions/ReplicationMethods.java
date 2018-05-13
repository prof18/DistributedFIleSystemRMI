package fs.actions;

import fs.objects.structure.FSTreeNode;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collection;

public class ReplicationMethods {

    private static ReplicationMethods INSTANCE = null;

    private ReplicationMethods(){
    }

    public static ReplicationMethods getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ReplicationMethods();
        return INSTANCE;
    }

    public void fileReplication(NetNodeLocation netNode, ReplicationWrapper repWr, NetNode nNode){
        System.out.println("FileReplication method");
        Registry registry;
        try {
            int it = 0;
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            System.out.println("Node URL: " + netNode.toUrl());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            boolean rep;
            do {
                System.out.println(repWr.getUFID());
                rep = node.saveFileReplica(repWr);

                if (rep) {
                    nNode.nodeFileAssociation(repWr.getUFID(), netNode);
                    netNode.addOccupiedSpace((int) repWr.getAttribute().getFileLength());
                    System.out.println("Replicazione file " + repWr.getUFID() + " riuscita.");
                } else {
                    System.out.println("Replicazione file " + repWr.getUFID() + " fallita.");
                    it++;
                }
            } while (!rep && it <=10);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void jsonReplication(NetNodeLocation netNode, FSTreeNode directory) {
        System.out.println("JsonReplication method nel nodo " + netNode.toUrl());
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            System.out.println("Node URL: " + netNode.toUrl());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            node.updateUI(directory);

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String fileID, String filePath, NetNodeLocation netNode, FSTreeNode treeFileDirectory){
        System.out.println("DeleteFile method");
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());

            System.out.println("Cancellazione file: " + fileID);
            System.out.println("Percorso file: " + filePath);
            System.out.println("Nel nodo: " + netNode.toUrl());


            if (node.deleteFile(fileID, filePath, treeFileDirectory)) {
                System.out.println("File e attributi eliminati con successo");
            } else {
                System.out.println("File e attributi non sono stati eliminati");
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void updateWritePermissonMap(String fileID, Collection<NetNodeLocation> nodeSet, ArrayList<NetNodeLocation> nodeList){
        System.out.println("UpdatePermissionMap method");
        Registry registry;
        try {
            for (NetNodeLocation netNode : nodeSet) {
                registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
                NetNode node = (NetNode) registry.lookup(netNode.toUrl());
                node.updateWritePermissionFileList(fileID, nodeList);
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
