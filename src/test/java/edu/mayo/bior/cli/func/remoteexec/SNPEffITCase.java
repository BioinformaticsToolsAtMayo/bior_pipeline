/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFEXE;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.ExecPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 *
 * @author m102417
 */
public class SNPEffITCase extends RemoteFunctionalTest {
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
	
	private static final String EOL = System.getProperty("line.separator");
	private static final Map<String, String> NO_CUSTOM_ENV = Collections.emptyMap();  

	@Test
	/** Test the treat input VCF */
	public void testTreatVcf() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("SNPEffITCase.testTreatVcf(): standard/simple treat vcf file...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/treat/treatInput.vcf"));
		List<String> actual = PipeTestUtils.getResults(p);
		//List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/treat/snpEffOutput.vcf");
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/snpEffOutput205.vcf");
		PipeTestUtils.assertListsEqual(expected, actual);
	}

	@Test
	/** Test the command line for SNPEff */
	public void testCommandLine() throws IOException, InterruptedException {
		System.out.println("SNPEffITCase.testCommandLine(): testing SnpEff by command line...");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String stdin = loadFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.vcf"));

		CommandOutput out = executeScript("bior_snpeff", stdin);

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);

		// pull out just data rows
		String data = out.stdout.replace(header, "");

		// JSON should be added as last column (9th)
		String[] cols = data.split("\t");
		String json = cols[cols.length - 1];
		assertEquals("???", json);
		
		Assert.fail("Testing of the command line not implemented yet.");
	}
	

	@Test 
	/** Test a bunch of good variants where the Functional_Class is NOT equal to "NONE"
	 *  (since most have "NONE", it's good to check the more rare cases)
	 *  Do a line-by-line comparison to validate that the input lines all match up with the output lines
	 */
	public void testFuncClassNotNoneVariants() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("SNPEffITCase.testFuncClassNotNoneVariants(): testing variants that have more rare outputs so we can tell if lines match up between bior_snpeff output and the expected output from the jar command...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/funcClassNotNone.input.vcf"));
		List<String> biorActualOutput = PipeTestUtils.getResults(p);
		List<String> expectedFromCmdJar = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/funcClassNotNone_cmdJar.expected.vcf");
		assertCommandJarOutputEquivalentToBiorOutput(expectedFromCmdJar, biorActualOutput);
	}
	
	@Test
	/** Test multiple-change variants (where there is both an insertion and a deletion) */
	public void testMultiChangeVariants() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("SNPEffITCase.testMultiChangeVariants(): variants with multiple insertions and deletions (that usually trips up SnpEff and causes our pipeline to hang if not caught)...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.vcf"));
		List<String> actual = PipeTestUtils.getResults(p);
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.expected.vcf");
		PipeTestUtils.assertListsEqual(expected, actual);
		
		Assert.fail("MAKE SURE WE CHECK THE CASE WHERE THE JSON FROM THE MULTI-CHANGE VARIANTS ARE NOT THE SAME AS THE JSON FROM THE SINGLE ONES, AND THAT IT WRITES THE CORRECT ERROR MESSAGE INTO THE JSON!");
	}

	
	
	@Test
	/** This can be useful when trying to isolate what SnpEff is trying to do, without all the 
	 *  extra junk coming from the pre and post pipeline elements	 */
	public void testSnpEffExeOnly() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		System.out.println("SNPEffITCase.testSnpEffExeOnly(): run only the exec part of the pipeline to isolate and test it...");

		UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(null), NO_CUSTOM_ENV, true, true);        	

		BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/treat/treatInput.vcf"));

		// launch snpeff java process
		snpeff.launch();

		// send VCF header, this is required
		snpeff.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");

		boolean outputHeaderProcessed = false;

		ArrayList<String> actualOutput = new ArrayList<String>();
		
		String line = br.readLine();
		while( line != null ) {

			// only send VCF data lines to snpeff
			if( ! line.startsWith("#")){

				// send data line to snpeff
				snpeff.send(line);

				// receive data line from snpeff
				String outputLine = snpeff.receive();

				// handle header outputted from SNPEFF
				while (outputLine.startsWith("#")) {
					actualOutput.add(outputLine);
					outputLine = snpeff.receive();
				}

				actualOutput.add(outputLine);
			}

			line = br.readLine();
		}            
		// tell SNPEFF we're done
		snpeff.terminate();

		// Compare the output
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/snpEffOutput205.vcf");
		PipeTestUtils.assertListsEqual(expected, actualOutput);
	}
	
	
	
	@Test
	/** This can be useful when trying to isolate what SnpEff is trying to do, without all the 
	 *  extra junk coming from the pre and post pipeline elements	 */
	public void dumpOutputOfMultiChangeVariants_SnpEffExeOnly() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		System.out.println("SNPEffITCase.dumpOutputOfMultiChangeVariants_SnpEffExeOnly(): ....");
		
		UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(null), NO_CUSTOM_ENV, true, true);        	

		String inputFile = "src/test/resources/tools/snpeff/variantsWithMultipleInsertionsDeletions.vcf";
		//String inputFile = "src/test/resources/tools/snpeff/example.vcf";
		BufferedReader br = new BufferedReader(new FileReader(inputFile));
		System.out.println("Loading vcf file: " + inputFile);

		System.out.println("Launching SnpEff....");
		
		// launch snpeff java process
		snpeff.launch();

		// send VCF header, this is required
		snpeff.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");

		boolean outputHeaderProcessed = false;

		ArrayList<String> actualOutput = new ArrayList<String>();
		
		
		String line = br.readLine();
		while( line != null ) {

			// only send VCF data lines to snpeff
			if( ! line.startsWith("#")){

				// send data line to snpeff
				snpeff.send(line);

				// receive data line from snpeff
				String outputLine = snpeff.receive();

				// handle header outputted from SNPEFF
				while (outputLine.startsWith("#")) {
					actualOutput.add(outputLine);
					System.out.println(outputLine);
					outputLine = snpeff.receive();
				}

				System.out.println(outputLine);
				actualOutput.add(outputLine);
			}

			line = br.readLine();
		}            
		// tell SNPEFF we're done
		snpeff.terminate();

		// Compare the output
		//List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/snpEffOutput205.vcf");
		//PipeTestUtils.assertListsEqual(expected, actualOutput);
	}
	
	
	@Test
	/** Test a series of bad (or potentially bad) variants */
	public void testBadVariants() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("SNPEffITCase.testBadVariants(): test a vcf file that contains poorly formatted variants...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/badVariants.vcf"));
		List<String> actual = PipeTestUtils.getResults(p);
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/badVariants.expected.vcf");
		PipeTestUtils.assertListsEqual(expected, actual);
	}
	

	
	//=======================================================================================
	//=======================================================================================
	
	/** Programmatically assert that the output from the command-line-jar call matches up with the 
	    results from running the bior command.  We will have to compare SnpEff output such as
			EFF=DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000393691|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000461221|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000468300|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000471181|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000491747|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000493795|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000309486|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000346315|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000351666|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000352993|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000354071|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000357654|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000412061|)
	    to
			{"Effect":"DOWNSTREAM","Effect_impact":"MODIFIER","Functional_class":"NONE","Gene_name":"BRCA1","Gene_bioType":"protein_coding","Coding":"CODING","Transcript":"ENST00000393691"}
	*/
	private void assertCommandJarOutputEquivalentToBiorOutput(List<String> cmdJarExpectedOutput, List<String> biorActualOutput) {

		// TODO: Phani was working on this part.....
		
		Assert.fail("Comparison not yet implemented");
	}
}
