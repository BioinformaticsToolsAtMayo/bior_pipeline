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
		
		String output = "21\t26960070\trs116645811\tG\tA\t.\t.\tA;EFF=INTRON(MODIFIER||||338|MRPL39|protein_coding|CODING|ENST00000352957|9),NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aCg/aTg|T334M|353|MRPL39|protein_coding|CODING|ENST00000307301|),UPSTREAM(MODIFIER|||||LINC00515|antisense|NON_CODING|ENST00000567517|)\t{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aCg/aTg\",\"Amino_acid_change\":\"T334M\",\"Amino_acid_length\":\"353\",\"Gene_name\":\"MRPL39\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000307301\"}";		
		//ExecPipe exe = new ExecPipe(command, true);        

        Pipe p = effp.getSNPEffPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/file.eff.vcf"));
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
		
		String output = "21\t26960070\trs116645811\tG\tA\t.\t.\tA;EFF=INTRON(MODIFIER||||338|MRPL39|protein_coding|CODING|ENST00000352957|9),NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aCg/aTg|T334M|353|MRPL39|protein_coding|CODING|ENST00000307301|),UPSTREAM(MODIFIER|||||LINC00515|antisense|NON_CODING|ENST00000567517|)\t{\"EFF\":[{\"Effect\":\"INTRON\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Amino_acid_length\":\"338\",\"Gene_name\":\"MRPL39\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000352957\",\"Exon\":\"9\"},{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aCg/aTg\",\"Amino_acid_change\":\"T334M\",\"Amino_acid_length\":\"353\",\"Gene_name\":\"MRPL39\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000307301\"},{\"Effect\":\"UPSTREAM\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Gene_name\":\"LINC00515\",\"Gene_bioType\":\"antisense\",\"Coding\":\"NON_CODING\",\"Transcript\":\"ENST00000567517\"}]}";
		//ExecPipe exe = new ExecPipe(command, true);        

        Pipe p = effp.getSNPEffPipeline(new CatPipe(), new IdentityPipe());
        //Pipe p = new Pipeline(new CatPipe(), new PrintPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/file.eff.vcf"));
        for(int i=0; p.hasNext(); i++){
        	String next = (String) p.next(); 
        	if(i==1){
        		assertEquals(output, next);
        		break;
            }
        }		
	}


}
