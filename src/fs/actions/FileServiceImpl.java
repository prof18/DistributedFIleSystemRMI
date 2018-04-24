package fs.actions;

import fs.actions.cache.ReadingNodeCache;
import fs.actions.cache.WritingNodeCache;
import fs.actions.interfaces.FileService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WrapperFileServiceUtil;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileAttribute;
import mediator_fs_net.MediatorFsNet;
import net.objects.NetNodeLocation;
import utils.Constants;
import utils.PropertiesHelper;
import utils.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.time.Instant;
import java.util.*;

/**
 * This class is on a higher level compared to NetNodeImpl and so FileServiceImpl initialize it.
 */
public class FileServiceImpl implements FileService {

    private MediatorFsNet mediator;
    private final String path;
    private ReadingNodeCache readingCache;
    private WritingNodeCache writingNodeCache;

    public FileServiceImpl(String path, MediatorFsNet mediatorFsNet) {
        mediator = mediatorFsNet;
        mediator.setFsStructure();
        this.path = path;
        System.out.println("sono tornato al costruttore");
        readingCache = new ReadingNodeCache();
        writingNodeCache = new WritingNodeCache(mediator);
    }

    /**
     * It Reads a file using the fileID.
     * The offset and the count are provide by the user
     * If the file is not found both locally or remotely the system throws FileNotFoundException
     */

    public byte[] read(String fileID, int offset, int count) throws FileNotFoundException {
        System.out.println("entrato nel READ");
        byte[] ret = read(fileID, offset);
        byte[] newRet = new byte[count];
        System.arraycopy(ret, 0, newRet, 0, count);
        return newRet;
    }

