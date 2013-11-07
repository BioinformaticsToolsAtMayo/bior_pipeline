package edu.mayo.bior.pipeline.Treat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import edu.mayo.bior.pipeline.Treat.format.Formatter;
import edu.mayo.bior.pipeline.Treat.format.FormatterPipeFunction;
import edu.mayo.bior.pipeline.Treat.format.HgncFormatter;
import edu.mayo.bior.pipeline.Treat.format.NcbiGeneFormatter;
import edu.mayo.bior.pipeline.Treat.format.OmimFormatter;
import edu.mayo.bior.pipeline.Treat.format.VEPFormatter;
import edu.mayo.bior.pipeline.Treat.format.VEPHgncFormatter;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.bior.util.BiorProperties.Key;
import edu.mayo.bior.util.DependancyUtil;

/**
 * @author Michael Meiners (m054457)
 * Date created: Nov 4, 2013
 */
public class TreatUtils {
	
	private static Logger sLogger = Logger.getLogger(TreatUtils.class);

	private BiorProperties	mBiorProps;	
	private List<String> mConfigColumnsToOutput = null;

	public TreatUtils() throws IOException {
		mBiorProps = new BiorProperties();
	}
	
	/** Do we need to add this pipe?  Yes, if any of its columns are in the config file
	   (or config properties are null, which signifies that the user wants ALL columns)
	   (or if the columnFormatter is null, which means it is a necessary pipe) */
	public  boolean isNeedPipe(Formatter colFormatter) {
		if( colFormatter == null || mConfigColumnsToOutput == null )
			return true;
		// Else we need to loop thru the columns this pipe would add.
		// If any are in the config file, then we need it 
		for(String colFromPipe : colFormatter.getHeaders()) {
			if(mConfigColumnsToOutput.contains(colFromPipe))
				return true;
		}
		
		// There are a few dependencies:
		//  - Add Vep 		if VepHgnc is wanted    	(vepHgnc depends on vep)
		//  - Add NcbiGene 	if Hgnc is wanted			(Hgnc    depends on NcbiGene)
		//	- Add NcbiGene AND Hgnc  if Omim is wanted	(Omim	 depends on BOTH NcbiGene AND Hgnc)
		if(colFormatter instanceof VEPFormatter  &&  isNeedPipe(new VEPHgncFormatter()))
			return true;
		if(colFormatter instanceof NcbiGeneFormatter  &&  isNeedPipe(new HgncFormatter()))
			return true;
		if(colFormatter instanceof NcbiGeneFormatter  &&  isNeedPipe(new OmimFormatter())) 
			return true;
		if(colFormatter instanceof HgncFormatter &&  isNeedPipe(new OmimFormatter())) 
			return true;

		
		// None are in the config file, so safe to bypass pipe
		return false;
	}

	/** Get the full path of the catalog as specified in the bior.properties file */
	public String getFile(Key propKey) {
		String path =  mBiorProps.get(Key.fileBase) + mBiorProps.get(propKey);
		return path;
	}

	
	/** Load the config file that contains the columns that bior_annotate is to keep */
	public List<String> loadConfig(String configFilePath) throws IOException {
		if(configFilePath == null || configFilePath.length() == 0)
			return null;
		
		mConfigColumnsToOutput = Files.readLines(new File(configFilePath), Charsets.UTF_8);
		// Remove those starting with "#"
		for(int i=mConfigColumnsToOutput.size()-1; i>=0; i--) {
			if(mConfigColumnsToOutput.get(i).startsWith("#") || mConfigColumnsToOutput.get(i).trim().length() == 0)
				mConfigColumnsToOutput.remove(i);
		}
		return mConfigColumnsToOutput;
	}
	

	/** Throw an exception if the config file column is not one of the possible ones */
	public void validateConfigFileColumns(List<String> configFileCols) {
		if(configFileCols == null)
			return;  // No config file specified - OK
		
		if(configFileCols.size() == 0) {
			final String MSG = "Error: The config file does not contain any output columns.  Please add some columns to output.  Or, to add all columns, do not add the config file option.";
			throw new IllegalArgumentException(MSG);
		}

		List<String> allCols = FormatterPipeFunction.getAllPossibleColumns();
		StringBuffer errMsg = new StringBuffer();
		for(String configCol : configFileCols) {
			if( ! allCols.contains(configCol) )
				errMsg.append("    " + configCol + "\n");
		}
		if(errMsg.length() > 0) {
			errMsg.insert(0, "Error: these columns specified in the config file are not recognized:\n");
			throw new IllegalArgumentException(errMsg.toString());
		}
	}
	
