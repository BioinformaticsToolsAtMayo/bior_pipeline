package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.VCFProgramPipes.VCFProgram2HistoryPipe;
import edu.mayo.bior.pipeline.VEP.VEPEXE;
import edu.mayo.bior.pipeline.VEP.VEPPostProcessingPipeline;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/** Required steps before being able to run these command:
 *  1) SSH to DragonRider dev server and setup the functestuser user
 *  	a) ssh bior@biordev.mayo.edu
 *  	b) su -
 *  	c) adduser functestuser --home /home/functestuser
 *  2) Setup environment scripts
 *  	a) vi /home/functestuser/.bashrc
 *  	b) (add these variables):
 *  			$xxxx=yyyy
 *  	c) 
 *  3) You MUST do the following BEFORE any of the tests relying on the command
 *     (or those commands will reference the old code that is in the target dir)
 *       (from terminal):
 *       cd bior_pipeline
 *       (this will put the latest code into the target directory where it can be run)
 *       mvn clean package -DskipTests
 * @author Michael Meiners (m054457)
 */
public class VEPITCase extends RemoteFunctionalTest {

	private final String VEPDIR = "src/test/resources/tools/vep/";
	
	// PASSED - 2013-04-19, 9:25
	@Test
	/** Test only the output of VEP itself based on input (no JSON conversion, just the raw output) */
	public void vepExeOnlyPipe() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		System.out.println("-----------------------------------------");
		System.out.println("VEPITCase.vepExeOnlyPipe()");
		double start = System.currentTimeMillis();
		String[] vepCmd = VEPEXE.getVEPCommand(null);
		System.out.println("VEP Command: " + Arrays.asList(vepCmd));
		VEPEXE vepExe = new VEPEXE(vepCmd);
		Pipeline p = new Pipeline(
				new CatPipe(),               //raw file
				new HistoryInPipe(),         //get rid of the header
				new MergePipe("\t"),
				new TransformFunctionPipe(vepExe),
				//new PrintPipe()
				new IdentityPipe()
				);
		PipeTestUtils.walkPipeline(p, 0);
		p.setStarts(Arrays.asList(VEPDIR + "example.vcf"));

		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "example.vcf.vep.correct");
		// Remove the header (first 3 lines)
		while(expected.size() > 0 && expected.get(0).startsWith("#"))
			expected.remove(0);
		
		List<String> actual = PipeTestUtils.getResults(p);

