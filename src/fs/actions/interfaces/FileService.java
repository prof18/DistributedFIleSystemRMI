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
     * @param curDir is the current directory where saving the file
     * @return the unique identifier UFID of the new file
     * @throws IOException
     */
    String create(String host, FSTreeNode curDir) throws IOException;

    /**
     * This method returns a wrapper that contains both the file and own attribute
     * @param UFID is the unique identifier of the selected file
     * @return a CacheFileWrapper that contains both the file and own attribute
     */
    CacheFileWrapper getFileAndAttribute(String UFID);

    /**
     * This method is used in order to delete a specific file
     * @param fileID is the unique identifier of the file
     * @param currentNode is the current node in the directory three
     * @param callback is the callback that informs the deletion of a file
     */

    void delete(String fileID, FSTreeNode currentNode, DeleteFileCallback callback);

    /**
     * With this method it is possible to get the attribute related to a File
     * @param fileID is the unique identifier of the file
     * @return an instance of FileAttribute that contains all the file attributes
     * @throws FileNotFoundException
     */
    FileAttribute getAttributes(String fileID) throws FileNotFoundException;

    /**
     * This method is used to update the information about a file
     * @param fileID is the unique identifier of a file
     * @param attr is the new instance of the class FileAttribute to save
     */
    void setAttributes(String fileID, FileAttribute attr);

    /**
     * Is the callback used to inform the UI of the deletion a file
     */
    interface DeleteFileCallback {
        void onItemChanged(FSTreeNode fsTreeNode);
    }
}