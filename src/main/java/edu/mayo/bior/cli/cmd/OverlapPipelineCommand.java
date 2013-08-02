/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;


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
	private String operation;
	
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
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
		
		Metadata metadata = new Metadata(CmdType.Query, new File(tabixFile).getCanonicalPath(), operation);
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
		Pipe<History, History>  logic     = new OverlapPipe(tabixFile, minxtend, maxxtend, column);
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		mPipeline.execute(preLogic, logic, postLogic);		
	}
}
