package net.objects;

import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.actions.GarbageService;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NetNodeImpl extends UnicastRemoteObject implements NetNode {


    private String path;
    private NetNodeLocation ownLocation;

    private MediatorFsNet mediatorFsNet;

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
    public synchronized JoinWrap join(String ipNode, int port, String name) {

        //check if name is already used
        String newName = checkHostName(name);

        connectedNodes.put((ipNode + port).hashCode(), new NetNodeLocation(ipNode, port, newName));

        Util.plot(connectedNodes);

        mainUI.updateConnectedNode(connectedNodes);

       // HashMap<Integer, NetNodeLocation> connectedNodes

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

    @Override
    public String getHostName() {
        return ownLocation.getName();
    }

    @Override
    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    @Override
    public CacheFileWrapper getFileOtherHost(String UFID) {
        //searching for the file in each nodes of the distributed FS
        for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
            NetNodeLocation location = entry.getValue();
            Registry registry;
            CacheFileWrapper fileWrapper = null;
            try {
                registry = LocateRegistry.getRegistry(location.getIp(), location.getPort());
                NetNode node = (NetNode) registry.lookup(location.toUrl());
                fileWrapper = node.getFile(UFID);
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }

            if (fileWrapper != null) {
                return fileWrapper;
            }
        }
        return null;
    }

    @Override
    public void replaceFileFromFS(ArrayList<WritingCacheFileWrapper> fileWrappers) {
        //searching a specific file in the distributed filesystem in order to update it
        for (WritingCacheFileWrapper fileWrapper : fileWrappers) {
            for (Map.Entry<Integer, NetNodeLocation> entry : connectedNodes.entrySet()) {
                if (!entry.getValue().equals(ownLocation)) {
                    NetNodeLocation location = entry.getValue();
                    Registry registry;
                    try {
                        registry = LocateRegistry.getRegistry(location.getIp(), location.getPort());
                        NetNode node = (NetNode) registry.lookup(location.toUrl());
                        System.out.println(node.replaceFile(fileWrapper, fileWrapper.getAttribute().getLastModifiedTime().getTime(), fileWrapper.getUFID()));
                    } catch (RemoteException | NotBoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public CacheFileWrapper getFile(String UFID) {
        return mediatorFsNet.getFileFromFS(UFID);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public String replaceFile(CacheFileWrapper newFile, long lastModified, String UFID) {
        //updating of a file in the own node
        CacheFileWrapper file = getFile(UFID);
        if (file == null) {
            return "In this host the file " + UFID + " is not present";
        } else {
            // if the file in this host is not edited it will be updated
            File file1 = new File(path + UFID);
            file1.delete();
            file1 = new File(path + UFID + ".attr");
            file1.delete();
            File fileToReplace = new File(path + UFID);
            try {
                FileOutputStream writer = new FileOutputStream(fileToReplace);
                writer.write(newFile.getContent());
                ObjectOutputStream ois = new ObjectOutputStream(new FileOutputStream(path + UFID + ".attr"));
                ois.writeObject(newFile.getAttribute());
                ois.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "The file " + UFID + " has been edited";
        }
    }


    public synchronized void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    @Override
    public String verify() {
        return "Connection verified";
    }

    public synchronized void checkNodes() {

        HashMap<Integer, NetNodeLocation> downNodes = new HashMap<>();

        //for each node in the list of connected nodes is verified the reachability
        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if ((ownLocation.getIp() + ownLocation.getPort()).hashCode() != entry.getKey()) {

                Registry registry;
                String tmpIp;
                int tmpPort;
                String tmpName;
                try {
                    tmpIp = entry.getValue().getIp();
                    tmpPort = entry.getValue().getPort();
                    tmpName = entry.getValue().getName();
                    registry = LocateRegistry.getRegistry(tmpIp, tmpPort);
                    String tmpPath = "rmi://" + tmpIp + ":" + tmpPort + "/" + tmpName;
                    NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
                    System.out.println(nodeTemp.verify());

                } catch (RemoteException e) {
                    downNodes.put(entry.getKey(), entry.getValue());
                    e.printStackTrace();

                } catch (NotBoundException e) {
                    System.out.println("[CheckNodes NotBoundException]");
                    e.printStackTrace();
                }
            }
        }

        for (Map.Entry<Integer, NetNodeLocation> entry : downNodes.entrySet()) {
            if (connectedNodes.containsKey(entry.getKey())) {
                //System.out.println("REMOVED NODE, port: " + entry.getValue().getPort() + "; Ip: " + entry.getValue().getIp());
                connectedNodes.remove(entry.getKey());
                mainUI.updateConnectedNode(connectedNodes);
            }
        }
    }
}