	public void throwErrorIfMissing(Key catalogOrIndexKey, FileType catalogOrIndex) throws FileNotFoundException {
		if( isFileThere(catalogOrIndexKey, catalogOrIndex) ) // OK, just return
			return;
		
		// Map the key to the name that should be displayed
		HashMap<Key,String> map = new HashMap<Key,String>();
		map.put(Key.dbsnpFile, 			"dbSNP");
		map.put(Key.dbsnpClinvarFile, 	"dbSNP ClinVar");
		map.put(Key.cosmicFile, 		"COSMIC");
		map.put(Key.blacklistedFile, 	"UCSC (blacklisted)");
		map.put(Key.conservationFile, 	"UCSC (conservation)");
		map.put(Key.enhancerFile, 		"UCSC (enhancer)");
		map.put(Key.tfbsFile, 			"UCSC (tfbs)");
		map.put(Key.tssFile, 			"UCSC (tss)");
		map.put(Key.uniqueFile, 		"UCSC (unique)");
		map.put(Key.repeatFile, 		"UCSC (repeat)");
		map.put(Key.regulationFile, 	"UCSC (regulation)");
		map.put(Key.mirBaseFile, 		"mirBase");
		map.put(Key.bgiFile, 			"BGI");
		map.put(Key.espFile, 			"ESP");
		map.put(Key.hapMapFile, 		"HapMap");
		map.put(Key.kGenomeFile, 		"1000 Genomes");
		map.put(Key.genesFile, 			"NCBIGene");
		map.put(Key.hgncFile, 			"HGNC");
		map.put(Key.omimFile, 			"OMIM");
		
		String errorMsg  = String.format("ERROR: %s catalog is required for the fields that were selected, but the %s was not found.", 
								map.get(catalogOrIndexKey), catalogOrIndex);
		String verifyMsg = "       Verify bior.properties file location matches an existing directory and catalog/index file (key = " + catalogOrIndexKey.toString() + ")";
		System.err.println(errorMsg);
		System.err.println(verifyMsg);

		throw new FileNotFoundException(getFile(Key.dbsnpFile));
	}
	
	 /** Check to see if the file is there given a key in the bior.properties file
	  * @param propKey - name of the property in the bior.properties file
	  * @param type    - type of file (index, catalog)
	  */
	public  enum FileType { catalog, index };
	private boolean isFileThere(Key propKey, FileType fileType){
	     String path = getFile(propKey);
	     boolean setup = false;
	     if(fileType.equals(FileType.catalog) ){
	         setup = DependancyUtil.isCatalogInstalled(path);
	     }else if( fileType.equals(FileType.index)) {
	         setup = DependancyUtil.isIndexInstalled(path);
	     }
	     return setup;
	 }

	
	public int getMaxAlts() {
		int maxAlts = mBiorProps.getAsInt(Key.MaxAlts, 20);
		return maxAlts;
	}
	
	public int getMaxLinesInFlight() {
		int maxLines = mBiorProps.getAsInt(Key.AnnotateMaxLinesInFlight, 10);
		sLogger.info("AnnotateMaxLinesInFlight = " + maxLines);
		if( maxLines <= 1 )
			throw new IllegalArgumentException("AnnotateMaxLinesInFlight must be 2 or greater to prevent hangs!");
		if( maxLines > 50 )
			sLogger.warn("WARNING: AnnotateMaxLinesInFlight is set to > 50.  This may cause a hang state as the process buffers can overflow and cause data loss, especially in the case of a high number of fanouts!");
		return maxLines;
	}
	
	public int getTimeout() {
		int cmdTimeoutInSeconds = mBiorProps.getAsInt(Key.TimeoutCommand, 30);
		sLogger.info("TimeoutCommand (seconds) = " + cmdTimeoutInSeconds);
		if( cmdTimeoutInSeconds < 10 || cmdTimeoutInSeconds > 300 )
			sLogger.warn("WARNING: TimeoutCommand is set to " + cmdTimeoutInSeconds + ".  It should probably be between 10 and 300.  Too short and it may timeout for a long line and crash the program unnecessarily.  Too long and it may wait a long time for a hung command to finish.");
		return cmdTimeoutInSeconds;
	}
	
	
	//==========================================================================


		
		
}
