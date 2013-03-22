package edu.mayo.bior.pipeline;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;

import edu.mayo.bior.pipeline.SNPEff.SNPEffPostProcessPipeline;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;

public class SNPEffPostProcessPipelineTest {

	@Test
	public void testPipeline_mostSignificantEffect() throws Exception {
		System.out.println("Testing SNPEffPostProcessPipelineTest.testPipeline_mostSignificantEffect()..");
		SNPEffPostProcessPipeline effp = new SNPEffPostProcessPipeline(true);
		
		String output = "chr1\t949608\trs1921\tG\tA\t0.0\t.\tEFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)\t{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aGc/aAc\",\"Amino_acid_change\":\"S83N\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"}";

        Pipe p = effp.getSNPEffPostProcessPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
        for(int i=0; p.hasNext(); i++){
        	String next = (String) p.next(); 
        	if(i==1){
        		assertEquals(output, next);
        		break;
            }
        }
	}

	@Test
	public void testPipeline_allEffects() throws Exception {
		System.out.println("Testing SNPEffPostProcessPipelineTest.testPipeline_testPipeline_allEffects()..");
		SNPEffPostProcessPipeline effp = new SNPEffPostProcessPipeline(false);
		
		String output = "chr1\t949608\trs1921\tG\tA\t0.0\t.\tEFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)\t{\"EFF\":[{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aGc/aAc\",\"Amino_acid_change\":\"S83N\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"},{\"Effect\":\"UPSTREAM\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Gene_name\":\"RP11-54O7.11\",\"Gene_bioType\":\"antisense\",\"Coding\":\"NON_CODING\",\"Transcript\":\"ENST00000458555\"}]}";
        Pipe p = effp.getSNPEffPostProcessPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
        for(int i=0; p.hasNext(); i++){
        	String next = (String) p.next(); 
        	if(i==1){
        		assertEquals(output, next);
        		break;
            }
        }		
	}


}
