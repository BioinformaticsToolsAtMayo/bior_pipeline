package edu.mayo.bior.cli.cmd;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.cli.InvalidDataException;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.VEP.VEPPipeline;
import edu.mayo.exec.AbnormalExitException;

public class VEPCommand  implements CommandPlugin {

//	private static final String ENV_NAME_BIOR_LITE_HOME = "BIOR_LITE_HOME";
//	private static final String ENV_VALUE_BIOR_LITE_HOME = System.getenv(ENV_NAME_BIOR_LITE_HOME);

	private static final String OPTION_BUFFER_SIZE = "b";
	private static final String OPTION_FORK = "f";
        private static final String OPTION_PIPELINE = "a";
        private static final String OPTION_DASH = "-";
        private static final String OPTION_DOUBLEDASH = "--";
        private static final String OPTION_SIFT = "sift";
        private static final String OPTION_POLYPHEN = "polyphen";
        private static final String OPTION_TERMS = "t";
        private static final String OPTION_REGULATORY = "regulatory";
        private static final String OPTION_CELLTYPE = "cell_type";
        private static final String OPTION_HGNC = "hgnc";
        private static final String OPTION_HGVS = "hgvs";
        private static final String OPTION_PROTEIN = "protein";
        private static final String OPTION_CANONICAL = "canonical";
        private static final String OPTION_NUMBERS = "numbers";
        private static final String OPTION_NOINTERGENIC = "no_intergenic";
        private static final String OPTION_ONLYCODING = "conding_only";
        private static final String OPTION_MOSTSEVERE = "most_severe";
        private static final String OPTION_CHECKREF = "check_ref";
        private static final String OPTION_CHECKEXISTING = "check_existing";
        private static final String OPTION_FAILED = "failed";
        private static final String OPTION_CHECKALLELES = "check_alleles";
        private static final String OPTION_CHECKSVS = "check_svs";
        private static final String OPTION_CHECKFREQUENCY = "check_frequency";
        private static final String OPTION_CHR = "chr";
        private static final String OPTION_GP = "gp";
        private static final String OPTION_ALLOWNONVARIANT = "allow_non_variant";
        private static final String OPTION_FREQPOP = "freq_pop";
        private static final String OPTION_FREQ = "freq_freq";
        private static final String OPTION_FREQGT = "freq_gt_lt";
        private static final String OPTION_FREQFILTER = "freq_filter";
        private static final String OPTION_GMAF = "gmaf";
        private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

    	private static final Logger sLogger = Logger.getLogger(VEPCommand.class);
        
	public void init(Properties props) throws Exception {
	}

/*	@Override
	public String getScriptName(CommandLine line) {
		return ENV_VALUE_BIOR_LITE_HOME + "/bin/" + "_bior_vep.sh";
	}
*/
	public String[] getScriptArgs(CommandLine line) {
		
        Integer bufferSize = 50;
        if (line.hasOption(OPTION_BUFFER_SIZE)) {
            bufferSize = new Integer(line.getOptionValue(OPTION_BUFFER_SIZE));
        }		
		
        Integer numForks = 1;
        if (line.hasOption(OPTION_FORK)) {
            numForks = new Integer(line.getOptionValue(OPTION_FORK));
        }
        
        String pipeline = "-w";
        if(line.hasOption(OPTION_PIPELINE)){
            pipeline = "--all";
        }

        return new String[] 
        		{ 
        			String.valueOf(bufferSize),
        			String.valueOf(numForks),
                                pipeline
        		};
	}

/*	@Override
	public Map<String, String> getEnvVars(CommandLine line) {
		
		Map<String, String> m = new HashMap<String, String>();		
		m.put(ENV_NAME_BIOR_LITE_HOME, ENV_VALUE_BIOR_LITE_HOME);
		return m;
	} */
	
	public void execute (CommandLine line,Options options) {
		
		VEPPipeline vepPipeline = null;
		
		try {
			vepPipeline = new VEPPipeline(null);
			mPipeline.execute(vepPipeline);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} catch (AbnormalExitException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} catch (InvalidDataException e) {
			// TODO Auto-generated catch block
			sLogger.error(e.getMessage());
		} finally {
			// tell SNPEFF we're done so it doesn't hang
			if(vepPipeline != null)
				try {
					vepPipeline.terminate();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					sLogger.error(e.getMessage());
				}
		}
		
		
		
	}
}