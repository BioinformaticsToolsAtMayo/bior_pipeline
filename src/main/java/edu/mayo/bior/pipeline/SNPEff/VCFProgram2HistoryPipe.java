/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.AbstractPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * @author m102417
 */
public class VCFProgram2HistoryPipe extends AbstractPipe<String, History> {

    public History histAppend(String[] tokens){
        History h = new History();
        for(int i=0; i<tokens.length; i++){
            h.add(tokens[i]);
        }
        return h;
    }

    @Override
    protected History processNextStart() throws NoSuchElementException {
        StringBuilder sb = new StringBuilder();
        String line = this.starts.next();
        History h = null;
        if (line != null) {
        if(line.startsWith("#")){
            //there is header intermingled with data...
            if(line.contains("\n")){
                String[] split = line.split("\n");
                for(int i=0; i<split.length; i++){
                    //remove the header and get the data
                    if(split[i].startsWith("#")){
                        ;//throw it away
                    }else{
                        h = histAppend(split[i].split("\t"));//this should just happen once
                    }
                }
            }
        }else{//no newlines... create the history out of the line directly
                h = histAppend(line.split("\t"));
        }
        
        }
        if(h == null){
            return processNextStart();
        }

        return h;
    }
    
}
