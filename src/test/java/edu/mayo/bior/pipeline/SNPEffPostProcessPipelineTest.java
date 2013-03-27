package edu.mayo.bior.pipeline;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.SNPEff.SNPEffPostProcessPipeline;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;

public class SNPEffPostProcessPipelineTest {
	
	@Test
	public void testPipeline_mostSignificantEffect() throws Exception {
		System.out.println("Testing SNPEffPostProcessPipelineTest.testPipeline_mostSignificantEffect()..");

		String inputFile = "src/test/resources/tools/snpeff/snpEffOutput205.vcf";
		
		SNPEffPostProcessPipeline effp = new SNPEffPostProcessPipeline(true);
		
		String output = "{\"Effect\":\"SYNONYMOUS_CODING\",\"Effect_impact\":\"LOW\",\"Functional_class\":\"SILENT\",\"Codon_change\":\"gtA/gtG\",\"Amino_acid_change\":\"V98\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"}";
				
		Pipe p = effp.getSNPEffPostProcessPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList(inputFile));
        
        String result = "";
        
        for(int i=0; p.hasNext(); i++){
        	History history = (History) p.next();
        	result = history.get(8);        	
        	if (i==1) break;        	
        }
        
   		assertEquals(output, result);
	}
	
	@Test
	public void testPipeline_allEffects() throws Exception {
		System.out.println("Testing SNPEffPostProcessPipelineTest.testPipeline_testPipeline_allEffects()..");

		String inputFile = "src/test/resources/tools/snpeff/snpEffOutput205.vcf";		
		SNPEffPostProcessPipeline effp = new SNPEffPostProcessPipeline(false);        
		String output = "{\"EFF\":[{\"Effect\":\"SYNONYMOUS_CODING\",\"Effect_impact\":\"LOW\",\"Functional_class\":\"SILENT\",\"Codon_change\":\"gtA/gtG\",\"Amino_acid_change\":\"V98\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"},{\"Effect\":\"UPSTREAM\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Gene_name\":\"RP11-54O7.11\",\"Gene_bioType\":\"antisense\",\"Coding\":\"NON_CODING\",\"Transcript\":\"ENST00000458555\"}]}";
		
		Pipe p = effp.getSNPEffPostProcessPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList(inputFile));
        
        String result="";
        
        for(int i=0; p.hasNext(); i++){
        	History next = (History) p.next(); 
       		result = next.get(8);
       		
       		if (i==1) break;
        }		
   		assertEquals(output, result);
	}
}