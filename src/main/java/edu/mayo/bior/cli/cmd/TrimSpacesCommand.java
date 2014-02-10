package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.transform.IdentityPipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.string.TrimSpacesPipe;

public class TrimSpacesCommand implements CommandPlugin
{
	private String mOperation;

	
	public void init(Properties props) throws Exception	{
		mOperation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception
	{
		TrimSpacesPipe trimSpacesPipe = new TrimSpacesPipe();
		UnixStreamPipeline unixPipe = new UnixStreamPipeline();
		unixPipe.execute(
				new IdentityPipe(),
				trimSpacesPipe,
				new IdentityPipe()
				);
	}	
}
