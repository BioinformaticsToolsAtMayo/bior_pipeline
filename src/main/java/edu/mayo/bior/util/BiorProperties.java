package edu.mayo.bior.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class is an extension of the java Properties class. The additional
 * functionality mainly deals with finding the properties file. The search
 * order for the files are:
 *   0. uses the value set by the static setFile() method.
 *   1. uses the file defined by the user's BIOR_PROP environment variable.
 *   2. uses the file named sys.properties in the user's home directory.
 *   3. uses the file defined by the java System property BIOR_PROP.
 *   4. uses /etc/sys.properties.
 *   5. uses JVM Classloader to locate sys.properties on the classpath
 * @author Daniel J. Quest, Michael Meiners
 */
public class BiorProperties {

	public enum Key { SnpEffJar, SnpEffConfig };
	
    private static final String BIOR_PROP = "BIOR_PROP";
    private static String file = null;
    private Properties prop = null;

    private static Logger sLogger = Logger.getLogger(BiorProperties.class); 
    
    /**
     * We are going to run this code once, the first time the class is
     * referenced.
     */
    static {
    	final String DEFAULT_PROP_FILE_NAME = "bior.properties";
    	
        if (System.getenv(BIOR_PROP) != null) {
            file = System.getenv(BIOR_PROP);
        } else if (new File(System.getenv("HOME") + "/" + DEFAULT_PROP_FILE_NAME).exists() == true) {
            file = System.getenv("HOME") + "/" + DEFAULT_PROP_FILE_NAME;
        } else if (System.getProperty(BIOR_PROP) != null) {
            file = System.getProperty(BIOR_PROP);
        } else if (new File(System.getenv("user.dir") + "/" + DEFAULT_PROP_FILE_NAME).exists() == true) {
            file = System.getProperty("user.dir") + "/conf/" + DEFAULT_PROP_FILE_NAME;
        } else if (BiorProperties.class.getClassLoader().getResource(DEFAULT_PROP_FILE_NAME) != null) {
        	// use classloader to find sys.properties on classpath
        	URL url = BiorProperties.class.getClassLoader().getResource(DEFAULT_PROP_FILE_NAME);
        	file = url.getFile();
        }

        sLogger.info("using " + file + " for " + DEFAULT_PROP_FILE_NAME);
    }

    /**
     * Allows the name of the file to be changed at any time. However, the change
     * won't take effect until the constructor is called. Once the constructor
     * is called, the properties change to reflect the new file. All subsequent
     * calls to the constructor will use the new file. All previous instances
     * will still hold the old properties, but the new file name (UGH!).
     * @param f The path and name of the properties file.
     */
    public static void setFile(String f) {
        file = f;
    }

    public BiorProperties() throws IOException {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            prop = new Properties();
            prop.load(inStream);
        } catch (IOException ex) {
            sLogger.error("Error loading properties file: " + file, ex);
            throw ex;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ex) {
                sLogger.error("Error closing properties file: " + file, ex);
                throw ex;
            }
        }
    }

    /**
     * Gets the value of the property. Returns null if the property (key) is
     * not found in the properties file.
     * @param key The name of the property.
     * @return value The value of the property.
     */
    public String get(Key key) {
        return prop.getProperty(key.toString());
    }
    
    public String get(String key){
        return prop.getProperty(key);
    }
    
    public Set<String> keySet(){
        return this.prop.stringPropertyNames();
    }
}