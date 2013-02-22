package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.cli.InvalidOptionArgValueException;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.JSON.lookup.LookupPipe;

public class LookupCommand implements CommandPlugin {

	private static final char OPTION_DRILL_COLUMN = 'c';
	private static final char INDEX_FILE = 'x';
	// JSON path to extract key (if column is specified, the json in that column is used.
	// If not, then the last column is used)
	private static final char OPTION_JSON_PATH = 'p';
	private static final char CATALOG_FILE = 'd';
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {		
		
		String catalogFilePath = line.getOptionValue(CATALOG_FILE);
		
		if (!doesFileExist(catalogFilePath)) {	
			Option catalogFileOpt = opts.getOption(String.valueOf(CATALOG_FILE));
			throw new InvalidOptionArgValueException(catalogFileOpt, 
														catalogFilePath, 
														"The catalog file path '" + catalogFilePath+ "' does not exist. Please specify a valid catalog file path."
													);
		}			
		
		// JSON may be null if parameter not specified
		// (NOTE: It is NOT required - if not specified, then the entire column will be used as Id)
		String jsonPath = line.getOptionValue(OPTION_JSON_PATH);

		String indexFilePath = "";
		if (line.hasOption(INDEX_FILE)) {
			indexFilePath = line.getOptionValue(INDEX_FILE);
		} else {
			//find the index file based on catalog-name??
			indexFilePath = buildIndexPath(catalogFilePath, jsonPath);
		}
		
		if (!doesFileExist(indexFilePath)) {
			Option indexFileOpt = opts.getOption(String.valueOf(INDEX_FILE));			
			throw new InvalidOptionArgValueException(indexFileOpt, 
					indexFilePath, 
					"The index file path '" + indexFilePath+ "' does not exist. Please specify a valid index file path." + "\n Do you need to create the index? \n use bior_index -h for more information."
				);
		}			
                
        Integer column = -1;
        if (line.hasOption(OPTION_DRILL_COLUMN)) {
        	column = new Integer(line.getOptionValue(OPTION_DRILL_COLUMN));
        }
        
        LookupPipe pipe = new LookupPipe(catalogFilePath, indexFilePath, column);
		
		mPipeline.execute(pipe);		
	}
	
	private String buildIndexPath(String catalogPath, String jsonPath) throws IOException {
		File bgzipFile = new File(catalogPath);
		
		// Get the catalog prefix up to the first dot
		String bgzipPrefix = bgzipFile.getName();
		int idxFirstDot = bgzipPrefix.indexOf(".");
		if(idxFirstDot != -1)
			bgzipPrefix = bgzipPrefix.substring(0, idxFirstDot);
		
		File bgzipParentDir = bgzipFile.getParentFile();
		String fullIndexPath = bgzipParentDir.getCanonicalPath() + "/index/" + bgzipPrefix + "." + jsonPath + ".idx.h2.db";
		return fullIndexPath;
	}
	
	private boolean doesFileExist(String filePath) throws IOException {
		boolean fileExists = false;
		
		File objFile = new File(filePath);
		
		if (objFile.exists())
			fileExists = true;
		
		return fileExists;
	}
}
