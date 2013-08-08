package edu.mayo.bior.pipeline.VEP;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import com.google.gson.JsonArray;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.history.History;

/**
 * @author Michael Meiners (m054457)
 * Date created: Apr 21, 2013
 */
public class VEPPipeline  extends Pipeline {

	private VEPEXE mVepExe = null;
	private boolean mIsWorstCaseOnly = true;
	private Queue<History>  mLastLineQ = new ArrayDeque<History>(); 
	
	public VEPPipeline(String[] command, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{            
        mIsWorstCaseOnly = pickworst;
        mVepExe = new VEPEXE(VEPEXE.getVEPCommand(new String[] {"1"} ));
        Pipeline pipeline = new Pipeline(
        		new TransformFunctionPipe(new VepPrePipe()),
        		new TransformFunctionPipe(mVepExe),
        		new TransformFunctionPipe(new VepPostPipe())
        		);
        this.setPipes(pipeline.getPipes());
	}
	
	class VepPrePipe implements PipeFunction<History,String> {
		/** Save the input line to a queue and strip off all columns after column 7 */
		public String compute(History hist) {
			mLastLineQ.add((History) hist.clone());
			for(int i=hist.size()-1; i > 6; i--) 
				hist.remove(i);
			return hist.getMergedData("\t");
		}
	}

	class VepPostPipe implements PipeFunction<String,History> {
		private VepFunctions mVepFunctions = new VepFunctions();
		
		/** Create a json array from VEP output, convert it to array, and if mIsWorstCaseOnly is set, 
		 *  then return the worst element from the array. 
		 * 	If there are no scores to compare, then return empty json. */
		public History compute(String histFromVep) {
			String vepCsq = histFromVep.split("\t")[7];
			JsonArray csqAsJsonArray = mVepFunctions.vepCsqToJsonList(vepCsq);
			String vepOut = mIsWorstCaseOnly 
					? mVepFunctions.getWorstCase(csqAsJsonArray).toString() 
					: "{\"CSQ\":" + csqAsJsonArray.toString() + "}";
			History original = mLastLineQ.remove();
			original.add(vepOut);
			return original;
		}
	}

	// NOTE: Make sure to call this terminate() method when the pipe is finished!!!!!
    public void terminate() throws InterruptedException{
        mVepExe.terminate();
    }
}
