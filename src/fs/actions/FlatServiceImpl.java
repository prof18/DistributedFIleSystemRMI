package fs.actions;

/*
FlatServiceImpl è ad un livello superiore rispetto a NodeImpl e quindi lo inizializza
 */

import fs.actions.cache.ReadingNodeCache;
import fs.actions.cache.WritingNodeCache;
import fs.actions.interfaces.FlatService;
import fs.actions.object.CacheFileWrapper;
import fs.actions.object.WrapperFlatServiceUtil;
import fs.actions.object.WritingCacheFileWrapper;
import fs.objects.structure.FileAttribute;
import mediator_fs_net.MediatorFsNet;
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

    private MediatorFsNet mediator;
    private final String path;
    private ReadingNodeCache readingCache;
    private WritingNodeCache writingNodeCache;

    public FlatServiceImpl(String path, MediatorFsNet mediatorFsNet) {
        mediator = mediatorFsNet;
        this.path = path;
        System.out.println("sono tornato al costruttore");
        readingCache = new ReadingNodeCache();
        writingNodeCache = new WritingNodeCache(mediator);
    }

    /**
     * Lettura del file con nome fileID, con offset e count inseriti dall'utente, in caso il file
     * non venga trovato sia in locale che in remoto viene lanciata l'eccezione FileNotFoundException.
     */
    public byte[] read(String fileID, int offset, int count) throws FileNotFoundException {
        byte[] ret = read(fileID, offset);
        byte[] newRet = new byte[count];
        System.arraycopy(ret, 0, newRet, 0, count);
        return newRet;
    }

    /**
     * Lettura del file con nome file ID, e offset assegnato con l'utente, il count è assegnato automaticamente
     * al valore corrispondente alla lunghezza del file, se il file non viene trovato sia in locale che in
     * remoto viene lanciata l'eccezione FileNotFoundException.
     */
    public byte[] read(String fileID, int offset) throws FileNotFoundException {
        File file = getFile(fileID).getFile();
        int count = (int) file.length();
        byte[] content = new byte[count];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            fileInputStream.read(content, offset, count);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return content;
    }

    //utilizza cache in scrittura
    public void write(String fileID, int offset, int count, byte[] data) {
        byte[] newContent = null;
        byte[] content = null;
        boolean fileAvailable = false;
        try {
            content = read(fileID, 0);
            fileAvailable = true;
        } catch (NullPointerException e) {
            System.out.println("il file ha qualche problema");
        } catch (FileNotFoundException e) {
            System.out.println("il file non è presente");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("problema con gli indici");
            e.printStackTrace();
            System.exit(-1);
        }

        if (fileAvailable) {
            newContent = joinArray(content, data, offset, count);
            ObjectInputStream ois = null;
            Date lastModified = null;
            FileAttribute fileAttribute = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(fileID + ".attr"));
                fileAttribute = (FileAttribute) ois.readObject();
                lastModified = fileAttribute.getLastModifiedTime();
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
                setAttributes(fileID, fileAttribute);
                writingNodeCache.add(new WritingCacheFileWrapper(file, fileAttribute, lastModified, fileID));
                System.out.println("newContent = " + new String(newContent));
                fileOutputStream.write(newContent, 0, newContent.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //utilizzo replicazione
    public String create(FileAttribute attribute) throws Exception {
        //TODO : aggiungere un identificativo per l'host
        String pathName = "file" + Date.from(Instant.now()).hashCode();
        File file = new File(pathName);
        if (file.exists()) {
            throw new FileNotFoundException();
        }
        file.createNewFile();
        FileOutputStream out = new FileOutputStream(pathName + ".attr");
        ObjectOutputStream oout = new ObjectOutputStream(out);
        oout.writeObject(attribute);
        oout.flush();
        //in questo punto deve essere aggiunta la replicazione
        return pathName;
    }

    /**
     * Utilizzo replicazione, ma viene gestita dal metodo sopra
     */
    @Override
    public String create() throws Exception {
        Date date = Date.from(Instant.now());
        FileAttribute attribute = new FileAttribute(0, date, date, 1);
        return create(attribute);

    }

    //bisogna decidere se il file deve essere eliminato solo in questo host oppure in tutti
    public void delete(String fileID) {
        File file = new File(path + fileID);
        File fileAttr = new File(path + fileID + ".attr");
        System.out.println(file.delete());
        System.out.println(fileAttr.delete());
    }

    /**
     * Ritorna gli attributi di uno specifico file
     *
     * @param fileID nome del file
     * @return ritorna gli attributi di un file in formato FileAttribute
     */


    public FileAttribute getAttributes(String fileID) {
        CacheFileWrapper cacheFileWrapper = null;
        try {
            cacheFileWrapper = getFile(fileID);
        } catch (FileNotFoundException e) {
            System.out.println("file non trovato sia in locale che in remoto");
        }
        return cacheFileWrapper.getAttribute();
    }

    /**
     * Setta gli attributi di un file,
     *
     * @param fileID nome del file
     * @param attr   nuovi attributi da scrivere nel file
     */


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
        File file = new File(path + UFID);
        if (file.exists()) {  //il file è contenuto nella memoria interna
            return new CacheFileWrapper(file, getLocalAttributeFile(UFID),UFID);
        } else {
            CacheFileWrapper cacheFileWrapper = getCacheFile(UFID);
            if (cacheFileWrapper != null) {//il file è contenuto nella cache
                return cacheFileWrapper;
            } else return mediator.getFile(UFID);
        }
    }

    private FileAttribute getLocalAttributeFile(String UFID) {
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
        return ret;
    }

    private CacheFileWrapper getCacheFile(String UFID) {
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
        File file = new File(UFID);
        FileAttribute ret;
        if (file.exists()) {
            ret = getAttributes(UFID);
            return new CacheFileWrapper(file, ret,UFID);
        } else {
            return null;
        }
    }

}
