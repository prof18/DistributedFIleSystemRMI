package fs.objects.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class TreeNode {

    private String UFID;
    private String nameNode;
    private ArrayList<TreeNode> childrens;
    private TreeNode parent;
    private ArrayList<FileWrapper> file;

    public TreeNode(String UFID, String nameNode, ArrayList<TreeNode> childrens, TreeNode parent, ArrayList<FileWrapper> file) {
        this.UFID = UFID;
        this.nameNode = nameNode;
        this.childrens = childrens;
        this.parent = parent;
        this.file = file;
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

    public ArrayList<FileWrapper> getFile() {
        return file;
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

    public void setFile(ArrayList<FileWrapper> file) {
        this.file = file;
    }

    public void addChild(TreeNode child){
        childrens.add(child);
    }

    public void addFile(FileWrapper file){
        this.file.add(file);
    }

    public FileWrapper findFile(String fileName){
        FileWrapper file = null;
        for(FileWrapper fw: this.file){
            if(fw.getFileName().compareTo(fileName) == 0){
                file = fw;
                break;
            }
        }

        return file;
    }

    private int findFilePos(String fileName){
        int pos = 0;
        for(FileWrapper fw: this.file){
            if(!(fw.getFileName().compareTo(fileName) == 0)) {
                pos++;
                break;
            }
        }

        return pos;
    }

    public void removeOneFile(String fileName){
        int pos = findFilePos(fileName);
        file.remove(pos);
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

}
