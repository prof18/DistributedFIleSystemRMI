package connection;

import objects.Node;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

public class Connect {

    public static void join(String ipMaster, String name, String ipHost) {
        Node node = null;
        Wrap nodi=null;
        try {
            node = new NodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare qui ipMaster e ipNode
            nodi=node.join(ipMaster,name,ipHost);
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
        for(Map.Entry<String, NodeLocation> entry:nodi.getNodes().entrySet()){
            if(entry.getKey()!=nodi.getOwnNode()){
                System.out.println("Comunicazione con : "+entry.getValue().toString());
                Registry registry= null;
                try {
                    registry = LocateRegistry.getRegistry(entry.getValue().getIp(),entry.getValue().getPort());
                    String path=entry.getValue().toUrl()+entry.getKey();
                    System.out.println(path);
                    node = (Node) registry.lookup(path);
                    System.out.println(node.saluta());
                } catch (RemoteException e) {
                    System.out.println("problema strano");

                } catch (NotBoundException e) {
                    System.out.println("non trovato errore");

                }

            }
        }
    }
}