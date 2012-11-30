package edu.mayo.bior.cli.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.VCF2VariantPipeline;

public class VCF2VariantPipelineCommand implements CommandPlugin {

	private VCF2VariantPipeline mPipeline = new VCF2VariantPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, InputStream inStream, OutputStream outStream) throws Exception {
		mPipeline.execute();		
	}
}

