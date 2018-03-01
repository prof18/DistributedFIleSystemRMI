package fileSystem.json;

import java.util.ArrayList;

public class JsonFolder {

   private String UFID;
   private boolean isRoot;
   private String folderName;
   private String parentUFID;
   private ArrayList<String> childrens;
   private ArrayList<JsonFile> files;

   public JsonFolder() {
   }

   public JsonFolder(String UFID, boolean isRoot, String folderName, String parentUFID, ArrayList<String> childrens, ArrayList<JsonFile> files) {
      this.UFID = UFID;
      this.isRoot = isRoot;
      this.folderName = folderName;
      this.parentUFID = parentUFID;
      this.childrens = childrens;
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

   public ArrayList<String> getChildrens() {
      return childrens;
   }

   public void setChildrens(ArrayList<String> childrens) {
      this.childrens = childrens;
   }

   public ArrayList<JsonFile> getFiles() {
      return files;
   }

   public void setFiles(ArrayList<JsonFile> files) {
      this.files = files;
   }
}
