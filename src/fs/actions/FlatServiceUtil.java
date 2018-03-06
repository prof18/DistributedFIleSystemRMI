package fs.actions;

import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class FlatServiceUtil {
    public static NetNode create(String ownIP, String nameService) {
        System.setProperty("java.rmi.server.hostname", ownIP);
        Registry registry = null;
        NetNode node = null;
        int port = 1099;
        boolean notFound = true;
        while (notFound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                notFound = false;
            } catch (RemoteException e) {
                System.out.println("porta occupata");
                port++;
            }
        }
        try {
            node = new NetNodeImpl(ownIP, port, nameService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String connectPath = "rmi://" + ownIP + ":" + port + "/" + nameService;
        System.out.println("connectPath = " + connectPath);
        try {
            registry.bind(connectPath, node);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
        System.out.println("nodo creato ritorno il nodo");
        return node;
    }
    public static void connect(NetNode ownNode,String masterIP,int portMaster,String nameService){
        String recPat = "rmi://" + masterIP + ":" + portMaster + "/" + nameService;
        try {
            Registry registryRec = LocateRegistry.getRegistry(masterIP, portMaster);
            NetNode node1 = (NetNode) registryRec.lookup(recPat);
            System.out.println("[AGGIORNAMENTO NODI]");
            HashMap<Integer,NetNodeLocation> retMap= node1.join(masterIP,portMaster,nameService);
            System.out.println();
            System.out.println("[MAPPA RITORNATA]");
            System.out.println();
            Util.plot(retMap);
            ownNode.setConnectedNodes(retMap);
//                System.out.println("[MAPPA AGGIORNATA]");
//                Util.plot(node.getHashMap());

            //Se i nodi sono solo 2 le Map saranno già aggiornate
            if (!(retMap.size() == 2)) {
                System.out.println();
                System.out.println("[AGGIORNAMENTO NODI CONNESSI SU TERZI]");
                System.out.println();
                for (Map.Entry<Integer, NetNodeLocation> entry : ownNode.getHashMap().entrySet()) {

                    if( !((ownNode.getHost()+port).hashCode()== entry.getKey() || (ipRec + porta).hashCode()== entry.getKey() ) ) {

                        NetNodeLocation tmp = entry.getValue();
                        String tmpPath = "rmi://" + tmp.getIp() + ":" + tmp.getPort() + "/" + tmp.getName();

                        Registry tmpRegistry = LocateRegistry.getRegistry(tmp.getIp(),tmp.getPort());
                        NetNode tmpNode = (NetNode) tmpRegistry.lookup(tmpPath);
                        tmpNode.setConnectedNodes(node.getHashMap());

                    }


                }
            }



        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }
}
