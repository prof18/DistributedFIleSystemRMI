package utils;

import com.google.gson.Gson;
import fileSystem.json.JsonFolder;

import java.util.ArrayList;

public class GSONHelper {

    private static GSONHelper INSTANCE = null;
    private Gson gson;

    public GSONHelper getInstance(){
        if (INSTANCE == null)
            INSTANCE = new GSONHelper();
        return INSTANCE;
    }

    private GSONHelper() {
        gson = new Gson();
    }

    public String folderToJson(ArrayList<JsonFolder> folders) {
        return gson.toJson(folders);
    }

}
