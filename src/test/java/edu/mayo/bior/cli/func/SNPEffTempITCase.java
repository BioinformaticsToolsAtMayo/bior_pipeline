package edu.mayo.bior.cli.func;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.SNPEff.SNPEffPostProcessPipeline;
import edu.mayo.pipes.ExecPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;

public class SNPEffTempITCase {

	//@Test
	public void test_ExecPipeOutput_with_SNPEffPostProcess() throws Exception {

		String snpeffpath = "/data/snpEff/snpEff_2_0_5/snpEff.jar";
        String snpeffconfig = "/data/snpEff/snpEff_2_0_5/snpEff.config";
        Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();

        String[] cmdArray = {"java", 
            "-Xmx4g", 
             "-jar", 
             snpeffpath,
             "eff",
             "-c",
             snpeffconfig,
             "-v",
             "GRCh37.64",
             "-o",
             "vcf",
             "-noLog",
             "-noStats"
             //">",
             //"/tmp/treatSNPEff.vcf"
             //"/dev/stdout"
         };

        SNPEffPostProcessPipeline effp = new SNPEffPostProcessPipeline(true);
        
		ExecPipe exec = new ExecPipe(cmdArray);
		
		Pipe snpPipe = effp.getSNPEffPostProcessPipeline(new CatPipe(), new IdentityPipe());
				
		Pipe mainPipe = new Pipeline(new CatPipe(), exec, snpPipe, new PrintPipe());
		mainPipe.setStarts(Arrays.asList(""));
		
		for(int i=0;mainPipe.hasNext();i++) {
			System.out.println(mainPipe.next());
		}
	}
}
