/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.ExecPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.aggregators.AggregatorPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author m102417
 */
public class SNPEffPipeline {
    
    public static void main(String[] args) throws IOException{
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        SNPEffPipeline effp = new SNPEffPipeline();
        
        String[] command = {"java", 
                            "-Xmx4g", 
                            "-jar", 
                            "snpEff/snpEff.jar eff",
                            "-c",
                            "snpEff/snpEff.config",
                            "-v",
                            "GRCh37.68",
                            "/dev/stdin",
                            ">",
                            "/dev/stdout"
        };
        ExecPipe exe = new ExecPipe(command, true);
        
        Pipe p = effp.getSNPEffPipeline(new CatPipe(), exe, new VCF2VariantPipe(), new PrintPipe());
        //Pipe p = new Pipeline(new CatPipe(), new PrintPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        while(p.hasNext()){
            p.next();
        }
    }
    

    /**
     * 
     * @param input - the pipe the input is comming from
     * @param transform - either we are taking VCF and we need to use VCF2VariantPipe() to transform it, or we have json variants as the last column
     * @param output - the pipe for where you want the output to go
     * @return 
     */
    public Pipe getSNPEffPipeline(Pipe input, Pipe exe, Pipe transform, Pipe output){
            String[] drillPath = new String[1];
            drillPath[0]= "INFO.CSQ";
            DrillPipe drill = new DrillPipe(false, drillPath);
            Pipe fixun = new TransformFunctionPipe<History,History>(new FixSNPEff());
            Pipe p = new Pipeline(
                input,//the input file
                new AggregatorPipe(5),
                //new HistoryInPipe(),
                //new VCF2VariantPipe(), 
                //new FindAndReplaceHPipe(8,"CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
                //drill,
                //fixun,
                exe,
                //new HistoryOutPipe(),
                output
                    );
            return p;
    }
            
    public static class FixSNPEff implements PipeFunction<History, History> {

        @Override
        public History compute(History a) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
    
}
