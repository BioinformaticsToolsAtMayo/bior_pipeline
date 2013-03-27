/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.ReplaceAllPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HCutPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
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
            String appendMe = "D";
            int[] cols = {1};
            Pipeline superviseMe = new Pipeline(
                    new MergePipe("\t"),
                    new ReplaceAllPipe("EFF.*$", "foo"),
                    new HistoryInPipe()
                    );
            BridgeOverPipe bridge = new BridgeOverPipe(superviseMe, new SNPEFFMerge());
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
}
