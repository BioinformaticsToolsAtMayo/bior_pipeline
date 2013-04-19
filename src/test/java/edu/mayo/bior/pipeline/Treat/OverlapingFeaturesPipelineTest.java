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
import edu.mayo.pipes.history.HistoryOutPipe;
import java.io.IOException;
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
public class OverlapingFeaturesPipelineTest {
    
    public OverlapingFeaturesPipelineTest() {
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
     * Uses the TREAT summarization logic
     * Test of init method, of class OverlapingFeaturesPipeline.
     */
    //@Test
    public void testInitSummarize() throws Exception {
        System.out.println("Testing Overlap Features Pipeline");
        Pipe input = new Pipeline(new CatPipe(), new HistoryInPipe());
        Pipe output = new Pipeline(new HistoryOutPipe(), new PrintPipe());
        OverlapingFeaturesPipeline p = new OverlapingFeaturesPipeline(input, output, false);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        while(p.hasNext()){
            p.next();
        }
    }
	
    
    /**
     * Test my code
     * 
     * @throws IOException
     */
	@SuppressWarnings ({"rawtypes", "unchecked"})
    //@Test
	public void gregTest () throws IOException
	{
		Pipe input = new Pipeline (new CatPipe (), new HistoryInPipe ());
		Pipe output = new Pipeline (new HistoryOutPipe (), new PrintPipe ());
		
		OverlapingFeaturesPipeline p = new OverlapingFeaturesPipeline (input, output, "/Users/m082166/Documents/BioR/", true);
		p.setStarts (Arrays.asList ("src/test/resources/tools/vep/example.vcf"));
		while (p.hasNext ())
		{
			p.next ();
		}
	}


    /**
     * Test of init method, of class OverlapingFeaturesPipeline.
     */
    //@Test
    public void testInit() throws Exception {
        System.out.println("Testing Overlap Features Pipeline");
        Pipe input = new Pipeline(new CatPipe(), new HistoryInPipe());
        Pipe output = new PrintPipe();
        OverlapingFeaturesPipeline p = new OverlapingFeaturesPipeline(input, output);//false
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        while(p.hasNext()){
            p.next();
        }
    }
}
