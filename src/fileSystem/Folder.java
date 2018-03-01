package fileSystem;

import java.util.ArrayList;

public class Folder {
    private String UUID;
    private String nameNode;
    private ArrayList<String> children;
    private String parent;


    public Folder(String UUID, String nameNode, ArrayList<String>  children, String parent){
        this.UUID = UUID;
        this.nameNode = nameNode;
        this.children = children;
        this.parent = parent;
    }

    /*public void createDir(FsOperation fsOP){
        if(parent == null){
            DirectoryTree root = new DirectoryTree(UUID, nameNode, data);
            root.addChildList(children);
            fsOP.setRoot(root);
        } else {
            DirectoryTree node = new DirectoryTree(UUID, nameNode);
            node.setParent(parent);
            node.addChildList(children);
            fsOP.createDirectory(node, nameNode);
        }
    }*/
}
