package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

public class DrillCommand implements CommandPlugin {

	private static final char OPTION_KEEP_JSON = 'k';	
	private static final char OPTION_PATH = 'p';
        private static final char OPTION_DRILL_COLUMN = 'c';
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	private String operation;
	
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception {
	
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

        String[] pathArr = paths.toArray(new String[0]);
		Metadata metadata = new Metadata(CmdType.Drill, col, operation, keepJSON, pathArr);
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
		Pipe<History, History>  logic     = new DrillPipe(keepJSON, pathArr, col);
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		mPipeline.execute(preLogic, logic, postLogic);
	}
}
