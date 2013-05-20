package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.exec.AbnormalExitException;

public class AnnotateCommand implements CommandPlugin {
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	private static final Logger sLogger = Logger.getLogger(AnnotateCommand.class);

	private static final String OPTION_GENOME_VERSION = "genome_version";
	
	private static final char OPTION_CONFIG_FILE = 'c';

	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
	
	}

	/**
	 * 
	 */
	public void execute(CommandLine line, Options opts) throws IOException, InterruptedException, 
																BrokenBarrierException, TimeoutException, AbnormalExitException, 
																InvalidOptionArgValueException, InvalidDataException{
			
		String configFilePath; 
		
		if (line.hasOption(OPTION_CONFIG_FILE)) {
			
			configFilePath = line.getOptionValue(OPTION_CONFIG_FILE);
			
			if ( ! new File(configFilePath).exists() ) {	
				throw new InvalidOptionArgValueException(
						opts.getOption(OPTION_CONFIG_FILE + ""),
						configFilePath, 
						"The Config file path '" + configFilePath + "' does not exist. Please specify a valid config file path."
						);
			}	
		} else { // config file option not specified. This should use the 'default' config file.
			configFilePath = "src/main/resources/default.config";
		}
		 		
		try {
			mPipeline.execute(new TreatPipeline(configFilePath));
		} catch(Exception ex) {
			throw new InvalidOptionArgValueException(
					opts.getOption(OPTION_CONFIG_FILE + ""),
					configFilePath, 
					ex.getMessage()
					);
		}
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