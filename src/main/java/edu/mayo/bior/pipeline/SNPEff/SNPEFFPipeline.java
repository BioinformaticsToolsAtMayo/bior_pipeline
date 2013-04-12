/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import edu.mayo.exec.AbnormalExitException;

import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.meta.BridgeOverPipe;

/**
 *
 * @author m102417
 */
public class SNPEFFPipeline extends Pipeline {
        public SNPEFFPipeline(String[] command) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, new IdentityPipe(), new IdentityPipe(), true);
	}
    
	public SNPEFFPipeline(String[] command, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, new IdentityPipe(), new IdentityPipe(), pickworst);
	}
	public SNPEFFPipeline(String[] command, Pipe input, Pipe output, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, input, output, pickworst);
	}

	private SNPEFFEXE snp;
	private void init(String[] command, Pipe input, Pipe output, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		//if(command == null){
	//		snp = new SNPEFFEXE();
		//}else{
			snp = new SNPEFFEXE(command);
	//	}
		Pipe exe = new TransformFunctionPipe(snp);
		SNPEffPostProcessPipeline ppp = new SNPEffPostProcessPipeline(pickworst);           
		Pipe post = ppp.getSNPEffTransformPipe(pickworst);
		Pipeline superviseMe = new Pipeline(
				new SNPEffPreProcessPipe(),//history-string 
				exe,//string-string
				new VCFProgram2HistoryPipe(),//string-history
				post //history-history
				);
		BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new SNPEFFMerge());

		Pipeline p = new Pipeline(
				input,//??-history better convert it to a history comming into the pipeline e.g. HistoryInPipe
				bridge, //the BridgeOverPipe: history-history
				output
				//new PrintPipe()
				);
		this.setPipes(p.getPipes());
	}
	
	
	// NOTE: Make sure to call this terminate() method when the pipe is finished!!!!!
    public void terminate() throws InterruptedException{
        snp.terminate();
    }
}
