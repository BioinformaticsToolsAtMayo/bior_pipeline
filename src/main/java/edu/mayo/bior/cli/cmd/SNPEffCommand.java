package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;

public class SNPEffCommand implements CommandPlugin{
	
	
	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	@Override
	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(CommandLine line, Options opts) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	

}
