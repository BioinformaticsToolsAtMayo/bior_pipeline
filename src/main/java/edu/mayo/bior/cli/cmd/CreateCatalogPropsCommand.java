package edu.mayo.bior.cli.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.UNIX.CatGZPipe;
import edu.mayo.pipes.util.PropertiesFileUtil;
import edu.mayo.pipes.util.metadata.AddMetadataLines.BiorMetaControlledVocabulary;

/** Given a catalog path, construct the metadata files associated with that catalog:
 *  <catalog>.datasource.properties
 *  <catalog>.columns.properties
 *  
 * @author Surendra Konathala, Michael Meiners (m054457)
 * Date created: Aug 1, 2013
 */
public class CreateCatalogPropsCommand implements CommandPlugin {

	// Catalog path
	public static final char OPTION_CATALOG = 'd';
	
	//BIOR vocabulary is defined here
	private List<BiorMetaControlledVocabulary> biorMetaControlledVocabulary = Arrays.asList(BiorMetaControlledVocabulary.values());
			
	public void init(Properties props) throws Exception {
	}
	
	public void execute(CommandLine line, Options opts) throws InvalidOptionArgValueException, InvalidDataException, IOException {	
		// Catalog path and key are required		
		String catalogBgzipPath = line.getOptionValue(OPTION_CATALOG);
		Option catalogOption = opts.getOption(OPTION_CATALOG + "");

		execNoCmd(catalogBgzipPath, catalogOption);
	}

	public void execNoCmd(String catalogBgzipPath, Option catalogOption)
			throws InvalidOptionArgValueException, InvalidDataException,
			IOException
	{
		File catalogFile = new File(catalogBgzipPath);
		
		if( catalogFile.exists() ) {
			String catalogFullFilename = catalogFile.getName();
			String catalogFilename = catalogFullFilename.substring(0, catalogFullFilename.indexOf("."));
			String catalogFilePath = catalogFile.getParent();
			
			// Create the datasource.props file
			createDatasourcePropsFile(catalogOption, catalogFile, catalogFilename, catalogFilePath);
			
			// Create the columns.props file
			createColumnPropsFile(catalogOption, catalogFile, catalogFilename, catalogFilePath);
			
		} else {
			throw new InvalidOptionArgValueException(
				catalogOption,				
				catalogBgzipPath,
				"Catalog file could not be found: " + catalogBgzipPath
			);
		}
	}

