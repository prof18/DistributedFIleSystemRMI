package fs.actions;

import fs.actions.object.WrapperFileServiceUtil;
import mediator_fs_net.MediatorFsNet;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;

public class ReplicationTask extends TimerTask implements Serializable {

    private NetNodeLocation netNode;
    private ReplicationWrapper repWr;
    private NetNode nNode;

    public ReplicationTask(NetNodeLocation node, ReplicationWrapper rw, NetNode nNode) {
        netNode = node;
        repWr = rw;
        this.nNode = nNode;
    }

    @Override
    public void run() {
        System.out.println("ReplicattionTask");
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

            /*if (node.saveFileReplica(repWr)) {

            } else {
                System.out.println("File non replicato");
            }*/
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}
