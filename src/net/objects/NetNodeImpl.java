package net.objects;

import fs.actions.FSStructure;
import fs.actions.ReplicationWrapper;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class NetNodeImpl extends UnicastRemoteObject implements NetNode {

    private MediatorFsNet mediatorFsNet;
    private String ownIP;
    private int port;
    private String path;
    private NetNodeLocation ownLocation;
    private HashMap<Integer, NetNodeLocation> connectedNodes;
    private HashMap<String, ListFileWrapper> fileNodeList = new HashMap<>();
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
            System.out.println("[NET-NODE-IMPL] Error in the initialization of the garbage collector, please restart the program");
            System.exit(-1);
        }

    }

    @Override
    public HashMap<String, ListFileWrapper> getFileNodeList() {
        return fileNodeList;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void setFileNodeList(HashMap<String, ListFileWrapper> receivedFNL, boolean typeSet) {

        if (typeSet) {

            for (Map.Entry<String, ListFileWrapper> entry : this.fileNodeList.entrySet()) {

                if (receivedFNL.containsKey(entry.getKey())) {
                    File file = new File(path + "/" + entry.getKey());
                    File attr = new File(path + "/" + entry.getKey() + ".attr");

                    file.delete();
                    attr.delete();

                } else {
                    receivedFNL.put(entry.getKey(), entry.getValue());
                }
            }

            this.fileNodeList = receivedFNL;

        } else {
            this.fileNodeList = receivedFNL;
        }


    }

    @Override
    public void modifyFileNodeList(HashMap<String, ListFileWrapper> toModify) {

        for (Map.Entry<String, ListFileWrapper> entry : toModify.entrySet()) {
            if (this.fileNodeList.containsKey(entry.getKey())) {
                this.fileNodeList.replace(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void beginFileNodeList() {

        String thisJson = this.getJson();

        GSONHelper helpJson = GSONHelper.getInstance();
        HashMap<String, JsonFolder> folder = helpJson.jsonToFolders(thisJson);

        HashMap<String, String> folderAndFile = new HashMap<>();

        HashMap<String, ListFileWrapper> fNodeList = new HashMap<>();

        for (Map.Entry<String, JsonFolder> entry : folder.entrySet()) {

            ArrayList<JsonFile> files = entry.getValue().getFiles();

            if (files.size() > 0) {
                for (JsonFile file1 : files) {

                    File file = new File(path + "/" + file1.getUFID());
                    File attr = new File(path + "/" + file1.getUFID() + ".attr");

                    if ((file.exists() && attr.exists())) {
                        ArrayList<NetNodeLocation> locations = new ArrayList<>();
                        locations.add(this.ownLocation);
                        ListFileWrapper tmp = new ListFileWrapper(locations);
                        fNodeList.put(file1.getUFID(), tmp);

                    } else {
                        folderAndFile.put(file1.getUFID(), entry.getKey());
                    }
                }
            }

        }

        FSStructure.getInstance().generateTreeStructure();
        FSTreeNode tree = FSStructure.getInstance().getTree();

        for (Map.Entry<String, String> entry : folderAndFile.entrySet()) {
            FSTreeNode tmp = tree.findNodeByUFID(tree, entry.getValue());
            tmp.removeOneFile(entry.getKey());
        }

        FSStructure.getInstance().generateJson(tree);
        this.setFileNodeList(fNodeList, false);
    }

    @Override
    public void updateWritePermissionMap(String UFID, ListFileWrapper listFileWrapper) {
        if (fileNodeList.containsKey(UFID)) {
            fileNodeList.replace(UFID, fileNodeList.get(UFID), listFileWrapper);
        } else {
            fileNodeList.put(UFID, listFileWrapper);
        }
    }

    @Override
    public NetNodeLocation getOwnLocation() {
        return ownLocation;
    }

    @Override
    public String getOwnIp() {
        return ownLocation.getIp();
    }

    @Override
    public synchronized JoinWrap join(String ipNode, int port, String name) {
        System.out.println("A new node is in the system: " + ipNode + " " + port + " " + name);

        //check if name is already used
        String newName = checkHostName(name);
        connectedNodes.put((ipNode + port).hashCode(), new NetNodeLocation(ipNode, port, newName));
        Util.plot(connectedNodes);
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

    @Override
    public String getHostName() {
        return ownLocation.getName();
    }

    @Override
    public int getOwnPort() {
        return ownLocation.getPort();
    }

    @Override
    public HashMap<Integer, NetNodeLocation> getHashMap() {
        return connectedNodes;
    }

    @Override
    public CacheFileWrapper getFileOtherHost(String UFID) {
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
                System.out.println("[GET-FILE-OTHER-HOST] Unable to get the file: " + UFID + " from other host");
            }

            if (fileWrapper != null) {
                return fileWrapper;
            }
        }
        return null;
    }


    @Override
    public CacheFileWrapper getFile(String UFID) {
        return mediatorFsNet.getFileFromFS(UFID);
    }

    @Override
    public synchronized void setConnectedNodes(HashMap<Integer, NetNodeLocation> connectedNodes) {
        this.connectedNodes = connectedNodes;
        Util.plot(this.connectedNodes);
        mainUI.updateConnectedNode(this.connectedNodes);
    }

    @Override
    public String verify() {
        return "--Connection Verified--";
    }

    @Override
    public synchronized void checkNodesAndReplica() {

        /*
        This routine verifies the nodes connection on the network and update the hashMap ConnectedNodes
         */

        System.out.println("[ CHECK NODES ]");

        HashMap<Integer, NetNodeLocation> downNodes = new HashMap<>();

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if (!((ownIP + port).hashCode() == entry.getKey())) {

                Registry registry;
                String tmpIp = "-NOT UPDATE-";
                int tmpPort = -1;
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
                    System.out.println("[CHECK-NODES-AND-REPLICA] Node not found on port: " + tmpPort + "; Ip: " + tmpIp);
                    downNodes.put(entry.getKey(), entry.getValue());

                } catch (NotBoundException e) {
                    System.out.println("[CHECK-NODES-AND-REPLICA] Node not found on port: " + tmpPort + "; Ip: " + tmpIp);
                    e.printStackTrace();
                }

            }
        }

        for (Map.Entry<Integer, NetNodeLocation> entry : downNodes.entrySet()) {
            if (connectedNodes.containsKey(entry.getKey())) {
                System.out.println("Node deleted, port: " + entry.getValue().getPort() + "; Ip: " + entry.getValue().getIp());
                connectedNodes.remove(entry.getKey());
            }
        }

        /*

        This second part of the routine instead verifies if the replica on the network are reachable or exist

        DISCLAIMER each node verifies only the replica of the files that it contains in local
        each node check its own files replica

         */

        boolean updateFileNodeList = false;

        System.out.println("[ CHECK REPLICA ]");

        /*
        If in the previous part a node is unreachable for sure some replica are lost
        and it is necessary to update the fileNodeList to remove the location of these replica
         */

        if (downNodes.size() != 0) {

            Util.plot(downNodes);
            Collection<NetNodeLocation> tmpColl = downNodes.values();

            for (NetNodeLocation nnl : tmpColl) {

                for (Map.Entry<String, ListFileWrapper> entry : fileNodeList.entrySet()) {

                    if (nnl.equals(entry.getValue().getLocations().get(0))) {
                        System.out.println("Node deleted:");
                        System.out.println(entry.getValue().getLocations().get(0).toString());
                        entry.getValue().getLocations().remove(0);

                    } else if (nnl.equals(entry.getValue().getLocations().get(1))) {
                        System.out.println("Node deleted:");
                        System.out.println(entry.getValue().getLocations().get(1).toString());
                        entry.getValue().getLocations().remove(1);
                    }
                }
            }
        }

        /*

        Here it verifies if all its files have a replica and in case it creates it

         */

        HashMap<String, NetNodeLocation> updateToDo = new HashMap<>();

        for (Map.Entry<String, ListFileWrapper> entry : fileNodeList.entrySet()) {

            ListFileWrapper tmp = entry.getValue();
            ArrayList<NetNodeLocation> tmpLocations = tmp.getLocations();

            CacheFileWrapper cacheFileWrapper = mediatorFsNet.getFile(entry.getKey());

            if (tmpLocations.size() == 2) {

                if (tmpLocations.get(0).equals(this.ownLocation)) {

                    boolean verified = this.checkSecReplica(tmpLocations.get(1), entry.getKey());
                    if (!verified) {
                        callSaveFile(tmpLocations.get(1), cacheFileWrapper);
                    }

                } else if (tmpLocations.get(1).equals(this.ownLocation)) {

                    boolean verified = this.checkSecReplica(tmpLocations.get(0), entry.getKey());
                    if (!verified) {
                        callSaveFile(tmpLocations.get(0), cacheFileWrapper);
                    }
                }

            } else if (tmpLocations.size() == 1) {

                if (tmpLocations.get(0).equals(this.ownLocation)) {

                    NetNodeLocation newLoc = callSaveFileReplica(cacheFileWrapper, entry.getKey());
                    updateToDo.put(entry.getKey(), newLoc);
                    updateFileNodeList = true;
                }
            }
        }

        HashMap<String, ListFileWrapper> UpFNL = new HashMap<>();

        if (updateToDo.size() != 0) {
            for (Map.Entry<String, NetNodeLocation> entry : updateToDo.entrySet()) {
                fileNodeList.get(entry.getKey()).getLocations().add(entry.getValue());

                ListFileWrapper tmp = new ListFileWrapper(fileNodeList.get(entry.getKey()).getLocations());
                UpFNL.put(entry.getKey(), tmp);
            }
        }

        if (updateFileNodeList) {
            updateAllFileNodeList(UpFNL);
        }
    }

    @Override
    public void updateAllFileNodeList(HashMap<String, ListFileWrapper> fileNodeList) {

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if ((ownIP + port).hashCode() != entry.getKey()) {
                Registry registry;

                String tmpIp = "-NOT UPDATE-";
                int tmpPort = -1;
                String tmpName;

                try {
                    tmpIp = entry.getValue().getIp();
                    tmpPort = entry.getValue().getPort();
                    tmpName = entry.getValue().getName();

                    registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

                    String tmpPath = "rmi://" + tmpIp + ":" + tmpPort + "/" + tmpName;

                    NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
                    nodeTemp.modifyFileNodeList(fileNodeList);

                } catch (RemoteException e) {
                    System.out.println("[UPDATE-ALL-FILE-NODE-LIST] connection problem" + tmpPort + "; Ip: " + tmpIp);

                } catch (NotBoundException e) {
                    System.out.println("[UPDATE-ALL-FILE-NODE-LIST] connection problem\" + tmpPort + \"; Ip: \" + tmpIp");
                    e.printStackTrace();

                }
            }
        }
    }

    @Override
    public NetNodeLocation callSaveFileReplica(CacheFileWrapper cacheFileWrapper, String UFID) {

        ReplicationWrapper rw = new ReplicationWrapper(cacheFileWrapper.getUFID());
        rw.setAttribute(cacheFileWrapper.getAttribute());

        byte[] ftb = Util.fileToBytes(path + "/" + UFID);
        byte[] fatb = Util.fileToBytes(path + "/" + UFID + ".attr");
        byte[] tftb = Util.append(ftb, fatb);

        rw.setChecksum(Util.getChecksum(tftb));
        rw.setContent(cacheFileWrapper.getContent());

        ArrayList<NetNodeLocation> nodeList = new ArrayList<>();
        Collection<NetNodeLocation> tmpColl = connectedNodes.values();

        for (NetNodeLocation nnl : tmpColl) {
            if (!nnl.equals(ownLocation)) {
                nodeList.add(nnl);
            }
        }

        ArrayList<NetNodeLocation> nodeWithSmallerOccupiedSpace = Util.listOfConnectedNodeForLongTime(nodeList);
        NetNodeLocation selectedNode = Util.selectedNode(nodeWithSmallerOccupiedSpace);

        Registry registry;
        String tmpIp;
        int tmpPort;

        try {

            tmpIp = selectedNode.getIp();
            tmpPort = selectedNode.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = selectedNode.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
            nodeTemp.saveFileReplica(rw);

        } catch (RemoteException | NotBoundException er) {
            er.printStackTrace();
            System.out.println("[CALL-SAVE-FILE-REPLICA] Unable to save file replica");
        }

        return selectedNode;

    }

    @Override
    public boolean checkSecReplica(NetNodeLocation e, String fileName) {

        Registry registry;

        String tmpIp;
        int tmpPort;
        boolean ver = false;

        try {
            tmpIp = e.getIp();
            tmpPort = e.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = e.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
            ver = nodeTemp.verifyFile(fileName);

        } catch (RemoteException | NotBoundException er) {
            er.printStackTrace();
            System.out.println("[CHECK-SEC-REPLICA] Unable to check replica");
        }

        return ver;

    }

    @Override
    public void callSaveFile(NetNodeLocation e, CacheFileWrapper cacheFileWrapper) {

        Registry registry;
        String tmpIp;
        int tmpPort;
        try {

            tmpIp = e.getIp();
            tmpPort = e.getPort();
            registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

            String tmpPath = e.toUrl();

            NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);

            nodeTemp.saveFile(cacheFileWrapper);

        } catch (RemoteException | NotBoundException er) {
            er.printStackTrace();
            System.out.println("[CALL-SAVE-FILE] Unable to save file");
        }
    }

    @Override
    public void saveFile(CacheFileWrapper cacheFileWrapper) {

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

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[SAVE FILE] Unable to save file");
        }

    }

    @Override
    public boolean verifyFile(String fileName) {
        return (new File(path + "/" + fileName).isFile() && new File(path + "/" + fileName + ".attr").isFile());
    }

    @Override
    public boolean saveFileReplica(ReplicationWrapper rw) {

        String filePath = path;

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

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[SAVE-FILE-REPLICA] Unable to save file replica");
            return false;
        }

        byte[] bytesArray = null;

        try {
            byte[] ftb = Files.readAllBytes(Paths.get(f.getPath()));
            byte[] fatb = Files.readAllBytes(Paths.get(fileAtt.getPath()));
            bytesArray = Util.append(ftb, fatb);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[SAVE-FILE-REPLICA] Unable to save file replica");

        }

        String checksum = Util.getChecksum(bytesArray);
        return checksum.compareTo(rw.getChecksum()) == 0;
    }

    @Override
    public boolean deleteFile(String UFID, String treeFileDirectoryUFID, long fileSize) {
        String filePath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        String totalFilePath = filePath + UFID;
        boolean filesDeleted = false;

        File file = new File(totalFilePath);
        File fileAttr = new File(totalFilePath + ".attr");
        boolean fileDelete = file.delete();
        boolean attrDelete = fileAttr.delete();

        if (fileDelete && attrDelete) {
            filesDeleted = true;
            for (NetNodeLocation nnl : fileNodeList.get(UFID).getLocations()) {
                nnl.reduceOccupiedSpace((int) fileSize);
            }
            if (treeFileDirectoryUFID != null) {
                mediatorFsNet.removeFileFromTree(UFID, treeFileDirectoryUFID);
            }


        }

        if (filesDeleted) {
            fileNodeList.remove(UFID);
        }

        return filesDeleted;
    }


    @Override
    public void nodeFileAssociation(String UFID, NetNodeLocation netNode) {
        if (!fileNodeList.containsKey(UFID)) {
            ArrayList<NetNodeLocation> a = new ArrayList<>();
            a.add(netNode);
            fileNodeList.put(UFID, new ListFileWrapper(a));
        } else {
            boolean t = false;
            for (int i = 0; i < fileNodeList.get(UFID).getLocations().size(); i++) {

                if (fileNodeList.get(UFID).getLocations().get(i).equals(netNode)) {
                    t = true;
                }
            }
            if (!t) {
                fileNodeList.get(UFID).getLocations().add(netNode);
            }
        }

        for (NetNodeLocation nnl : connectedNodes.values()) {
            Registry registry;
            try {
                registry = LocateRegistry.getRegistry(nnl.getIp(), nnl.getPort());
                NetNode nn = (NetNode) registry.lookup(nnl.toUrl());
                nn.updateWritePermissionMap(UFID, fileNodeList.get(UFID));
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
                System.out.println("[NODE-FILE-ASSOCIATION] Unable to associate the file with the node");
            }

        }
    }

    public void updateUI(FSTreeNode treeRoot) {
        MediatorFsNet.getInstance().updateJson(treeRoot);
    }

    @Override
    public String getJson() {
        return PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG);
    }

    @Override
    public synchronized void setJson(String json, boolean up) {
        PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, json);

        if (up) {
            FSStructure.getInstance().generateTreeStructure();
            mediatorFsNet.updateJson(FSStructure.getInstance().getTree().findRoot());
        }

    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public synchronized void connectionMergeJson(String json) {


        GSONHelper helpJson = GSONHelper.getInstance();
        HashMap<String, JsonFolder> receivedFolder = helpJson.jsonToFolders(json);

        String thisJson = this.getJson();

        if (thisJson != null) {

            HashMap<String, JsonFolder> ownFolder = helpJson.jsonToFolders(thisJson);
            ArrayList<JsonFile> ownFilesRoot = ownFolder.get("root").getFiles();

            Date date = new Date();
            String time = date.toString();

            if (ownFilesRoot.size() != 0) {

                for (JsonFile anOwnFilesRoot : ownFilesRoot) {

                    String currentName = anOwnFilesRoot.getFileName();
                    String currentUFID = anOwnFilesRoot.getUFID();

                    File file = new File(path + "/" + currentUFID);
                    File attr = new File(path + "/" + currentUFID + ".attr");

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
                        anOwnFilesRoot.setFileName(newName);
                        receivedFolder.get("root").getFiles().add(anOwnFilesRoot);


                    } else if (sameUFID) {
                        file.delete();
                        attr.delete();

                    } else {
                        receivedFolder.get("root").getFiles().add(anOwnFilesRoot);
                    }
                }
            }

            ownFolder.remove("root");

            for (Map.Entry<String, JsonFolder> entry : ownFolder.entrySet()) {

                if (receivedFolder.containsKey(entry.getKey())) {

                    ArrayList<JsonFile> files = entry.getValue().getFiles();
                    ArrayList<JsonFile> filesExist = new ArrayList<>();

                    for (JsonFile file1 : files) {
                        File file = new File(path + "/" + file1.getUFID());
                        File attr = new File(path + "/" + file1.getUFID() + ".attr");

                        if (file.exists() && attr.exists()) {
                            filesExist.add(file1);
                        }
                    }

                    if (filesExist.size() > 0) {

                        ArrayList<JsonFile> receivedFiles = receivedFolder.get(entry.getKey()).getFiles();

                        if (receivedFiles.size() > 0) {

                            for (JsonFile receivedFile : receivedFiles) {
                                for (int i = 0; i < filesExist.size(); i++) {
                                    if (receivedFile.equals(filesExist.get(i))) {
                                        filesExist.remove(i);
                                        break;
                                    }

                                }
                            }

                            if (filesExist.size() > 0) {
                                receivedFiles.addAll(filesExist);
                            }

                        } else {
                            receivedFolder.get(entry.getKey()).setFiles(filesExist);
                        }
                    }

                } else {
                    ArrayList<JsonFile> filesExist = entry.getValue().getFiles();

                    String currentName = entry.getValue().getFolderName();
                    String newName = entry.getValue().getFolderName();

                    for (Map.Entry<String, JsonFolder> entry2 : receivedFolder.entrySet()) {

                        if (entry2.getValue().getFolderName().equals(currentName)) {
                            newName = currentName + " ( offline different folder " + time + " )";
                            break;
                        }
                    }

                    receivedFolder.put(entry.getKey(), entry.getValue());
                    receivedFolder.get(entry.getKey()).setFiles(filesExist);
                    receivedFolder.get(entry.getKey()).setFolderName(newName);

                    if (entry.getValue().getParentUFID().equals("root")) {
                        receivedFolder.get("root").getChildren().add(entry.getKey());
                    }
                }
            }

            String newJson = helpJson.foldersToJson(receivedFolder);
            this.setJson(newJson, false);
            this.callUpdateAllJson(newJson);

        } else {
            this.setJson(json, false);
        }

    }

    @Override
    public void callUpdateAllJson(String json) {

        for (Map.Entry<Integer, NetNodeLocation> entry : this.connectedNodes.entrySet()) {

            if ((ownIP + port).hashCode() != entry.getKey()) {

                Registry registry;

                String tmpIp = "-NOT UPDATE-";
                int tmpPort = -1;
                String tmpName;

                try {

                    tmpIp = entry.getValue().getIp();
                    tmpPort = entry.getValue().getPort();
                    tmpName = entry.getValue().getName();

                    registry = LocateRegistry.getRegistry(tmpIp, tmpPort);

                    String tmpPath = "rmi://" + tmpIp + ":" + tmpPort + "/" + tmpName;
                    NetNode nodeTemp = (NetNode) registry.lookup(tmpPath);
                    nodeTemp.setJson(json, true);

                } catch (RemoteException e) {
                    System.out.println("[CALL-UPDATE-ALL-JSON] Connection problems" + tmpPort + "; Ip: " + tmpIp);

                } catch (NotBoundException e) {
                    System.out.println("[CALL-UPDATE-ALL-JSON] Connections Problems\" + tmpPort + \"; Ip: \" + tmpIp");
                    e.printStackTrace();

                }
            }
        }
    }
}