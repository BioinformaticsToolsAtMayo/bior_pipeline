/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *  This unit test just tests getting the command, actually running VEP is in 
 * The VEPITCase functional test.
 * @author m102417
 */
public class VEPEXETest {
    
    public VEPEXETest() {
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
     * Test of getVEPCommand method, of class VEPEXE.
     */
    //@Test
    public void testGetVEPCommand() throws Exception {
        System.out.println("getVEPCommand");
        String bufferSize = "";
        String[] result = VEPEXE.getVEPCommand(bufferSize);
        //result[0] is vep's perl, don't test that, it is configurable
        //result[1] is vep's path, don't test that, it is also configurable
        assertEquals("-i", result[2]);
        //assertEquals("/dev/stdin", result[3]);

    }
    
    //moveMe -- a functional test that we need to move to the vepitcase
        	/**
	 * note: if you want to dig deep and debug this code, you probably want to set your log4j properties to:
	 * ##active
           ## log4j configuration used during build and unit tests
           #log4j.rootLogger=DEBUG, console
           #log4j.threshhold=ALL

           ## console appender logs to STDOUT
           #log4j.appender.console=org.apache.log4j.ConsoleAppender
           ##log4j.appender.console.layout=org.apache.log4j.PatternLayout
           #log4j.appender.console.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
	 * 
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testExecSNPEffPipe() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		System.out.println("Test the raw output of a run on SNPEff versus the expected output (w/o header)");
		VEPEXE vep = new VEPEXE(VEPEXE.getVEPMac("1"));
		Pipe t = new TransformFunctionPipe(vep);
		Pipeline p = new Pipeline(
				new CatPipe(),               //raw file
				new HistoryInPipe(),         //get rid of the header
				new MergePipe("\t"),
                                t,
				//new PrintPipe()
				new IdentityPipe()
				);
		p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
		//expected results
		BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/vep/example.vcf.vep.correct"));
                                                                        
                String expected = "";
                String res = "";
                
                //remove the header
                br.readLine();
                br.readLine();
                br.readLine();
                
                while(expected != null){
                    expected = br.readLine();
                    if(expected == null) break;
                    System.out.println("Expected: " + expected);
                    res = (String) p.next();
                    System.out.println("Result: " + res);
                    assertEquals(expected, res);
                }
                
	}


}
