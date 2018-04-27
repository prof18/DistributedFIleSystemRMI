package fs.actions;


import fs.objects.structure.FSTreeNode;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;

public class RemoveTask extends TimerTask {

    private String fileID;
    String filePath;
    private NetNodeLocation netNode;

    public RemoveTask(String fileID, String filePath, NetNodeLocation node) {
        this.fileID = fileID;
        this.filePath = filePath;
        netNode = node;
    }

    @Override
    public void run() {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());

            System.out.println("Cancellazione file: " + fileID);
            System.out.println("Percorso file: " + filePath);
            System.out.println("Nel nodo: " + netNode.toUrl());


            if (node.deleteFile(fileID, filePath)) {
                System.out.println("File e attributi eliminati con successo");
            } else {
                System.out.println("File e attributi non sono stati eliminati");
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }
}
