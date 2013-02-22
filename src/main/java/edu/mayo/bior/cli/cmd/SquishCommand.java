package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.history.SquishPipe;

public class SquishCommand implements CommandPlugin {

        private static final char OPTION_COLLAPSE_COLUMN = 'c';
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {
                
                Integer col = 1;
                if (line.hasOption(OPTION_COLLAPSE_COLUMN)) {
                    col = new Integer(line.getOptionValue(OPTION_COLLAPSE_COLUMN));
                }
	
		SquishPipe pipe = new SquishPipe(col);
		
		mPipeline.execute(pipe);		
	}
}
