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

/**
 * This pipeline can be used to expose a Pipe to behave like a UNIX command 
 * that operates with STDIN and STDOUT text streams.  To make this possible,
 * this implementation will:
 * 
 *  <ol>
 *  <li>deserialize a HISTORY from the incoming text stream from STDIN</li>
 *  <li>invoke the given Pipe to do some business logic on the HISTORY</li>
 *  <li>serialize a HISTORY into an outgoing text stream to STDOUT</li>
 *  </ol>
 * 
 * STDIN --> HISTORY --> LOGIC PIPE --> HISTORY* --> STDOUT
 * </br>
 * </br>
 * 
 * NOTE: It is required that the given Pipe has a HISTORY as input and output.
 * 
 */
public class UnixStreamPipeline {

	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * @param logic A Pipe that takes a HISTORY as input and output.
	 */
	public void execute(Pipe logic) {
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		SplitPipe 		split 	= new SplitPipe("\t");
		MergePipe		merge 	= new MergePipe("\t");
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,		// each STDIN line	--> String 
					split,	// each String		-->	history created
					logic,	// history			--> modified history*
					merge,	// history*			--> String
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
