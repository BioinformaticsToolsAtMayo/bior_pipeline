/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.bioinformatics.BED2JSONPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 *
 * @author m102417
 */
public class BED2JSONCommand implements CommandPlugin {

    	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
	
	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {
		
		BED2JSONPipe pipe = new BED2JSONPipe();
		
		mPipeline.execute(pipe);		
	}
}
