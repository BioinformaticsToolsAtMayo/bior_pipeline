/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.cmd;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.tinkerpop.pipes.Pipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.bioinformatics.BED2JSONPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.metadata.Metadata.CmdType;

/**
 *
 * @author m102417
 */
public class BED2JSONCommand implements CommandPlugin {

    	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();
    	private String operation;
	
	public void init(Properties props) throws Exception {
		operation = props.getProperty("command.name");
	}

	public void execute(CommandLine line, Options opts) throws Exception {
		
		Metadata metadata = new Metadata(CmdType.ToTJson, operation);
		
		Pipe<String,  History>  preLogic  = new HistoryInPipe(metadata);
		Pipe<History, History>  logic     = new BED2JSONPipe();
		Pipe<History, String>   postLogic = new HistoryOutPipe();
		
		mPipeline.execute(preLogic, logic, postLogic);
	}
}
