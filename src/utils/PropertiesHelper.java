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
        propFile = new File("config.properties");
        props = new Properties();
    }

    /**
     * Load a value from the configuration file
     *
     * @param key   The key to load
     * @return      The value loaded
     */
    public String loadConfig(String key) {
        try {
            InputStream inputStream = new FileInputStream(propFile);
            props.load(inputStream);
            inputStream.close();
            return props.getProperty(key);
        } catch (IOException e) {
            System.out.println("Config not found");
            return "";
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
            System.out.println("Configuration saved");
        } catch (IOException e) {
            System.out.println("Config not Saved");
        }
    }

}
