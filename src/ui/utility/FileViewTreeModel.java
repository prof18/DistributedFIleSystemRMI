package ui.utility;

import fs.objects.structure.FSTreeNode;
import fs.objects.structure.FileWrapper;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Vector;

public class FileViewTreeModel implements TreeModel {

    private FSTreeNode node;

    private Vector<TreeModelListener> treeModelListeners =
            new Vector<TreeModelListener>();

    public FileViewTreeModel(FSTreeNode node) {
        this.node = node;
    }

    public void setNode(FSTreeNode node) {
        this.node = node;
    }

    private ArrayList<Object> getChildAndFile(FSTreeNode node) {
        ArrayList<Object> children = new ArrayList<>();
        if (node.getChildrens() != null)
            children.addAll(node.getChildrens());
        if (node.getFiles() != null)
            children.addAll(node.getFiles());
        return children;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        FSTreeNode node = (FSTreeNode) parent;
        return getChildAndFile(node).indexOf((child));
    }


    // Returns the child of parent at index index in the parent's child array.
    @Override
    public Object getChild(Object parent, int i) {
        FSTreeNode node = (FSTreeNode) parent;
        return getChildAndFile(node).get(i);
    }

    @Override
    public int getChildCount(Object parent) {
        FSTreeNode node = (FSTreeNode) parent;
        return getChildAndFile(node).size();
    }


    @Override
    public Object getRoot() {
        return node;
    }

    // Messaged when the user has altered the value for the item identified by path to newValue.
    @Override
    public void valueForPathChanged(TreePath treePath, Object o) {

    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof FileWrapper;
    }

    //Adds a listener for the TreeModelEvent posted after the tree changes.
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }
}
