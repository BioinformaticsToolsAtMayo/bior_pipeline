package edu.mayo.bior.cli.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.pipeline.createCatalogProps.ColumnMetaFromCatalogCrawling;
import edu.mayo.bior.pipeline.createCatalogProps.ColumnMetaFromVcf;
import edu.mayo.bior.util.ClasspathUtil;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.util.metadata.AddMetadataLines;
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
	
	// Catalog's original vcf source file (optional)
	public static final char OPTION_ORIGINAL_VCF = 'v';
	
	// Target directory (optional) - by default it is the same as the catalog
	public static final char OPTION_TARGET_DIR = 't';
	
	//BIOR vocabulary is defined here
	private List<BiorMetaControlledVocabulary> biorMetaControlledVocabulary = Arrays.asList(BiorMetaControlledVocabulary.values());
			
	private final String COLUMNS_EXTENSION 		= ".columns.tsv";
	private final String DATASOURCE_EXTENSION 	= ".datasource.properties";
	
	
	public void init(Properties props) throws Exception {
	}
	
	public void execute(CommandLine line, Options opts) throws InvalidOptionArgValueException, InvalidDataException, IOException {	
		// Catalog path and key are required		
		String catalogBgzipPath = line.getOptionValue(OPTION_CATALOG);
		// Original VCF file that the catalog was constructed from (optional, and may not apply)
		// null if not applicable
		boolean isVcfSpecified = line.hasOption(OPTION_ORIGINAL_VCF);
		String catalogOriginalVcfPath = line.getOptionValue(OPTION_ORIGINAL_VCF);
		
		String targetDir = line.getOptionValue(OPTION_TARGET_DIR);
		
		try {
			execNoCmd(catalogBgzipPath, catalogOriginalVcfPath, targetDir, isVcfSpecified);
		} catch( IOException ioe) {
			throw new InvalidDataException("Error accessing one of the files: " + ioe.getMessage());
		} catch (URISyntaxException e) {
			throw new InvalidDataException("Could not find the columns defaults properties file: " + e.getMessage());
		}
	}

	public void execNoCmd(String catalogBgzipPath, String catalogOriginalVcfPath, String targetDirPath, boolean isVcfSpecified)
			throws InvalidOptionArgValueException, InvalidDataException,
			IOException, URISyntaxException
	{
		File catalogFile = new File(catalogBgzipPath);
		
		// Throw exception if catalog does not exist
		if( ! catalogFile.exists() )
			throw new IOException("Catalog file could not be found: " + catalogBgzipPath);

		// If the target dir is not specified, then use the same dir as the catalog
		if( targetDirPath == null || targetDirPath.length() == 0 )
			targetDirPath = new File(catalogBgzipPath).getCanonicalFile().getParent();
		
		// Throw exception if target directory  does not exist
		File targetDir = new File(targetDirPath).getCanonicalFile();
		if( ! targetDir.exists() )
			throw new IOException("Target directory does not exist: " + targetDir);
		
		// Throw exception if target dir is NOT a directory
		if( ! targetDir.isDirectory() )
			throw new IOException("Target directory is not a directory: " + targetDir);

		// Throw exception if target dir is NOT writable
		if( ! targetDir.canWrite() )
			throw new IOException("Target directory is not writable: " + targetDir);
		
		String catalogFullFilename = catalogFile.getName();
		String catalogFilenamePrefix = catalogFullFilename;
		if( catalogFullFilename.endsWith(".tsv") )
			catalogFilenamePrefix = catalogFullFilename.substring(0, catalogFullFilename.lastIndexOf(".tsv"));
		else if( catalogFullFilename.endsWith(".tsv.bgz") )
			catalogFilenamePrefix = catalogFullFilename.substring(0, catalogFullFilename.lastIndexOf(".tsv.bgz"));
		File columnsPropsFile = new File(targetDir.getCanonicalPath() + File.separator + catalogFilenamePrefix + COLUMNS_EXTENSION);
		File datasrcPropsFile = new File(targetDir.getCanonicalPath() + File.separator + catalogFilenamePrefix + DATASOURCE_EXTENSION);

		// Throw exception if datasource or columns properties file is an existing directory
		if( datasrcPropsFile.isDirectory() )
			throw new IOException("Datasource properties file is an existing directory.  It should be a file");
		if( columnsPropsFile.isDirectory() )
			throw new IOException("Columns properties file is an existing directory.  It should be a file");
		
		// Throw exception if either columns or datasource properties file already exists
		if( datasrcPropsFile.exists() )
			throw new IOException("Datasource properties file already exists.  If you would like to recreate the properties files, first remove the existing ones, then recreate them.");
		if( columnsPropsFile.exists() )
			throw new IOException("Columns properties file already exists.  If you would like to recreate the properties files, first remove the existing ones, then recreate them.");

		// If original vcf was specified, but it does not exist, then throw exception
		File originalVcfFile  = isVcfSpecified ? new File(catalogOriginalVcfPath) : null;
		if( isVcfSpecified  &&  (originalVcfFile == null || ! originalVcfFile.exists()) )
			throw new IOException("Orginal VCF source file that the catalog was built from does not exist!: " + catalogOriginalVcfPath);
		
		
		// If crawling the catalog (NOT using the original VCF file to parse the info), then check size of catalog and warn user if it is big
		boolean isCrawlCatalog = ! isVcfSpecified;
		if( isCrawlCatalog ) {
			final long MB = 1 * 1024 * 1024;
			if( catalogFile.length() > (10 * MB) )
				System.out.println("  WARNING: Catalog is large (" + (catalogFile.length() / MB) + "MB).  This may take a while to process)...");
		}

		
		// Create the datasource.props file
		createDatasourcePropsFile(datasrcPropsFile);
			
		// Create the columns.props file
		createColumnPropsFile(columnsPropsFile, catalogFile, originalVcfFile, isVcfSpecified);
		
		System.out.println("Datasource properties file created at: " + datasrcPropsFile.getCanonicalPath());
		System.out.println("Columns    properties file created at: " + columnsPropsFile.getCanonicalPath());
		
		System.out.println("Done.");
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
	protected void createDatasourcePropsFile(File datasourcePropsFile) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		datasourcePropsFile.createNewFile();
		
		//write all values from the enum to the file
		List<String> dsAttribs = Arrays.asList(
				BiorMetaControlledVocabulary.SHORTNAME.toString(),
				BiorMetaControlledVocabulary.DESCRIPTION.toString(),
				BiorMetaControlledVocabulary.SOURCE.toString(),
				BiorMetaControlledVocabulary.VERSION.toString(),
				BiorMetaControlledVocabulary.BUILD.toString()
				);

		String catalogNamePrefix = datasourcePropsFile.getName().replace(DATASOURCE_EXTENSION, "");

		// Build up the content
		StringBuilder content = new StringBuilder();
		content.append("### Datasource properties file for Catalog - " + catalogNamePrefix + ". Please fill in the descriptions to the keys below.\n");
		for(String key : dsAttribs)
			content.append(key + "=\n");

		this.writeToFile(datasourcePropsFile, content.toString());
	}
	

	/**
	 * 
	 * @param originalVcfFile 
	 * @param catalogFile
	 * @param isVcfSpecified 
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 * @throws URISyntaxException 
	 */
	protected void createColumnPropsFile(File columnPropsFile, File catalogFile, File originalVcfFile, boolean isVcfSpecified) throws InvalidOptionArgValueException, InvalidDataException, IOException, URISyntaxException {
	    List<ColumnMetaData> colMetaList = mergeCrawlerWithVcf(catalogFile, originalVcfFile, isVcfSpecified);
	    colMetaList = mergeWithDefaults(colMetaList);
	    
	    StringBuilder content = getColumnContents(columnPropsFile, colMetaList);

	    //Write the props file
	    columnPropsFile.createNewFile();
		this.writeToFile(columnPropsFile, content.toString());			
	}

	private List<ColumnMetaData> mergeCrawlerWithVcf(File catalogFile, File originalVcfFile, boolean isVcfSpecified) throws IOException {
		// First crawl the catalog to get all possible values (since this may have BioR or VCF keys that would not appear in the VCF INFO fields)
	    List<ColumnMetaData> colMetaList = new ColumnMetaFromCatalogCrawling().getColumnMetadata(catalogFile.getCanonicalPath());
	    
	    // If there IS a VCF file specified, then we want to use the values derived from that, 
	    // and add the VCF values to the list, replacing any values obtained from crawling
	    // (since those are only a best guess)
	    if( isVcfSpecified && originalVcfFile != null  &&  originalVcfFile.exists() ) {
	    	List<ColumnMetaData> fromVcfColMetaList = new ColumnMetaFromVcf().getColumnMetadata(originalVcfFile.getCanonicalPath());
	    	for(ColumnMetaData colMetaVcf : fromVcfColMetaList) {
    			// If the metadata is already in the list from the catalog crawler, 
    			// THEN remove it since the vcf one should be more accurate
    			// This should also add in any fields from BioR or the VCF first 7 columns
	    		for(int i=colMetaList.size()-1; i>=0; i--) {
	    			if( colMetaList.get(i).columnName.equals(colMetaVcf.columnName) ) {
	    				colMetaList.remove(i);
	    			}
	    		}
	    		// Now, add the metadata from the vcf parser
	    		colMetaList.add(colMetaVcf);
	    	}
	    }
		return colMetaList;
	}
	
	/** Merge with default column meta data for BioR and VCF fields.  
	 *  The defaults take preference over the crawled and VCF values.
	 *  NOTE: The list is modified and returned.
	 * @param colMetaList
	 * @return
	 * @throws URISyntaxException 
	 * @throws IOException 
	 */
	private List<ColumnMetaData> mergeWithDefaults(List<ColumnMetaData> colMetaList) throws URISyntaxException, IOException {
		// Load the defaults (known BioR and VCF columns)
	    File colDefaultsProps = ClasspathUtil.loadResource("/allCatalogs" + COLUMNS_EXTENSION);
	    HashMap<String, ColumnMetaData> defaultsColNameToMetaMap = new AddMetadataLines().parseColumnProperties(colDefaultsProps.getCanonicalPath());
	    for(int i=0; i < colMetaList.size(); i++) {
	    	String colName = colMetaList.get(i).columnName;
	    	
	    	// If the column name is present but matches one we have in the defaults file, then use the default
	    	if( defaultsColNameToMetaMap.containsKey(colName) )
	    		colMetaList.set(i, defaultsColNameToMetaMap.get(colName));
	    }
	    return colMetaList;
	}

	private StringBuilder getColumnContents(File columnPropsFile, List<ColumnMetaData> colMetaList) throws FileNotFoundException, URISyntaxException, IOException
	{
	    String catalogNamePrefix = columnPropsFile.getName().replace(COLUMNS_EXTENSION, "");
	    StringBuilder content = new StringBuilder();
	    content.append("### Column properties file for Catalog - " + catalogNamePrefix + ". Please fill in and/or edit the descriptions to the keys below.\n");
	    content.append("##-----------------------------------------------------\n");
	    content.append("##ColumnName=The key or column name\n");
	    content.append("##Type=The type of the object, as can be determined from parsing the VCF file or taking and educated guess based on the catalog values (Possible values: JSON, String, Integer, Float, Boolean)\n");
	    content.append("##Count=The number of values that repeatedly occur  (Possible values: 0 (Boolean), 1 (JSON,Integer,Float,String), or '.' (JsonArrays - which will be resolved to other primitives such as String,Integer,Float)\n");
	    content.append("##Description=The description of the ColumnName\n");
	    content.append("##-----------------------------------------------------\n");
	    content.append("#ColumnName	Type	Count	Description\n");

	    // Sort the list
	    Collections.sort(colMetaList);
	    // Merge with defaults - adding to the content string
	    for(ColumnMetaData colMeta : colMetaList)
	    	content.append(colMeta.toString() + "\n");
		return content;
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


}