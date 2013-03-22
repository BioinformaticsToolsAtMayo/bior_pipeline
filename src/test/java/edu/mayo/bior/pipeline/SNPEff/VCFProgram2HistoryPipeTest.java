/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.History;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author m102417
 */
public class VCFProgram2HistoryPipeTest {
    
    public VCFProgram2HistoryPipeTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
        
       String s1 = "chr1	949608	rs1921	G	A	0.0	.	EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)";
       String s2 = "chr1	949654	rs8997	A	G	0.0	.	EFF=SYNONYMOUS_CODING(LOW|SILENT|gtA/gtG|V98|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)";
       String s3 = "chr1	1588717	rs61774959	G	A	0.0	.	EFF=DOWNSTREAM(MODIFIER||||SLC35E2B|protein_coding|CODING|ENST00000234800|),DOWNSTREAM(MODIFIER||||SLC35E2B|protein_coding|CODING|ENST00000378662|),TRANSCRIPT(MODIFIER||||AL691432.2|unprocessed_pseudogene|NON_CODING|ENST00000317673|),TRANSCRIPT(MODIFIER||||AL691432.2|unprocessed_pseudogene|NON_CODING|ENST00000340677|),TRANSCRIPT(MODIFIER||||AL691432.2|unprocessed_pseudogene|NON_CODING|ENST00000407249|),TRANSCRIPT(MODIFIER||||AL691432.2|unprocessed_pseudogene|NON_CODING|ENST00000513088|),UPSTREAM(MODIFIER||||AL691432.2|unprocessed_pseudogene|NON_CODING|ENST00000341832|)";

    /**
     * Test of histAppend method, of class VCFProgram2HistoryPipe.
     */
    @Test
    public void testHistAppend() {
        System.out.println("histAppend");
        History h = null;
        String[] tokens = null;
        VCFProgram2HistoryPipe vcf2h = new VCFProgram2HistoryPipe();
        Pipeline p = new Pipeline(
                new CatPipe(),
                vcf2h,
                new MergePipe("\t"),
                //new PrintPipe()
                new IdentityPipe()
                );
        p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
        for(int i=0; p.hasNext(); i++){
            String s = (String) p.next();
            if(i==0){
                assertEquals(s, s1);
            }
            if(i==1){
                assertEquals(s, s2);
            }
            if(i==2){
                assertEquals(s, s3);
            }
        }
        
    }

 
}
