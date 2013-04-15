/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VCFProgramPipes;

import com.tinkerpop.pipes.AbstractPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *  Converts the raw output of some program that works on VCF to history objects
 * @author m102417
 */
public class VCFProgram2HistoryPipe extends AbstractPipe<String, History> {
    
    private String[] meta = null;
    private int count = 0;
    
    //do nothing special constructor
    public VCFProgram2HistoryPipe(){
        
    }
    
    /**
     * Add header construction to metadata
     * @param metadataLines - lines (prefixed with #
     */ 
    public VCFProgram2HistoryPipe(String[] metadataLines){
        meta = metadataLines;
    }

    private void loadMeta(History h){
        List<String> orgH = History.getMetaData().getOriginalHeader();
        for(String line : meta){
            orgH.add(line);
        }
    }
    
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
        //add a user injected header, if needed (mostly for pipes that follow as this will be thrown away in the VCFProgramMergePipe as of now
        if((count==0) && meta != null){ loadMeta(h); }//on the first row only do this
        count++;
        return h;
    }
    
}
