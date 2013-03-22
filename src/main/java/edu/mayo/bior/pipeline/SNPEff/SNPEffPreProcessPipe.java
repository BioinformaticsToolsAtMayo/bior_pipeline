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

    private int count = 0;
    @Override
    protected String processNextStart() throws NoSuchElementException {
        StringBuilder sb = new StringBuilder();
        History h = this.starts.next();
        if(count==0){
            sb.append("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO\n");
        }
        for(int i=0; i<7;i++){
            sb.append(h.get(i));
            sb.append("\t");
        }
        sb.append(".");
        count++;
        return sb.toString();
    }
    
}
