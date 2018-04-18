package utils;

import fs.objects.json.JsonFile;
import fs.objects.json.JsonFolder;
import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;
import net.objects.RegistryWrapper;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.xml.bind.DatatypeConverter;

public class Util {

    public static RegistryWrapper getNextFreePort() {
        Registry registry = null;
        int port = 1099;
        boolean notFound = true;
        while (notFound) {
            try {
                registry = LocateRegistry.createRegistry(port);
                notFound = false;
            } catch (RemoteException e) {
                System.out.println("porta occupata");
                port++;
            }
        }
        return new RegistryWrapper(port, registry);
    }

    public static void plot(HashMap<Integer, NetNodeLocation> hashMap) {
        String leftAlignFormat = "| %-15d | %-10s | %-8d |  %-8s|%n";

        System.out.format("+-----------------+------------+----------+----------+%n");
        System.out.format("| NameHost        | Ip         | Port     |   Name   |%n");
        System.out.format("+-----------------+------------+----------+----------+%n");

        for (Map.Entry<Integer, NetNodeLocation> entry : hashMap.entrySet()) {
            System.out.format(leftAlignFormat, entry.getKey(), entry.getValue().getIp(), entry.getValue().getPort(), entry.getValue().getName());
            System.out.format("+-----------------+------------+----------+----------+%n");
        }

    }

    public static void plotService(Registry registry) {
        try {
            String[] lista = registry.list();
            for (String tmp : lista) {
                System.out.printf(tmp);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method generates a fake file system configuration and save it on the
     * properties
     */
    public static void saveFSExample() {
        HashMap<String, JsonFolder> folderMap = generateFakeObjects();
        String json = GSONHelper.getInstance().foldersToJson(folderMap);
        System.out.println("json = " + json);

        PropertiesHelper.getInstance().writeConfig(Constants.FOLDERS_CONFIG, json);
    }

    private static HashMap<String, JsonFolder> generateFakeObjects() {

        HashMap<String, JsonFolder> folderMap = new HashMap<>();

        //Root
        JsonFolder root = new JsonFolder();
        String rootUFID = UUID.randomUUID().toString();
        root.setUFID(rootUFID);
        root.setRoot(true);
        root.setFolderName("root");
        root.setLastEditTime(System.currentTimeMillis());

        //folder1
        JsonFolder folder1 = new JsonFolder();
        String folder1UFID = UUID.randomUUID().toString();
        folder1.setUFID(folder1UFID);
        folder1.setRoot(false);
        folder1.setFolderName("Folder 1");
        folder1.setParentUFID(rootUFID);
        folder1.setLastEditTime(System.currentTimeMillis());

        //folder2
        JsonFolder folder2 = new JsonFolder();
        String folder2UFID = UUID.randomUUID().toString();
        folder2.setUFID(folder2UFID);
        folder2.setRoot(false);
        folder2.setFolderName("Folder 2");
        folder2.setParentUFID(folder1UFID);
        folder2.setLastEditTime(1519167600000L);

        //folder3
        JsonFolder folder3 = new JsonFolder();
        String folder3UFID = UUID.randomUUID().toString();
        folder3.setUFID(folder3UFID);
        folder3.setRoot(false);
        folder3.setFolderName("Folder 3");
        folder3.setParentUFID(folder1UFID);
        long folder3Time = System.currentTimeMillis();
        folder3.setLastEditTime(folder3Time);
        folder1.setLastEditTime(folder3Time);
        root.setLastEditTime(folder3Time);

        //set root children
        ArrayList<String> childrenRoot = new ArrayList<>();
        childrenRoot.add(folder1UFID);
        root.setChildren(childrenRoot);

        //set folder 1 children
        ArrayList<String> folder1Children = new ArrayList<>();
        folder1Children.add(folder2UFID);
        folder1Children.add(folder3UFID);
        folder1.setChildren(folder1Children);

        //generate files
        JsonFile file1 = new JsonFile();
        String UFIDFile1 = UUID.randomUUID().toString();
        file1.setUFID(UFIDFile1);
        file1.setFileName("File 1");
        file1.setPath("/File 1");
        FileAttribute attribute1 = new FileAttribute();
        attribute1.setFileLength(2048L);
        attribute1.setOwner("marco");
        attribute1.setType("txt");
        attribute1.setCreationTime(new Date(1519167600000L));
        attribute1.setLastModifiedTime(new Date(1519167600000L));
        file1.setAttribute(attribute1);

        JsonFile file2 = new JsonFile();
        String UFIDFile2 = UUID.randomUUID().toString();
        file2.setUFID(UFIDFile2);
        file2.setFileName("File 2");
        file2.setPath("/File 2");
        FileAttribute attribute2 = new FileAttribute();
        attribute2.setFileLength(10556L);
        attribute2.setOwner("lr");
        attribute2.setType("pdf");
        long file2Time = System.currentTimeMillis();
        attribute2.setCreationTime(new Date(file2Time));
        attribute2.setLastModifiedTime(new Date(file2Time));
        file2.setAttribute(attribute2);
        root.setLastEditTime(file2Time);

        JsonFile file3 = new JsonFile();
        String UFIDFile3 = UUID.randomUUID().toString();
        file3.setUFID(UFIDFile3);
        file3.setFileName("File 3");
        file3.setPath("/Folder 1 /Folder 2/File 3");
        FileAttribute attribute3 = new FileAttribute();
        attribute3.setFileLength(25478L);
        attribute3.setOwner("zigio");
        attribute3.setType("img");
        attribute3.setCreationTime(new Date(1519167600000L));
        attribute3.setLastModifiedTime(new Date(1519167600000L));
        file3.setAttribute(attribute3);

        //set root files
        ArrayList<JsonFile> rootFile = new ArrayList<>();
        rootFile.add(file1);
        rootFile.add(file2);
        root.setFiles(rootFile);

        ArrayList<JsonFile> folder2File = new ArrayList<>();
        folder2File.add(file3);
        folder2.setFiles(folder2File);

        folderMap.put(rootUFID, root);
        folderMap.put(folder1UFID, folder1);
        folderMap.put(folder2UFID, folder2);
        folderMap.put(folder3UFID, folder3);

        return folderMap;

    }

    public static String getChecksum(byte[] ab) {
        String result;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(ab);
            byte[] hash = md.digest();
            result = bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "Checksum not calculated";
        }

        return result;
    }

    private static String bytesToHex(byte[] hash) {

        return DatatypeConverter.printHexBinary(hash).toLowerCase();

    }

}
