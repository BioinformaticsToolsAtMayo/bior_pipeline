package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

public class SameVariantCommand implements CommandPlugin {

	private static final char OPTION_COLUMN = 'c';
	private static final char OPTION_TABIX_FILE = 'd';
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	private String operation;

	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception {

		// default column is last column (e.g. -1)
		int column = -1;
		if (line.hasOption(OPTION_COLUMN)) {
			column = Integer.parseInt(line.getOptionValue(OPTION_COLUMN));
		}

		// get one or more tabix files in a List
		List<String> tabixFiles = new ArrayList<String>();
		if (line.hasOption(OPTION_TABIX_FILE)) {
			for (String value : line.getOptionValues(OPTION_TABIX_FILE)) {
				tabixFiles.add(value);
			}
		}		
		
		// construct a new pipeline that contains one or more SameVariantPipes
		int historyPosition = column;
		List<Pipe> chain = new ArrayList<Pipe>();
		ArrayList<Metadata> metadataList = new ArrayList<Metadata>();
		for (String tabixFile: tabixFiles) {
			chain.add(new SameVariantPipe(tabixFile, historyPosition));
			
			// update history position that points to input variant JSON column
			// for NEXT pipe to account for additional column
			if (historyPosition < 0) {
				// negative, 
				historyPosition--;
			} else {
				// positive, 
				historyPosition++;
			}
			metadataList.add(new Metadata(CmdType.Query, new File(tabixFile).getCanonicalPath(), operation));
		}		
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadataList);
		Pipe<History, History>  logic     = new Pipeline<History, History>(chain);
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		mPipeline.execute(preLogic, logic, postLogic);		
	}
}
