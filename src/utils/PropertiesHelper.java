package utils;

import java.io.*;
import java.util.Properties;

/**
 * An helper to handle the Properties
 */
public class PropertiesHelper {

    private static PropertiesHelper INSTANCE = null;
    private static File propFile;
    private static Properties props;

    public static PropertiesHelper getInstance() {
        if (INSTANCE == null)
            INSTANCE = new PropertiesHelper();
        return INSTANCE;
    }

    private PropertiesHelper() {
        props = new Properties();
    }

    /**
     * Load the Properties file from a specific path
     *
     * @param path The path of the properties file
     */
    public static void setPropFile(String path) {
        PropertiesHelper.propFile = new File(path);
        try {
            InputStream inputStream = new FileInputStream(propFile);
            props.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            System.out.println("[SET-PROP-FILE] Configuration File Not Found");
        }
    }

    /**
     * Load a value from the configuration file
     *
     * @param key The key to load
     * @return The value loaded
     */
    public String loadConfig(String key) {
        try {
            InputStream inputStream = new FileInputStream(propFile);
            props.load(inputStream);
            inputStream.close();
            return props.getProperty(key);
        } catch (IOException e) {
            System.out.println("[LOAD-CONFIG] Config not found");
            return null;
        }
    }

    /**
     * Save a value to the configuration file
     *
     * @param key   The key to save
     * @param value The value to save
     */
    public void writeConfig(String key, String value) {
        try {
            props.setProperty(key, value);
            OutputStream outputStream = new FileOutputStream(propFile);
            props.store(outputStream, "DFS settings");
            outputStream.close();
        } catch (IOException e) {
            System.out.println("[WRITE-CONFIG]Configuration not Saved");
        } catch (NullPointerException f) {
            System.out.println("[WRITE-CONFIG] NPE during writing config");
        }
    }

}
