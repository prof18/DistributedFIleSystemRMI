package net.objects;

import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.actions.GarbageService;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.Constants;
import utils.PropertiesHelper;
import utils.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private MediatorFsNet mediatorFsNet;
    private String ownIP;
    private int port;
    //private String hostName = "host";
    private String path;
    private NetNodeLocation ownLocation;
    //<host,ip>
    private HashMap<Integer, NetNodeLocation> connectedNodes;

    private MainUI mainUI;

    public NetNodeImpl(String path, String ownIP, int port, MediatorFsNet mediatorFsNet1, MainUI mainUI) throws RemoteException {
        super();

        this.path = path;
        mediatorFsNet = mediatorFsNet1;
        this.mainUI = mainUI;

        //creation of a random name for the new nodes
        String name = "host" + new Random().nextInt(1000);

        ownLocation = new NetNodeLocation(ownIP, port, name);
        connectedNodes = new HashMap<>();
        connectedNodes.put((ownIP + port).hashCode(), new NetNodeLocation(ownIP, port, name));

        Util.plot(connectedNodes);

        mainUI.updateConnectedNode(connectedNodes);

        //starting the service that controls the reachable nodes
        GarbageService v;
        try {
            v = new GarbageService(ownLocation.getIp(), ownLocation.getName(), ownLocation.getPort());
            Thread t = new Thread(v);
            t.start();
        } catch (RemoteException e) {
            System.out.println("Error in the initialization of the garbage collector, please restart the program");
            System.exit(-1);
        }

    }

    @Override
    public String getOwnIp() {
        return ownLocation.getIp();
    }


    @Override
    public synchronized JoinWrap join(String ipNode, int port, String name) {
        System.out.println("si è connesso un nuovo nodo: " + ipNode + " " + port + " " + name);

        //check if name is already used
        String newName = checkHostName(name);

        connectedNodes.put((ipNode + port).hashCode(), new NetNodeLocation(ipNode, port, newName));
        System.out.println("[JOIN]");
        Util.plot(connectedNodes);
        System.out.println("aggiornamento nodi UI "+getHostName());
        mainUI.updateConnectedNode(connectedNodes);

        return new JoinWrap(newName, connectedNodes);
    }

    @Override
    public void setNameLocation(String name) {
        this.ownLocation.setName(name);
    }

    @Override
    public String checkHostName(String oldName) {

        String newName = oldName;
        boolean validName = false;
        while (!validName) {

            boolean changed = false;
            for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

                NetNodeLocation tmp = entry.getValue();

                if (tmp.getName().equals(oldName)) {
                    newName = "host" + new Random().nextInt(1000);
                    changed = true;
                }
            }

            validName = !changed;
        }
        return newName;
    }

    /*public NetNodeWrap add(String ip, int port) {
        return null;
    }*/

    @Override
    public String getHostName() {
        return ownLocation.getName();
    }


    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    public CacheFileWrapper getFileOtherHost(String UFID) {
        System.out.println("getFileOtherHosts " + UFID + " del nodo : " + this.ownLocation.toUrl());
        for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
            NetNodeLocation location = entry.getValue();
            Registry registry = null;
            CacheFileWrapper fileWrapper = null;
            try {
                registry = LocateRegistry.getRegistry(location.getIp(), location.getPort());
                NetNode node = (NetNode) registry.lookup(location.toUrl());
                fileWrapper = node.getFile(UFID);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }

            if (fileWrapper != null) {
                System.out.println("ritornato da getFileOtherHosts");
                return fileWrapper;
            }
        }
        return null;
    }

    @Override
    public void replaceFileFromFS(ArrayList<WritingCacheFileWrapper> fileWrappers) {
        System.out.println("Entrato in replaceFileFromFS del nodo " + ownLocation.toUrl());
        for (WritingCacheFileWrapper fileWrapper : fileWrappers) {
            for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
                if (!entry.getValue().equals(ownLocation)) {
                    NetNodeLocation location = entry.getValue();
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(location.getIp(), location.getPort());
                        System.out.println("[replaceFileFromFS]visitando il nodo : " + location.toUrl());
                        NetNode node = (NetNode) registry.lookup(location.toUrl());
                        System.out.println(node.replaceFile(fileWrapper, fileWrapper.getAttribute().getLastModifiedTime().getTime(), fileWrapper.getUFID()));
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
        System.out.println("getFile " + UFID + " del nodo : " + this.ownLocation.toUrl());
        return mediatorFsNet.getFilefromFS(UFID);
    }

    public String replaceFile(CacheFileWrapper newFile, long lastModified, String UFID) {
        System.out.println("entrato in replaceFile del nodo : " + ownLocation.toUrl());
        CacheFileWrapper file = getFile(UFID);
        try {
            FileInputStream fis = new FileInputStream(UFID);
            System.out.println("file obsoleto è" + new String(fis.readAllBytes()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("UFID = " + UFID);
        if (file == null) {
            System.out.println("[REPLACE FILE] il file non è presente nel nodo " + ownLocation.toUrl());
            return "In questo host il file " + UFID + " non è presente";
        } else {
            System.out.println("lastModified : " + lastModified);
            System.out.println("lastModified other : " + file.getAttribute().getLastModifiedTime().getTime());
            // se il file in questo host non è stato modicato nel mentre si procede alla modifica
            //TODO è stato tolto il check per fare delle prove lastModified == file.getAttribute().getLastModifiedTime().getTime()
            //TODO è un errore da capire
            if (true) {
                System.out.println("[REPLACE FILE non è stato modificato]");
                File file1 = new File(path + UFID);
                System.out.println("eliminato il file " + file1.delete());
                file1 = new File(path + UFID + ".attr");
                System.out.println("eliminato il file attributi" + file1.delete());
                File newFileh = new File(path + UFID);
                try {
                    FileOutputStream writer = new FileOutputStream(newFileh);
                    System.out.println("FileOutputStream : " + newFile.getFile().toString());
                    writer.write(newFile.getContent());
                    ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(path + UFID + ".attr"));
                    ois.writeObject(newFile.getAttribute());
                    ois.flush();
                    System.out.println("[REPLACEFILE] scrittura conclusa");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "Il file " + UFID + " è stato modificato";
            }
            //altrimenti si lancia un'eccezione
            return "impossibile";
        }
    }


    public synchronized void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
        Util.plot(this.connectedNodes);
        mainUI.updateConnectedNode(this.connectedNodes);

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

    public boolean saveFileReplica(ReplicationWrapper rw) {

        String filePath = rw.getPath();

        if (filePath.length() > 1) { //non è la radice
            String directoryPath = filePath.substring(0, filePath.length() - 1);
            File directory = new File(directoryPath);

            if (!directory.exists()) { //verifica esistenza della directory, se non esiste la crea.
                directory.mkdirs();
            }
        }

        File fileAtt = new File(filePath + rw.getUFID() + ".attr");
        File f = new File(filePath + rw.getUFID());

        try {

            FileOutputStream fos = new FileOutputStream(f, false);
            fos.write(rw.getContent());
            fos.flush();
            fos.close();

            fos = new FileOutputStream(fileAtt);
            ObjectOutputStream oot = new ObjectOutputStream(fos);
            oot.writeObject(rw.getAttribute());
            oot.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        byte[] bytesArray = null;
        Path path = Paths.get(f.getPath());

        try {
            bytesArray = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String checksum = Util.getChecksum(bytesArray);
        if (checksum.compareTo(rw.getChecksum()) != 0) {
            return false;
        }

        PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, rw.getjSon());

        return true;
    }

    /*public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public MediatorFsNet getMediator() {
        return mediatorFsNet;
    }

    @Override

    public boolean updateFileList(String fileID, ArrayList<NetNodeLocation> nodeList) {
        ArrayList<NetNodeLocation> nodeLocations = mediatorFsNet.getWrapperFileServiceUtil().getNetNodeList().get(fileID);
        for (NetNodeLocation nnl: nodeLocations) {
            if(nodeList.get(nodeList.indexOf(nnl)).canWrite()){
                nnl.lockWriting();
            }else{
                nnl.unlockWriting();
            }
        }

        return true;
    }
}