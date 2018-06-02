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
@SuppressWarnings("ResultOfMethodCallIgnored")
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
     * If the file is not found both locally or remotely the system throws FileNotFoundException
     */
    @Override
    public ReadWrapper read(String fileID, int offset) throws FileNotFoundException {

        CacheFileWrapper wrapper = getFile(fileID);
        if (wrapper == null) {
            System.out.println("File: " + fileID + " not found");
            return null;
        }

        boolean flag = false;
        try {
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

    /**
     * This method is used to release the lock when the user closes the reading window of a file
     *
     * @param fileID is the unique identifier of the file
     */
    @Override
    public void close(String fileID) {
        if (inWriting) {
            try {
                mediator.getNode().getFileNodeList().get(fileID).setWritable(true);
                ListFileWrapper listFileWrapper = mediator.getNode().getFileNodeList().get(fileID);
                mediator.getNode().updateWritePermissionMap(fileID, listFileWrapper);
                ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), listFileWrapper);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Error while release the writing lock");
            }
        }
        inWriting = false;
    }

    /**
     * This method is used to write new byte in a specific file
     *
     * @param fileID            is the unique identifier of the file
     * @param offset            is the start index where begin to write
     * @param count             is the number of byte to write
     * @param data              is the array of byte to write
     * @param fileDirectoryUFID is the identifier of the directory that contains the file
     * @throws FileNotFoundException if the file not exists
     */
    @Override
    public void write(String fileID, int offset, int count, byte[] data, String fileDirectoryUFID) throws FileNotFoundException {

        /*These commands are used to obtain the list of nodes that keep a file in own memory**/

        ListFileWrapper nodeList = null;
        try {
            nodeList = mediator.getNode().getFileNodeList().get(fileID);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[WRITE] Unable to acquire the node list");
        }
        if (nodeList.getLocations().size() == 0) {
            return;
        }

        /*These method are used to update the information about the lock**/

        ArrayList<NetNodeLocation> tempNodeFileList = new ArrayList<>(nodeList.getLocations());
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        byte[] repContent;
        int oldLength = 0; //file length before write the new content
        NetNodeLocation localHost = null;
        try {
            localHost = mediator.getNode().getOwnLocation();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[WRITE] Unable to acquire the host information");
        }

        try {
            if (localHost != null && mediator.getNode().getFileNodeList().get(fileID).getLocations().size() > 1) {


                for (int i = 0; i < nodeList.getLocations().size(); i++) {
                    if (nodeList.getLocations().get(i).equals(localHost)) {
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
                    System.out.println("[WRITE] Unable to update the write permissions");

                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[WRITE] Unable to update the write permissions");

        }


        /*These commands are used to update the content of the file**/

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
            FileAttribute fileAttribute = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(path + fileID + ".attr"));
                fileAttribute = (FileAttribute) ois.readObject();
                oldLength = (int) fileAttribute.getFileLength();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("[WRITE] Unable to read the file attribute");
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
                System.out.println("[WRITE] Unable to write the file");
            }

        } else {
            FileInputStream fis = new FileInputStream(cacheFileWrapper.getFile());
            oldLength = (int) cacheFileWrapper.getFile().length();
            byte[] context = new byte[oldLength];
            try {
                fis.read(context);
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[WRITE] Unable to read the cache");
            }
            File newFile = new File(cacheFileWrapper.getUFID());
            FileOutputStream fos = new FileOutputStream(newFile);
            byte[] newctx = joinArray(context, data, offset, count);
            try {
                fos.write(newctx);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[WRITE] Unable to write the cache");
            }
            repContent = newctx.clone();
            cacheFileWrapper.getAttribute().setLastModifiedTime(Date.from(Instant.now()));
            cacheFileWrapper.getAttribute().setFileLength(repContent.length);
        }

        /*These command update the information about the writing lock**/

        try {
            if (mediator.getNode().getHashMap().size() > 1 && mediator.getNode().getFileNodeList().get(fileID).getLocations().size() > 1) {
                ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), nodeList);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[WRITE] Unable to write the write permissions");
        }

        /*
         * These command are used to update the structure of the directory three and subsequently of the json representation
         */
        FSStructure.getInstance().generateTreeStructure();
        FSTreeNode root = FSStructure.getInstance().getTree();

        FSTreeNode fileNode;
        if (fileDirectoryUFID.compareTo("root") != 0 && fileDirectoryUFID.compareTo("") != 0) {
            fileNode = root.findNodeByUFID(root, fileDirectoryUFID);
        } else {
            fileNode = root;
        }
        FileWrapper fileInTree = fileNode.getFile(fileID);
        fileInTree.setAttribute(cacheFileWrapper.getAttribute());
        fileInTree.setContent(repContent);
        FSStructure.getInstance().generateJson(FSStructure.getInstance().getTree());

        //file content and attributes replication
        /*
         * These commands manage the replication of the updated file in the other nodes
         */
        String fileName = mediator.getFsStructure().getTree().getFileName(fileID);
        ReplicationWrapper rw = new ReplicationWrapper(fileID);
        rw.setAttribute(cacheFileWrapper.getAttribute());
        rw.setContent(repContent);
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
        rw.setJSON(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));

        for (NetNodeLocation aTempNodeFileList : tempNodeFileList) {
            aTempNodeFileList.reduceOccupiedSpace(oldLength);
            ReplicationMethods.getInstance().fileReplication(aTempNodeFileList, rw, mediator.getNode());
        }

        root.setJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        mediator.jsonReplication(root);

        for (NetNodeLocation nnl : tempNodeFileList) {
            int pos = nodeList.getLocations().indexOf(nnl);
            nodeList.getLocations().get(pos).unlockWriting();
        }

        try {
            mediator.getNode().getFileNodeList().get(fileID).setWritable(true);
            ListFileWrapper listFileWrapper = mediator.getNode().getFileNodeList().get(fileID);
            mediator.getNode().updateWritePermissionMap(fileID, listFileWrapper);
            ReplicationMethods.getInstance().updateWritePermissionMap(fileID, mediator.getNode().getHashMap().values(), listFileWrapper);
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[WRITE] Unable to update the write permissions");
        }


    }

    /**
     * This method is used to create a new file in the filesystem
     *
     * @param host      is the name of the host that creates the file
     * @param attribute is the attribute of the new file
     * @param curDir    is the directory where the file is created
     * @param fileName  is tge name of the file
     * @return the unique identifier of the new file
     * @throws IOException if there are reading or writing problem
     */
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

        if (mediator.getNode().getHashMap().size() > 1) {
            ReplicationWrapper rw = new ReplicationWrapper(UFID);
            rw.setPath(curDir.getPath());
            rw.setAttribute(attribute);
            rw.setContent(ftb);
            rw.setChecksum(fw.getChecksum());
            replication(rw, mediator.getNode());
        }

        return UFID;
    }

    /**
     * This method creates a file with a default attribute for a file
     *
     * @param host     is the host that creates the new file
     * @param curDir   is the current directory where saving the file
     * @param fileName is the name of the file
     * @return the unique identifier of the new file
     * @throws IOException if there are reading or writing problems
     */
    @Override
    public String create(String host, FSTreeNode curDir, String fileName) throws IOException {
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        attribute.setOwner(PropertiesHelper.getInstance().loadConfig(Constants.USERNAME_CONFIG));
        return create(host, attribute, curDir, fileName);

    }

    /**
     * This method is used to delete a file from the filesystem
     *
     * @param fileID   is the unique identifier of the file
     * @param curDir   is the directory where is saved the file
     * @param callback is the callback that informs the deletion of a file
     */
    @Override
    public void delete(String fileID, FSTreeNode curDir, DeleteFileCallback callback) {

        String directoryPath = PropertiesHelper.getInstance().loadConfig(Constants.WORKING_DIR_CONFIG);
        File file = new File(directoryPath + fileID);
        File fileAttr = new File(directoryPath + fileID + ".attr");

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
            System.out.println("[DELETE] Unable to delete the file: " + fileID);
        }

        //remove local files
        try {
            if (file.exists() && file.isFile()) {
                if (file.delete() && fileAttr.delete() && mediator.getNode().getFileNodeList().containsKey(fileID)) {
                    mediator.getNode().getFileNodeList().remove(fileID);
                    curDir.removeOneFile(fileID);
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[DELETE] Unable to delete the local file: " + fileID);
        }

        //removing the deleted file from the cache
        readingCache.remove(fileID);
        FSTreeNode root = mediator.getFsStructure().getTree();
        int removeIndex = -1;

        for (int i = 0; i < root.getFiles().size(); i++) {
            String tempUFID = root.getFiles().get(i).getUFID();
            if (tempUFID.equals(fileID)) {
                removeIndex = i;
                break;
            }
        }
        if (removeIndex != -1) {
            root.getFiles().remove(removeIndex);
        }

        mediator.getFsStructure().generateJson(root);

        try {
            mediator.getNode().callUpdateAllJson(PropertiesHelper.getInstance().loadConfig(Constants.FOLDERS_CONFIG));
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[DELETE] Unable to update the JSON");
        }

        callback.onItemChanged(curDir);
    }

    /**
     * @param fileID is the unique identifier of the file
     * @return the class that wraps all the attribute about a file
     * @throws FileNotFoundException if the file not exists
     */
    @Override
    public FileAttribute getAttributes(String fileID) throws FileNotFoundException {
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        if (cacheFileWrapper == null) throw new FileNotFoundException();
        return cacheFileWrapper.getAttribute();
    }

    /**
     * This method is used to update the attribute of a file
     * @param fileID is the unique identifier of a file
     * @param attr   is the new instance of the class FileAttribute to save
     */
    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path + fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[SET-ATTRIBUTES] Unable to set the attributes");
        }
    }

    /**
     * This method is used to concatenate the old and the new content of a file
     * @param first is the old content
     * @param second is the new content
     * @param offset is the position where put the new content
     * @param count is the number of bytes to add of the new content
     * @return the updated array of byte
     * @throws IndexOutOfBoundsException if indexes are wrong
     */
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

    /**
     * This method return a file and the own attribute
     * @param UFID is the identifier of the file
     * @return is the wrapper that contains all the required information
     * @throws FileNotFoundException if the file not exists
     */
    private CacheFileWrapper getFile(String UFID) throws FileNotFoundException {
        File file = new File(path + UFID);
        if (file.exists()) {
            return new CacheFileWrapper(file, getLocalAttributeFile(UFID), UFID, true);
        } else {
            CacheFileWrapper cacheFileWrapper = getCacheFile(UFID);
            if (cacheFileWrapper != null) {
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

    /**
     * This method is used to gather the attribute of a local file
     * @param UFID is the unique identifier of the file
     * @return the attribute of the file
     */
    private FileAttribute getLocalAttributeFile(String UFID) {
        ObjectInputStream ois;
        FileAttribute ret = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(path + UFID + ".attr"));
            ret = (FileAttribute) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("[GET-LOCAL-ATTRIBUTE-FILE] Unable to read the attributes in local");
        }
        return ret;
    }

    /**
     * This method is used to gat a file from the reading cache of the node
     * @param UFID is the unique identifier of the file
     * @return all the information related to a file wrap in a CacheFileWrapper
     */
    private CacheFileWrapper getCacheFile(String UFID) {
        CacheFileWrapper retFile = readingCache.get(UFID);
        if (retFile != null) {
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

    /**
     * This method is used to get a file and own attribute in a local node
     * @param UFID is the unique identifier of the selected file
     * @return a wrapper that holds the file and own attribute
     */
    public CacheFileWrapper getFileAndAttribute(String UFID) {
        File file = new File(path + UFID);
        FileAttribute ret;
        if (file.exists()) {
            try {
                ret = getAttributes(UFID);
                return new CacheFileWrapper(file, ret, UFID, true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.println("[GET-FILE-AND-ATTRIBUTES] Unable to get the file: " + UFID);
            }

        }
        return null;
    }

    /**
     * Method for the choice of the node where replicate the created file
     * @param repWr wrapper for the data file to replicated
     * @param node  local node
     */
    private void replication(ReplicationWrapper repWr, NetNode node) {

        HashMap<String, ListFileWrapper> hm = null;
        try {
            hm = node.getFileNodeList();
        } catch (RemoteException e) {
            e.printStackTrace();
            System.out.println("[REPLICATION] Unable to get the node file list");
        }

        NetNodeLocation selectedNode;

        if (!hm.containsKey(repWr.getUFID()) || hm.get(repWr.getUFID()).getLocations().size() <= 1) {
            ArrayList<NetNodeLocation> nodeList = new ArrayList<>();
            Collection<NetNodeLocation> tmpColl;
            try {
                tmpColl = node.getHashMap().values();
                for (NetNodeLocation nnl : tmpColl) {
                    if (nnl.toUrl().compareTo(mediator.getNode().getOwnLocation().toUrl()) != 0)
                        nodeList.add(nnl);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("[REPLICATION] Unable to add a node in the Node List");
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
            System.out.println("[REPLICATION] There isn't a node available for replication");
            return;
        }

        // call the method for remotely replication
        ReplicationMethods.getInstance().fileReplication(selectedNode, repWr, node);
    }

    /**
     * This method is used to removes a node from the node list
     * @param nodeList is the list of nodes
     * @param node is the node to remove
     * @return the updated list of the node
     */
    private ArrayList<NetNodeLocation> removeLocalNode(ArrayList<NetNodeLocation> nodeList, NetNode node) {

        for (int i = 0; i < nodeList.size(); i++) {
            try {
                if (nodeList.get(i).toUrl().compareTo(node.getOwnLocation().toUrl()) == 0) {
                    nodeList.remove(i);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("[REMOVE-LOCAL-NODE] Unable to remove the node");
            }
        }
        return nodeList;
    }

}