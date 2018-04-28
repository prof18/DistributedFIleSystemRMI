package utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fs.objects.json.JsonFolder;

import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * A helper class to read JSON
 */
public class GSONHelper {

    private static GSONHelper INSTANCE = null;
    private Gson gson;

    public static GSONHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new GSONHelper();
        return INSTANCE;
    }

    private GSONHelper() {
        gson = new Gson();
    }

    /**
     * Generates a JSON string
     *
     * @param folders   The object to convert to JSON
     * @return          A JSON String
     */
    public String foldersToJson(HashMap<String, JsonFolder> folders) {
        return gson.toJson(folders);
    }

    /**
     * Convert an OBJECT to a JSON String
     *
     * @param json  The JSON string to convert
     * @return      The Object converted to JSON
     */
    public HashMap<String, JsonFolder> jsonToFolders(String json) {
        Type folderMapType = new TypeToken<HashMap<String, JsonFolder>>() {
        }.getType();
        return gson.fromJson(json, folderMapType);
    }


}
