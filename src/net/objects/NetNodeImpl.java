package net.objects;

import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FileAttribute;
import mediator_fs_net.MediatorFsNet;
import net.actions.GarbageService;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private MediatorFsNet mediatorFsNet;
    private String ownIP;
    private int port;
    private int num = 0;
    private String hostName = "host";
    private String path;
    private NetNodeLocation ownLocation;
    //<host,ip>
    private HashMap<Integer, NetNodeLocation> connectedNodes;

    public NetNodeImpl(String path, String ownIP, int port, String name,MediatorFsNet mediatorFsNet1) throws RemoteException {
        super();
        mediatorFsNet=mediatorFsNet1;
        this.path = path;
        this.ownIP = ownIP;
        this.port = port;
        ownLocation=new NetNodeLocation(ownIP,port,name);
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

    public CacheFileWrapper getFileOtherHost(String UFID){
        System.out.println("getFileOtherHosts "+UFID+" del nodo : "+this.ownLocation.toUrl());
        for (Map.Entry<Integer,NetNodeLocation> entry:connectedNodes.entrySet()) {
            NetNodeLocation location=entry.getValue();
            Registry registry= null;
            CacheFileWrapper fileWrapper=null;
            try {
                registry = LocateRegistry.getRegistry(location.getIp(),location.getPort());
                NetNode node=(NetNode) registry.lookup(location.toUrl());
                fileWrapper=node.getFile(UFID);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

            if(fileWrapper!=null) {
                System.out.println("ritornato da getFileOtherHosts");
                return fileWrapper;
            }
        }
        return null;
    }

    @Override
    public void replaceFileFromFS(ArrayList<WritingCacheFileWrapper> fileWrappers) {
        System.out.println("Entrato in replaceFileFromFS del nodo "+ownLocation.toUrl());
        for (WritingCacheFileWrapper fileWrapper : fileWrappers) {
            for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
                if (!entry.getValue().equals(ownLocation)) {
                    NetNodeLocation location = entry.getValue();
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(location.getIp(), location.getPort());
                        System.out.println("[replaceFileFromFS]visitando il nodo : "+location.toUrl());
                        NetNode node = (NetNode) registry.lookup(location.toUrl());
                        node.replaceFile(fileWrapper, fileWrapper.getAttribute().getLastModifiedTime().getTime(), fileWrapper.getUFID());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    } catch (NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public CacheFileWrapper getFile(String UFID) {
        System.out.println("getFile "+UFID+" del nodo : "+this.ownLocation.toUrl());
        return mediatorFsNet.getFilefromFS(UFID);
    }

    public String replaceFile(CacheFileWrapper newFile,long lastModified,String UFID) {
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
