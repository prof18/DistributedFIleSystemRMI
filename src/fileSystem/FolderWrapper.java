package fileSystem;

import java.util.ArrayList;
import java.util.List;

public class FolderWrapper {
    public List<Folder> folderList = new ArrayList<>();

    @Override
    public String toString(){
        return "FolderWrapper: " + folderList;
    }

}
