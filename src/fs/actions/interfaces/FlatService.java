package fs.actions.interfaces;

import fs.actions.object.CacheFileWrapper;
import fs.objects.structure.FileAttribute;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.rmi.Remote;

public interface FlatService extends Remote, Serializable {

    byte[] read(String fileID, int i, int n) throws FileNotFoundException;

    byte[] read(String fileId, int offset) throws FileNotFoundException;

    void write(String fileID, int i, int count, byte[] data) throws FileNotFoundException;

    String create(String host,FileAttribute attribute) throws Exception;

    String create(String host) throws Exception;

    CacheFileWrapper getFileAndAttribute(String UFID);

    void delete(String fileID);

    FileAttribute getAttributes(String fileID);

    void setAttributes(String fileID, FileAttribute attr);
}