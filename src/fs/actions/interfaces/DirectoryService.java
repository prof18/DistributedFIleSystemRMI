package fs.actions.interfaces;

import fs.objects.structure.FSTreeNode;

public interface DirectoryService {

    void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback);
    void renameDirectory(FSTreeNode nodeToRename, String newName, NewItemCallback callback);
    void deleteDirectory(FSTreeNode nodeToDelete, NewItemCallback callback);

    String lookup(String dir, String name);
    void addName(String dir, String name, String fileID);
    void unName(String dir, String name);
    String getNames(String dir, String pattern);

    interface NewItemCallback {
        void onItemChanged(FSTreeNode fsTreeNode);
    }
}
