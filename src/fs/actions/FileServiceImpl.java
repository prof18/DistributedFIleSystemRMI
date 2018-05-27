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
        readingCache = new ReadingNodeCache();
    }

    private static byte[] toByteArray(Object obj) throws IOException {
        byte[] bytes;
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

        CacheFileWrapper wrapper = getFile(fileID);
        if (wrapper == null) {
            System.out.println("File: " + fileID + " not found");
            return null;
        }

        boolean flag = false;
        try {
            for (Map.Entry<String, ListFileWrapper> list : mediator.getNode().getFileNodeList().entrySet()) {
                System.out.println(list.getKey() + "   " + list.getValue().isWritable());
            }

            if (mediator.getNode().getFileNodeList().get(fileID) != null) {
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
        File file = wrapper.getFile();
        int count = (int) file.length();
        byte[] content = new byte[count];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            fileInputStream.read(content, offset, count);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return new ReadWrapper(content, flag);
    }

    public void close(String fileID) {
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
        ListFileWrapper nodeList = null;
        try {
            nodeList = mediator.getNode().getFileNodeList().get(fileID);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (nodeList.getLocations().size() == 0) {
            return;
        }

        boolean canReplicate = true;

        ArrayList<NetNodeLocation> tempNodeFileList = new ArrayList<>(nodeList.getLocations());
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        byte[] repContent;
        int oldLength = 0; //file length before write the new content
        NetNodeLocation localHost = null;
        try {
            localHost = mediator.getNode().getOwnLocation();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            if (localHost != null && mediator.getNode().getFileNodeList().get(fileID).getLocations().size() > 1) {

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
            byte[] newContent;
            byte[] content = null;
            try {
                content = read(fileID, 0).getContent();
            } catch (NullPointerException e) {
                System.out.println("[WRITE] A problem has occurred during the write process");
            } catch (FileNotFoundException e) {
                System.out.println("[WRITE] File not found");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[WRITE] Error with indices");
                e.printStackTrace();
                System.exit(-1);
            }
            if (data.length < content.length) {
                content = null;
            }
            newContent = joinArray(content, data, offset, count);
            repContent = newContent.clone();
            ObjectInputStream ois;
            Date lastModified = null;
            FileAttribute fileAttribute = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(path + fileID + ".attr"));
                fileAttribute = (FileAttribute) ois.readObject();
                lastModified = fileAttribute.getLastModifiedTime();
                oldLength = (int) fileAttribute.getFileLength();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            try {
                File file = new File(path + fileID);
                FileOutputStream fileOutputStream = new FileOutputStream(file, false);
                fileAttribute.setLastModifiedTime(Date.from(Instant.now()));
                fileAttribute.setFileLength(newContent.length);
                setAttributes(fileID, fileAttribute);
                cacheFileWrapper.getAttribute().setFileLength(repContent.length);
                fileOutputStream.write(newContent, 0, newContent.length);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            FileInputStream fis = new FileInputStream(cacheFileWrapper.getFile());
            oldLength = (int) cacheFileWrapper.getFile().length();
            byte[] context = new byte[oldLength];
            try {
                fis.read(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File newFile = new File(cacheFileWrapper.getUFID());
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] newctx = joinArray(context, data, offset, count);
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

        FSTreeNode fileNode;
        if (fileDirectoryUFID.compareTo("root") != 0 && fileDirectoryUFID != null && fileDirectoryUFID.compareTo("") != 0) {
            fileNode = root.findNodeByUFID(root, fileDirectoryUFID);
        } else {
            fileNode = root;
        }
        FileWrapper fileInTree = fileNode.getFile(fileID);
        fileInTree.setAttribute(cacheFileWrapper.getAttribute());
        fileInTree.setContent(repContent);

        FSStructure.getInstance().generateJson(FSStructure.getInstance().getTree());

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
            rw.setPath(mediator.getFsStructure().getTree().getPath());
            mediator.getFsStructure().generateTreeStructure();
            rw.setjSon(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));


            for (int i = 0; i < tempNodeFileList.size(); i++) {
                tempNodeFileList.get(i).reduceOccupiedSpace(oldLength);
                ReplicationMethods.getInstance().fileReplication(tempNodeFileList.get(i), rw, mediator.getNode());
            }

            root.setJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
            mediator.jsonReplication(root);

            for (NetNodeLocation nnl : tempNodeFileList) {
                int pos = nodeList.getLocations().indexOf(nnl);
                nodeList.getLocations().get(pos).unlockWriting();
            }
        }

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
        fw.setPath(curDir.getPath());
        fw.setChecksum(Util.getChecksum(tftb));

        //Replication
        if (mediator.getNode().getFileNodeList().size() > 1 || mediator.getNode().getHashMap().size() > 1) {
            Date creationDate = new Date().from(Instant.now());

            ReplicationWrapper rw = new ReplicationWrapper(UFID, file.getName());
            rw.setPath(curDir.getPath());
            rw.setAttribute(attribute);
            rw.setContent(ftb);
            rw.setChecksum(fw.getChecksum());
            System.out.println("Replication Done");
            replication(rw, mediator.getNode());
        }

        return UFID;
    }

    @Override
    public String create(String host, FSTreeNode curDir, String fileName) throws IOException { //crea il file (nomehost+timestamp) in locale
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        attribute.setOwner(PropertiesHelper.getInstance().loadConfig(Constants.USERNAME_CONFIG));
        return create(host, attribute, curDir, fileName);

    }

    public void delete(String fileID, FSTreeNode curDir, DeleteFileCallback callback) {

        String directoryPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        File file = new File(directoryPath + fileID);
        File fileAttr = new File(directoryPath + fileID + ".attr");

        //eliminazione negli altri nodi

      /*  try {
            if (mediator.getNode().getFileNodeList() != null) {
                System.out.println("Node with the file: " + mediator.getNode().getFileNodeList().get(fileID).getLocations().size());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/

        try {

            ArrayList<NetNodeLocation> listOfNode = new ArrayList<>(mediator.getNode().getFileNodeList().get(fileID).getLocations());

            if (listOfNode.size() > 1) {
                for (NetNodeLocation nnl : mediator.getNode().getFileNodeList().get(fileID).getLocations()) {
                    if (nnl.toString().compareTo(mediator.getNode().getOwnLocation().toString()) != 0) {
                        long fileSize = curDir.getFile(fileID).getAttribute().getFileLength();
                        ReplicationMethods.getInstance().deleteFile(fileID, nnl, curDir, fileSize);
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
                    mediator.getNode().getFileNodeList().remove(fileID);
                    curDir.removeOneFile(fileID);
                }

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }


        FSTreeNode root = mediator.getFsStructure().getTree();
        int removeIndex=-1;
        for (FileWrapper fw:root.getFiles()) {
            System.out.println(fw.getUFID());
        }

        for (int i=0;i<root.getFiles().size();i++)
        {
            String tempUFID=root.getFiles().get(i).getUFID();
            if(tempUFID.equals(fileID)){
                removeIndex=i;
                break;
            }
        }
        if(removeIndex!=-1){
            root.getFiles().remove(removeIndex);
        }

        mediator.getFsStructure().generateJson(root);
        System.out.println("JSON after delete:");
        System.out.println(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));


        try {
            mediator.getNode().callUpdateAllJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        }
        catch (RemoteException e){
            e.printStackTrace();
        }

        callback.onItemChanged(curDir);
    }

    public FileAttribute getAttributes(String fileID) throws FileNotFoundException {
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        if (cacheFileWrapper == null) throw new FileNotFoundException();
        return cacheFileWrapper.getAttribute();
    }

    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //TODO: gestire aggiornamento delle informazioni
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

        return cleanArray(ret);


    }

    private byte[] cleanArray(byte[] tmp) {
        int count = tmp.length - 1;
        while (tmp[count] == 0) {
            count--;
        }
        return Arrays.copyOfRange(tmp, 0, count + 1);
    }

    private CacheFileWrapper getFile(String UFID) throws FileNotFoundException {
        System.out.println(path + UFID);
        File file = new File(path + UFID);
        if (file.exists()) {
            System.out.println("The file is in local memory");
            return new CacheFileWrapper(file, getLocalAttributeFile(UFID), UFID, true);
        } else {
            System.out.println("The file is NOT in local memory");
            CacheFileWrapper cacheFileWrapper = getCacheFile(UFID);
            if (cacheFileWrapper != null) {
                System.out.println("The file is in the cache");
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
        ObjectInputStream ois = null;
        try {
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
        return ret;
    }

    private CacheFileWrapper getCacheFile(String UFID) {
        CacheFileWrapper retFile = readingCache.get(UFID);
        if (retFile != null) {
            //devo controllare se è ancora valido il file salvato nella cache
            long elapsedTime = Date.from(Instant.now()).getTime() - retFile.getLastValidatedTime();
            if (elapsedTime < readingCache.getTimeInterval()) {
                return retFile;
            } else {
                CacheFileWrapper fileWrapperMaster = mediator.getFile(UFID);
                if (fileWrapperMaster.getLastValidatedTime() == retFile.getLastValidatedTime()) {
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
            try {
                ret = getAttributes(UFID);
                return new CacheFileWrapper(file, ret, UFID, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        } else {
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

        if (!hm.containsKey(repWr.getUFID()) || hm.get(repWr.getUFID()).getLocations().size() <= 1) {
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

            ArrayList<NetNodeLocation> nodeSmallerOccupiedSpace = Util.listOfConnectedNodeForLongTime(nodeList);
            selectedNode = Util.selectedNode(nodeSmallerOccupiedSpace);
        } else {
            ArrayList<NetNodeLocation> nodeList = hm.get(repWr.getUFID()).getLocations();
            nodeList = removeLocalNode(nodeList, node);
            ArrayList<NetNodeLocation> nodeSmallerOccupiedSpace = Util.listOfConnectedNodeForLongTime(nodeList);
            selectedNode = Util.selectedNode(nodeSmallerOccupiedSpace);
        }


        if (selectedNode == null) {
            System.out.println("There isn't a node available for replication");
            return;
        }

        //chiamata da remoto per la scrittura del file con acknowledge, se esito positivo
        //associo il file al nodo, altrimenti rieseguo la chiamata di scrittura.
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