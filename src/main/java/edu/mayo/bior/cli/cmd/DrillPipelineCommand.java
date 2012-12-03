package edu.mayo.bior.cli.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.DrillPipeline;

public class DrillPipelineCommand implements CommandPlugin {

	private static final char OPTION_KEEP_JSON = 'k';	
	private static final char OPTION_PATH = 'p';
		
	private DrillPipeline mPipeline = new DrillPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, InputStream inStream, OutputStream outStream) throws Exception {
	
		boolean keepJson = false;
		if (line.hasOption(OPTION_KEEP_JSON)) {
			keepJson = true;
		}

		List<String> paths = new ArrayList<String>();
		if (line.hasOption(OPTION_PATH)) {
			for (String value : line.getOptionValues(OPTION_PATH)) {
				paths.add(value);
			}
		}		
	
		mPipeline.execute(keepJson, paths.toArray(new String[0]));		
	}
}
