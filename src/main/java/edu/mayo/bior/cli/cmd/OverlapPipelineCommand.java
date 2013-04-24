/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;


/**
 *
 * @author m102417
 */
public class OverlapPipelineCommand implements CommandPlugin {
    
	private static final char OPTION_TABIX_FILE = 'd'; //-d usually means 'database' in the BioR case, it is a catalog
        private static final char OPTION_COLUMN = 'c';
        private static final char OPTION_MINXTEND = 'w';
        private static final char OPTION_MAXXTEND = 'x';
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {
	
		String tabixFile = "";
		if (line.hasOption(OPTION_TABIX_FILE)) {
			tabixFile = line.getOptionValue(OPTION_TABIX_FILE);
		}
                		// default column is last column (e.g. -1)
		int column = -1;
		if (line.hasOption(OPTION_COLUMN)) {
			column = Integer.parseInt(line.getOptionValue(OPTION_COLUMN));
		}
                int minxtend = 0;
                if (line.hasOption(OPTION_MINXTEND)) {
			minxtend = Integer.parseInt(line.getOptionValue(OPTION_MINXTEND));
		}
                int maxxtend = 0;
                if (line.hasOption(OPTION_MAXXTEND)) {
			maxxtend = Integer.parseInt(line.getOptionValue(OPTION_MAXXTEND));
		}
                
               
                OverlapPipe overlapPipe = new OverlapPipe(tabixFile, minxtend, maxxtend, column);
		mPipeline.execute(overlapPipe);		
	}
}
