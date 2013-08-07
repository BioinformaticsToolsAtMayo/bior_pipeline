package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

public class VCF2VariantCommand implements CommandPlugin
{
	
	private UnixStreamPipeline pipeline = new UnixStreamPipeline();
	
	private String operation;
	
	public void init(Properties props) throws Exception
	{
		operation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception
	{
		Metadata metadata = new Metadata(operation);
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
		Pipe<History, History>  logic     = new VCF2VariantPipe();
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		pipeline.execute(preLogic, logic, postLogic);
	}
}