package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.VEP.VEPPipeline;
import edu.mayo.bior.util.ClasspathUtil;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;

public class VEPCommand  implements CommandPlugin {

	// Buffer size not allowed by user - must be 1 to prevent hangs
    //private static final String OPTION_BUFFER_SIZE = 	"b";
    private static final String OPTION_FORK = 			"f";
    private static final String OPTION_PIPELINE = 		"a";
    private static final String OPTION_PICKWORST = 		"all";
    private static final String OPTION_DASH = 			"-";
    private static final String OPTION_DOUBLEDASH = 	"--";
    private static final String OPTION_TERMS = 			"t";
    private static final String OPTION_PROTEIN = 		"protein";
    private static final String OPTION_NOINTERGENIC = 	"no_intergenic";
    private static final String OPTION_ONLYCODING = 	"conding_only";
    private static final String OPTION_CHECKEXISTING = 	"check_existing";
    private static final String OPTION_CHECKALLELES = 	"check_alleles";
    private static final String OPTION_ALLOWNONVARIANT ="allow_non_variant";
    private static final String OPTION_FREQPOP = 		"freq_pop";
    private static final String OPTION_FREQ = 			"freq_freq";
    private static final String OPTION_FREQGT = 		"freq_gt_lt";
    private static final String OPTION_FREQFILTER = 	"freq_filter";


	

    private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
    private String operation;


	private static final Logger sLogger = Logger.getLogger(VEPCommand.class);
        
	
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}

	
	public void execute (CommandLine line,Options options) throws Exception {
		
		boolean pickworst = Boolean.TRUE;
		if (line.hasOption(OPTION_PICKWORST)){
			pickworst = Boolean.FALSE;
		}
		VEPPipeline vepPipeline = null;
				
		try {
			File dataSourceProps = ClasspathUtil.loadResource("/tools/vep.datasource.properties");
			File columnProps     = ClasspathUtil.loadResource("/tools/vep.columns.properties");
			
			Metadata metadata = new Metadata(dataSourceProps.getCanonicalPath(), columnProps.getCanonicalPath(), operation);
			
			vepPipeline = new VEPPipeline(getCommandLineOptions(line),pickworst);
			
			Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
			Pipe<History, History>  logic     = vepPipeline;
			Pipe<History, String>   postLogic = new HistoryOutPipe();
			
			mPipeline.execute(preLogic, logic, postLogic);
			
		} finally {
			// tell VEP we're done so it doesn't hang
			if(vepPipeline != null) {
				try {
					vepPipeline.terminate();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					sLogger.error(e.getMessage());
				}
			}
		}
	}
		
	private String[] getCommandLineOptions(CommandLine line) {
		
		List<String> cmdoptions = new ArrayList<String>();
		

		if (line.hasOption(OPTION_FORK))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_FORK + " " + line.getOptionValue(OPTION_FORK));
		
		if (line.hasOption(OPTION_ALLOWNONVARIANT))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_ALLOWNONVARIANT);

		if (line.hasOption(OPTION_CHECKALLELES))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_CHECKALLELES);
		 
		if (line.hasOption(OPTION_NOINTERGENIC))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_NOINTERGENIC);
		 
		if (line.hasOption(OPTION_ONLYCODING))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_ONLYCODING);
		 
		if (line.hasOption(OPTION_PROTEIN ))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_PROTEIN);
		 
		if (line.hasOption(OPTION_TERMS))
			cmdoptions.add(OPTION_DASH + OPTION_TERMS + " " + line.getOptionValue(OPTION_TERMS));
		 
		if (line.hasOption(OPTION_CHECKEXISTING))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_CHECKEXISTING);
		
		if (line.hasOption(OPTION_FREQPOP))
			cmdoptions.add(OPTION_DOUBLEDASH + OPTION_FREQPOP + line.getOptionValue("OPTION_FREQPOP"));
		 
		return cmdoptions.toArray(new String[cmdoptions.size()]);
}

}