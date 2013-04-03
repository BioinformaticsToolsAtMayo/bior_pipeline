/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.pipeFunctions.StitchPipeFunction;

/**
 *
 * @author m102417
 */
public class SNPEFFMerge implements StitchPipeFunction<History,History,History> {
    private int count = 0;
    /**
     * Merge the two lists back together using whatever logic is needed (logic goes int the compute() method).
     * @param a - the array list coming out of the pipe
     * @param b - the array list before it went into the pipe
     * @return 
     */
    
    public History compute(History a, History b) {
        if(count==0){
            ColumnMetaData cmd = new ColumnMetaData("SNPEff");
            History.getMetaData().getColumns().add(cmd);
        }
        count++;
        String r = a.get(a.size()-1);
        b.add(r);
        return b;
    }
}
