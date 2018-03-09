package net.objects;

import fs.actions.CacheFileWrapper;
import fs.objects.structure.FileAttribute;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private String ownIP;
    private int port;
    private int num = 0;
    private String hostName = "host";
    private String path;

    //<host,ip>
    private HashMap<Integer, NetNodeLocation> connectedNodes;

    public NetNodeImpl(String path,String ownIP, int port, String name) throws RemoteException {
        super();
        this.path=path;
        this.ownIP = ownIP;
        this.port = port;
        connectedNodes = new HashMap<>();
        connectedNodes.put((ownIP + port).hashCode(), new NetNodeLocation(ownIP, port, name));
        System.out.println("[COSTRUTTORE]");
        Util.plot(connectedNodes);


    }

    @Override
    public String getHost() throws RemoteException {
        return ownIP;
    }

    @Override
    public int getPort() throws RemoteException {
        return port;
    }

    @Override
    public synchronized HashMap<Integer, NetNodeLocation> join(String ipNode, int port, String name) throws RemoteException {
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

    @Override
    public CacheFileWrapper getFile(String UFID) throws RemoteException {
        File file = new File(path + UFID);
        File fileAttribute = null;
        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(path + UFID + ".attr");
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                fileAttribute = (File) objectInputStream.readObject();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            NetNodeLocation netNodeLocation = new NetNodeLocation(ownIP, port, hostName);
            return new CacheFileWrapper(file, null, netNodeLocation);
        } else
            return null;


    }

    public synchronized void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
        Util.plot(this.connectedNodes);
    }

    @Override
    public String verify() throws RemoteException {
        return "--COLLEGAMENTO VERIFICATO--";
    }

    public synchronized void checkNodes() throws RemoteException {

        HashMap<Integer, NetNodeLocation> downNodes = new HashMap<>();

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if ((ownIP + port).hashCode() == entry.getKey()) {

                System.out.println("[ CHECKNODES ]");

                Registry registry = null;

                String tmpIp = "-NOT UPDATE-";
                int tmpPort = -1;
                String tmpName = "-NOT UPDATE-";

                try {

                    tmpIp = entry.getValue().getIp();
                    tmpPort = entry.getValue().getPort();
                    tmpName = entry.getValue().getName();

                    registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

                    String tmpPath = "rmi://" + tmpIp + ":" + tmpPort + "/" + tmpName;

                    NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
                    System.out.println(nodeTemp.verify());

                } catch (RemoteException e) {
                    System.out.println("NODO non trovato alla porta: " + tmpPort + "; Ip: " + tmpIp);
                    downNodes.put(entry.getKey(), entry.getValue());
                    e.printStackTrace();

                } catch (NotBoundException e) {
                    System.out.println("NotBoundException checkNodes2");
                    e.printStackTrace();

                }

            }
        }

        for (Map.Entry<Integer, NetNodeLocation> entry : downNodes.entrySet()) {
            if (connectedNodes.containsKey(entry.getKey())) {
                System.out.println("RIMOSSO NODO, porta: " + entry.getValue().getPort() + "; Ip: " + entry.getValue().getIp());
                connectedNodes.remove(entry.getKey());
            }
        }


    }

}
