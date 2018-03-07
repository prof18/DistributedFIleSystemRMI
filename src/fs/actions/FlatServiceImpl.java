package fs.actions;

/*
FlatServiceImpl è ad un livello superiore rispetto a NodeImpl e quindi lo inizializza
 */

import fs.actions.interfaces.FlatService;
import fs.objects.structure.FileAttribute;
import net.objects.NetNodeImpl;
import net.objects.NetNodeLocation;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;
import java.util.*;


public class FlatServiceImpl implements FlatService {

    private HashMap<Integer, NetNodeLocation> nodes;
    private String path;
    private NodeCache nodeCache;

    public FlatServiceImpl(String path, String ownIP, String nameService) {
        Scanner scanner = new Scanner(System.in);
        this.path = path;
        this.nodes = FlatServiceUtil.create(ownIP, nameService);
        System.out.println("sono tornato al costruttore");
        nodeCache = new NodeCache();
    }


    public byte[] read(String fileID, int offset, int count) throws FileNotFoundException {
        FileInputStream fileInputStream = null;
        byte[] content = null;
        try {
            File file = new File(path + fileID);
            if (!file.exists()) {
                CacheFileWrapper retFile = nodeCache.get(fileID);
                if (retFile != null) {
                    System.out.println("file trovato nella cache");
                    //devo controllare se è ancora valido il file salvato nella cache
                    long elapsedTime = Date.from(Instant.now()).getTime() - retFile.getLastValidatedTime();
                    if (elapsedTime < nodeCache.getTimeInterval()) {
                        System.out.println("il file è ancora valido");
                        file = retFile.getFile();
                    } else {
                        NetNodeLocation netNodeLocation = retFile.getOriginLocation();
                        Registry registry = LocateRegistry.getRegistry(netNodeLocation.getIp(), netNodeLocation.getPort());
                        try {
                            NetNodeImpl node = (NetNodeImpl) registry.lookup(netNodeLocation.getName());
                            CacheFileWrapper fileWrapperMaster = node.getFile(fileID);
                            if (fileWrapperMaster.getLastValidatedTime() == retFile.getLastValidatedTime()) {
                                System.out.println("il file non è stato modificato");
                                retFile.setLastValidatedTime(Date.from(Instant.now()));
                                file = retFile.getFile();
                            } else {
                                System.out.println("il file è stato modificato");
                                retFile = fileWrapperMaster;
                                file = retFile.getFile();
                            }
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    System.out.println("il file non è contenuto nella cache");
                    for (Map.Entry<Integer, NetNodeLocation> entry : nodes.entrySet()) {
                        Registry registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                        try {
                            NetNodeImpl node = (NetNodeImpl) registry.lookup(entry.getValue().getName());
                            CacheFileWrapper fileTemp = node.getFile(fileID);
                            if (fileTemp != null) {
                                file = fileTemp.getFile();
                                nodeCache.put(fileID, new CacheFileWrapper(fileTemp.getFile(), fileTemp.getAttribute(), entry.getValue()));
                                break;
                            }
                        } catch (NotBoundException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            System.out.println("[READ XX] lunghezza file: " + file.length());
            content = new byte[(int) file.length()];
            fileInputStream = new FileInputStream(file);
            System.out.println("count = " + count);
            System.out.println("length content = " + content.length);
            fileInputStream.read(content, offset, count);
            System.out.println("[READ XX] contenuto: " + new String(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    public byte[] read(String fileID, int offset) throws FileNotFoundException {
        File file = new File(path + fileID);
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        long length = file.length();
        System.out.println("[READ]: lunghezza file " + length);
        byte[] content = read(fileID, offset, (int) length);
        System.out.println("[READ]: contenuto: " + new String(content));
        return content;
    }


    public void write(String fileID, int offset, int count, byte[] data) throws FileNotFoundException {
        byte[] newContent = null;
        try {
            byte[] content = read(fileID, 0);
            newContent = new byte[0];
            newContent = joinArray(content, data, offset, count);
        } catch (Exception e) {
            System.out.println("problema con gli indici");
            e.printStackTrace();
            System.exit(-1);
        }

        FileOutputStream fileOutputStream = null;
        try {
            File file = new File(path + fileID);
            System.out.println(file.delete());
            file = new File(path + fileID);
            fileOutputStream = new FileOutputStream(file);
            System.out.println("newContent = " + new String(newContent));
            fileOutputStream.write(newContent, 0, newContent.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String create(String name, FileAttribute attribute) throws Exception {
        File file = new File(name);
        if (file.exists()) {
            throw new Exception();
        }
        file.createNewFile();
        File fileAttr = new File(name + ".attr");
        FileOutputStream fileOutputStream = new FileOutputStream(name + ".attr");
        ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
        outputStream.writeObject(fileAttr);
        outputStream.close();
        return "dir" + "name";
    }

    @Override
    public String create(String name) throws Exception {
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        create(path + name, attribute);
        return path + name;
    }

    public void delete(String fileID) {
        File file = new File(path + fileID);
        File fileAttr = new File(path + fileID + ".attr");
        System.out.println(file.delete());
        System.out.println(fileAttr.delete());
    }


    public FileAttribute getAttributes(String fileID) {
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        FileAttribute fileAttribute = null;
        try {
            fileInputStream = new FileInputStream(fileID + ".attr");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            objectInputStream = new ObjectInputStream(fileInputStream);
            fileAttribute = (FileAttribute) objectInputStream.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return fileAttribute;

    }


    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] joinArray(byte[] first, byte[] second, int offset, int count) throws Exception {
        System.out.println("[JOINARRAY]: prima stringa : " + new String(first));
        System.out.println("[JOINARRAY]: seconda stringa : " + new String(second));
        byte[] ret = new byte[first.length + second.length];
        int i = 0;
        if (offset > first.length) throw new Exception();
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
        System.out.println("[JOIN]: la stringa concatenata: " + new String(ret));
        byte[] cleanRet = cleanArray(ret);
        System.out.println("[JOIN]: la stringa concatenata pulita: " + new String(cleanRet));
        return cleanRet;
    }

    private byte[] cleanArray(byte[] tmp) {
        int count = tmp.length - 1;
        while (tmp[count] == 0) {
            count--;
        }
        byte[] newArray = Arrays.copyOfRange(tmp, 0, count + 1);
        return newArray;


    }
}
