/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
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
public class AlleleFrequenciesPipelineTest {
    
    public AlleleFrequenciesPipelineTest() {
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

    /**
     * Test of init method, of class AlleleFrequenciesPipeline.
     */
    //@Test
    public void testInit() throws Exception {
        System.out.println("Testing Allele Frequencies Pipeline");
        Pipe input = new Pipeline(new CatPipe(), new HistoryInPipe());
        Pipe output = new PrintPipe();
        AlleleFrequenciesPipeline p = new AlleleFrequenciesPipeline(input, output);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        while(p.hasNext()){
            p.next();
        }
    }
}
