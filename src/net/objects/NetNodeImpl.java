package net.objects;

import net.objects.interfaces.NetNode;
import utils.Util;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private int num = 0;

    private String hostName = " --num host not update --";

    //<host,ip>
    private HashMap<Integer, NetNodeLocation> connectedNodes;

    public NetNodeImpl(String ownIP, int port, String name) throws RemoteException {
        super();
        connectedNodes = new HashMap<>();
        connectedNodes.put((ownIP + port).hashCode(), new NetNodeLocation(ownIP, port, name));
        System.out.println("[COSTRUTTORE]");
        Util.plot(connectedNodes);
    }

    @Override
    public HashMap<Integer, NetNodeLocation> join(String ipNode, int port, String name) throws RemoteException {
        System.out.println("si è connesso un nuovo nodo: " + ipNode + " " + port + " " + name);
        connectedNodes.put((ipNode + port).hashCode(), new NetNodeLocation(ipNode, port, name));
        System.out.println("[JOIN]");
        Util.plot(connectedNodes);
        return connectedNodes;
    }


    public int getAndSetNum() {
        int temp = num;
        num++;
        return temp;
    }


    public NetNodeWrap add(String ip, int port) throws RemoteException {

        return null;
    }


    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    public String getHostName() {
        return hostName;
    }


    public void updateCoNodes(HashMap<Integer, NetNodeLocation> coNodes) {

        for (Map.Entry<Integer, NetNodeLocation> entry : coNodes.entrySet()) {

            for (Map.Entry<Integer, NetNodeLocation> entry2 : connectedNodes.entrySet()) {

                if (!entry.getKey().equals(entry2.getKey())) {

                    System.out.println("Aggiungo il nodo : " + entry.getKey().toString() + "ai connectedNodes di : " + hostName);

                    connectedNodes.put((entry.getValue().getIp() + entry.getValue().getIp()).hashCode(), entry.getValue());

                    System.out.println("Comunicazione con : " + entry.getValue().toString());
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                        String path = entry.getValue().toUrl() + entry.getKey();
                        System.out.println(path);
                        NetNode netNodeTemp = (NetNode) registry.lookup(path);
                        //System.out.println(hostName + " saluta : " + netNodeTemp.getHostName() + "--" + netNodeTemp.saluta());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        System.out.println("problema strano2");

                    } catch (NotBoundException e) {
                        e.printStackTrace();
                        System.out.println("non trovato errore2");

                    }

                }


            }

        }


    }


}
