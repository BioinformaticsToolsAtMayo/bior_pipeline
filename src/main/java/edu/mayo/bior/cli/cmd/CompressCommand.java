package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.CompressPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.FieldSpecification;
import edu.mayo.pipes.util.FieldSpecification.FieldDirection;

public class CompressCommand implements CommandPlugin
{
	private static final String DEFAULT_SEPARATOR = "|";
	private static final String DEFAULT_ESCAPE_SEPARATOR = "\\|";	

	private static final char OPTION_SEPARATOR = 's';	
	private static final char OPTION_ESCAPE    = 'e';	
	private static final char OPTION_REVERSE   = 'r';	
	private static final char OPTION_ALIGN     = 'a';	
		
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception
	{
	}

	public void execute(CommandLine line, Options opts) throws Exception
	{
		// get COLUMNS argument
		String fieldSpecStr = line.getArgs()[0];
		
		FieldDirection direction;
		if (line.hasOption(OPTION_REVERSE))
		{
			direction = FieldDirection.RIGHT_TO_LEFT;
		}
		else
		{
			direction = FieldDirection.LEFT_TO_RIGHT;
		}		
		FieldSpecification fieldSpec = new FieldSpecification(fieldSpecStr, direction);
		
		// grab from option flag
		String delimiter = DEFAULT_SEPARATOR;
		if (line.hasOption(OPTION_SEPARATOR))
		{
			delimiter = line.getOptionValue(OPTION_SEPARATOR);
		}
		
		String escapeDelimiter = DEFAULT_ESCAPE_SEPARATOR;
		if (line.hasOption(OPTION_ESCAPE))
		{
			escapeDelimiter = line.getOptionValue(OPTION_ESCAPE);
		}
		
		boolean useSetCompression = true;
		if (line.hasOption(OPTION_ALIGN))
		{
			useSetCompression = false;
		}

		CompressPipe compressPipe = new CompressPipe(fieldSpec, delimiter, escapeDelimiter, useSetCompression);

		Pipeline pipeline = new Pipeline(
				new HistoryInPipe(compressPipe.getMetadata()),
				compressPipe,
				new HistoryOutPipe()
				);
		
		mPipeline.execute(pipeline);
	}	
}
