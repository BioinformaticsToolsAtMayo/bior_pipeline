/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;

import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
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
    // TODO: Disabled until Dan can refactor
    public void testGetVEPCommand() throws Exception {
        System.out.println("getVEPCommand");
        String bufferSize = "";
        String[] result = VEPEXE.getVEPCommand(bufferSize);
        //result[0] is vep's perl, don't test that, it is configurable
        //result[1] is vep's path, don't test that, it is also configurable
        assertEquals("-i", result[2]);
        //assertEquals("/dev/stdin", result[3]);

    }
    
    /**
     * This pipeline will clean the input before passing it to vep, then stitch it back together
     */
    @Test
    public void testVEPBridgePipeline() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
        Pipe input = new Pipeline(new CatPipe(), new HistoryInPipe());
        //Pipeline p = new VEPPipeline(VEPEXE.getVEPMac("1"), input, new PrintPipe(), true);
        Pipeline p = new VEPPipeline(VEPEXE.getVEPMac("1"), input, new PrintPipe(), false);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        while(p.hasNext()){
            p.next();
        }
    }
    
    //moveMe - a functional test for the new VEP Pipeline
    //This is a little test to make sure everything is working prior to integrating the bridgeover functions
    //@Test
    public void testVEPPipeline() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
        System.out.println("testVEPPipeline");
        String[] vepHeader = new String[1];
        vepHeader[0] = "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE\">";
        VEPEXE vep = new VEPEXE(VEPEXE.getVEPMac("1"));
	Pipe exe = new TransformFunctionPipe(vep);
        VEPPostProcessingPipeline ppp = new VEPPostProcessingPipeline();
        //Pipe post = ppp.getWorstCasePipeline(new IdentityPipe(), new IdentityPipe(), false);
        Pipe post = ppp.getCartesianProductPipeline(new IdentityPipe(), new IdentityPipe(),false);

        Pipeline p = new Pipeline(
                new CatPipe(),
                new HistoryInPipe(),         //get rid of the header
		new MergePipe("\t"),
                exe,
                new VCFProgram2HistoryPipe(vepHeader),   
                post,
                new HistoryOutPipe(),
                new PrintPipe()
                );
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
        for(int i=0; p.hasNext(); i++){
            p.next();
        }
        vep.terminate();
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
        	 * @throws AbnormalExitException 
	 */
	//@Test
    // TODO: Disabled until Dan can refactor
	public void testExecSNPEffPipe() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
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
