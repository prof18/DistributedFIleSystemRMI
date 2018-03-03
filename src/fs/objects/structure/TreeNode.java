package fs.objects.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TreeNode {


    private String UFID;
    private String nameNode;
    private ArrayList<TreeNode> childrens;
    private TreeNode parent;
    private ArrayList<FileWrapper> files;

    public TreeNode(String UFID, String nameNode, ArrayList<TreeNode> childrens, TreeNode parent, ArrayList<FileWrapper> files) {
        this.UFID = UFID;
        this.nameNode = nameNode;
        this.childrens = childrens;
        this.parent = parent;
        this.files = files;
    }

    public TreeNode() {

    }

    public String getUFID() {
        return UFID;
    }

    public String getNameNode() {
        return nameNode;
    }

    public ArrayList<TreeNode> getChildrens() {
        return childrens;
    }

    public TreeNode getParent() {
        return parent;
    }

    public ArrayList<FileWrapper> getFiles() {
        return files;
    }

    public void setUFID(String UFID) {
        this.UFID = UFID;
    }

    public void setNameNode(String nameNode) {
        this.nameNode = nameNode;
    }

    public void setChildrens(ArrayList<TreeNode> childrens) {
        this.childrens = childrens;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public void setFiles(ArrayList<FileWrapper> files) {
        this.files = files;
    }

    public void addChild(TreeNode child){
        childrens.add(child);
    }

    public void addFile(FileWrapper file){
        this.files.add(file);
    }

    public FileWrapper findFile(String fileName){
        FileWrapper file = null;
        for(FileWrapper fw: this.files){
            if(fw.getFileName().compareTo(fileName) == 0){
                file = fw;
                break;
            }
        }

        return file;
    }

    private int findFilePos(String fileName){
        int pos = 0;
        for(FileWrapper fw: this.files){
            if(!(fw.getFileName().compareTo(fileName) == 0)) {
                pos++;
                break;
            }
        }

        return pos;
    }

    public void removeOneFile(String fileName){
        int pos = findFilePos(fileName);
        files.remove(pos);
    }

    public void removeParent(){
        this.parent = null;
    }

    public boolean isRoot(){
        return (this.parent == null);
    }

    public boolean hasChild(){
        if(childrens == null || childrens.size() == 0){
            return false;
        }
        return true;
    }

    public boolean hasChild(TreeNode node){
        return node.hasChild();
    }

    public boolean hasChild(String nodeName){
        if(hasChild()){
            for(TreeNode node: childrens){
                if(node.getNameNode().compareTo(nodeName) == 0){
                    return true;
                }
            }
        }

        return false;
    }

    public TreeNode getChild(String nodeName){
        TreeNode findNode = null;
        if (hasChild()){
            for(TreeNode node: childrens){
                if(node.hasChild(nodeName)){
                    findNode = node;
                    break;
                }
            }
        }

        return findNode;
    }

    public String getPath(){
        String path = "/";
        if(!isRoot()){
            TreeNode node = parent;
            while(!node.isRoot()){
                path = "/" + node.getNameNode() + path;
                node = node.getParent();
            }
            path = path + nameNode;
        }
        return path;
    }

    public TreeNode findRoot(){
        TreeNode root = null;
        if (isRoot()){
            System.out.println("E' la radice");
        }else{
            TreeNode node = parent;
            while(!node.isRoot()){
                root = node.getParent();
            }
        }

        return root;
    }

    public String printAll(){
        String stringTree = "";
        ArrayList<String> tree = T_BFS(findRoot());

        for(String n: tree){
            stringTree = stringTree + " " + n;
        }
        return stringTree;
    }

    public ArrayList<String> T_BFS(TreeNode root){
        ArrayList<String> tree = new ArrayList<>();
        Queue<TreeNode> tQueue = new LinkedList<>();
        if (root == null){
            tree = null;
        } else {
            tQueue.add(root);
            while(!tQueue.isEmpty()){
                TreeNode node = tQueue.remove();
                tree.add(node.getNameNode());

                if(node.hasChild()){
                    for(TreeNode n: node.getChildrens()){
                        tQueue.add(n);
                    }
                }
            }
        }

        return tree;
    }

    public String printTree() {

        StringBuilder sb = new StringBuilder();
        Queue<TreeNode> queue = new LinkedList<>();
        queue.add(this);

        while(!queue.isEmpty()) {

            TreeNode node = queue.remove();
            if (node.getParent() == null) {
                //the node is the root
                sb.append("##### Root:\n\tParent: none\n\t");

            } else {
                //the node is not the root
                sb.append("##### ").append(node.getNameNode()).append(":\n\tParent: ").append(node.getParent().getNameNode()).append("\n\t");
            }
            sb.append("UFID: ").append(node.getUFID());

            if (node.getChildrens() != null || !node.getFiles().isEmpty()) {

                sb.append("\n\tChildren: ");
                for (TreeNode child : node.getChildrens()) {
                    sb.append("\n\t\t***").append(child.getNameNode())
                            .append("\n\t\tUFID: ").append(node.getUFID());
                    queue.add(child);
                }
            }

            if (node.getFiles() != null || !node.getFiles().isEmpty()) {
                sb.append("\n\tFiles:");
                for (FileWrapper file: node.getFiles()) {
                    sb.append("\n\t\t***").append(file.getFileName())
                            .append("\n\t\tUFID: ").append(file.getUFID());
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
