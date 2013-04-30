package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.cli.CommandPlugin;

public class AnnotateCommand implements CommandPlugin {
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	private static final Logger sLogger = Logger.getLogger(AnnotateCommand.class);

	private static final String OPTION_GENOME_VERSION = "genome_version";

	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
	
	}

	/**
	 * 
	 */
	public void execute(CommandLine line, Options opts) throws Exception {
		
		mPipeline.execute(new TreatPipeline());
		
	}
	
	private String[] getCommandLineOptions(CommandLine line) {
		
		List<String> cmdoptions = new ArrayList<String>();
		
		// TODO build the options list
		String genomeDatabase;
		if (line.hasOption(OPTION_GENOME_VERSION)) {
			genomeDatabase = line.getOptionValue(OPTION_GENOME_VERSION);			
		} else {
			// default
			genomeDatabase = "GRCh37.64";
		}
		// add as an argument, not an option
		sLogger.debug(String.format("Using genome database version %s", genomeDatabase));
		cmdoptions.add(genomeDatabase);
		
		return cmdoptions.toArray(new String[cmdoptions.size()]);
	}
}