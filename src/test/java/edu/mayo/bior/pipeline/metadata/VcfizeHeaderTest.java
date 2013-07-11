package edu.mayo.bior.pipeline.metadata;

import java.util.Arrays;

import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;

public class VcfizeHeaderTest {

	@Test
	public void testConvertToVCFPipe() throws Exception {
		Pipeline p = new Pipeline(
									new CatPipe(), 
									new HistoryInPipe(), 
									new VcfizeHeader(), 
									//new HistoryOutPipe(),
									new PrintPipe()
									);
		
		//p.setStarts(Arrays.asList("src/test/resources/vcfizer/validvcf.vcf"));
		p.setStarts(Arrays.asList("src/test/resources/metadata/2_bior_withMetadata.vcf"));
			
		
		while(p.hasNext()) {			
			p.next();
		}
		//System.out.println("2......");
	}
}
