package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.bior.pipeline.Treat.OverlapingFeaturesPipeline;
import edu.mayo.bior.pipeline.VEP.VEPPipeline;
import edu.mayo.cli.CommandPlugin;

public class AnnotateCommand implements CommandPlugin {
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	private static final Logger sLogger = Logger.getLogger(AnnotateCommand.class);
	private static final String OPTION_PICKWORST = "all";
	private static final String OPTION_GENOME_VERSION = "genome_version";

	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
	
	}

	/**
	 * 
	 */
	public void execute(CommandLine line, Options opts) throws Exception {
		boolean pickworst = Boolean.TRUE;
		
		if (line.hasOption(OPTION_PICKWORST)){
			pickworst = Boolean.FALSE;
		}
		
		VEPPipeline vepPipeline = null;
		SNPEFFPipeline snpEffPipeline = null;
		
		OverlapingFeaturesPipeline overlapingFeaturesPipeline = null;
		Pipeline annotate = null; 
		
		try {
			
			//snpEffPipeline = new SNPEFFPipeline(getCommandLineOptions(line),pickworst);
			//vepPipeline = new VEPPipeline(getCommandLineOptions(line),pickworst);
			
			
			overlapingFeaturesPipeline = new OverlapingFeaturesPipeline();
			
			// TODO finalise all the pipelines
			annotate = new Pipeline(overlapingFeaturesPipeline);
			
			mPipeline.execute(annotate);
		} catch (Exception e) {
			System.out.println("Error:"+e);
			e.printStackTrace();
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