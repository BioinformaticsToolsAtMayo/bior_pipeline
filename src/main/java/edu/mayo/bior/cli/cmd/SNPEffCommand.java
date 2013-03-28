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
	
	private static final char OPTION_INPUTFILE= 'i';
	private static final String OPTION_ONLYREG = "onlyReg" ;
	private static final String OPTION_HOMO = "hom";
	private static final char OPTION_OUTPUTPATH = 'p';
    private static final char OPTION_DRILL_COLUMN = 'c';
	
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	public void init(Properties props) throws Exception {
	       
		
		
	}

	
	public void execute(CommandLine line, Options opts) throws Exception {
		
		boolean onlyreg = true;
		
		List<String> cmdoptions = new ArrayList<String>();
		
		if (line.hasOption(OPTION_INPUTFILE))
		    {
			 String value = line.getOptionValue("OPTION_INPUTFILE");
		     cmdoptions.add(value);	
			}
		
		
		if (line.hasOption(OPTION_OUTPUTPATH)) 
		     {
			
			  cmdoptions.add(line.getOptionValue(OPTION_OUTPUTPATH));
			
		     }
		
		
		
		
		SNPEFFPipeline snpeffPipe = new SNPEFFPipeline(null);
		
		 mPipeline.execute(snpeffPipe);
		
	}
	
	

}
