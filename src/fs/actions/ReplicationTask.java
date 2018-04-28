package fs.actions;

import fs.actions.object.WrapperFileServiceUtil;
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
    private WrapperFileServiceUtil wfsu;

    public ReplicationTask(NetNodeLocation node, ReplicationWrapper rw, WrapperFileServiceUtil wfsu) {
        netNode = node;
        repWr = rw;
        this.wfsu = wfsu;
    }

    @Override
    public void run() {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(netNode.getIp(), netNode.getPort());
            NetNode node = (NetNode) registry.lookup(netNode.toUrl());
            boolean rep;
            do {
                rep = node.saveFileReplica(repWr);

                if (rep) {
                    wfsu.nodeFileAssociation(repWr.getUFID(), netNode);
                    netNode.addOccupiedSpace((int) repWr.getAttribute().getFileLength());
                    System.out.printf("Replicazione file " + repWr.getUFID() + " riuscita.");
                } else {
                    System.out.println("Replicazione file " + repWr.getUFID() + " fallita.");
                }
            } while (!rep);

            /*if (node.saveFileReplica(repWr)) {

            } else {
                System.out.println("File non replicato");
            }*/
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

}
