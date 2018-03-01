package fileSystem;

import fs.objects.FileWrapper;

import java.util.ArrayList;

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

    }

    public void addFile(FileWrapper file){

    }

    public String printAll(){
      return new String();
    }

}
