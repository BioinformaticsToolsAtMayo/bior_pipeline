package edu.mayo.bior.pipeline;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.SplitPipe;
import edu.mayo.pipes.JSON.DrillPipe;

/**
 * Pipeline that takes a history containing JSON as the last column and "drills"
 * out one or more JSON values.
 */
public class DrillPipeline {

	public void execute(boolean keepJSON, String[] paths) {
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		SplitPipe 		split 	= new SplitPipe("\t");
		DrillPipe		drill	= new DrillPipe(keepJSON, paths);
		MergePipe		merge 	= new MergePipe("\t");
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,		// each STDIN line	--> String 
					split,	// each String		-->	history created
					drill,	// history			--> history (w/ drilled cols)
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
