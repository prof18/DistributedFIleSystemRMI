package fileSystem;

import java.util.ArrayList;

public class Folder<T> {
    private String UUID;
    private String nameNode = "";
    private ArrayList<DirectoryTree<T>> children = null;
    private DirectoryTree<T> parent = null;
    private T data = null;


    public Folder(String UUID, String nameNode, ArrayList<DirectoryTree<T>> children, DirectoryTree parent, T data){
        this.UUID = UUID;
        this.nameNode = nameNode;
        this.children = children;
        this.parent = parent;
        this.data = data;
    }

    public void createDir(FsOperation fsOP){
        if(parent == null){
            DirectoryTree root = new DirectoryTree(UUID, nameNode, data);
            root.addChildList(children);
            fsOP.setRoot(root);
        }else{
            DirectoryTree node = new DirectoryTree(UUID, nameNode, data);
            node.setParent(parent);
            node.addChildList(children);
            fsOP.createDirectory(node, nameNode);
        }
    }
}
