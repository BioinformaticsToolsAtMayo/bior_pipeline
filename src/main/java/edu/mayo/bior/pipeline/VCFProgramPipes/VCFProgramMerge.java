/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VCFProgramPipes;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.pipeFunctions.StitchPipeFunction;

/**
 *
 * @author m102417
 */
public class VCFProgramMerge implements StitchPipeFunction<History,History,History> {
    private int mCount = 0;
    private String mProgram = "UNKNOWN"; //SNPEff or vep or whatever
    
    /**
     * default constructor will c
     */
    public VCFProgramMerge(){
        
    }
    
    public VCFProgramMerge(String programName){
    	mProgram = programName;
    }
    /**
     * Merge the two lists back together using whatever logic is needed (logic goes int the compute() method).
     * @param a - the array list coming out of the pipe
     * @param b - the array list before it went into the pipe
     * @return 
     */
    
    public History compute(History a, History b) {
    	// NOTE: adding the "program" column on the metadata resulted in TWO "VEP" column names
        if(mCount==0){
            ColumnMetaData cmd = new ColumnMetaData(mProgram);
            History.getMetaData().getColumns().add(cmd);
        }
        mCount++;
        String lastFromA = a.get(a.size()-1);
        b.add(lastFromA);
        return b;
    }
}
