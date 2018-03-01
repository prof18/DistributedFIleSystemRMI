package net.actions;

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

public class Connect {

    public static void join(String ipMaster, String name, String ipHost) {
        System.out.println("ipMaster = " + ipMaster);
        System.out.println("ipHost = " + ipHost);
        System.out.println("name = " + name);
        NetNode netNode = null;
        NetNodeWrap nodi = null;
        try {
            netNode = new NetNodeImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        try {
            //modificare qui ipMaster e ipNode
            nodi = netNode.join(ipMaster, name, ipHost);
            System.out.println("mi sono aggiunto al filesystem");
            System.out.println("numero di nodi " + nodi.getNodes().size());
            Util.plot(nodi.getNodes());
            System.out.println();
            System.out.println();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.out.println("sono il nodo : " + nodi.getOwnNode());
        System.out.println("cosa vuoi fare ...");
        for (Map.Entry<String, NetNodeLocation> entry : nodi.getNodes().entrySet()) {
            if (!entry.getKey().equals(nodi.getOwnNode())) {
                System.out.println("Comunicazione con : " + entry.getValue().toString());
                Registry registry = null;
                try {
                    System.setProperty("java.rmi.server.hostname", ipHost);
                    registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                    String path = entry.getValue().toUrl() + entry.getKey();
                    System.out.println(path);
                    System.out.println(registry.toString());
                    String[] lista = registry.list();
                    for (String tmp : lista) {
                        System.out.println(tmp);
                    }
                    System.out.println("path problema :" + path);
                    NetNode netNodeTemp = (NetNode) registry.lookup(path);
                    System.out.println(netNodeTemp.saluta());

                    System.out.println("Aggiorno i connectedNodes del netNodeTemp");
                    netNodeTemp.updateCoNodes(netNode.getHashMap());

                } catch (RemoteException e) {
                    System.out.println("problema strano1");
                    e.printStackTrace();

                } catch (NotBoundException e) {
                    System.out.println("non trovato errore1");
                    e.printStackTrace();

                }

            }
        }
    }
}
