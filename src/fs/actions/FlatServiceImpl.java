package fs.actions;

/*
FlatServiceImpl è ad un livello superiore rispetto a NodeImpl e quindi lo inizializza
 */

import fs.actions.interfaces.FlatService;
import fs.objects.structure.FileAttribute;
import net.objects.NetNodeLocation;
import net.objects.interfaces.NetNode;
import utils.Util;

import java.io.*;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.Instant;
import java.util.*;


public class FlatServiceImpl implements FlatService {

    private HashMap<Integer, NetNodeLocation> nodes;
    private final String path;
    private ReadingNodeCache readingCache;
    private WritingNodeCache writingNodeCache;

    public FlatServiceImpl(String path, String ownIP, String nameService) {
        this.path = path;
        this.nodes = FlatServiceUtil.create(path, ownIP, nameService);
        System.out.println("sono tornato al costruttore");
        readingCache = new ReadingNodeCache();
        writingNodeCache=new WritingNodeCache();
        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                writingNodeCache.setNodeLocations(nodes);
            }
        },0,6000);
    }

    //utilizza cache in lettura
    public byte[] read(String fileID, int offset, int count) throws FileNotFoundException {
        byte[] ret = read(fileID, offset);
        byte[] newRet = new byte[count];
        System.arraycopy(ret, 0, newRet, 0, count);
        return newRet;
    }

    //utilizza cache in lettura
    public byte[] read(String fileID, int offset) throws FileNotFoundException {
            File file=getFile(fileID).getFile();
            System.out.println("[READ XX] lunghezza file: " + file.length());
            int count = (int) file.length();
            byte[] content = new byte[count];
            FileInputStream fileInputStream = new FileInputStream(file);
            System.out.println("count = " + count);
            System.out.println("length content = " + content.length);
        try {
            fileInputStream.read(content, offset, count);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        System.out.println("[READ XX] contenuto: " + new String(content));
        return content;
    }

    //utilizza cache in scrittura
    public void write(String fileID, int offset, int count, byte[] data) {
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

        ObjectInputStream ois = null;
        Date lastModified=null;
        FileAttribute fileAttribute=null;
        try {
            ois = new ObjectInputStream(new FileInputStream("test.attr"));
            fileAttribute = (FileAttribute) ois.readObject();
            lastModified=fileAttribute.getLastModifiedTime();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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
            setAttributes(fileID,fileAttribute);
            writingNodeCache.add(new WritingCacheFileWrapper(file,fileAttribute,lastModified,null));
            System.out.println("newContent = " + new String(newContent));
            fileOutputStream.write(newContent, 0, newContent.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //utilizzo replicazione
    public String create(String pathName, FileAttribute attribute) throws Exception {
        System.out.println("pathName = " + pathName);
        File file = new File(pathName);
        if (file.exists()) {
            throw new Exception();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(pathName + ".attr");
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(attribute);
        oout.flush();
        //in questo punto deve essere aggiunta la replicazione
        return "dir" + "name";
    }

    //utilizzo replicazione
    @Override
    public String create(String name) throws Exception {
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        create(path + name, attribute);
        //in questo punto deve essere aggiunta la replicazione
        return path + name;
    }

    //bisogna decidere se il file deve essere eliminato solo in questo host oppure in tutti
    public void delete(String fileID) {
        File file = new File(path + fileID);
        File fileAttr = new File(path + fileID + ".attr");
        System.out.println(file.delete());
        System.out.println(fileAttr.delete());
    }

    // utilizza cache in lettura
    public FileAttribute getAttributes(String fileID) {
        CacheFileWrapper cacheFileWrapper=getFile(fileID);
        return cacheFileWrapper.getAttribute();
    }

    // utilizza cache in scrittura
    public void setAttributes(String fileID, FileAttribute attr) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileID + ".attr");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(attr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //gestire aggiornamento delle informazioni
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
        return Arrays.copyOfRange(tmp, 0, count + 1);



    }

    private CacheFileWrapper getFile(String UFID) {
        File file = new File(path + UFID);
        if (file.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream("test.attr"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileAttribute ret = null;
            try {
                ret = (FileAttribute) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return new CacheFileWrapper(file, ret, null);
        } else {
            CacheFileWrapper retFile = readingCache.get(UFID);
            if (retFile != null) {
                System.out.println("file trovato nella cache");
                //devo controllare se è ancora valido il file salvato nella cache
                long elapsedTime = Date.from(Instant.now()).getTime() - retFile.getLastValidatedTime();
                System.out.println("elapsedTime = " + elapsedTime);
                if (elapsedTime < readingCache.getTimeInterval()) {
                    System.out.println("[CACHE] : il file è ancora valido");
                    return retFile;
                } else {
                    System.out.println("[CACHE] : il file è obsoleto");
                    NetNodeLocation netNodeLocation = retFile.getOriginLocation();
                    System.out.println("netNodeLocation.toUrl() = " + netNodeLocation.toUrl());
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(netNodeLocation.getIp(), netNodeLocation.getPort());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    try {
                        NetNode node = (NetNode) registry.lookup(netNodeLocation.toUrl());
                        CacheFileWrapper fileWrapperMaster = node.getFile(UFID);
                        if (fileWrapperMaster.getLastValidatedTime() == retFile.getLastValidatedTime()) {
                            System.out.println("il file non è stato modificato");
                            retFile.setLastValidatedTime(Date.from(Instant.now()));
                            file = retFile.getFile();
                        } else {
                            System.out.println("il file è stato modificato");
                            retFile = fileWrapperMaster;
                            file = retFile.getFile();
                        }
                        return retFile;
                    } catch (NotBoundException | AccessException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("il file non è contenuto nella cache");
                for (Map.Entry<Integer, NetNodeLocation> entry : nodes.entrySet()) {
                    Registry registry = null;
                    try {
                        registry = LocateRegistry.getRegistry(entry.getValue().getIp(), entry.getValue().getPort());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    try {
                        Util.plotService(registry);
                        NetNode node = null;
                        try {
                            node = (NetNode) registry.lookup(entry.getValue().toUrl());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        CacheFileWrapper fileTemp = node.getFile(UFID);
                        if (fileTemp != null) {
                            readingCache.put(UFID, new CacheFileWrapper(fileTemp.getFile(), fileTemp.getAttribute(), entry.getValue()));
                            return fileTemp;
                        }
                    } catch (NotBoundException | RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return null;
    }
}
