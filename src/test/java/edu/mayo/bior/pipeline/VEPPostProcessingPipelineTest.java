/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.VEPPostProcessingPipeline.FixSiftandPolyphen;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import java.util.Arrays;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipelineTest {
    
    public VEPPostProcessingPipelineTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class VEPPostProcessingPipeline.
     */
    @Test
    public void testGetPipeline() {
        System.out.println("test get pipeline: VEPPostProcessingPipeline...");
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
        Pipe p = vepp.getPipeline(new CatPipe(), new IdentityPipe());

        p.setStarts(Arrays.asList("src/test/resources/tools/vep/vep.vcf"));
        while(p.hasNext()){
            String s = (String) p.next().toString();
            System.out.println(s);
        }

    }
}
