/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

/**
 *
 * @author dquest
 */

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

/**
 *
 * @author m102417
 */
public class AlleleFrequenciesPipeline extends Pipeline {
    
        public AlleleFrequenciesPipeline() {
		init(new IdentityPipe(), new IdentityPipe());
	}
    
	public AlleleFrequenciesPipeline(Pipe input, Pipe output){
		init(input, output);
	}

	public void init(Pipe input, Pipe output) {            
            Pipeline p = new Pipeline(
				input,//??-history better convert it to a history comming into the pipeline e.g. HistoryInPipe
				 //logic - all the pipes that do the work: history-history
				output
				//new PrintPipe()
				);
            this.setPipes(p.getPipes());
	}
              
    
}