//		System.out.println("Expected: -----------------");
//		PipeTestUtils.printLines(expected);
//		System.out.println("Actual: -------------------");
//		PipeTestUtils.printLines(actual);
//		System.out.println("---------------------------");
		
		vepExe.terminate();

		PipeTestUtils.assertListsEqual(expected, actual);
		
		double end = System.currentTimeMillis();
		System.out.println("VEPITCase.testExecVepPipe() - Total runtime: " + (end-start)/1000.0);
	}
	
	@Test
	/** Verify everything is working using the pipeline */
	public void pipelineWorstEffect() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		System.out.println("-----------------------------------------");
		System.out.println("VEPITCase.pipelineWorstEffect()");

		String[] vepHeader = new String[] { "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|HGNC|DISTANCE|SIFT|PolyPhen|CELL_TYPE\">" };
		VEPEXE vep = new VEPEXE(VEPEXE.getVEPCommand(null));
		VEPPostProcessingPipeline vepPostProcess = new VEPPostProcessingPipeline();
		Pipe post = vepPostProcess.getWorstCasePipeline(new IdentityPipe(), new IdentityPipe(), false);
		//Pipe post = vepPostProcess.getCartesianProductPipeline(new IdentityPipe(), new IdentityPipe(),false);

		Pipeline p = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(),  //get rid of the header
				new MergePipe("\t"),
				new TransformFunctionPipe(vep),
				new VCFProgram2HistoryPipe(vepHeader),
				//new String2HistoryPipe("\t"),
				post,
				new HistoryOutPipe()
				);

		PipeTestUtils.walkPipeline(p, 0);

		p.setStarts(Arrays.asList(VEPDIR + "vepsample.vcf"));
		
		List<String> actual = PipeTestUtils.getResults(p);
		vep.terminate();
		
		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "vepsample.expected.worstonly.vcf");
		
		System.out.println("Expected: -----------------");
		PipeTestUtils.printLines(expected);
		System.out.println("Actual: -------------------");
		PipeTestUtils.printLines(actual);
		System.out.println("---------------------------");

		PipeTestUtils.assertListsEqual(expected, actual);
	}

	@Test
	/** Verify everything is working using the pipeline and doing a fanout */
	public void pipelineFanout() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException{
		System.out.println("-----------------------------------------");
		System.out.println("VEPITCase.pipelineFanout()");

		String[] vepHeader = new String[] { "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|HGNC|DISTANCE|SIFT|PolyPhen|CELL_TYPE\">" };
		VEPEXE vep = new VEPEXE(VEPEXE.getVEPCommand(null));
		VEPPostProcessingPipeline vepPostProcess = new VEPPostProcessingPipeline();
		Pipe post = vepPostProcess.getCartesianProductPipeline(new IdentityPipe(), new IdentityPipe(), false);
		//Pipe post = vepPostProcess.getCartesianProductPipeline(new IdentityPipe(), new IdentityPipe(),false);

		Pipeline p = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(),  //get rid of the header
				new MergePipe("\t"),
				new TransformFunctionPipe(vep),
				new VCFProgram2HistoryPipe(vepHeader),
				//new String2HistoryPipe("\t"),
				post,
				new HistoryOutPipe()
				);

		PipeTestUtils.walkPipeline(p, 0);

		p.setStarts(Arrays.asList(VEPDIR + "vepsample.vcf"));
		
		List<String> actual = PipeTestUtils.getResults(p);
		vep.terminate();
		
		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "vepsample.expected.fanout.vcf");
		
		System.out.println("Expected: -----------------");
		PipeTestUtils.printLines(expected);
		System.out.println("Actual: -------------------");
		PipeTestUtils.printLines(actual);
		System.out.println("---------------------------");

		PipeTestUtils.assertListsEqual(expected, actual);
	}
	
	@Test
	/** Test the whole bior_vep command with fanout of multiple effects
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void cmdWithFanout() throws IOException, InterruptedException {
		System.out.println("VEPITCase.cmdWithFanout(): testing sample VEP vcf file");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String vcfIn = loadFile(new File(VEPDIR + "vepsample.vcf"));

		CommandOutput out = executeScript("bior_vep", vcfIn, "--all");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "vepsample.expected.fanout.vcf");
		
		System.out.println("Expected: -----------------");
		PipeTestUtils.printLines(expected);
		System.out.println("Actual: -------------------");
		PipeTestUtils.printLines(actual);
		System.out.println("---------------------------");

		// The output should contain some sift and polyphen scores
		assertTrue(actualStr.contains("\"PolyPhen\":\"benign(0.001)\""));
		assertTrue(actualStr.contains("\"SIFT\":\"tolerated(0.05)\""));
		assertTrue(actualStr.contains("\"PolyPhen_TERM\":\"benign\""));
		assertTrue(actualStr.contains("\"SIFT_TERM\":\"tolerated\""));

		PipeTestUtils.assertListsEqual(expected, actual);
		
	}

	@Test
	/** Test the whole bior_vep command with worst effect ONLY for each variant
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void cmdWorstEffectOnly() throws IOException, InterruptedException {
		System.out.println("VEPITCase.cmdWorstEffectOnly(): Running the command line call and only getting the worst effect per variant as output");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String vcfIn = loadFile(new File(VEPDIR + "vepsample.vcf"));

		CommandOutput out = executeScript("bior_vep", vcfIn);

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
		List<String> expected = FileCompareUtils.loadFile(VEPDIR + "vepsample.expected.worstonly.vcf");
		
		System.out.println("Expected: -----------------");
		PipeTestUtils.printLines(expected);
		System.out.println("Actual: -------------------");
		PipeTestUtils.printLines(actual);
		System.out.println("---------------------------");

		// The output should contain some sift and polyphen scores
		assertTrue(actualStr.contains("\"PolyPhen\":\"benign(0.001)\""));
		assertTrue(actualStr.contains("\"SIFT\":\"tolerated(0.05)\""));
		assertTrue(actualStr.contains("\"PolyPhen_TERM\":\"benign\""));
		assertTrue(actualStr.contains("\"SIFT_TERM\":\"tolerated\""));

		PipeTestUtils.assertListsEqual(expected, actual);
	}

}
