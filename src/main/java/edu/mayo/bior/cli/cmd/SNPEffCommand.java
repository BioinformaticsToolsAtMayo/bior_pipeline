package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;

public class SNPEffCommand implements CommandPlugin{

	private static final char OPTION_INPUTFORMAT= 'i';
	private static final String OPTION_HOMO = "hom";
	private static final String OPTION_SNP = "snp";
	private static final String OPTION_HET = "het";
	private static final String OPTION_DELETIONS = "del";
	private static final String OPTION_INSERTIONS = "ins";
	private static final String OPTION_MINQ = "minQ";
	private static final String OPTION_MAXQ = "maxQ" ;
	private static final String OPTION_MINC = "minC";
	private static final String OPTION_MAXC = "maxC";
	private static final String OPTION_NODOWNSTREAM = "no-downstream";
	private static final String OPTION_NOINTERGENIC = "no-integenic";
	private static final String OPTION_NOINTRON = "no-intron";
	private static final String OPTION_NOUPSTREAM = "no-upstream";
	private static final String OPTION_NOUTR = "no-utr";
	private static final String OPTION_ONLYCODING = "onlyCoding";
	private static final int OPTION_OFFSET0 = 0;
	private static final int OPTION_OFFSET1 = 1;
	private static final char OPTION_OUTPUTFORMAT = 'o';
	private static final String OPTION_UPDOWNSTREAMLENGTH = "ud" ;
	private static final String OPTION_INOFFSET = "if";
	private static final String OPTION_OUTOFFSET = "of" ;
	private static final char OPTION_STATSFILE = 's';   
	private static final String OPTION_INTERVALFILE = "interval";
	private static final char OPTION_VERBOSE = 'v';
	private static final char OPTION_QUIET = 'q';
	/* BLACK LIST */
	private static final String OPTION_BEDFILE = "fi" ;
	private static final String OPTION_CHR = "chr";
	private static final String OPTION_ONLYREG = "onlyReg";
	private static final String OPTION_NMP = "nmp";
	private static final String OPTION_REGULATION = "reg" ;
	private static final char OPTION_CONFIG = 'c';
	private static final char OPTION_HELP = 'h';
	private static final String OPTION_NOLOGS = "noLogs";
	private static final String OPTION_NOSTATS = "noStats";
	private static final  String OPTION_DASH = "-";

	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	private static final Logger sLogger = Logger.getLogger(SNPEffCommand.class);

	
	public void init(Properties props) throws Exception {
	}


	public void execute(CommandLine line, Options opts) throws Exception {
		SNPEFFPipeline snpEffPipe = null;
		try {
			snpEffPipe = new SNPEFFPipeline(getCommandLineOptions(line));
	    //	snpEffPipe = new SNPEFFPipeline(null);
			
			mPipeline.execute(snpEffPipe);
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

		boolean onlyreg = true;

		if (line.hasOption(OPTION_INPUTFORMAT)) 
		{
			String value = line.getOptionValue(OPTION_INPUTFORMAT);
			if(value == "vcf")  {
				cmdoptions.add(OPTION_DASH + value);
			} else {
				System.out.println("Does not support files other than VCF");	
			}
		}

		if (line.hasOption(OPTION_OUTPUTFORMAT))  {
			
			String value = line.getOptionValue(OPTION_OUTPUTFORMAT);
					if(value == "vcf")  {
						cmdoptions.add(OPTION_DASH + value);
					} else {
						System.out.println("Does not support files other than VCF");
						
					}
		}

		if (line.hasOption(OPTION_SNP)) {
			cmdoptions.add(OPTION_DASH + OPTION_SNP);
		}

		if (line.hasOption(OPTION_NMP)) {
			System.out.println(" -nmp flag is not supported on current SNPEff Version");
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
			cmdoptions.add(OPTION_DASH + OPTION_NODOWNSTREAM);
		}

		if (line.hasOption(OPTION_NOINTERGENIC)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOINTERGENIC);
		}

		if (line.hasOption(OPTION_NOINTRON)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOINTRON) ;
		}

		if (line.hasOption(OPTION_NOUPSTREAM)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOUPSTREAM);
		}

		if (line.hasOption(OPTION_NOUTR)) {
			cmdoptions.add(OPTION_DASH + OPTION_NOUTR) ;
		}
       
		if (line.hasOption(OPTION_INTERVALFILE)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_INTERVALFILE + " " + line.getOptionValue(OPTION_INTERVALFILE));
		}
		
		if (line.hasOption(OPTION_MAXQ)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_MAXQ + " " + Integer.parseInt(line.getOptionValue(OPTION_MAXQ)));
		}
	
         if (line.hasOption(OPTION_MINQ)) {
			
			cmdoptions.add(OPTION_DASH + OPTION_MINQ + " " + Integer.parseInt(line.getOptionValue(OPTION_MINQ)));
		}
	
         if (line.hasOption(OPTION_MAXC)) {
 			
 			cmdoptions.add(OPTION_DASH + OPTION_MAXC + " " + Integer.parseInt(line.getOptionValue(OPTION_MAXC)));
 		}
 	
         if (line.hasOption(OPTION_MINC)) {
 			
 			cmdoptions.add(OPTION_DASH + OPTION_MINC + " " + Integer.parseInt(line.getOptionValue(OPTION_MINC)));
 		}
 	
			
           if (line.hasOption(OPTION_STATSFILE)) {
   			
   			cmdoptions.add(OPTION_DASH + OPTION_STATSFILE + " " + line.getOptionValue(OPTION_STATSFILE));
   		}
           
           if ( line.hasOption(OPTION_UPDOWNSTREAMLENGTH)) {
          	 
          	 cmdoptions.add(OPTION_DASH + OPTION_UPDOWNSTREAMLENGTH + " " + Integer.parseInt(line.getOptionValue(OPTION_UPDOWNSTREAMLENGTH)));
           }
   		
         if( line.hasOption(OPTION_ONLYCODING)) {
        	 
        	 cmdoptions.add(OPTION_DASH + OPTION_ONLYCODING + " " + Boolean.getBoolean(OPTION_ONLYCODING)); 
         }
         
     /** Black List Options..There is no need for this documentation since bior says unrecognized paramater and redirects towards help  
        
         if (line.hasOption(OPTION_BEDFILE)) {
        	 
        	 System.out.println(" -fi Currently bed file is not supported for filtering"); 
         }
         
         if(line.hasOption(OPTION_CHR)) {
        	 
        	 System.out.println("-chr Currently not supported");
         }
         
         if (line.hasOption(OPTION_ONLYREG)) {
        	 
        	 System.out.println("-onlyReg filter is not supported");
        	 
         }
         
         if(line.hasOption(OPTION_REGULATION)) {
        	 
        	 System.out.println("-reg <name> Option is not supported");
         }
         
        if (line.hasOption(String.valueOf(OPTION_OFFSET0))) {
        	 
        	 System.out.println("Offset options are not supported");
         }
		
          if (line.hasOption(String.valueOf(OPTION_OFFSET1))) {
        	 
        	 System.out.println("Offfset options are not supported");
         }
          
         if ( line.hasOption(OPTION_INOFFSET)) {
        	 
        	System.out.println("Offset options are not supported");
         }
		
           if ( line.hasOption(OPTION_OUTOFFSET)) {
        	 
        	 System.out.println("Offset options are not supported");
         } 
      
           **/
         
      	
	    
	
		
		
	    if (cmdoptions != null) {

		       return cmdoptions.toArray(new String[cmdoptions.size()]);
		
		  } else {
			
			   return null;
		  }
	}
}

