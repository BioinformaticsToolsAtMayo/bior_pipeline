package edu.mayo.bior.pipeline;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.test.PipeTestUtils;

/*
 * Tests if VCFGeneratorPipe is converting a tab BIOR generated data into VCF and headers are added
 * 
 */
public class VCFGeneratorPipeTest {
	
	//@Test
	public void testVCFGeneratorPipe() {
		
		List<String> input = Arrays.asList(
	    		"##Header start",
	    		"#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO\tBIOR.SNPeff.Effect",
	    		"1\t10144\trs144773400\tTA\tT\t.\t.\t.\tDeleterious"
	    	);
		
		Pipeline pipe = new Pipeline(
	    		new HistoryInPipe(),
	    		new VCFGeneratorPipe(),    		
	    		new HistoryOutPipe(),
	    		new PrintPipe()
	    		);
		
		pipe.setStarts(input);
    	//pipe.setStarts(Arrays.asList("src/test/resources/testData/metadata/validvcf.vcf"));
    	    	    	
    	List<String> actual = PipeTestUtils.getResults(pipe);
    	System.out.println("Actual=\n"+Arrays.asList(actual));
		
		
	}

}
