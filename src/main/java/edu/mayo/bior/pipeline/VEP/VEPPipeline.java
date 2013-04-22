/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgramMerge;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgramPreProcessPipe;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.meta.BridgeOverPipe;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author m102417
 */
public class VEPPipeline extends Pipeline {
    
        public VEPPipeline(String[] command) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, new IdentityPipe(), new IdentityPipe(), true);
	}
    
	public VEPPipeline(String[] command, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, new IdentityPipe(), new IdentityPipe(), pickworst);
	}
	public VEPPipeline(Pipe input, Pipe output, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(VEPEXE.getVEPCommand(null), input, output, pickworst);
	}
	public VEPPipeline(String[] command, Pipe input, Pipe output, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		init(command, input, output, pickworst);
	}

	private VEPEXE vep;
	public void init(String[] command, Pipe input, Pipe output, boolean pickworst) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{            
            //execution
            vep = new VEPEXE(command);
            Pipe exe = new TransformFunctionPipe(vep);
            
            String[] vepHeader = new String[1];
            vepHeader[0] = "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE\">";

            //post processing
            VEPPostProcessingPipeline ppp = new VEPPostProcessingPipeline();
            Pipe post;
            if(pickworst){
                post = ppp.getWorstCasePipeline(new IdentityPipe(), new IdentityPipe(), false);
            }else {
                post = ppp.getCartesianProductPipeline(new IdentityPipe(), new IdentityPipe(),false);
            }

            Pipeline superviseMe = new Pipeline(
				new VCFProgramPreProcessPipe(),//history-string 
				exe,//string-string
				new VCFProgram2HistoryPipe(vepHeader),//string-history
				post //history-history
                        //new IdentityPipe()
				);
            BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new VCFProgramMerge("VEP"));

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
        vep.terminate();
    }
    
}
