package edu.mayo.bior.pipeline;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;

public class SNPEffPipelineTest {

	@Test
	public void testPipeline_mostSignificantEffect() throws Exception {
		System.out.println("Testing SNPEffPipelineTest.testPipeline_mostSignificantEffect()..");
		SNPEffPipeline effp = new SNPEffPipeline(true);
		
		String output = "chr1\t949654\trs8997\tA\tG\t0.0\t.\tEFF=SYNONYMOUS_CODING(LOW|SILENT|gtA/gtG|V98|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)	{\"Effect\":\"SYNONYMOUS_CODING\",\"Effect_impact\":\"LOW\",\"Functional_class\":\"SILENT\",\"Codon_change\":\"gtA/gtG\",\"Amino_acid_change\":\"V98\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"}";
		//ExecPipe exe = new ExecPipe(command, true);        

        Pipe p = effp.getSNPEffPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
        //p.setStarts(Arrays.asList(sampleInput));
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
		System.out.println("Testing SNPEffPipelineTest.testPipeline_testPipeline_allEffects()..");
		SNPEffPipeline effp = new SNPEffPipeline(false);
		
		String output = "chr1\t949654\trs8997\tA\tG\t0.0\t.\tEFF=SYNONYMOUS_CODING(LOW|SILENT|gtA/gtG|V98|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)	{\"EFF\":[{\"Effect\":\"SYNONYMOUS_CODING\",\"Effect_impact\":\"LOW\",\"Functional_class\":\"SILENT\",\"Codon_change\":\"gtA/gtG\",\"Amino_acid_change\":\"V98\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"},{\"Effect\":\"UPSTREAM\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Gene_name\":\"RP11-54O7.11\",\"Gene_bioType\":\"antisense\",\"Coding\":\"NON_CODING\",\"Transcript\":\"ENST00000458555\"}]}";
		//ExecPipe exe = new ExecPipe(command, true);        

        Pipe p = effp.getSNPEffPipeline(new CatPipe(), new IdentityPipe());
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
