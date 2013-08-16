package edu.mayo.bior.cli.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
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

import edu.mayo.bior.util.ClasspathUtil;
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

		try {
			execNoCmd(catalogBgzipPath, catalogOption);
		} catch (URISyntaxException e) {
			throw new IOException("Could not find the columns defaults properties file");
		}
	}

	public void execNoCmd(String catalogBgzipPath, Option catalogOption)
			throws InvalidOptionArgValueException, InvalidDataException,
			IOException, URISyntaxException
	{
		File catalogFile = new File(catalogBgzipPath);
		
		if( catalogFile.exists() ) {
			String catalogFullFilename = catalogFile.getName();
			String catalogFilenamePrefix = catalogFullFilename.replace(".tsv.bgz", "");
			String catalogFilePath = catalogFile.getParent();
			
			// Create the datasource.props file
			createDatasourcePropsFile(catalogOption, catalogFile, catalogFilenamePrefix, catalogFilePath);
			
			// Create the columns.props file
			createColumnPropsFile(catalogOption, catalogFile, catalogFilenamePrefix, catalogFilePath);
			
			System.out.println("Done.");
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
	 * @param catalogFilenamePrefix
	 * @param catalogFilePath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	protected void createDatasourcePropsFile(Option catalogOption, File catalogFile, String catalogFilenamePrefix, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		//Check to see if "catalogFilePath" is WRITABLE
		File dir = new File(catalogFilePath);
		if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
			throw new InvalidOptionArgValueException(
				catalogOption,				
				catalogFile.getCanonicalPath(),
				"Source file directory doesn't exist or is not writable: " + catalogFile.getCanonicalPath()					
			);
	    }
		
		File datasourcePropsFile = new File(catalogFilePath + File.separator + catalogFilenamePrefix + ".datasource" + ".properties");
		
		if (datasourcePropsFile.exists()) {
			throw new InvalidOptionArgValueException(
				catalogOption,				
				catalogFile.getCanonicalPath(),
				"Datasource properties file already exists for this catalog - " + catalogFile.getCanonicalPath()
			);
		}
	    System.out.println("Writing datasource properties file: " + datasourcePropsFile.getCanonicalPath());

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

		String content = buildContent(catalogFilenamePrefix, "Datasource", dsAttribs, null);
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
	 * @throws URISyntaxException 
	 */
	protected void createColumnPropsFile(Option catalogOption,File catalogFile, String catalogFilename, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException, URISyntaxException {
	    File columnPropsFile = new File(catalogFilePath + File.separator + catalogFilename+".columns"+".properties");
	    if (columnPropsFile.exists()) {
			throw new InvalidOptionArgValueException(
				catalogOption,
				catalogFilePath,
				"Column properties file already exists for this catalog - " + catalogFilePath
			);
		}
	    System.out.println("Writing columns    properties file: " + columnPropsFile.getCanonicalPath());
	    final long ONE_MB = 1 * 1024 * 1024;
	    final long CATALOG_SIZE_MB = catalogFile.length() / ONE_MB;
	    if( CATALOG_SIZE_MB > 10 )
	    	System.out.println("  WARNING: Catalog is large (" + CATALOG_SIZE_MB + "MB).  This may take a while to process)...");
	    
	    // Get all JSON keys from the catalog
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
	    File colDefaultsProps = ClasspathUtil.loadResource("/allCatalogs.columns.properties");
	    Properties defaults = new PropertiesFileUtil(colDefaultsProps.getCanonicalPath()).getProperties();
		String content = buildContent(catalogFilename, "Column", allJsonKeys, defaults);
	    columnPropsFile.createNewFile();
		this.writeToFile(columnPropsFile, content);			
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
	    	// Key may be several layers deep (such as INFO.OBJ.SOMEKEY), so build the full JSON path
	    	String key = (parentJsonPath == null || parentJsonPath.length() == 0  ?  ""  :  parentJsonPath + ".") + entry.getKey();
            JsonElement value = entry.getValue();
	    	
            // If it is a complex object, then break it down further
            if( value instanceof JsonObject )
            	jsonKeys.addAll(getJsonKeys(value.getAsJsonObject().toString(), key));
            else // should be a primitive or an array
	    		jsonKeys.add(key);
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
		sb.append(". Please fill in the description to the keys below.\n");
		
		for(String key : listValues) {
			String desc = defaultDescriptions == null  ?  null  :  defaultDescriptions.getProperty(key);
			sb.append(key + "=" +  (desc != null ? desc : "") + "\n");
		}
		
		return sb.toString();
	}
}