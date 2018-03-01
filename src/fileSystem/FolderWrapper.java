package fileSystem;

import fileSystem.json.JsonFolder;

import java.util.ArrayList;
import java.util.List;

public class FolderWrapper {
    public List<JsonFolder> jsonFIleList = new ArrayList<>();

    @Override
    public String toString(){
        return "FolderWrapper: " + jsonFIleList;
    }

}
