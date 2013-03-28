/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.meta.BridgeOverPipe;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author m102417
 */
public class SNPEFFPipeline extends Pipeline {
    public SNPEFFPipeline(String[] command) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
        init(command, new IdentityPipe(), new IdentityPipe());
    }
    public SNPEFFPipeline(Pipe input, Pipe output) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
        init(null, input, output);
    }
    public SNPEFFPipeline(String[] command, Pipe input, Pipe output) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
        init(null, input, output);
    }
            
    public void init(String[] command, Pipe input, Pipe output) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
            SNPEFFEXE snp;
            if(command == null){
                snp = new SNPEFFEXE();
            }else{
                snp = new SNPEFFEXE(command);
            }
            Pipe exe = new TransformFunctionPipe(snp);
            SNPEffPostProcessPipeline ppp = new SNPEffPostProcessPipeline(true);           
            Pipe post = ppp.getSNPEffTransformPipe(true);
            Pipeline superviseMe = new Pipeline(
                    new SNPEffPreProcessPipe(),//history-string 
                    exe,//string-string
                    new VCFProgram2HistoryPipe(),//string-history
                    post //history-history
                    );
            BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new SNPEFFMerge());
            
            Pipeline p = new Pipeline(
                    input,//filename-string
                    new HistoryInPipe(),//string-history
                    bridge, //the BridgeOverPipe: history-history
                    output
                    //new PrintPipe()
            );
            this.setPipes(p.getPipes());
    }
    
}
