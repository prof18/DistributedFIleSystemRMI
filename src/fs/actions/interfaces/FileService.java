package fs.actions.interfaces;

import fs.actions.object.CacheFileWrapper;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileAttribute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;

public interface FileService extends Remote, Serializable {

    byte[] read(String fileID, int i, int n) throws FileNotFoundException;

    byte[] read(String fileId, int offset) throws FileNotFoundException;

    void write(String fileID, int i, int count, byte[] data) throws FileNotFoundException;


    String create(String host) throws IOException;

    CacheFileWrapper getFileAndAttribute(String UFID);

    void delete(String fileID);

    FileAttribute getAttributes(String fileID) throws FileNotFoundException;

    void setAttributes(String fileID, FileAttribute attr);

}