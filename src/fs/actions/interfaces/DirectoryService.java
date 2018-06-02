package fs.actions.interfaces;

import fs.objects.structure.FSTreeNode;

/**
 * This interface represent all the operation related to the manage of the directory
 */
public interface DirectoryService {

    /**
     * This method is used to connect the directory service with the file service
     *
     * @param fileService is the instance of the class to connect
     */
    void setFileService(FileService fileService);

    /**
     * This method is used to created a new directory
     *
     * @param currentNode is the node where creating the new directory
     * @param dirName     is the name to assign to the new directory
     * @param callback    is the callback used to inform the UI about the creation of the directory
     */
    void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback);

    /**
     * This method is used to change the name of a directory
     *
     * @param nodeToRename it is the node that represents the directory to rename
     * @param newName      it is the new name to assign to the directory
     * @param callback     is the callback used to inform the UI about the changing of the directory name
     */
    void renameDirectory(FSTreeNode nodeToRename, String newName, NewItemCallback callback);

    /**
     * This method is used to remove a directory
     *
     * @param nodeToDelete is the node that represent the directory to delete
     * @param callback     is the callback used to inform the UI about the directory deletion
     */
    void deleteDirectory(FSTreeNode nodeToDelete, NewItemCallback callback);

    /**
     * This method is used to add a new file to the directory
     *
     * @param currentNode is the node that represents the directory where add the file
     * @param name        is the name to assign to the file
     * @param fileID      is the unique identifier of the file
     * @param callback    is the callback used to inform the UI about the adding of the file
     */
    void addName(FSTreeNode currentNode, String name, String fileID, NewItemCallback callback);

    /**
     * This callback is used to inform the UI about the change in the directory
     */
    interface NewItemCallback {
        void onItemChanged(FSTreeNode fsTreeNode);

    }
}
