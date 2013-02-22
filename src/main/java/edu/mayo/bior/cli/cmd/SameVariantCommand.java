package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.history.History;

public class SameVariantCommand implements CommandPlugin {

	private static final char OPTION_COLUMN = 'c';
	private static final char OPTION_TABIX_FILE = 'd';
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	public void init(Properties props) throws Exception {
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
		}		
		Pipeline<History, History> sameVariantPipeline = new Pipeline<History, History>(chain); 
				
		mPipeline.execute(sameVariantPipeline);
	}
}
