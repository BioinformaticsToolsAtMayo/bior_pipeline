/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.google.gson.Gson;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipeline {
    
    	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * @param logic A Pipe that takes a HISTORY as input and output.
	 */
	public void execute(Pipe<History, History> logic) {
				
		// pipes
		InputStreamPipe	in 		= new InputStreamPipe();
		HistoryInPipe	historyIn = new HistoryInPipe();
		HistoryOutPipe	historyOut = new HistoryOutPipe();
		PrintPipe		print	= new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					//in,			// each STDIN line	--> String
					historyIn,	// String			--> history
					logic,		// history			--> modified history*
					historyOut,	// history*			--> String
					print		// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
                pipeline.setStarts(Arrays.asList(System.in));

                // run pipeline
                while (pipeline.hasNext()) {
                        pipeline.next();
                }		
	}
        
        public static class FixSiftandPolyphen implements PipeFunction<History,History> {
            public String parseTerm(String s){
                return s;
            }
            public int parseScore(String s){
                return 0;
            }
            Gson gson = new Gson();
            public History compute(History history) {
                    //just lop off the last column... could extend later...
                    String json = history.get(history.size()-1);
                    HashMap hm = gson.fromJson(json, HashMap.class);
                    if(hm.containsKey("SIFT")){
                        System.out.println(hm.get("SIFT"));
                    }
                    String toJson = gson.toJson(hm);
                    return history;
            }   
        }    
    
}
