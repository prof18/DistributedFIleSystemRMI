package ui.utility;

import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;

/**
 * The element of the File Table View
 */
public class TableItem {

    private FSTreeNode treeNode;
    private FileWrapper fileWrapper;
    private boolean isFile;

    public FSTreeNode getTreeNode() {
        return treeNode;
    }

    public void setTreeNode(FSTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    public FileWrapper getFileWrapper() {
        return fileWrapper;
    }

    public void setFileWrapper(FileWrapper fileWrapper) {
        this.fileWrapper = fileWrapper;
    }

    public boolean isFile() {
        return isFile;
    }

    public void setFile(boolean file) {
        isFile = file;
    }
}
