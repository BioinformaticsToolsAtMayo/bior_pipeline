/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.OverlapPipeline;


/**
 *
 * @author m102417
 */
public class OverlapPipelineCommand implements CommandPlugin {
    
	private static final char OPTION_TABIX_FILE = 'd'; //-d usually means 'database' in the BioR case, it is a catalog	
		
	private OverlapPipeline mPipeline = new OverlapPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {
	
		String tabixFile = "";
		if (line.hasOption(OPTION_TABIX_FILE)) {
			tabixFile = line.getOptionValue(OPTION_TABIX_FILE);
		}		
	
		mPipeline.execute(tabixFile);		
	}
}
