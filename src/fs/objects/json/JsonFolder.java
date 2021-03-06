package fs.objects.json;

import java.util.ArrayList;

/**
 * This object represent a folder in the JSON
 */
public class JsonFolder {

    private String UFID;
    private long lastEditTime;
    private boolean isRoot;
    private String folderName;
    private String parentUFID;
    private ArrayList<String> children;
    private ArrayList<JsonFile> files;
    private String owner;

    public JsonFolder() {
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUFID() {
        return UFID;
    }

    public void setUFID(String UFID) {
        this.UFID = UFID;
    }

    public long getLastEditTime() {
        return lastEditTime;
    }

    public void setLastEditTime(long lastEditTime) {
        this.lastEditTime = lastEditTime;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setRoot(boolean root) {
        isRoot = root;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getParentUFID() {
        return parentUFID;
    }

    public void setParentUFID(String parentUFID) {
        this.parentUFID = parentUFID;
    }

    public ArrayList<String> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<String> children) {
        this.children = children;
    }

    public ArrayList<JsonFile> getFiles() {
        return files;
    }

    public void setFiles(ArrayList<JsonFile> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "JsonFolder{" +
                "UFID='" + UFID + '\'' +
                ", isRoot=" + isRoot +
                ", folderName='" + folderName + '\'' +
                ", parentUFID='" + parentUFID + '\'' +
                ", children=" + children +
                ", files=" + files +
                '}';
    }
}
