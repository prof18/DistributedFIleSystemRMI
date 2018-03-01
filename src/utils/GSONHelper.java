package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fileSystem.json.JsonFolder;

import java.lang.reflect.Type;
import java.util.HashMap;

public class GSONHelper {

    private static GSONHelper INSTANCE = null;
    private Gson gson;

    public static GSONHelper getInstance(){
        if (INSTANCE == null)
            INSTANCE = new GSONHelper();
        return INSTANCE;
    }

    private GSONHelper() {
        gson = new Gson();
    }

    public String foldersToJson(HashMap<String, JsonFolder> folders) {
        return gson.toJson(folders);
    }

    public HashMap<String, JsonFolder> jsonToFolders(String json) {
        Type folderMapType = new TypeToken<HashMap<String, JsonFolder>>(){}.getType();
        return gson.fromJson(json, folderMapType);
    }


}
