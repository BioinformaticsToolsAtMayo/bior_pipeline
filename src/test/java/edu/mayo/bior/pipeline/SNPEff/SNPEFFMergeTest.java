/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgramMerge;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgramPreProcessPipe;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.ReplaceAllPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.HCutPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.meta.BridgeOverPipe;
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
public class SNPEFFMergeTest {
    
    public SNPEFFMergeTest() {
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

        @Test
        public void testBridge(){
            System.out.println("testBridge");
            int[] cols = {1};
            Pipeline superviseMe = new Pipeline(
                    new MergePipe("\t"),
                    new ReplaceAllPipe("EFF.*$", "foo"),
                    new HistoryInPipe()
                    );
            BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new VCFProgramMerge());
            Pipeline p = new Pipeline(
                    new CatPipe(),
                    new HistoryInPipe(),
                    bridge, //the BridgeOverPipe
                    new IdentityPipe()
                    //new PrintPipe()
            );
            p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
            for(int i=0; p.hasNext(); i++){
                History h = (History) p.next();
                if(i==0){
                    assertEquals("chr1", h.get(0));
                    assertEquals("949608", h.get(1));
                    assertEquals("rs1921", h.get(2));
                    assertEquals("G", h.get(3));
                    assertEquals("A", h.get(4));
                    assertEquals("0.0", h.get(5));
                    assertEquals(".", h.get(6));                  
                    assertEquals("EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)", h.get(7));
                    assertEquals("foo", h.get(8));                   
//chr1, 949608, rs1921, G, A, 0.0, ., EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|), foo]

                }
                if(i==1){

                }
                //if(i==4)break;
                  
                
            }
        }
        @Test
        public void testBridge2(){
            System.out.println("Test Bridge with Post-Process Pipe");
            SNPEffPostProcessPipeline ppp = new SNPEffPostProcessPipeline(true);           
            Pipe post = ppp.getSNPEffTransformPipe(true);
            Pipeline superviseMe = new Pipeline(
                    new VCFProgramPreProcessPipe(8),//history-string //when not running this test, you will not want col 8, so use 7 here!
                    new VCFProgram2HistoryPipe(),//string-history
                    post //history-history
                    );
            BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new VCFProgramMerge());
            Pipeline p = new Pipeline(
                    new CatPipe(),//filename-string
                    new GrepEPipe("^#"),//string-string
                    new HistoryInPipe(),//string-history
                    //new PrintPipe(),
                    bridge, //the BridgeOverPipe
                    new IdentityPipe()
                    //new PrintPipe()
            );
            p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/snpEffOutput205.vcf"));
            for(int i=0; p.hasNext(); i++){
                History h = (History) p.next();
                //p.next();
                if(i==0){
                    assertEquals("chr1", h.get(0));
                    assertEquals("949608", h.get(1));
                    assertEquals("rs1921", h.get(2));
                    assertEquals("G", h.get(3));
                    assertEquals("A", h.get(4));
                    assertEquals("0.0", h.get(5));
                    assertEquals(".", h.get(6));                  
                    assertEquals("EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|)", h.get(7));
                    assertEquals("{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aGc/aAc\",\"Amino_acid_change\":\"S83N\",\"Gene_name\":\"ISG15\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000379389\",\"Exon\":\"exon_1_949364_949920\"}", h.get(8));                   
//chr1, 949608, rs1921, G, A, 0.0, ., EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aGc/aAc|S83N|ISG15|protein_coding|CODING|ENST00000379389|exon_1_949364_949920),UPSTREAM(MODIFIER||||RP11-54O7.11|antisense|NON_CODING|ENST00000458555|), foo]

                }
                if(i==1){

                }
                //if(i==4)break;
                  
                
            }
        }



}
