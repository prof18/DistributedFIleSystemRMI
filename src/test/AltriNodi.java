package test;

import net.objects.interfaces.NetNode;
import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;
import net.objects.NetNodeWrap;
import utils.Util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class AltriNodi {
    public static void main(String[] args) {
        NetNode netNode = null;
        NetNodeWrap nodi=null;
        try {
            netNode = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare qui ipMaster e ipNode
            nodi = netNode.join("10.8.0.4", "LR18", "10.8.0.4");
            System.out.println("mi sono aggiunto al filesystem");
            System.out.println("numero di nodi "+nodi.getNodes().size());
            Util.plot(nodi.getNodes());
            System.out.println();
            System.out.println();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("sono il nodo : "+nodi.getOwnNode());
        System.out.println("cosa vuoi fare ...");
        for(Map.Entry<String, NetNodeLocation> entry:nodi.getNodes().entrySet()){
            if(entry.getKey()!=nodi.getOwnNode()){
                System.out.println("Comunicazione con : "+entry.getValue().toString());
                Registry registry= null;
                try {
                    System.setProperty("java.rmi.server.hostname", entry.getValue().getIp());
                    System.out.println("java.rmi.server.hostname: " + System.getProperty("java.rmi.server.hostname"));
                    registry = LocateRegistry.getRegistry(entry.getValue().getIp(),entry.getValue().getPort());
                    String path=entry.getValue().toUrl()+entry.getKey();
                    System.out.println(path);
                    netNode = (NetNode) registry.lookup(path);
                    System.out.println(netNode.saluta());
                } catch (RemoteException e) {
                    System.out.println("problema strano");

                } catch (NotBoundException e) {
                    System.out.println("non trovato errore");

                }

            }
        }

    }
}
