package edu.mayo.bior.pipeline;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.cli.InvalidDataException;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;

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

	private static Logger sLogger = Logger.getLogger(UnixStreamPipeline.class);
	
        
        
        public void execute(Pipe<History, History> logic) throws InvalidDataException {
            HistoryInPipe	historyIn = new HistoryInPipe();
            HistoryOutPipe	historyOut = new HistoryOutPipe();
            execute(historyIn, logic, historyOut);
        }
	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * @param logic A Pipe that takes a HISTORY as input and output.
	 */
	public void execute(Pipe inMeta, Pipe<History, History> logic, Pipe outMeta) throws InvalidDataException {				
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,			// each STDIN line	--> String
					inMeta,	// String			--> history
					logic,		// history			--> modified history*
					outMeta,	// history*			--> String
					print		// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
        pipeline.setStarts(Arrays.asList(System.in));

        // track how many data rows encounter an error
        int invalidDataErrorCnt = 0;        
        
        // run pipeline
        boolean hasNext = true;
        while (hasNext) {
            try {
            	
            	pipeline.next();
            	
            } catch (NoSuchElementException e) {
            	
            	// reached the end
            	hasNext = false;
            	
            } catch (InvalidPipeInputException e) {
            	invalidDataErrorCnt++;
            	sLogger.error(e.getMessage());            	
            }        	
        }
        
        if (invalidDataErrorCnt > 0) {
        	String mesg = 
        		String.format(
        			"WARNING: Found %s data error(s).  Not all input data rows could be successfully processed.", 
        			String.valueOf(invalidDataErrorCnt)
        		);
        	throw new InvalidDataException(mesg);
        }
	}
}
