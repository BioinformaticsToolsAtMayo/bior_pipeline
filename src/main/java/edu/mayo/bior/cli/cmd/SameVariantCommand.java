package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.history.History;

public class SameVariantCommand implements CommandPlugin {

	private static final char OPTION_COLUMN = 'c';
	private static final char OPTION_TABIX_FILE = 'd';
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {

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
		
		// TODO: wire this up to the SameVariantPipe by passing
		// column and tabixFiles to the constructor
		Pipe<History, History> p = null;
		
		mPipeline.execute(p);
	}
}