    /**
     * This method differs from the previous one
     * because the lenght of the file is automatically assigned to the count.
     */
    public byte[] read(String fileID, int offset) throws FileNotFoundException {
        System.out.println("Entrato nel read senza parametri");
        CacheFileWrapper wrapper = getFile(fileID);
        System.out.println("ritornato al read senza parametri");
        if (wrapper == null) {
            System.out.println("il file : " + fileID + " non è stato trovato");
            return null;
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
        return content;
    }

    public void write(String fileID, int offset, int count, byte[] data) throws FileNotFoundException {
        System.out.println("entrato nel write");
        ArrayList<NetNodeLocation> nodeList = mediator.getWrapperFileServiceUtil().getNetNodeList().get(fileID);
        ArrayList<NetNodeLocation> tempNodeList = new ArrayList<>(nodeList);
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        byte[] repContent = null;
        int oldLength = 0; //file length before write the new content
        String localHost = null;
        try {
            localHost = mediator.getNode().getOwnIp();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (localHost != null) {
            if (nodeList.get(nodeList.indexOf(localHost)).canWrite()) {
                for (int i = 0; i < nodeList.size(); i++) {
                    if (nodeList.get(i).getIp().compareTo(localHost) == 0) { //trovo il nodo locale
                        tempNodeList.remove(i);
                        nodeList.get(i).unlockWriting();
                        break;
                    }
                }


                for (NetNodeLocation nnl : tempNodeList) {
                    int pos = nodeList.indexOf(nnl);
                    nodeList.get(pos).lockWriting();
                }
                new MapUpdateTask(fileID, mediator.getWrapperFileServiceUtil().getLocationHashMap().values(), nodeList);


                if (cacheFileWrapper == null) throw new FileNotFoundException();
                if (cacheFileWrapper.isLocal()) {
                    System.out.println("il file che si vuole sovrascrivere è locale");
                    byte[] newContent = null;
                    byte[] content = null;
                    try {
                        System.out.println("[WRITE] lettura del file prima della modifica");
                        content = read(fileID, 0);
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
                    repContent = newContent = joinArray(content, data, offset, count);
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

                    FileOutputStream fileOutputStream = null;
                    try {
                        File file = new File(path + fileID);
                        System.out.println(file.delete());
                        file = new File(path + fileID);
                        fileOutputStream = new FileOutputStream(file);
                        fileAttribute.setFileLength(file.length());
                        fileAttribute.setLastModifiedTime(Date.from(Instant.now()));
                        setAttributes(fileID, fileAttribute);
                        writingNodeCache.add(new WritingCacheFileWrapper(file, fileAttribute, lastModified, fileID, true));
                        System.out.println("newContent = " + new String(newContent));
                        fileOutputStream.write(newContent, 0, newContent.length);
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
                    repContent = newctx;
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
                    cacheFileWrapper.getAttribute().setLastModifiedTime(Date.from(Instant.now()));
                    WritingCacheFileWrapper wcfw = new WritingCacheFileWrapper(newFile, cacheFileWrapper.getAttribute(), Date.from(Instant.now()), fileID, false);
                    writingNodeCache.add(wcfw);
                }
                ReplicationWrapper rw = new ReplicationWrapper(fileID, mediator.getFsStructure().getTree().getFileName(fileID));
                rw.setAttribute(cacheFileWrapper.getAttribute());
                rw.setContent(repContent);
                System.out.println("mediator: " + mediator.getFsStructure().getTree().getPath());
                rw.setPath(mediator.getFsStructure().getTree().getPath());
                mediator.getFsStructure().generateTreeStructure();
                rw.setjSon(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
                System.out.println("Set path file:" + rw.getPath());

                for (NetNodeLocation ndl : nodeList) {
                    ndl.reduceOccupiedSpace(oldLength);
                    new ReplicationTask(ndl, rw, mediator.getWrapperFileServiceUtil()).run();
                }

                for (NetNodeLocation nnl : tempNodeList) {
                    int pos = nodeList.indexOf(nnl);
                    nodeList.get(pos).unlockWriting();
                }
                new MapUpdateTask(fileID, mediator.getWrapperFileServiceUtil().getLocationHashMap().values(), nodeList);

            } else {
                System.out.println("Impossibile scrivere sul file " + fileID + ", qualcuno sta scrivendo.");
            }

        }
    }


    public String create(String host, FileAttribute attribute, FSTreeNode curDir) throws IOException {
        String UFID = host + "_" + Date.from(Instant.now()).hashCode();
        String directoryPath = curDir.getPathWithoutRoot();
        File directory = new File(path + directoryPath);
        if (!directory.exists()) { //verifica esistenza della directory, se non esiste la crea.
            directory.mkdirs();
        }
        String filePath = path + directoryPath + UFID;
        File file = new File(filePath);
        if (file.exists()) {
            throw new FileNotFoundException();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(filePath + ".attr");
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(attribute);
        oout.flush();
        //la replicazione
        Date creationDate = new Date().from(Instant.now());

        byte[] ftb = fileToBytes(filePath);

        mediator.setFsStructure();

        ReplicationWrapper rw = new ReplicationWrapper(UFID, file.getName());
        System.out.println("Local path file:" + filePath);
        rw.setPath(curDir.getPath());
        System.out.println("Set path file from file system root:" + rw.getPath());
        rw.setAttribute(new FileAttribute(file.length(), creationDate, creationDate, 0));
        rw.setContent(ftb);
        rw.setChecksum(Util.getChecksum(ftb));
        mediator.getFsStructure().generateTreeStructure();
        rw.setjSon(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        replication(rw, mediator.getWrapperFileServiceUtil());

        return UFID;
    }

    @Override
    public String create(String host, FSTreeNode curDir) throws IOException { //crea il file (nomehost+timestamp) in locale
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        //TODO andrea controlla questa cosa aggiungendo un pò di sout
        return create(host, attribute, curDir);

    }
    

    public void delete(String fileID, FSTreeNode currentNode, DeleteFileCallback callback) {
        //eliminazione in locale
        File file = new File(path + fileID);
        File fileAttr = new File(path + fileID + ".attr");
        System.out.println(file.delete());
        System.out.println(fileAttr.delete());

        ArrayList<NetNodeLocation> list = mediator.getWrapperFileServiceUtil().getNetNodeList().get(fileID);

        int j = 0;
        for (int i = 0; i < list.size(); i++) {
            try {
                if (list.get(i).getIp().compareTo(mediator.getNode().getOwnIp()) == 0) {
                    j = i;
                    break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        list.remove(j);

        currentNode.removeOneFile(currentNode.getFileName(fileID));

        //eliminazione totale

        for (NetNodeLocation nnl : mediator.getWrapperFileServiceUtil().getNetNodeList().get(fileID)) {
            new RemoveTask(fileID, nnl).run();
        }

        mediator.getWrapperFileServiceUtil().getNetNodeList().remove(fileID);

        callback.onItemChanged(currentNode);
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
        System.out.println("entrato in getCacheFile");
        CacheFileWrapper retFile = readingCache.get(UFID);
        if (retFile != null) {
            System.out.println("file trovato nella cache");
            //devo controllare se è ancora valido il file salvato nella cache
            long elapsedTime = Date.from(Instant.now()).getTime() - retFile.getLastValidatedTime();
            if (elapsedTime < readingCache.getTimeInterval()) {
                return retFile;
            } else {
                System.out.println("[CACHE] : il file è obsoleto");
                CacheFileWrapper fileWrapperMaster = mediator.getFile(UFID);
                if (fileWrapperMaster.getLastValidatedTime() == retFile.getLastValidatedTime()) {
                    System.out.println("il file non è stato modificato");
                    retFile.setLastValidatedTime(Date.from(Instant.now()));
                    return retFile;
                } else {
                    System.out.println("il file è stato modificato");
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

    private void replication(ReplicationWrapper repWr, WrapperFileServiceUtil wfsu) { //politica replicazione nodo con meno spazio occupato e da maggior tempo connesso

        HashMap<String, ArrayList<NetNodeLocation>> hm = wfsu.getNetNodeList();

        if (!hm.containsKey(repWr.getUFID())) {
            System.out.println("File not found");
            return;
        }

        ArrayList<NetNodeLocation> nodeList = hm.get(repWr.getUFID());
        ArrayList<NetNodeLocation> nodeBiggerTime = listOfMaxConnectedNode(nodeList);
        NetNodeLocation selectedNode = selectedNode(nodeBiggerTime);

        if (selectedNode == null) {
            System.out.println("Disastro!!! Siamo rovinati!!!");
            System.out.println(" Nessun nodo trovato per la replicazione :( ");
            return;
        }

        //chiamata da remoto per la scrittura del file con acknowledge, se esito positivo
        //associo il file al nodo, altrimenti rieseguo la chiamata di scrittura.
        new ReplicationTask(selectedNode, repWr, wfsu).run();
    }

    private ArrayList<NetNodeLocation> listOfMaxConnectedNode(ArrayList<NetNodeLocation> list) {
        long maxConnectedTime = maxTimeConnection(list);
        ArrayList<NetNodeLocation> nodeList = new ArrayList<>();
        for (NetNodeLocation node : list) {
            if (node.getTimeStamp() == maxConnectedTime) {
                nodeList.add(node);
            }
        }

        return nodeList;
    }

    private long maxTimeConnection(ArrayList<NetNodeLocation> list) {
        long connectedTime = 0;
        long selectedTimeStamp = 0;
        long currentTime = new Date().getTime();
        for (NetNodeLocation dn : list) {
            if (currentTime - dn.getTimeStamp() > connectedTime) {
                selectedTimeStamp = dn.getTimeStamp();
                connectedTime = currentTime - dn.getTimeStamp();
            }
        }

        return selectedTimeStamp;
    }

    private NetNodeLocation selectedNode(ArrayList<NetNodeLocation> list) {
        NetNodeLocation selectedNode = null;
        int occupiedSpace = Integer.MAX_VALUE;
        for (NetNodeLocation node : list) {
            if (node.getTotalByte() < occupiedSpace) {
                occupiedSpace = node.getTotalByte();
                selectedNode = node;
            }
        }

        return selectedNode;
    }

    private static byte[] fileToBytes(String pathFile) {

        byte[] bytesArray = null;
        Path path = Paths.get(pathFile);

        try {
            bytesArray = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*FileInputStream fis = null;

        try {
            bytesArray = new byte[(int) f.length()];

            //read file into bytes[]
            fis = new FileInputStream(f);
            fis.read(bytesArray);


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }*/

        return bytesArray;

    }

}