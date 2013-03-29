/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.History;
import java.util.NoSuchElementException;

/**
 *
 * @author m102417
 */
public class SNPEffPreProcessPipe extends AbstractPipe<History, String> {
    
    private int mNumColsToSave = 7;
    
    public SNPEffPreProcessPipe(){
        
    }
    
    public SNPEffPreProcessPipe(int colsToSave){
    	mNumColsToSave = colsToSave;
    }

    private boolean isFirst = true;
    @Override
    protected String processNextStart() throws NoSuchElementException {
    	History history = this.starts.next();
        return getFirstSevenColumnsPlusDot(history);
    }
    

	private String getFirstSevenColumnsPlusDot(History history) {
		// Skip line if it has 0 cols - may be end line
		if(history.size() == 0)
			throw new NoSuchElementException("Blank line encountered - assuming end of input stream");
		// Throw exception 
		else if(history.size() < mNumColsToSave)
			throw new InvalidPipeInputException("Number of columns in input file was less than " + mNumColsToSave, this);

		StringBuilder sb = new StringBuilder();
		for(int i=0; i<mNumColsToSave;i++){
			sb.append(history.get(i));
			sb.append("\t");
	    }
		sb.append(".");
		return sb.toString();
	}
}