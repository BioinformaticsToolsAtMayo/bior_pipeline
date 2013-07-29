package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;

import edu.mayo.bior.pipeline.VCFGeneratorPipe;

public class VCFGeneratorCommand implements CommandPlugin {

private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {
		
		VCFGeneratorPipe pipe = new VCFGeneratorPipe();
		
		mPipeline.execute(pipe);		
	}
}
