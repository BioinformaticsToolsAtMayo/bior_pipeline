/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.AbstractPipe;
import edu.mayo.pipes.history.History;
import java.util.NoSuchElementException;

/**
 *
 * @author m102417
 */
public class SNPEffPreProcessPipe extends AbstractPipe<History, String> {

    private boolean isFirst = true;
    @Override
    protected String processNextStart() throws NoSuchElementException {
    	String result = "";
    	if(isFirst) {
    		 result = "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO";
    		 isFirst = false;
    	} else {
    		History history = this.starts.next();
    		result = getFirstSevenColumnsPlusDot(history);
    	}
        return result;
    }
    

	private String getFirstSevenColumnsPlusDot(History history) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<7;i++){
			sb.append(history.get(i));
			sb.append("\t");
	    }
		sb.append(".");
		return sb.toString();
	}
}