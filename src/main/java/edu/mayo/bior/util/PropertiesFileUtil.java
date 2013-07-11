package edu.mayo.bior.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;


public class PropertiesFileUtil {
	private static Logger sLogger = Logger.getLogger(PropertiesFileUtil.class); 

	private String filename = null;
	private Properties prop = null;

	public PropertiesFileUtil(String filename) throws IOException {
		this.filename = filename;
		this.ProcessPropertiesFile();
	}
	
	public void ProcessPropertiesFile() throws IOException {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(this.filename);
            prop = new Properties();
            prop.load(inStream);
        } catch (IOException ex) {
            sLogger.error("Error loading properties file: " + filename, ex);
            throw ex;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException ex) {
                sLogger.error("Error closing properties file: " + filename, ex);
                throw ex;
            }
        }
    }
	
    public String get(String key){
        return prop.getProperty(key);
    }

}
