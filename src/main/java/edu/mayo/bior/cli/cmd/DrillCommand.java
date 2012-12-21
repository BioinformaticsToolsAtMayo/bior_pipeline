package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.JSON.DrillPipe;

public class DrillCommand implements CommandPlugin {

	private static final char OPTION_KEEP_JSON = 'k';	
	private static final char OPTION_PATH = 'p';
        private static final char OPTION_DRILL_COLUMN = 'c';
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {
	
		boolean keepJSON = false;
		if (line.hasOption(OPTION_KEEP_JSON)) {
			keepJSON = true;
		}

		List<String> paths = new ArrayList<String>();
		if (line.hasOption(OPTION_PATH)) {
			for (String value : line.getOptionValues(OPTION_PATH)) {
				paths.add(value);
			}
		}
                
                Integer col = -1;
                if (line.hasOption(OPTION_DRILL_COLUMN)) {
                    col = new Integer(line.getOptionValue(OPTION_DRILL_COLUMN));
                }
	
		DrillPipe pipe = new DrillPipe(keepJSON, paths.toArray(new String[0]), col);
		
		mPipeline.execute(pipe);		
	}
}
