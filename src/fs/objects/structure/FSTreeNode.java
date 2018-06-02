package fs.objects.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This objects represents the internal structure of the File System
 */
public class FSTreeNode implements Serializable {


    private String UFID;
    private String nameNode;
    private ArrayList<FSTreeNode> children;
    private FSTreeNode parent;
    private ArrayList<FileWrapper> files = new ArrayList<>();
    private long lastEditTime;
    private String jsonTree;
    private String owner;

    public FSTreeNode(String UFID, String nameNode, ArrayList<FSTreeNode> children, FSTreeNode parent) {
        this.UFID = UFID;
        this.nameNode = nameNode;
        this.children = children;
        this.parent = parent;
    }

    public FSTreeNode() {
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public void updateAncestorTime() {
        Queue<FSTreeNode> queue = new LinkedList<>();
        queue.add(this.getParent());
        while (!queue.isEmpty()) {
            FSTreeNode node = queue.poll();
            node.setLastEditTime(this.getLastEditTime());
            if (node.getParent() != null)
                queue.add(node.getParent());
        }
    }

    public String getUFID() {
        return UFID;
    }

    public String getNameNode() {
        return nameNode;
    }

    public ArrayList<FSTreeNode> getChildren() {
        return children;
    }

    public FSTreeNode getParent() {
        return parent;
    }

    public ArrayList<FileWrapper> getFiles() {
        return files;
    }

    public String getJson() {
        return jsonTree;
    }

    public void setJson(String gsonTree) {
        this.jsonTree = gsonTree;
    }

    public void setUFID(String UFID) {
        this.UFID = UFID;
    }

    public void setNameNode(String nameNode) {
        this.nameNode = nameNode;
    }

    public void setChildren(ArrayList<FSTreeNode> children) {
        this.children = children;
    }

    public void setParent(FSTreeNode parent) {
        this.parent = parent;
    }

    public void setFiles(ArrayList<FileWrapper> files) {
        this.files = files;
    }

    public void addChild(FSTreeNode child) {
        children.add(child);
    }

    public void addFiles(FileWrapper file) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(file);
    }

    private int findFilePos(String UFID) {
        int pos = -1;
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getUFID().compareTo(UFID) == 0) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    public FSTreeNode findNodeByUFID(FSTreeNode node, String UFID) {
        Queue<FSTreeNode> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            FSTreeNode nodeExtracted = queue.poll();
            if (nodeExtracted != null) {
                if (nodeExtracted.getUFID().compareTo(UFID) == 0)
                    return nodeExtracted;
                else
                    queue.addAll(nodeExtracted.getChildren());
            }
        }
        return null;
    }

    public FSTreeNode findNodeByName(FSTreeNode node, String name) {
        Queue<FSTreeNode> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            FSTreeNode nodeExtracted = queue.poll();
            if (nodeExtracted != null) {
                if (nodeExtracted.getNameNode().compareTo(name) == 0)
                    return nodeExtracted;
                else
                    queue.addAll(nodeExtracted.getChildren());
            }
        }
        return null;
    }

    public void removeOneFile(String UFID) {
        int pos = findFilePos(UFID);
        if (pos != -1) {
            files.remove(pos);
        }
    }

    public void removeParent() {
        if (!isRoot()) {
            parent = null;
        }
    }

    public boolean isRoot() {
        return (parent == null);
    }

    public boolean hasChild() {
        return children != null && children.size() != 0;
    }

    public boolean hasChild(String UFID) {
        boolean result = false;
        if (hasChild()) {
            for (FSTreeNode node : children) {
                if (node.getUFID().compareTo(UFID) == 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    public FileWrapper getFile(String UFID) {
        FileWrapper fileFound = null;

        for (FileWrapper fw : files) {
            if (fw.getUFID().compareTo(UFID) == 0) {
                fileFound = fw;
            }
        }

        return fileFound;
    }

    public String getFileName(String UFID) {
        String name = null;

        for (FileWrapper fw : files) {
            if ((fw.getUFID()).compareTo(UFID) == 0) {
                name = fw.getUFID();
            }
        }

        return name;
    }

    public ArrayList<FileWrapper> getAllFilesWhenDeleteDirectory() {
        ArrayList<FileWrapper> filesUFID = new ArrayList<>();
        if (!files.isEmpty()) {
            filesUFID.addAll(files);
        }
        Queue<FSTreeNode> queue = new LinkedList<>();
        if (!children.isEmpty()) {
            queue.addAll(children);
        }

        while (!queue.isEmpty()) {
            FSTreeNode nodeExtracted = queue.poll();
            if (nodeExtracted != null) {
                if (!nodeExtracted.getFiles().isEmpty()) {
                    filesUFID.addAll(nodeExtracted.getFiles());
                } else
                    queue.addAll(nodeExtracted.getChildren());
            }
        }
        return filesUFID;
    }

    public String getPath() {

        return "/" + getPathWithoutRoot();
    }

    public String getPathWithoutRoot() {
        String path = "";
        if (!isRoot()) {
            FSTreeNode node = parent;
            while (!node.isRoot()) {
                path = node.getNameNode() + "/" + path;
                node = node.getParent();
            }
            path = path + nameNode;
        }
        return path;
    }

    public FSTreeNode findRoot() {
        FSTreeNode root;
        if (isRoot()) {
            root = this;
        } else {
            FSTreeNode node = parent;
            while (!node.isRoot()) {
                node = node.getParent();
            }
            root = node;
        }

        return root;
    }

    public String getFolderSize() {

        long size = 0;

        ArrayList<FileWrapper> fileWrappers = new ArrayList<>();
        Queue<FSTreeNode> queue = new LinkedList<>();

        queue.add(this);

        while (!queue.isEmpty()) {
            FSTreeNode node = queue.poll();

            if (node != null) {
                queue.addAll(node.getChildren());
                fileWrappers.addAll(node.getFiles());
            }
        }

        for (FileWrapper wrapper : fileWrappers) {
            size += wrapper.getAttribute().getFileLength();
        }

        return String.valueOf(size);

    }

    public String printTree() {

        StringBuilder sb = new StringBuilder();
        Queue<FSTreeNode> queue = new LinkedList<>();
        queue.add(this);

        while (!queue.isEmpty()) {

            FSTreeNode node = queue.remove();
            if (node.getParent() == null) {
                //the node is the root
                sb.append("##### Root:\n\tParent: none\n\t");

            } else {
                //the node is not the root
                sb.append("##### ").append(node.getNameNode()).append(":\n\tParent: ").append(node.getParent().getNameNode()).append("\n\t");
            }
            sb.append("UFID: ").append(node.getUFID());

            if (node.getChildren() != null && !node.getChildren().isEmpty()) {

                sb.append("\n\tChildren: ");
                for (FSTreeNode child : node.getChildren()) {
                    sb.append("\n\t\t***").append(child.getNameNode())
                            .append("\n\t\tUFID: ").append(node.getUFID());
                    queue.add(child);
                }
            }

            if (node.getFiles() != null && !node.getFiles().isEmpty()) {
                sb.append("\n\tFiles:");
                for (FileWrapper file : node.getFiles()) {
                    sb.append("\n\t\t***").append(file.getFileName())
                            .append("\n\t\tUFID: ").append(file.getUFID());
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return nameNode;
    }
}
