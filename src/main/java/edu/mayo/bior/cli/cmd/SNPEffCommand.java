package edu.mayo.bior.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;

public class SNPEffCommand implements CommandPlugin{

	private static final char OPTION_INPUTFORMAT= 'i';
	private static final String OPTION_ONLYREG = "onlyReg" ;
	private static final String OPTION_HOMO = "hom";
	private static final String OPTION_SNP = "snp";
	private static final String OPTION_HET = "het";
	private static final String OPTION_DELETIONS = "del";
	private static final String OPTION_INSERTIONS = "ins";
	private static final String OPTION_MINQ = "minQ";
	private static final String OPTION_MAXQ = "maxQ" ;
	private static final String OPTION_MINC = "minC";
	private static final String OPTION_MAXC = "maxC";
	private static final String OPTION_NMP = "nmp";
	private static final String OPTION_NODOWNSTREAM = "no-downstream";
	private static final String OPTION_NOINTERGENIC = "no-integenic";
	private static final String OPTION_NOINTRON = "no-intron";
	private static final String OPTION_NOUPSTREAM = "no-upstream";
	private static final String OPTION_NOUTR = "no-utr";
	private static final String OPTION_ONLYCODING = "onlyCoding";
	private static final int OPTION_OFFSET0 = 0;
	private static final int OPTION_OFFSET1 = 1;
	private static final char OPTION_OUTPUTFORMAT = 'o';
	private static final String OPTION_REGULATION = "reg" ; /* Value */ 
	private static final String OPTION_UPDOWNSTREAMLENGTH = "ud" ;
	private static final String OPTION_INOFFSET = "if";
	private static final String OPTION_OUTOFFSET = "of" ;
	private static final char OPTION_STATSFILE = 's';   

	/* BLACK LIST */
	private static final String OPTION_INTERVALFILE = "interval";
	private static final String OPTION_BEDFILE = "fi" ;
	private static final String OPTION_CHR = "chr";

	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	public void init(Properties props) throws Exception {
	}


	public void execute(CommandLine line, Options opts) throws Exception {

		boolean onlyreg = true;

		List<String> cmdoptions = new ArrayList<String>();

		if (line.hasOption(OPTION_INPUTFORMAT)) 
		{
			String value = line.getOptionValue(OPTION_INPUTFORMAT);
			if(value == "vcf")  {
				cmdoptions.add(value);
			} else {
				System.out.println("Does not support files other than VCF");	
			}
		}

		if (line.hasOption(OPTION_OUTPUTFORMAT))  {
			// String value = line.getOptionValue(OPTION_OUTPUTFORMAT)
			cmdoptions.add(line.getOptionValue(OPTION_OUTPUTFORMAT));
		}

		if (line.hasOption(OPTION_SNP)) {
			cmdoptions.add(OPTION_SNP);
		}

		if (line.hasOption(OPTION_NMP)) {
			cmdoptions.add(OPTION_NMP);
		}
		
		if (line.hasOption(OPTION_HET)) {
			cmdoptions.add(OPTION_HET);
		}

		if (line.hasOption(OPTION_HOMO)) {
			cmdoptions.add(OPTION_HOMO);
		}

		if (line.hasOption(OPTION_DELETIONS)) {
			cmdoptions.add(OPTION_DELETIONS);
		}

		if (line.hasOption(OPTION_INSERTIONS)) {
			cmdoptions.add(OPTION_INSERTIONS);
		}

		if (line.hasOption(OPTION_NODOWNSTREAM)) {
			cmdoptions.add(OPTION_NODOWNSTREAM);
		}

		if (line.hasOption(OPTION_NOINTERGENIC)) {
			cmdoptions.add(OPTION_NOINTERGENIC);
		}

		if (line.hasOption(OPTION_NOINTRON)) {
			cmdoptions.add(OPTION_NOINTRON) ;
		}

		if (line.hasOption(OPTION_NOUPSTREAM)) {
			cmdoptions.add(OPTION_NOUPSTREAM);
		}

		if (line.hasOption(OPTION_NOUTR)) {
			cmdoptions.add(OPTION_NOUTR) ;
		}
       
		SNPEFFPipeline snpeffPipe = null;
		
		if (cmdoptions != null) {
			
		
		System.out.println(cmdoptions.toString());  
		
		String[] cmdops = (String[]) cmdoptions.toArray(new String[cmdoptions.size()]);
		
		snpeffPipe = new SNPEFFPipeline(cmdops);
    
		} else
			snpeffPipe = new SNPEFFPipeline(null);
		
		mPipeline.execute(snpeffPipe);

		// tell SNPEFF we're done
		snpeffPipe.terminate();
	}
}
