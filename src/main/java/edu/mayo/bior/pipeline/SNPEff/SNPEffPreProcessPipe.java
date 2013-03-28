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
    
    private int saveCol = 7;
    
    public SNPEffPreProcessPipe(){
        
    }
    
    public SNPEffPreProcessPipe(int colsToSave){
        saveCol = colsToSave;
    }

    private boolean isFirst = true;
    @Override
    protected String processNextStart() throws NoSuchElementException {
    	History history = this.starts.next();
        return getFirstSevenColumnsPlusDot(history);
    }
    

	private String getFirstSevenColumnsPlusDot(History history) {
            //System.err.println(history.toString());
            //if(history.size()<7){
            //    System.err.println(history.toString());
            //}
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<saveCol;i++){
			sb.append(history.get(i));
			sb.append("\t");
	    }
		sb.append(".");
		return sb.toString();
	}
}