	/**
	 * 
	 * @param catalogFile
	 * @param catalogFilename
	 * @param catalogFilePath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	protected void createDatasourcePropsFile(Option catalogOption, File catalogFile, String catalogFilename, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		//Check to see if "catalogFilePath" is WRITABLE
		File dir = new File(catalogFilePath);
		if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
			throw new InvalidOptionArgValueException(
				catalogOption,				
				catalogFile.getCanonicalPath(),
				"Source file directory doesn't exist or is not writable: " + catalogFile.getCanonicalPath()					
			);
	    }
		
		//Create the props file
		File datasourcePropsFile = new File(catalogFilePath + catalogFile.separator + catalogFilename + ".datasource" + ".properties");
		
		if (datasourcePropsFile.exists()) {
			throw new InvalidOptionArgValueException(
				catalogOption,				
				catalogFile.getCanonicalPath(),
				"Datasource properties file already exists for this catalog - " + catalogFile.getCanonicalPath()
			);
		}
		datasourcePropsFile.createNewFile();
		//System.out.println(datasourcePropsFile.exists());
		
		//write all values from the enum to the file
		List<String> dsAttribs = Arrays.asList(
				BiorMetaControlledVocabulary.SHORTNAME.toString(),
				BiorMetaControlledVocabulary.DESCRIPTION.toString(),
				BiorMetaControlledVocabulary.SOURCE.toString(),
				BiorMetaControlledVocabulary.VERSION.toString(),
				BiorMetaControlledVocabulary.BUILD.toString()
				);

		String content = buildContent(catalogFilename, "Datasource", dsAttribs, null);
		this.writeToFile(datasourcePropsFile, content);
		//return true;
	}
	

	/**
	 * 
	 * @param catalogFile
	 * @param catalogFilename
	 * @param catalogFilePath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	protected void createColumnPropsFile(Option catalogOption,File catalogFile, String catalogFilename, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		Pipeline pipe = new Pipeline(new CatGZPipe("gzip"));
		pipe.setStarts(Arrays.asList(catalogFile.getPath()));
		
		List<String> allJsonKeys = new ArrayList<String>();
		while(pipe.hasNext()) {
			String line = (String)pipe.next();
			
			// Skip the line if it is blank, or is a header line (starts with "#")
			if( line.trim().length() == 0  ||  line.startsWith("#"))
				continue;

			String[] aVal = line.split("\t");
			String jsonStr = aVal[3];
			List<String> jsonPaths = getJsonKeys(jsonStr, "");
			// Add all json paths to the full list
			for(String jsonPath : jsonPaths) {
				// If not found in the list yet, add it
				if( ! allJsonKeys.contains(jsonPath) )
					allJsonKeys.add(jsonPath);
			}
		}
		
	    //Create the props file
	    File columnPropsFile = new File(catalogFilePath + catalogFile.separator + catalogFilename+".columns"+".properties");
	    if (columnPropsFile.exists()) {
			throw new InvalidOptionArgValueException(
				catalogOption,
				catalogFilePath,
				"Column properties file already exists for this catalog - " + catalogFilePath
			);
		}	
	    
	    columnPropsFile.createNewFile();
	    Properties defaults = new PropertiesFileUtil("src/main/resources/allCatalogs.columns.properties").getProperties();
		String content = buildContent(catalogFilename, "Column", allJsonKeys, defaults);
		this.writeToFile(columnPropsFile, content);			
		//return true;		
	}
	
	
	/** Given a JSON string, pull out all keys.  Ex: {"CHR":1,"POS":111,"INFO":{"SAO":1.0,"SSR":2.0}}<br>
	 *  Return:  CHR, POS, INFO.SAO, INFO.SSR
	 * @param jsonStr The full JSON string to search
	 * @return A list of all leaf keys in the JSON string 
	 */
	private List<String> getJsonKeys(String jsonStr, String parentJsonPath) {
		List<String> jsonKeys = new ArrayList<String>();
	    JsonObject root = new JsonParser().parse(jsonStr).getAsJsonObject();
	    
	    for (Map.Entry<String,JsonElement> entry : root.entrySet()) {
	    	String key = (parentJsonPath == null || parentJsonPath.length() == 0  ?  ""  :  parentJsonPath + ".") + entry.getKey();
            JsonElement value = entry.getValue();
	    	
            // If it is a complex object, then break it down further
            if(value instanceof JsonPrimitive)
	    		jsonKeys.add(key);
            else
            	jsonKeys.addAll(getJsonKeys(value.getAsJsonObject().toString(), key));
	    }
	    return jsonKeys;
	}

	/**
	 * Write content to a file
	 * @param source
	 * @param content
	 * @throws IOException
	 */
	private void writeToFile(File source, String content) throws IOException {		
		//write all keys in enum to the file
		FileWriter fw = new FileWriter(source.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
	}
	
	/**
	 * 
	 * @param catalogFilename
	 * @param type - Datasource or Columns
	 * @param listValues
	 * @param defaultDescriptions The descriptions (values) to associate with known keys.  This is useful for setting common column properties such as the bior properties "_landmark", "_minBP", etc, or VCF-columns such as "CHROM", or "POS".
	 * @return
	 */
	private String buildContent(String catalogFilename, String type, List<String> listValues, Properties defaultDescriptions) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("# " + type + " properties file for Catalog - ");
		sb.append(catalogFilename);
		sb.append(". Please fill in the description to the below keys.\n");
		
		for(String key : listValues) {
			String desc = defaultDescriptions == null  ?  null  :  defaultDescriptions.getProperty(key);
			sb.append(key + "=" +  (desc != null ? desc : "") + "\n");
		}
		
		return sb.toString();
	}
}