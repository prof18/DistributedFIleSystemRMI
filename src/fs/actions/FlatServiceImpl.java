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
        System.out.println("entrato nel READ");
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

    //utilizza cache in scrittura

    //TODO: verifica Write
    public void write(String fileID, int offset, int count, byte[] data) throws FileNotFoundException {
        System.out.println("entrato nel write");
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
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
            newContent = joinArray(content, data, offset, count);
            System.out.println("[WRITE] nuovo contenuto da sovrascrivere : " + new String(newContent));
            ObjectInputStream ois = null;
            Date lastModified = null;
            FileAttribute fileAttribute = null;
            try {
                System.out.println("[WRITE] " + path + fileID);
                ois = new ObjectInputStream(new FileInputStream(path + fileID + ".attr"));
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
                writingNodeCache.add(new WritingCacheFileWrapper(file, fileAttribute, lastModified, fileID, true));
                System.out.println("newContent = " + new String(newContent));
                fileOutputStream.write(newContent, 0, newContent.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("il file che si vuole sovrascrivere non è locale");
            FileInputStream fis = new FileInputStream(cacheFileWrapper.getFile());
            byte[] context = new byte[(int) cacheFileWrapper.getFile().length()];
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
            FileOutputStream fos=new FileOutputStream(cacheFileWrapper.getFile());
            byte[] newctx=joinArray(context,data,offset,count);
            System.out.println("contenuto da scrivere : "+new String(newctx));
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
            WritingCacheFileWrapper wcfw=new WritingCacheFileWrapper(cacheFileWrapper.getFile(),cacheFileWrapper.getAttribute(),Date.from(Instant.now()),fileID,false);
            writingNodeCache.add(wcfw);
        }


    }


    //utilizzo replicazione

    //TODO:verifica create
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
    //TODO:verifica delete
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


    //TODO: verifica getAttributes
    public FileAttribute getAttributes(String fileID) {
        System.out.println("entrato in getAttributes");
        CacheFileWrapper cacheFileWrapper = getFile(fileID);
        return cacheFileWrapper.getAttribute();
    }

    /**
     * Setta gli attributi di un file,
     *
     * @param fileID nome del file
     * @param attr   nuovi attributi da scrivere nel file
     */


    //TODO: verifica setAttributes
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


    private CacheFileWrapper getFile(String UFID) {
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
        FileAttribute ret;
        if (file.exists()) {
            System.out.println("trovato il file " + path + UFID);
            ret = getAttributes(UFID);
            return new CacheFileWrapper(file, ret, UFID, true);
        } else {
            System.out.println("non trovato il file " + UFID);
            return null;
        }
    }

}
