package fs.objects.structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class FSTreeNode implements Serializable {


    private String UFID;
    private String nameNode;
    private ArrayList<FSTreeNode> childrens;
    private FSTreeNode parent;
    private ArrayList<FileWrapper> files;
    private long lastEditTime;
    private String gsonTree;


    public FSTreeNode(String UFID, String nameNode, ArrayList<FSTreeNode> childrens, FSTreeNode parent, ArrayList<FileWrapper> files) {
        this.UFID = UFID;
        this.nameNode = nameNode;
        this.childrens = childrens;
        this.parent = parent;
        this.files = files;
    }

    public FSTreeNode() {

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
        return childrens;
    }

    public FSTreeNode getParent() {
        return parent;
    }

    public ArrayList<FileWrapper> getFiles() {
        return files;
    }

    public String getGson() {
        return gsonTree;
    }

    public void setGson(String gsonTree) {
        this.gsonTree = gsonTree;
    }

    public void setUFID(String UFID) {
        this.UFID = UFID;
    }

    public void setNameNode(String nameNode) {
        this.nameNode = nameNode;
    }

    public void setChildrens(ArrayList<FSTreeNode> children) {
        this.childrens = children;
    }

    public void setParent(FSTreeNode parent) {
        this.parent = parent;
    }

    public void setFiles(ArrayList<FileWrapper> files) {
        this.files = files;
    }

    public void addChild(FSTreeNode child) {
        childrens.add(child);
    }

    public void addFile(FileWrapper file) {
        this.files.add(file);
    }

   /* public FileWrapper findFile(String fileName){
        FileWrapper file = null;
        for(FileWrapper fw: this.files){
            if(fw.getFileName().compareTo(fileName) == 0){
                file = fw;
                break;
            }
        }

        return file;
    }*/

    private int findFilePos(String fileName) {
        int pos = 0;
        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getFileName().compareTo(fileName) == 0) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    public FSTreeNode findNode(FSTreeNode node, String nodeName) {
        if (node.getNameNode().compareTo(nodeName) == 0) {
            return node;
        } else {
            for (FSTreeNode child: node.getChildren()) {
                FSTreeNode result = findNode(child, nodeName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public void removeOneFile(String fileName) {

        int pos = findFilePos(fileName);
        files.remove(pos);
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
        return childrens != null && childrens.size() != 0;
    }

    public boolean hasChild(FSTreeNode node) {
        return node.hasChild();
    }

    public boolean hasChild(String nodeName) {
        if (hasChild()) {
            for (FSTreeNode node : childrens) {
                if (node.getNameNode().compareTo(nodeName) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public FSTreeNode getChild(String nodeName) {
        FSTreeNode findNode = null;
        if (hasChild()) {
            for (FSTreeNode node : childrens) {
                if (node.hasChild(nodeName)) {
                    findNode = node;
                    break;
                }
            }
        }

        return findNode;
    }

    public boolean hasFile(String fileName) {


        for (FileWrapper fw : files) {
            if (fw.getFileName().compareTo(fileName) == 0) {
                return true;
            }
        }


        return false;
    }

    public FileWrapper getFile(String fileName) {
        FileWrapper fileFound = null;
        if (hasFile(fileName)) {
            for (FileWrapper fw : files) {
                if (fw.getFileName().compareTo(fileName) == 0) {
                    fileFound = fw;
                }
            }
        }
        return fileFound;
    }

    public String getFileName(String UFID) {
        FileWrapper fileFound;
        if (hasFile(UFID)) {
            for (FileWrapper fw : files) {
                if (fw.getUFID().compareTo(UFID) == 0) {
                    fileFound = fw;
                    return fileFound.getFileName();
                }
            }
        }
        return "";
    }

    public String getPath() {

        return "/" + getPathWithoutRoot();
    }

    public String getPathWithoutRoot() {
        String path = "";
        if (!isRoot()) {
            FSTreeNode node = parent;
            while (!node.isRoot()) {
                path = "/" + node.getNameNode() + path;
                node = node.getParent();
            }
            path = path + nameNode;
        }
        return path;
    }

    public FSTreeNode findRoot() {
        FSTreeNode root = null;
        if (isRoot()) {
            root = this;
            System.out.println("E' la radice");
        } else {
            FSTreeNode node = parent;
            while (!node.isRoot()) {
                root = node.getParent();
            }
        }

        return root;
    }

    public String printAll() {
        String stringTree = "";
        ArrayList<String> tree = T_BFS(findRoot());

        for (String n : tree) {
            stringTree = stringTree + " " + n;
        }
        return stringTree;
    }

    public ArrayList<String> T_BFS(FSTreeNode root) {
        ArrayList<String> tree = new ArrayList<>();
        Queue<FSTreeNode> nodeQueue = new LinkedList<>();
        if (root == null) {
            tree = null;
        } else {
            nodeQueue.add(root);
            while (!nodeQueue.isEmpty()) {
                FSTreeNode node = nodeQueue.remove();
                tree.add(node.getNameNode());

                if (node.hasChild()) {
                    for (FSTreeNode n : node.getChildren()) {
                        nodeQueue.add(n);
                    }
                }
            }
        }

        return tree;
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
