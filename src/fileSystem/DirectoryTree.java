package fileSystem;

import java.util.ArrayList;

public class DirectoryTree<T> {
    private String UUID;
    private String nameNode = "";
    private ArrayList<DirectoryTree<T>> children;// = new ArrayList<DirectoryTree<T>>();
    private DirectoryTree<T> parent = null;
    private T data;

    public DirectoryTree(String UUID, String nameNode, T data){
        this.UUID = UUID;
        this.nameNode = nameNode;
        this.data = data;
    }

    public DirectoryTree(DirectoryTree<T> parent, T data) {
        this.data = data;
        this.parent = parent;
    }

    public ArrayList<DirectoryTree<T>> getChildren() {
        return children;
    }

    public void setParent(DirectoryTree<T> parent) {
        //parent.addChild(this);
        this.parent = parent;
    }

    public DirectoryTree<T> getParent(){
        return this.parent;
    }

    public void addChild(T data, String nameNode) {

        DirectoryTree<T> child = new DirectoryTree(java.util.UUID.randomUUID().toString(), nameNode, data);
        //child.setParent(this);
        if (this.children == null){
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }

    public void addChild(DirectoryTree<T> child) {
        //child.setParent(this);
        if (this.children == null){
            this.children = new ArrayList<>();
        }
        this.children.add(child);
    }
    public void addChildList(ArrayList<DirectoryTree<T>> childList){
        this.children = childList;
    }

    public T getData() {
        return this.data;
    }

    public String getUUID() {
        return UUID;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        if(this.children.size() == 0)
            return true;
        else
            return false;
    }

    public void removeParent() {
        this.parent = null;
    }

    public String getNameNode() {
        return nameNode;
    }

    public void setNameNode(String nameNode) {
        this.nameNode = nameNode;
    }

    public boolean hasChild(DirectoryTree<T> child){

        if (children == null || children.isEmpty())
            return false;

        for (DirectoryTree<T> ch : children){
            if (ch.getNameNode().equals(child.getNameNode())){
                return true;
            }
        }
        return false;
    }
    public boolean hasChild(String name){

        if (children == null || children.isEmpty())
            return false;

        for (DirectoryTree<T> ch : children){
            if (ch.getNameNode().equals(name)){
                return true;
            }
        }
        return false;
    }

    public DirectoryTree<T> getChild(String name){
        if (children == null || children.isEmpty())
            return null;

        for (DirectoryTree<T> ch : children){
            if (ch.getNameNode().equals(name)){
                return ch;
            }
        }
        return null;
    }

    public String getPath(){
        if (isRoot())
            return nameNode;

        return  parent.getPath() + "/" + getNameNode();
    }


    public ArrayList<DirectoryTree<T>> getRouteToNode() {

        if (isRoot()) {
            ArrayList<DirectoryTree<T>> list = new ArrayList<DirectoryTree<T>>();
            list.add(this);
            return list;
        }
        ArrayList list = parent.getRouteToNode();
        list.add(this);
        return list;
    }

    public DirectoryTree<T> getRoot(){
        DirectoryTree<T> tempNode = getParent();
        while(!tempNode.isRoot()){
            tempNode = tempNode.getParent();
        }
        return tempNode;
    }

    /**
     * get node with same path
     */
    public DirectoryTree<T> getNode(String path) {

        for (DirectoryTree<T> ch : children) {
            if (path.startsWith(ch.getNameNode())) {

                String nextPath = path.substring(path.indexOf("/"));
                return ch.getNode(nextPath);
            }
        }

        if (path.startsWith(nameNode)) {
            return this;
        }

        return null;
    }

    public String printAll(){
        StringBuilder str = new StringBuilder("(name= " + nameNode + ") \n");
        if (children != null && !children.isEmpty()){
            str.append("\t{ \t");
            for (DirectoryTree<T> n : children){
                str.append(n.printAll());
            }
            str.append("\t}\t");
        }
        return str.toString();
    }
}