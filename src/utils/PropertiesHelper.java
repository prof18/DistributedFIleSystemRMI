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
     * Load the Proprieties file from a specific path
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
            System.out.println("Configuration File Not Found");
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
            System.out.println("Config not found");
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

            if (key.equals(Constants.FOLDERS_CONFIG)) {
                System.out.println("Writing JSON");
                System.out.println("-------------");
                System.out.println("Json:");
                System.out.println(value);
                Util.printStackTrace();
            }

            System.out.println("PropertiesHelper.writeConfig key: " + key);
            System.out.println("PropertiesHelper.writeConfig value: " + value);
            props.setProperty(key, value);
            OutputStream outputStream = new FileOutputStream(propFile);
            props.store(outputStream, "DFS settings");
            outputStream.close();
            System.out.println("Configuration saved");
        } catch (IOException e) {
            System.out.println("Configuration not Saved");
        } catch (NullPointerException f) {
            System.out.println("NPE during writing config");
        }
    }

}
