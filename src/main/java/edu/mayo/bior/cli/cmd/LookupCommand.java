package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.JSON.lookup.LookupPipe;

public class LookupCommand implements CommandPlugin {

	private static final char OPTION_DRILL_COLUMN = 'c';
	private static final char INDEX_FILE = 'x';
	// JSON path to extract key (if column is specified, the json in that column is used.
	// If not, then the last column is used)
	private static final char OPTION_JSON_PATH = 'p';
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {
		String indexFilePath = "";
		
		String catalogPath = line.getArgs()[0];
		
		// JSON may be null if parameter not specified
		// (NOTE: It is NOT required - if not specified, then the entire column will be used as Id)
		String jsonPath = line.getOptionValue(OPTION_JSON_PATH);
		
		if (line.hasOption(INDEX_FILE)) {
			indexFilePath  = line.getOptionValue(INDEX_FILE);
		} else {
			//find the index file based on catalog-name??
		}
                
        Integer column = -1;
        if (line.hasOption(OPTION_DRILL_COLUMN)) {
        	column = new Integer(line.getOptionValue(OPTION_DRILL_COLUMN));
        }
	    
        LookupPipe pipe = new LookupPipe(catalogPath, indexFilePath, jsonPath, column);
		
		mPipeline.execute(pipe);		
	}
}
