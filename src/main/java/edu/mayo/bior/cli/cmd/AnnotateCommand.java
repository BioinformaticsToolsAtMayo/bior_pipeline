package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import edu.mayo.bior.util.DependancyUtil;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.UnixStreamPipeline.Status;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipelineSingleThread;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;

public class AnnotateCommand implements CommandPlugin {
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	private static final Logger sLogger = Logger.getLogger(AnnotateCommand.class);

	private static final String OPTION_GENOME_VERSION = "genome_version";
	
	private static final char OPTION_CONFIG_FILE   = 'c';
	private static final char OPTION_MULTI_PROCESS = 'm';
	// If user provided path to status file (to write status after command completes)
	private static final char OPTION_STATUS        = 's';

	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception
	{
		try {	
			String configFilePath=null; 
			
			if (line.hasOption(OPTION_CONFIG_FILE)) {
				configFilePath = line.getOptionValue(OPTION_CONFIG_FILE);
				if ( ! new File(configFilePath).exists()  ||  new File(configFilePath).length() == 0 ) {	
					throw new InvalidOptionArgValueException(
							opts.getOption(OPTION_CONFIG_FILE + ""),
							configFilePath, 
							"The Config file path '" + configFilePath + "' does not exist (or is empty). Please specify a valid config file path."
							);
				}
			} 

			try {
				boolean isMultiProcess = line.hasOption(OPTION_MULTI_PROCESS);
				if( isMultiProcess ) {
					sLogger.warn("WARNING: Running bior_annotate as a MULTI-PROCESS command!!!");
					TreatPipeline treatPipeline = new TreatPipeline(configFilePath);
					List<Metadata>  treatMetadata = treatPipeline.getMetadata(); 
					mPipeline.execute(new HistoryInPipe(treatMetadata), treatPipeline, new HistoryOutPipe());
				} else {  // NOTE: THIS IS THE DEFAULT ONE THAT WILL BE RUN!!! ========================================
					sLogger.info("NOTE: Running bior_annotate as a single-threaded command.");
					TreatPipelineSingleThread treatPipeline = new TreatPipelineSingleThread(configFilePath);
					List<Metadata>  treatMetadata = treatPipeline.getMetadata(); 
					mPipeline.execute(new HistoryInPipe(treatMetadata), treatPipeline, new HistoryOutPipe());
				}
				// Ran to completion, so successful.  (even if individual lines failed)
				mPipeline.getStatus().isSuccessful = true;
			} catch(IllegalArgumentException ex) {
				throw new InvalidOptionArgValueException(
						opts.getOption(OPTION_CONFIG_FILE + ""),
						configFilePath, 
						ex.getMessage()
						);
			} catch (URISyntaxException e) {
				throw new IOException("Could not load properties file for catalog or tool: " + e.getMessage());
			}
		}catch(Exception e) {
			mPipeline.getStatus().isSuccessful = false;
			System.err.println("ERROR: " + e.getMessage());
			throw e;
		}
		
		// Write status out to a properties file
		if( line.hasOption(OPTION_STATUS))	{
			String statusFilePath = line.getOptionValue(OPTION_STATUS);
			FileUtils.writeStringToFile(new File(statusFilePath), mPipeline.getStatus().toString());
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