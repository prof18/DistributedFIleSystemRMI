package fs.actions.interfaces;

import fs.objects.structure.FSTreeNode;

public interface FSOperationI {

    void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback);
    void renameDirectory(FSTreeNode nodeToRename, String newName, NewItemCallback callback);
    void deleteDirectory(FSTreeNode nodeToDelete, NewItemCallback callback);

    interface NewItemCallback {
        void onItemChanged(FSTreeNode fsTreeNode);
    }
}
