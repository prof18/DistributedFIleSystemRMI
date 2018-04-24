package fs.actions.interfaces;

import fs.actions.object.CacheFileWrapper;
import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileAttribute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;

/**
 * This interface represent all the operation related to the manage of the file
 */
public interface FileService extends Remote, Serializable {
    /**
     * This method is used to read a subset of a file content
     * @param fileID is the unique identifier of the file
     * @param offset indicates the first byte to read
     * @param count is the number of byte to read
     * @return the array of byte to read
     * @throws FileNotFoundException
     */
    byte[] read(String fileID, int offset, int count) throws FileNotFoundException;

    /**
     * This method is used to read all the content of a file
     * @param fileId is the unique identifier of the file
     * @param offset indicates the first byte to read
     * @return the array of byte to read
     * @throws FileNotFoundException
     */
    byte[] read(String fileId, int offset) throws FileNotFoundException;

    /**
     * This method is used to write in a file content
     * @param fileID is the unique identifier of the file
     * @param offset is the start index where begin to write
     * @param count is the number of byte to write
     * @param data is the array of byte to write
     * @throws FileNotFoundException
     */
    void write(String fileID, int offset, int count, byte[] data) throws FileNotFoundException;

    /**
     * This method is used to create a new file in the filesystem
     * @param host is the host that creates the new file
     * @param curDir
     * @return
     * @throws IOException
     */
    String create(String host, FSTreeNode curDir) throws IOException;

    /**
     *
     * @param UFID
     * @return
     */
    CacheFileWrapper getFileAndAttribute(String UFID);

    /**
     *
     * @param fileID
     */
    void delete(String fileID);

    /**
     *
     * @param fileID
     * @return
     * @throws FileNotFoundException
     */
    FileAttribute getAttributes(String fileID) throws FileNotFoundException;

    /**
     *
     * @param fileID
     * @param attr
     */
    void setAttributes(String fileID, FileAttribute attr);

}