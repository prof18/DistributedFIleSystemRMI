package fs.objects.json;

import java.util.ArrayList;

public class JsonFolder {

   private String UFID;
   private boolean isRoot;
   private String folderName;
   private String parentUFID;
   private ArrayList<String> children;
   private ArrayList<JsonFile> files;

   public JsonFolder() {
   }

   public JsonFolder(String UFID, boolean isRoot, String folderName, String parentUFID, ArrayList<String> children, ArrayList<JsonFile> files) {
      this.UFID = UFID;
      this.isRoot = isRoot;
      this.folderName = folderName;
      this.parentUFID = parentUFID;
      this.children = children;
      this.files = files;
   }

   public String getUFID() {
      return UFID;
   }

   public void setUFID(String UFID) {
      this.UFID = UFID;
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
