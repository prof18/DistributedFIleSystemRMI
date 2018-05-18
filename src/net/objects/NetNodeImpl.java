package net.objects;

import fs.actions.FSStructure;
import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.json.JsonFile;
import fs.objects.json.JsonFolder;
import fs.objects.structure.FSTreeNode;
import mediator_fs_net.MediatorFsNet;
import net.actions.GarbageService;
import net.objects.interfaces.NetNode;
import ui.frame.MainUI;
import utils.Constants;
import utils.GSONHelper;
import utils.PropertiesHelper;
import utils.Util;

import java.io.*;
import java.nio.file.Files;
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
    private HashMap<String, ListFileWrapper> fileNodeList = new HashMap(); //hashmap file-nodi che possiedono una copia di tale file.
    private MainUI mainUI;

    public NetNodeImpl(String path, String ownIP, int port, MediatorFsNet mediatorFsNet1, MainUI mainUI) throws RemoteException {
        super();

        this.path = path;
        mediatorFsNet = mediatorFsNet1;
        this.mainUI = mainUI;

        //creation of a random name for the new nodes
        String name = "host" + new Random().nextInt(1000);

        this.ownIP = ownIP;
        this.port = port;

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


    public HashMap<String, ListFileWrapper> getFileNodeList() {
        return fileNodeList;
    }

    public void setFileNodeList(HashMap<String, ListFileWrapper> fileNodeList) {
        this.fileNodeList = fileNodeList;
    }

    public void updateWritePermissionMap(String UFID, ListFileWrapper listFileWrapper) {
        System.out.println("UPDATE FILE NODE LIST");
        if (fileNodeList.containsKey(UFID)) {
            fileNodeList.replace(UFID, fileNodeList.get(UFID), listFileWrapper);
        } else {
            fileNodeList.put(UFID, listFileWrapper);
        }
        System.out.println("VISUALIZZAZIONE DELL'ELENCO FLAG");
        for (Map.Entry<String, ListFileWrapper> entry : fileNodeList.entrySet()) {
            System.out.println(entry.getKey() + "   " + entry.getValue().isWritable());
        }

    }

    public NetNodeLocation getOwnLocation() {
        return ownLocation;
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
        System.out.println("aggiornamento nodi UI " + getHostName());
        mainUI.updateConnectedNode(connectedNodes);

        return new JoinWrap(newName, connectedNodes, fileNodeList);
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

    public int getOwnPort() {
        return ownLocation.getPort();
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
                        System.out.println(node.
                                replaceFile(fileWrapper, fileWrapper.getAttribute().getLastModifiedTime().getTime(), fileWrapper.getUFID()));
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
            //bisogna aggiungere il path del file;
            FileInputStream fis = new FileInputStream(path + UFID);
            //TODO: aggiungere cattura del file
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

    public synchronized void checkNodesAndReplica() {

        HashMap<Integer, NetNodeLocation> downNodes = new HashMap<>();

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if (!((ownIP + port).hashCode() == entry.getKey())) {

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
                    //e.printStackTrace();

                } catch (NotBoundException e) {
                    System.out.println("NotBoundException checkNodes2");
                    //e.printStackTrace();

                }

            }
        }

        for (Map.Entry<Integer, NetNodeLocation> entry : downNodes.entrySet()) {
            if (connectedNodes.containsKey(entry.getKey())) {
                System.out.println("RIMOSSO NODO, porta: " + entry.getValue().getPort() + "; Ip: " + entry.getValue().getIp());
                connectedNodes.remove(entry.getKey());
            }
        }


        //CONTROLLO DELLA REPLICAZIONE

        System.out.println("[ CHECK REPLICA ]");

        for (Map.Entry<String, ListFileWrapper> entry : fileNodeList.entrySet()) {

            ListFileWrapper tmp = entry.getValue();
            ArrayList<NetNodeLocation> tmpLocations = tmp.getLocations();

            if (tmpLocations.size() > 2) {
                System.out.println("ERRORE DUPLICAZIONE SBAGLIATA");
            }


            if (tmpLocations.size() == 2) {
                if (tmpLocations.get(0).equals(this.ownLocation)) {

                    boolean verified = this.checkSecReplica(tmpLocations.get(1), entry.getKey());
                    if (!verified) {
                        CacheFileWrapper cacheFileWrapper = mediatorFsNet.getFile(entry.getKey());
                        callSaveFile(tmpLocations.get(1), cacheFileWrapper);
                    }

                } else if (tmpLocations.get(1).equals(this.ownLocation)) {

                    boolean verified = this.checkSecReplica(tmpLocations.get(0), entry.getKey());
                    if (!verified) {
                        CacheFileWrapper cacheFileWrapper = mediatorFsNet.getFile(entry.getKey());
                        callSaveFile(tmpLocations.get(0), cacheFileWrapper);
                    }

                }
            } else {

                if (tmpLocations.get(0).equals(this.ownLocation)) {

                    System.out.println("CREATE THE MISSED REPLICA");
                    CacheFileWrapper cacheFileWrapper = mediatorFsNet.getFile(entry.getKey());
                    callSaveFileReplica(cacheFileWrapper, entry.getKey());

                }

            }
        }

    }

    public void callSaveFileReplica(CacheFileWrapper cacheFileWrapper, String UFID) {

        ReplicationWrapper rw = new ReplicationWrapper(cacheFileWrapper.getUFID(), null);
        rw.setAttribute(cacheFileWrapper.getAttribute());

        byte[] ftb = Util.fileToBytes(path + "/" + UFID);
        byte[] fatb = Util.fileToBytes(path + "/" + UFID + ".attr");
        byte[] tftb = Util.append(ftb, fatb);

        rw.setChecksum(Util.getChecksum(tftb));
        rw.setContent(cacheFileWrapper.getContent());

        ArrayList<NetNodeLocation> nodeList = new ArrayList<>();
        Collection<NetNodeLocation> tmpColl = connectedNodes.values();
        if (tmpColl != null) {
            for (NetNodeLocation nnl : tmpColl) {
                if (!nnl.equals(ownLocation)) {
                    nodeList.add(nnl);
                }
            }
        }

        ArrayList<NetNodeLocation> nodeBiggerTime = Util.listOfMaxConnectedNode(nodeList);
        NetNodeLocation selectedNode = Util.selectedNode(nodeBiggerTime);

        Registry registry = null;

        String tmpIp = "-NOT UPDATE-";
        int tmpPort = -1;
        boolean ver = false;

        try {

            tmpIp = selectedNode.getIp();
            tmpPort = selectedNode.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = selectedNode.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
            nodeTemp.saveFileReplica(rw);

        } catch (RemoteException er) {
            System.out.println("checkSecReplica");

        } catch (NotBoundException er) {
            System.out.println("checkSecReplica2");
        }

    }

    public boolean checkSecReplica(NetNodeLocation e, String fileName) {

        System.out.println("[ CHECKReplica]");

        Registry registry = null;

        String tmpIp = "-NOT UPDATE-";
        int tmpPort = -1;
        boolean ver = false;

        try {

            tmpIp = e.getIp();
            tmpPort = e.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = e.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
            ver = nodeTemp.verifyFile(fileName);

        } catch (RemoteException er) {
            System.out.println("checkSecReplica");

        } catch (NotBoundException er) {
            System.out.println("checkSecReplica2");
        }

        return ver;

    }

    public boolean callSaveFile(NetNodeLocation e, CacheFileWrapper cacheFileWrapper) {

        System.out.println("[ call Save File ]");

        Registry registry = null;

        String tmpIp = "-NOT UPDATE-";
        int tmpPort = -1;
        try {

            tmpIp = e.getIp();
            tmpPort = e.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = e.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);

            nodeTemp.saveFile(cacheFileWrapper);

        } catch (RemoteException er) {
            System.out.println("callSaveFileReplica");

        } catch (NotBoundException er) {
            System.out.println("callSaveFileReplica2");
        }

        return true;
    }

    public boolean saveFile(CacheFileWrapper cacheFileWrapper) {

        File f = new File(path + cacheFileWrapper.getUFID());
        File fileAtt = new File(path + cacheFileWrapper.getUFID() + ".attr");

        try {

            FileOutputStream fos = new FileOutputStream(f, false);
            fos.write(cacheFileWrapper.getContent());
            fos.flush();
            fos.close();

            fos = new FileOutputStream(fileAtt, false);
            ObjectOutputStream oot = new ObjectOutputStream(fos);
            oot.writeObject(cacheFileWrapper.getAttribute());
            oot.flush();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;


    }

    public boolean verifyFile(String fileName) {
        return new File(path + "/" + fileName).isFile();
    }

    public boolean saveFileReplica(ReplicationWrapper rw) {

        String filePath = path;

        System.out.println("saveFileReplica");
        System.out.println(rw.getUFID());

        File f = new File(filePath + rw.getUFID());
        File fileAtt = new File(filePath + rw.getUFID() + ".attr");

        try {

            FileOutputStream fos = new FileOutputStream(f, false);
            fos.write(rw.getContent());
            fos.flush();
            fos.close();

            fos = new FileOutputStream(fileAtt, false);
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

        try {
            byte[] ftb = Files.readAllBytes(Paths.get(f.getPath()));
            byte[] fatb = Files.readAllBytes(Paths.get(fileAtt.getPath()));
            bytesArray = Util.append(ftb, fatb);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String checksum = Util.getChecksum(bytesArray);
//        System.out.println("Checksum in saveFileReplica: " + checksum);
//        System.out.println("Checksum contenuto nel replicationWrapper: " + rw.getChecksum());
        if (checksum.compareTo(rw.getChecksum()) != 0) {
            return false;
        }


        if (rw.getjSon() != null) {
            PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, rw.getjSon());
        }
        //  FSStructure.getInstance().generateTreeStructure();

        return true;
    }

    public boolean deleteFile(String UFID, String treeFileDirectoryUFID) {
        String filePath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        String totalFilePath = filePath + UFID;
        System.out.println("Percorso totale file: " + totalFilePath);
        boolean filesDeleted = false;
        File file = new File(totalFilePath);
        File fileAttr = new File(totalFilePath + ".attr");
        boolean fileDelete = file.delete();
        boolean attrDelete = fileAttr.delete();

        System.out.println("Cancellazione file: " + UFID);
        System.out.println("Percorso file: " + filePath);

        if (fileDelete && attrDelete) {
            filesDeleted = true;
            mediatorFsNet.removeFileFromTree(UFID, treeFileDirectoryUFID);
        }

        if (filesDeleted && fileNodeList.containsKey(UFID)) {
            fileNodeList.remove(UFID);
        }

        return filesDeleted;
    }

    public MediatorFsNet getMediator() {
        return mediatorFsNet;
    }

    public void nodeFileAssociation(String UFID, NetNodeLocation netNode) {
        System.out.println("NODE FILE REPLICATION");
        if (!fileNodeList.containsKey(UFID)) {
            ArrayList<NetNodeLocation> a = new ArrayList<>();
            a.add(netNode);
            fileNodeList.put(UFID, new ListFileWrapper(a));
        } else {
            fileNodeList.get(UFID).getLocations().add(netNode);
        }

        for (NetNodeLocation nnl : connectedNodes.values()) {
            Registry registry = null;
            try {
                registry = LocateRegistry.getRegistry(nnl.getIp(), nnl.getPort());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.out.println("Node URL: " + nnl.toUrl());
            try {
                NetNode nn = (NetNode) registry.lookup(nnl.toUrl());
                nn.updateWritePermissionMap(UFID, fileNodeList.get(UFID));
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }

        }
    }

    public void updateUI(FSTreeNode treeRoot) {
        mediatorFsNet.updateJson(treeRoot);
    }

    public String getJson() {
        return PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
    }

    public synchronized void setJson(String json, boolean up) {
        PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, json);

        if (up) {
            System.out.println("[MONA SONO NELL ALTRO TERMINALE PER QUELLO NON MI VEDI]");
            FSStructure.getInstance().generateTreeStructure();
            mediatorFsNet.updateJson(FSStructure.getInstance().getTree().findRoot());
            //MainUI.updateModels(FSStructure.getInstance().getTree().findRoot(), false);
        }

    }

    public synchronized void updateJson(String json) {

        System.out.println("[UPDATE JSON]");

        GSONHelper helpJson = GSONHelper.getInstance();

        HashMap<String, JsonFolder> receivedFolder = helpJson.jsonToFolders(json);

        String thisJson = this.getJson();

        if (thisJson != null) {

            System.out.println("[Json già presente]");

            HashMap<String, JsonFolder> ownFolder = helpJson.jsonToFolders(thisJson);

            ArrayList<JsonFile> ownFilesRoot = ownFolder.get("root").getFiles();

            Date date = new Date();
            String time = date.toString();

            if (ownFilesRoot.size() != 0) {

                for (int i = 0; i < ownFilesRoot.size(); i++) {

                    String currentName = ownFilesRoot.get(i).getFileName();
                    String currentUFID = ownFilesRoot.get(i).getUFID();

                    File file = new File(path + "/" + currentUFID);
                    if (file.exists()) {

                        System.out.println("IL FILE ESISTE QUINDI AGGIORNO IL JSON");
                        boolean sameUFID = false;

                        for (int j = 0; j < receivedFolder.get("root").getFiles().size(); j++) {

                            if (receivedFolder.get("root").getFiles().get(j).getUFID().equals(currentUFID)) {
                                sameUFID = true;
                                break;
                            }

                        }

                        boolean sameName = false;
                        if (!sameUFID) {
                            for (int j = 0; j < receivedFolder.get("root").getFiles().size(); j++) {

                                if (receivedFolder.get("root").getFiles().get(j).getFileName().equals(currentName)) {
                                    sameName = true;
                                    break;
                                }

                            }
                        }

                        if (sameName) {

                            String newName = currentName + " ( " + "offline different file" + " " + time + " ) ";
                            ownFilesRoot.get(i).setFileName(newName);
                            receivedFolder.get("root").getFiles().add(ownFilesRoot.get(i));


                        } else if (sameUFID) {

                            file.delete();
                            File attr = new File(path + "/" + currentUFID + ".attr");
                            attr.delete();
                            System.out.println("cancello il file in locale");

                        } else {
                            receivedFolder.get("root").getFiles().add(ownFilesRoot.get(i));
                        }
                    }
                }
            }

            ownFolder.remove("root");

            for (Map.Entry<String, JsonFolder> entry : ownFolder.entrySet()) {

                if (!receivedFolder.containsKey(entry.getKey())) {

                    ArrayList<JsonFile> files = entry.getValue().getFiles();
                    ArrayList<JsonFile> filesExist = new ArrayList<>();

                    for (int i = 0; i < files.size(); i++) {
                        File file = new File(path + "/" + files.get(i).getUFID());
                        if (file.exists()) {
                            filesExist.add(files.get(i));
                        }
                    }

                    entry.getValue().setFiles(filesExist);

                    if (filesExist.size() > 0) {
                        String currentName = entry.getValue().getFolderName();

                        for (Map.Entry<String, JsonFolder> entry2 : receivedFolder.entrySet()) {

                            if (entry2.getValue().getFolderName().equals(currentName)) {

                                String newName = currentName + " ( offline different folder " + time + " )";
                                entry.getValue().setFolderName(newName);
                                break;
                            }
                        }

                        receivedFolder.put(entry.getKey(), entry.getValue());
                        if (entry.getValue().getParentUFID().equals("root")) {
                            receivedFolder.get("root").getChildren().add(entry.getKey());
                        }
                    }

                }
//                else{
//
//                    ArrayList<JsonFile> files =  entry.getValue().getFiles();
//
//                    for (int i = 0; i< files.size(); i++ ) {
//
//                    }
//
//
//                }
            }

            String newJson = helpJson.foldersToJson(receivedFolder);
            this.setJson(newJson, false);

            this.callUpdateAllJson(newJson);


        } else {
            System.out.println("[Json non presente]");
            this.setJson(json, false);
        }

    }


    public void callUpdateAllJson(String json) {

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if ((ownIP + port).hashCode() != entry.getKey()) {

                System.out.println("[callUpdateAllJson]");

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
                    //nodeTemp.updateJson(json, true);
                    nodeTemp.setJson(json, true);

                } catch (RemoteException e) {
                    System.out.println("[callUpdateAllJson] problemi connessione" + tmpPort + "; Ip: " + tmpIp);

                } catch (NotBoundException e) {
                    System.out.println("[NotBoundException-callUpdateAllJson] problemi connessione\" + tmpPort + \"; Ip: \" + tmpIp");
                    e.printStackTrace();

                }
            }
        }
    }
}