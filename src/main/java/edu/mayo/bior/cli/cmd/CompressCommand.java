package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.CompressPipe;
import edu.mayo.pipes.util.FieldSpecification;

public class CompressCommand implements CommandPlugin
{
	private static final String DEFAULT_SEPARATOR = "|";

	private static final char OPTION_SEPARATOR = 's';	
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception
	{
	}

	public void execute(CommandLine line, Options opts) throws Exception
	{
		// get COLUMNS argument
		String fieldSpecStr = line.getArgs()[0];
		FieldSpecification fieldSpec = new FieldSpecification(fieldSpecStr);
		
		// grab from option flag
		String delimiter = DEFAULT_SEPARATOR;
		if (line.hasOption(OPTION_SEPARATOR))
		{
			delimiter = line.getOptionValue(OPTION_SEPARATOR);
		}
		
		CompressPipe pipe = new CompressPipe(fieldSpec, delimiter);;
		 
		
		mPipeline.execute(pipe);		
	}	
}
