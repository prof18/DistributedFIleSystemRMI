package fs.actions;

import fs.actions.cache.ReadingNodeCache;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.ListFileWrapper;
import fs.actions.object.ReadWrapper;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileAttribute;
import fs.objects.structure.FileWrapper;
import mediator_fs_net.MediatorFsNet;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Constants;
import utils.PropertiesHelper;
import utils.Util;

import java.io.*;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.*;

/**
 * This class is on a higher level compared to NetNodeImpl and so FileServiceImpl initialize it.
 */
public class FileServiceImpl implements FileService {

    private final String path;
    private MediatorFsNet mediator;
    private ReadingNodeCache readingCache;
    private boolean inWriting;

    public FileServiceImpl(String path) {
        mediator = MediatorFsNet.getInstance();
        mediator.setFsStructure();
        this.path = path;
        System.out.println("sono tornato al costruttore");
        readingCache = new ReadingNodeCache();
    }

    private static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
        return bytes;
    }

    /**
     * It Reads a file using the fileID.
     * The offset and the count are provide by the user
     * If the file is not found both locally or remotely the system throws FileNotFoundException
     */


    public ReadWrapper read(String fileID, int offset, int count) throws FileNotFoundException {
        System.out.println("entrato nel READ");
        ReadWrapper ret = read(fileID, offset);
        byte[] newRet = new byte[count];
        System.arraycopy(ret.getContent(), 0, newRet, 0, count);
        return new ReadWrapper(newRet, ret.isWritable());
    }

    /**
     * This method differs from the previous one
     * because the lenght of the file is automatically assigned to the count.
     */
    //TODO: controllare il flag per accesso
    public ReadWrapper read(String fileID, int offset) throws FileNotFoundException {


        System.out.println("Entrato nel read senza parametri");
        CacheFileWrapper wrapper = getFile(fileID);
        System.out.println("ritornato al read senza parametri");
        if (wrapper == null) {
            System.out.println("il file : " + fileID + " non è stato trovato");
            return null;
        }

        boolean flag = false;
        try {
            for (Map.Entry<String, ListFileWrapper> list : mediator.getNode().getFileNodeList().entrySet()) {
                System.out.println(list.getKey() + "   " + list.getValue().isWritable());
            }
            //TODO

            if (mediator.getNode().getFileNodeList().get(fileID) != null) {
                System.out.println("read file flag : " + mediator.getNode().getFileNodeList().get(fileID).isWritable());
                flag = mediator.getNode().getFileNodeList().get(fileID).isWritable();
                inWriting = flag;
            }
            if (flag) {
                ListFileWrapper listFileWrapper = mediator.getNode().getFileNodeList().get(fileID);
                listFileWrapper.setWritable(false);
                mediator.getNode().updateWritePermissionMap(fileID, listFileWrapper);
                ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), listFileWrapper);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("wrapper nel read non null");
        File file = wrapper.getFile();
        int count = (int) file.length();
        System.out.println("count = " + count);
        byte[] content = new byte[count];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            fileInputStream.read(content, offset, count);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("content = " + new String(content));
        return new ReadWrapper(content, flag);
    }

    public void close(String fileID) {
        System.out.println("rilascio privilegi di scrittura");
        if (inWriting) {
            try {
                mediator.getNode().getFileNodeList().get(fileID).setWritable(true);
                ListFileWrapper listFileWrapper = mediator.getNode().getFileNodeList().get(fileID);
                mediator.getNode().updateWritePermissionMap(fileID, listFileWrapper);
                ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), listFileWrapper);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        inWriting = false;

    }

    public void write(String fileID, int offset, int count, byte[] data, String fileDirectoryUFID) throws FileNotFoundException {
        System.out.println("entrato nel write");
        ListFileWrapper nodeList = null;
        try {
            nodeList = mediator.getNode().getFileNodeList().get(fileID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (nodeList.getLocations().size() == 0) {
            System.out.println("nessun file trovato");
            return;
        }

        boolean canReplicate = true;
       /* for (NetNodeLocation nnl : nodeList.getLocations()) {
            try {
                if ((nnl.toUrl()).compareTo(mediator.getNode().getOwnLocation().toUrl()) == 0) {
                    canReplicate = nodeList.isWritable();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        try {
            if (mediator.getNode().getFileNodeList().get(fileID).getLocations().size() <= 1) {
                canReplicate = false;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
        ArrayList<NetNodeLocation> tempNodeFileList = new ArrayList<>(nodeList.getLocations());
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        byte[] repContent = null;
        int oldLength = 0; //file length before write the new content
        NetNodeLocation localHost = null;
        try {
            localHost = mediator.getNode().getOwnLocation();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            if (localHost != null && mediator.getNode().getFileNodeList().get(fileID).getLocations().size() > 1) {
                //debug
                System.out.println("Localhost : " + localHost.toUrl());
                System.out.println("lista node list : ");
                for (int i = 0; i < nodeList.getLocations().size(); i++) {
                    System.out.println(nodeList.getLocations().get(i).toUrl());
                }

                //fine debug
                for (int i = 0; i < nodeList.getLocations().size(); i++) {
                    if (nodeList.getLocations().get(i).equals(localHost)) { //trovo il nodo locale
                        tempNodeFileList.remove(i);
                        nodeList.getLocations().get(i).unlockWriting();
                        break;
                    }


                    for (NetNodeLocation nnl : tempNodeFileList) {
                        int pos = nodeList.getLocations().indexOf(nnl);
                        nodeList.getLocations().get(pos).lockWriting();
                    }
                }
                try {
                    ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), nodeList);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (cacheFileWrapper == null) throw new FileNotFoundException();

        if (cacheFileWrapper.isLocal()) {
            System.out.println("il file che si vuole sovrascrivere è locale");
            byte[] newContent;
            byte[] content = null;
            try {
                System.out.println("[WRITE] lettura del file prima della modifica");
                content = read(fileID, 0).getContent();
                System.out.println("[WRITE] prima della modifica: " + new String(content));
            } catch (NullPointerException e) {
                System.out.println("il file ha qualche problema");
            } catch (FileNotFoundException e) {
                System.out.println("il file non è presente");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("problema con gli indici");
                e.printStackTrace();
                System.exit(-1);
            }
            if (data.length < content.length) {
                content = null;
            }
            newContent = joinArray(content, data, offset, count);
            repContent = newContent.clone();
            System.out.println("[WRITE] nuovo contenuto da sovrascrivere : " + new String(newContent));
            ObjectInputStream ois = null;
            Date lastModified = null;
            FileAttribute fileAttribute = null;
            try {
                System.out.println("[WRITE] " + path + fileID);
                ois = new ObjectInputStream(new FileInputStream(path + fileID + ".attr"));
                fileAttribute = (FileAttribute) ois.readObject();
                lastModified = fileAttribute.getLastModifiedTime();
                oldLength = (int) fileAttribute.getFileLength();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                File file = new File(path + fileID);
                System.out.println("File delete: " + file.delete());
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                fileAttribute.setLastModifiedTime(Date.from(Instant.now()));
                fileAttribute.setFileLength(newContent.length);
                setAttributes(fileID, fileAttribute);
                cacheFileWrapper.getAttribute().setFileLength(repContent.length);
                System.out.println("newContent = " + new String(newContent));
                fileOutputStream.write(newContent, 0, newContent.length);
                //fileOutputStream.write(newContent);
                //fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("il file che si vuole sovrascrivere non è locale");
            FileInputStream fis = new FileInputStream(cacheFileWrapper.getFile());
            oldLength = (int) cacheFileWrapper.getFile().length();
            byte[] context = new byte[oldLength];
            System.out.println("length " + context.length);
            try {
                fis.read(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("context = " + new String(context));
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File newFile = new File(cacheFileWrapper.getUFID());
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] newctx = joinArray(context, data, offset, count);
            System.out.println("contenuto da scrivere : " + new String(newctx));
            try {
                fos.write(newctx);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            repContent = newctx.clone();
            cacheFileWrapper.getAttribute().setLastModifiedTime(Date.from(Instant.now()));

            cacheFileWrapper.getAttribute().setFileLength(repContent.length);

        }

        try {
            if (mediator.getNode().getHashMap().size() > 1 && mediator.getNode().getFileNodeList().get(fileID).getLocations().size() > 1) {

                ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), nodeList);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        FSStructure.getInstance().generateTreeStructure();

        FSTreeNode root = FSStructure.getInstance().getTree();

        System.out.println("fileDirectoryUFID is: " + fileDirectoryUFID);
        System.out.println("Instance tree: " + root.toString());
        FSTreeNode fileNode;
        if (fileDirectoryUFID.compareTo("root") != 0 && fileDirectoryUFID != null && fileDirectoryUFID.compareTo("") != 0) {
            fileNode = root.findNodeByUFID(root, fileDirectoryUFID);
        } else {
            System.out.println("fileDirectoryName is null.");
            fileNode = root;
        }
        System.out.println("fileNode: " + fileNode);
        FileWrapper fileInTree = fileNode.getFile(fileID);
        fileInTree.setAttribute(cacheFileWrapper.getAttribute());
        fileInTree.setContent(repContent);

        System.out.println("File size from attribute: " + cacheFileWrapper.getAttribute().getFileLength());

        FSStructure.getInstance().generateJson(FSStructure.getInstance().getTree());

        System.out.println("Can replicate: " + canReplicate);

        //file content and attributes replication
        if (canReplicate) {
            String fileName = mediator.getFsStructure().getTree().getFileName(fileID);
            ReplicationWrapper rw = new ReplicationWrapper(fileID, fileName);
            rw.setAttribute(cacheFileWrapper.getAttribute());
            rw.setContent(repContent);
            //TODO: modificato verificare se è giusto
            byte[] fatb = new byte[0];
            try {
                fatb = toByteArray(cacheFileWrapper.getAttribute());
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] ftb = repContent;
            byte[] tftb = Util.append(ftb, fatb);
            rw.setChecksum(Util.getChecksum(tftb));
            System.out.println("Path " + mediator.getFsStructure().getTree().getPath());
            rw.setPath(mediator.getFsStructure().getTree().getPath());
            mediator.getFsStructure().generateTreeStructure();
            rw.setjSon(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            System.out.println("Set path file:" + rw.getPath());


            for (int i = 0; i < tempNodeFileList.size(); i++) {
                tempNodeFileList.get(i).reduceOccupiedSpace(oldLength);
                ReplicationMethods.getInstance().fileReplication(tempNodeFileList.get(i), rw, mediator.getNode());
            }

            root.setGson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            mediator.jsonReplicaton(root);

            for (NetNodeLocation nnl : tempNodeFileList) {
                int pos = nodeList.getLocations().indexOf(nnl);
                nodeList.getLocations().get(pos).unlockWriting();
            }
        }

        System.out.println("rilascio privilegi di scrittura");
        try {
            mediator.getNode().getFileNodeList().get(fileID).setWritable(true);
            ListFileWrapper listFileWrapper = mediator.getNode().getFileNodeList().get(fileID);
            mediator.getNode().updateWritePermissionMap(fileID, listFileWrapper);
            ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), listFileWrapper);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public String create(String host, FileAttribute attribute, FSTreeNode curDir, String fileName) throws IOException {
        String UFID = host + "_" + Date.from(Instant.now()).hashCode();
        attribute.setOwner(PropertiesHelper.getInstance().loadConfig(Constants.USERNAME_CONFIG));
        attribute.setType("Text (.txt)");
        attribute.setFileLength(0);
        String filePath = path + UFID;
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileNotFoundException();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(filePath + ".attr");
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(attribute);
        oout.flush();
        byte[] ftb = Util.fileToBytes(filePath);
        byte[] fatb = Util.fileToBytes(filePath + ".attr");
        byte[] tftb = Util.append(ftb, fatb);
        ArrayList<NetNodeLocation> nl = new ArrayList<>();
        nl.add(mediator.getNode().getOwnLocation());
        mediator.getNode().getFileNodeList().put(UFID, new ListFileWrapper(nl));
        FileWrapper fw = new FileWrapper(UFID, fileName);
        ReplicationMethods.getInstance().updateWritePermissionMap(UFID, mediator.getNode().getHashMap().values(), mediator.getNode().getFileNodeList().get(UFID));

        fw.setAttribute(attribute);
        System.out.println("Directory path " + curDir.getPathWithoutRoot());
        fw.setPath(curDir.getPath());
        fw.setChecksum(Util.getChecksum(tftb));

        //la replicazione
        System.out.println("Eseguo la replicazione del file creato");
        System.out.println("Nodi che hanno il file " + mediator.getNode().getFileNodeList().size());
        System.out.println("Numero chiavi e valori Hashmap nodi collegati: " + mediator.getNode().getHashMap().entrySet().size());
        System.out.println("Dimensione Hashmap nodi collegati " + mediator.getNode().getHashMap().size());
        if (mediator.getNode().getFileNodeList().size() > 1 || mediator.getNode().getHashMap().size() > 1) {
            Date creationDate = new Date().from(Instant.now());

            System.out.println("Creazione contenitore per il file da replicare");
            ReplicationWrapper rw = new ReplicationWrapper(UFID, file.getName());
            System.out.println("Local path file:" + filePath);
            rw.setPath(curDir.getPath());
            System.out.println("Set path file from file system root:" + rw.getPath());
            rw.setAttribute(attribute);
            rw.setContent(ftb);
            rw.setChecksum(fw.getChecksum());
            //rw.setjSon(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            System.out.println("Creazione contenitore riuscita");

            System.out.println("Esecuzione metodo replication");
            replication(rw, mediator.getNode());
        }

        return UFID;
    }

    @Override
    public String create(String host, FSTreeNode curDir, String fileName) throws IOException { //crea il file (nomehost+timestamp) in locale
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);

        //TODO andrea controlla questa cosa aggiungendo un pò di sout
        return create(host, attribute, curDir, fileName);

    }

    public void delete(String fileID, FSTreeNode curDir, DeleteFileCallback callback) {

        String directoryPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        File file = new File(directoryPath + fileID);
        File fileAttr = new File(directoryPath + fileID + ".attr");

        //eliminazione negli altri nodi

        try {
            if (mediator.getNode().getFileNodeList() != null) {
                System.out.println("Nodi con il file: " + mediator.getNode().getFileNodeList().get(fileID).getLocations().size());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {

            ArrayList<NetNodeLocation> listOfNode = new ArrayList<>(mediator.getNode().getFileNodeList().get(fileID).getLocations());

            if (listOfNode.size() > 1) {
                for (NetNodeLocation nnl : mediator.getNode().getFileNodeList().get(fileID).getLocations()) {
                    if (nnl.toString().compareTo(mediator.getNode().getOwnLocation().toString()) != 0) {
                        ReplicationMethods.getInstance().deleteFile(fileID, nnl, curDir);
                    }
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //eliminazione in locale
        try {
            if (file.exists() && file.isFile()) {
                if (file.delete() && fileAttr.delete() && mediator.getNode().getFileNodeList().containsKey(fileID)) {
                    System.out.println("File cancellati in locale");
                    mediator.getNode().getFileNodeList().remove(fileID);
                    curDir.removeOneFile(fileID);
                }

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Replicazione del json per l'albero dopo eliminazione file");

        System.out.println("prima della modifica");
        System.out.println(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        FSTreeNode root = mediator.getFsStructure().getTree();
        int removeIndex=-1;
        System.out.println("file ID : "+fileID);
        System.out.println("root files");
        for (FileWrapper fw:root.getFiles()) {
            System.out.println(fw.getUFID());
        }

        System.out.println("size curDir : "+curDir.getFiles().size());
        for (int i=0;i<root.getFiles().size();i++)
        {
            System.out.println("loop search");
            String tempUFID=root.getFiles().get(i).getUFID();
            System.out.println("tempUFID : "+tempUFID);
            if(tempUFID.equals(fileID)){
                System.out.println("trovato il file");
                removeIndex=i;
                break;
            }
        }
        if(removeIndex!=-1){
            System.out.println("rimozione del file");
            root.getFiles().remove(removeIndex);
        }

        mediator.getFsStructure().generateJson(root);
        System.out.println("dopo della modifica");
        System.out.println(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));


        //settare il JSON sui nodi che non hanno il nodo salvato


        try {
            mediator.getNode().callUpdateAllJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        }
        catch (RemoteException e){
            e.printStackTrace();
        }

        callback.onItemChanged(curDir);
    }

    public FileAttribute getAttributes(String fileID) throws FileNotFoundException {
        System.out.println("entrato in getAttributes");
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        if (cacheFileWrapper == null) throw new FileNotFoundException();
        return cacheFileWrapper.getAttribute();
    }

    //TODO: verifica setAttributes
    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //gestire aggiornamento delle informazioni
    }

    private byte[] joinArray(byte[] first, byte[] second, int offset, int count) throws IndexOutOfBoundsException {
        if (first == null) {
            return second;
        }
        byte[] ret = new byte[first.length + second.length];
        int i = 0;
        if (offset > first.length) throw new IndexOutOfBoundsException();
        while (i < first.length && i < offset) {
            ret[i] = first[i];
            i++;
        }
        int j = 0;
        while (j < count && j < second.length) {
            ret[i] = second[j];
            j++;
            i++;
        }
        while (i < first.length) {
            ret[i] = first[i];
            i++;
        }

        byte[] cleanRet = cleanArray(ret);
        return cleanRet;


    }

    private byte[] cleanArray(byte[] tmp) {
        int count = tmp.length - 1;
        while (tmp[count] == 0) {
            count--;
        }
        return Arrays.copyOfRange(tmp, 0, count + 1);
    }

    private CacheFileWrapper getFile(String UFID) throws FileNotFoundException {
        System.out.println("entrato in getFile");
        System.out.println(path + UFID);
        File file = new File(path + UFID);
        if (file.exists()) {  //il file è contenuto nella memoria interna
            System.out.println("contenuto nella memoria interna");
            return new CacheFileWrapper(file, getLocalAttributeFile(UFID), UFID, true);
        } else {
            System.out.println("il file non è contenuto nella memoria interna");
            CacheFileWrapper cacheFileWrapper = getCacheFile(UFID);
            if (cacheFileWrapper != null) {//il file è contenuto nella cache
                System.out.println("il file è contenuto nella cache");
                return cacheFileWrapper;
            } else {
                CacheFileWrapper cacheFile = mediator.getFile(UFID);
                if (cacheFile == null) throw new FileNotFoundException();
                cacheFile.setLocal(false);
                readingCache.put(UFID, cacheFile);
                return cacheFile;
            }
        }
    }

    private FileAttribute getLocalAttributeFile(String UFID) {
        System.out.println("GetLocalAttributeFile");
        ObjectInputStream ois = null;
        try {
            System.out.println(path + UFID + ".attr");
            ois = new ObjectInputStream(new FileInputStream(path + UFID + ".attr"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileAttribute ret = null;
        try {
            ret = (FileAttribute) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("uscito da getLocalAttributeFile");
        return ret;
    }

    private CacheFileWrapper getCacheFile(String UFID) {
        System.out.println("[CACHE] entrato in getCacheFile");
        CacheFileWrapper retFile = readingCache.get(UFID);
        if (retFile != null) {
            System.out.println("[CACHE] file trovato nella cache");
            //devo controllare se è ancora valido il file salvato nella cache
            long elapsedTime = Date.from(Instant.now()).getTime() - retFile.getLastValidatedTime();
            if (elapsedTime < readingCache.getTimeInterval()) {
                return retFile;
            } else {
                System.out.println("[CACHE] : il file è obsoleto");
                CacheFileWrapper fileWrapperMaster = mediator.getFile(UFID);
                if (fileWrapperMaster.getLastValidatedTime() == retFile.getLastValidatedTime()) {
                    System.out.println("[CACHE] il file è stato modificato");
                    retFile = fileWrapperMaster;
                    return retFile;
                }
            }
        }
        return null;
    }

    public CacheFileWrapper getFileAndAttribute(String UFID) {
        File file = new File(path + UFID);
        FileAttribute ret = null;
        if (file.exists()) {
            System.out.println("trovato il file " + path + UFID);
            try {
                ret = getAttributes(UFID);
                return new CacheFileWrapper(file, ret, UFID, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("non trovato il file " + UFID);
            return null;
        }
        return null;
    }

    private void replication(ReplicationWrapper repWr, NetNode node) throws RemoteException { //politica replicazione nodo con meno spazio occupato e da maggior tempo connesso

        HashMap<String, ListFileWrapper> hm = null;
        try {
            hm = node.getFileNodeList();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        NetNodeLocation selectedNode;

        System.out.println("Ricerca nodo per la replicazione");

        if (!hm.containsKey(repWr.getUFID()) || hm.get(repWr.getUFID()).getLocations().size() <= 1) {
            System.out.println("File not found in hashmap file-node");
            ArrayList<NetNodeLocation> nodeList = new ArrayList<>();
            Collection<NetNodeLocation> tmpColl = null;
            try {
                tmpColl = node.getHashMap().values();

            if (tmpColl != null) {
                for (NetNodeLocation nnl : tmpColl) {
                    if (nnl.toUrl().compareTo(mediator.getNode().getOwnLocation().toUrl()) != 0)
                    nodeList.add(nnl);
                }
            }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            //ArrayList<NetNodeLocation> nodeBiggerTime = Util.listOfMaxConnectedNode(nodeList);
            ArrayList<NetNodeLocation> nodeSmallerOccupiedSpace = Util.listOConnectedNodeWithMinOccupiedSpace(nodeList);
            selectedNode = Util.selectedNode(nodeSmallerOccupiedSpace);
        } else {
            ArrayList<NetNodeLocation> nodeList = hm.get(repWr.getUFID()).getLocations();
            nodeList = removeLocalNode(nodeList, node);
            //ArrayList<NetNodeLocation> nodeBiggerTime = Util.listOfMaxConnectedNode(nodeList);
            ArrayList<NetNodeLocation> nodeSmallerOccupiedSpace = Util.listOConnectedNodeWithMinOccupiedSpace(nodeList);
            selectedNode = Util.selectedNode(nodeSmallerOccupiedSpace);
        }


        if (selectedNode == null) {
            System.out.println("Disastro!!! Siamo rovinati!!!");
            System.out.println(" Nessun nodo trovato per la replicazione :( ");
            return;
        } else {
            System.out.println("Nodo scelto: " + selectedNode.toUrl());
        }

        //chiamata da remoto per la scrittura del file con acknowledge, se esito positivo
        //associo il file al nodo, altrimenti rieseguo la chiamata di scrittura.
        System.out.println("Nodo trovato, avvio task replicazione");
        System.out.println("chiamato il replication task da riga 596");
        ReplicationMethods.getInstance().fileReplication(selectedNode, repWr, node);
    }

    private ArrayList<NetNodeLocation> removeLocalNode(ArrayList<NetNodeLocation> nodeList, NetNode node) {

        for (int i = 0; i < nodeList.size(); i++) {
            try {
                if (nodeList.get(i).toUrl().compareTo(node.getOwnLocation().toUrl()) == 0) {
                    nodeList.remove(i);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return nodeList;
    }

}