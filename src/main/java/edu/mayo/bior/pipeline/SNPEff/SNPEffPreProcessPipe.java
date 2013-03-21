/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.AbstractPipe;
import java.util.NoSuchElementException;

/**
 *
 * @author m102417
 */
public class SNPEffPreProcessPipe extends AbstractPipe<String, String> {

    @Override
    protected String processNextStart() throws NoSuchElementException {
        StringBuilder sb = new StringBuilder();
        String next = this.starts.next();
        while(next.startsWith("#")){
            sb.append(next);
            sb.append("\n");
            next = this.starts.next();
        }
        sb.append(next);
        return sb.toString();
    }
    
}
