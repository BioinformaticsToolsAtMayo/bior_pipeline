/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.SplitPipe;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author m102417
 */
public class OverlapPipeline {
    
    	public void execute(String tabixFile) throws IOException {
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		SplitPipe 		split 	= new SplitPipe("\t");
		OverlapPipe		op	= new OverlapPipe(tabixFile);
		MergePipe		merge 	= new MergePipe("\t");
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,		// each STDIN line	--> String 
					split,	// each String		-->	history created, assume last column is valid JSON
					op,	// history			--> history (w/ query results in last column... note this could 'fan out')
					merge,	// history			--> String
					print	// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
        pipeline.setStarts(Arrays.asList(System.in));

        // run pipeline
        while (pipeline.hasNext()) {
        	pipeline.next();
        }
		
	}
    
}
