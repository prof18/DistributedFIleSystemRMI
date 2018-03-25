package fs.actions.interfaces;

import fs.objects.structure.FSTreeNode;

public interface FSOperationI {

    void createDirectory(FSTreeNode currentNode, String dirName, NewItemCallback callback);

    interface NewItemCallback {
        void onItemCreated(FSTreeNode fsTreeNode);
    }
}
