package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

public class SNPEffCommand implements CommandPlugin{

	public static final String DEFAULT_GENOME_VERSION = "GRCh37.64";
	
	private static final String OPTION_HOMO         = "hom";
	private static final String OPTION_SNP          = "snp";
	private static final String OPTION_HET          = "het";
	private static final String OPTION_DELETIONS    = "del";
	private static final String OPTION_INSERTIONS   = "ins";
	private static final String OPTION_MINQ         = "minQ";
	private static final String OPTION_MAXQ         = "maxQ" ;
	private static final String OPTION_MINC         = "minC";
	private static final String OPTION_MAXC         = "maxC";
	private static final String OPTION_NODOWNSTREAM = "no_downstream";
	private static final String OPTION_NOINTERGENIC = "no_integenic";
	private static final String OPTION_NOINTRON     = "no_intron";
	private static final String OPTION_NOUPSTREAM   = "no_upstream";
	private static final String OPTION_NOUTR        = "no_utr";
	private static final String OPTION_ONLYCODING   = "onlyCoding";
	private static final String OPTION_UPDOWNSTREAMLENGTH = "ud" ;
	private static final char OPTION_STATSFILE      = 's';   
	private static final String OPTION_INTERVALFILE = "interval";
	private static final String OPTION_PICKALL      = "all";
	private static final String OPTION_GENOME_VERSION = "genome_version";
	
	/* BLACK LIST */
//	private static final char OPTION_INPUTFORMAT= 'i';
//	private static final char OPTION_OUTPUTFORMAT = 'o';
//	private static final String OPTION_BEDFILE = "fi" ;
//	private static final String OPTION_CHR = "chr";
//	private static final String OPTION_ONLYREG = "onlyReg";
//	private static final String OPTION_NMP = "nmp";
//	private static final String OPTION_REGULATION = "reg" ;
//	private static final char OPTION_CONFIG = 'c';
//	private static final char OPTION_HELP = 'h';
//	private static final String OPTION_NOLOGS = "noLogs";
//	private static final String OPTION_NOSTATS = "noStats";
//	private static final int OPTION_OFFSET0 = 0;
//	private static final int OPTION_OFFSET1 = 1;
//	private static final String OPTION_INOFFSET = "if";
//	private static final String OPTION_OUTOFFSET = "of" ;
//	private static final char OPTION_VERBOSE = 'v';
//	private static final char OPTION_QUIET = 'q';
	
	private static final  String OPTION_DASH = "-";

	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	private String operation;
	
	private static final Logger sLogger = Logger.getLogger(SNPEffCommand.class);

	
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception {
		
		SNPEFFPipeline snpEffPipe = null;
		boolean pickworst = Boolean.TRUE;
		if(line.hasOption(OPTION_PICKALL)){
			 pickworst = Boolean.FALSE;			 
		}
		
		Metadata metadata = new Metadata(operation);				
		try {
		
			snpEffPipe = new SNPEFFPipeline(getCommandLineOptions(line),pickworst);

			Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
			Pipe<History, History>  logic     = snpEffPipe;
			Pipe<History, String>   postLogic = new HistoryOutPipe();
			
			mPipeline.execute(preLogic, logic, postLogic);
			
		} catch(Exception e) {
			sLogger.error("Could not execute SNPEffCommand.  " + e.getMessage());
			throw e;
		} finally {
			// tell SNPEFF we're done so it doesn't hang
			if(snpEffPipe != null)
				snpEffPipe.terminate();
		}
	}
	
	private String[] getCommandLineOptions(CommandLine line) {
		
		List<String> cmdoptions = new ArrayList<String>();

		String genomeDatabase;
		if (line.hasOption(OPTION_GENOME_VERSION)) {
			genomeDatabase = line.getOptionValue(OPTION_GENOME_VERSION);			
		} else {
			// default
			genomeDatabase = DEFAULT_GENOME_VERSION;
		}
		// add as an argument, not an option
		sLogger.debug(String.format("Using genome database version %s", genomeDatabase));
		cmdoptions.add(genomeDatabase);
		
		if (line.hasOption(OPTION_SNP)) {
			cmdoptions.add(OPTION_DASH + OPTION_SNP);
		}

		if (line.hasOption(OPTION_HET)) {
			cmdoptions.add(OPTION_DASH + OPTION_HET);
		}

		if (line.hasOption(OPTION_HOMO)) {
			cmdoptions.add(OPTION_DASH + OPTION_HOMO);
		}

		if (line.hasOption(OPTION_DELETIONS)) {
			cmdoptions.add(OPTION_DASH + OPTION_DELETIONS);
		}

		if (line.hasOption(OPTION_INSERTIONS)) {
			cmdoptions.add(OPTION_DASH + OPTION_INSERTIONS);
		}

		if (line.hasOption(OPTION_NODOWNSTREAM)) {
			cmdoptions.add(OPTION_DASH + OPTION_NODOWNSTREAM.replace("_", "-"));
		}

		if (line.hasOption(OPTION_NOINTERGENIC)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOINTERGENIC.replace("_", "-"));
		}

		if (line.hasOption(OPTION_NOINTRON)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOINTRON.replace("_", "-")) ;
		}

		if (line.hasOption(OPTION_NOUPSTREAM)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOUPSTREAM.replace("_", "-"));
		}

		if (line.hasOption(OPTION_NOUTR)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOUTR.replace("_", "-")) ;
		}
       
		if (line.hasOption(OPTION_INTERVALFILE)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_INTERVALFILE);
			cmdoptions.add(line.getOptionValue(OPTION_INTERVALFILE));
		}
		
		if (line.hasOption(OPTION_MAXQ)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_MAXQ);
			cmdoptions.add(line.getOptionValue(OPTION_MAXQ));
		}
	
         if (line.hasOption(OPTION_MINQ)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_MINQ);
			cmdoptions.add(line.getOptionValue(OPTION_MINQ));
		}
	
         if (line.hasOption(OPTION_MAXC)) {
 			
 			cmdoptions.add(OPTION_DASH + OPTION_MAXC);
 			cmdoptions.add(line.getOptionValue(OPTION_MAXC));
 		}
 	
         if (line.hasOption(OPTION_MINC)) {
 			
 			cmdoptions.add(OPTION_DASH + OPTION_MINC);
 			cmdoptions.add(line.getOptionValue(OPTION_MINC));
 		}
 	
			
           if (line.hasOption(OPTION_STATSFILE)) {
   			
   			cmdoptions.add(OPTION_DASH + OPTION_STATSFILE);
   			cmdoptions.add(line.getOptionValue(OPTION_STATSFILE));
   		}
           
           if ( line.hasOption(OPTION_UPDOWNSTREAMLENGTH)) {
          	 
          	 cmdoptions.add(OPTION_DASH + OPTION_UPDOWNSTREAMLENGTH);
             cmdoptions.add(line.getOptionValue(OPTION_UPDOWNSTREAMLENGTH));
           }
   		
         if( line.hasOption(OPTION_ONLYCODING)) {
        	 
        	 cmdoptions.add(OPTION_DASH + OPTION_ONLYCODING ); 
             cmdoptions.add(line.getOptionValue(OPTION_ONLYCODING));
         }
         		
    	return cmdoptions.toArray(new String[cmdoptions.size()]);
	}
}

