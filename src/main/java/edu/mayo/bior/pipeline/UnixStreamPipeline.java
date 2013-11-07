package edu.mayo.bior.pipeline;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
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
 * that operates with STDIN and STDOUT text streams.
 */
public class UnixStreamPipeline
{

	private static Logger sLogger = Logger.getLogger(UnixStreamPipeline.class);
	
	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * This method will:
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
	 * @param logic
	 * 		A Pipe that takes a HISTORY as input and output.
	 * 
	 * @throws InvalidDataException
	 */
    public void execute(Pipe<History, History> logic) throws InvalidDataException
    {
        HistoryInPipe	historyIn = new HistoryInPipe();
        HistoryOutPipe	historyOut = new HistoryOutPipe();
        execute(historyIn, logic, historyOut);
    }
    
	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * STDIN --> PRE LOGIC PIPE --> LOGIC PIPE --> POST LOGIC PIPE --> STDOUT
	 * </br>
	 * </br>
	 * 
	 * NOTE: It is required that the given PRE LOGIC PIPE produce an output
	 * compatible with what the LOGIC PIPE expects as input.  Likewise, the
	 * LOGIC pipe is required to produce an output compatible with what the
	 * POST LOGIC PIPE expects as input.  If a PRE or POST pipe is not needed, 
	 * use an {@link IdentityPipe} as a placeholder.
	 * 
	 * @param preLogic
	 * 		A pipe that runs directly before the logic pipe.  This pipe must take
	 * 		a {@link String} as input.
	 * @param logic
	 * 		A Pipe that performs the business logic.
	 * @param postLogic
	 * 		A pipe that runs directly after the logic pipe.  This pipe must produce
	 * 		a {@link String} as output.
	 * 
	 * @throws InvalidDataException
	 */
	public void execute(Pipe preLogic, Pipe logic, Pipe postLogic) throws InvalidDataException
	{				
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,			// each STDIN line	--> String
					preLogic,	// String			--> history
					logic,		// history			--> modified history*
					postLogic,	// history*			--> String
					print		// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
        pipeline.setStarts(Arrays.asList(System.in));

        // track how many data rows encounter an error
        int invalidDataErrorCnt = 0;        
        
        // run pipeline
        boolean hasNext = true;
        //int line = 1;
        while (hasNext)
        {
            try
            {
            	//System.err.println("XXXXX: Line " + line++);
            	pipeline.next();            	
            }
            catch (NoSuchElementException e)
            {            	
            	// reached the end
            	hasNext = false;            	
            }
            catch (InvalidPipeInputException e)
            {
            	invalidDataErrorCnt++;
            	sLogger.error(e.getMessage());            	
            }        	
        }
        
        if (invalidDataErrorCnt > 0)
        {
        	String mesg = 
        		String.format(
        			"WARNING: Found %s data error(s).  Not all input data rows could be successfully processed.", 
        			String.valueOf(invalidDataErrorCnt)
        		);
        	throw new InvalidDataException(mesg);
        }
	}
}
