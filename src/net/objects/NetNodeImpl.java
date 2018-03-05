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

    private String hostName = "host";

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

    @Override
    public String getHostName() throws RemoteException {
        return hostName;
    }


    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    public void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    @Override
    public String verify() throws RemoteException {
        return " collegamento verificato";
    }


}
