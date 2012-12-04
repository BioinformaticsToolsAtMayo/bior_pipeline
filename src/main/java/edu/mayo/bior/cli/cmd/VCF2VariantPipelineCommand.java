package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;

public class VCF2VariantPipelineCommand implements CommandPlugin {

	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {
		
		VCF2VariantPipe pipe = new VCF2VariantPipe();
		
		mPipeline.execute(pipe);		
	}
}

