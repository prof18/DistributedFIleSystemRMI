package net.objects;

import fs.actions.CacheFileWrapper;
import fs.objects.structure.FileAttribute;
import net.actions.GarbageService;
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

    public NetNodeImpl(String path, String ownIP, int port, String name) throws RemoteException {
        super();
        this.path = path;
        this.ownIP = ownIP;
        this.port = port;
        connectedNodes = new HashMap<>();
        connectedNodes.put((ownIP + port).hashCode(), new NetNodeLocation(ownIP, port, name));
        System.out.println("[COSTRUTTORE]");
        Util.plot(connectedNodes);
        System.out.println("AVVIO THREAD");
        GarbageService v;
        try {
            v = new GarbageService(this.ownIP, this.hostName, this.port);
            Thread t = new Thread(v);
            t.start();
        } catch (RemoteException e) {
            System.out.println("Avvio Thread Remote Exc");
            e.printStackTrace();
        }


    }

    @Override
    public String getHost() {
        return ownIP;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public synchronized HashMap<Integer, NetNodeLocation> join(String ipNode, int port, String name) {
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


    public NetNodeWrap add(String ip, int port) {
        return null;
    }

    @Override
    public String getHostName() {
        return hostName;
    }


    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    @Override
    public CacheFileWrapper getFile(String UFID) {
        File file = new File(path + UFID);
        FileAttribute ret = null;
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream("test.attr"));
                ret = (FileAttribute) ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            NetNodeLocation netNodeLocation = new NetNodeLocation(ownIP, port, hostName);
            return new CacheFileWrapper(file, ret, netNodeLocation);
        } else {
            return null;
        }
    }

    public String replaceFile(CacheFileWrapper newFile,long lastModified,String UFID) throws RemoteException{
        CacheFileWrapper file=getFile(UFID);
        try {
            FileInputStream fis=new FileInputStream(UFID);
            System.out.println("file "+new String(fis.readAllBytes()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UFID = " + UFID);
        if(file==null){
            return "In questo host il file "+UFID+" non è presente";
        }
        else{
            // se il file in questo host non è stato modicato nel mentre si procede alla modifica
            if(lastModified==file.getAttribute().getLastModifiedTime().getTime())
            {
                File file1=new File(UFID);
                file1.delete();
                file1=new File(UFID+".attr");
                file1.delete();
                File newFileh=new File(UFID);
                try {
                    FileWriter writer=new FileWriter(newFileh);
                    System.out.println("Writer : "+newFile.getFile().toString());
                    writer.write(newFile.getFile().toString());
                    ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(UFID+".attr"));
                    ois.writeObject(newFile.getAttribute());
                    ois.flush();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //altrimenti si lancia un'eccezione
            return "Il file "+UFID+" è stato modificato";
        }
    }


    public synchronized void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
        Util.plot(this.connectedNodes);
    }

    @Override
    public String verify() {
        return "--COLLEGAMENTO VERIFICATO--";
    }

    public synchronized void checkNodes() {

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
