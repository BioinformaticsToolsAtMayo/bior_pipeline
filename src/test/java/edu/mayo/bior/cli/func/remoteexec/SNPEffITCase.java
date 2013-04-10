/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFEXE;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
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

	// PASSED!!!
	@Test
	/** Test the treat input VCF */
	public void treatVcf() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.treatVcf(): standard/simple treat vcf file...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/treat/treatInput.vcf"));
		List<String> actual = PipeTestUtils.pipeOutputToStrings(p);
		//List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/treat/snpEffOutput.vcf");
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/treat.expected.vcf");
		printOutput(actual);
		PipeTestUtils.assertListsEqual(expected, actual);
	}

	// PASSED!!!
	@Test
	/** Test the command line for SNPEff */
	public void cmdLineWithMultiIndels() throws IOException, InterruptedException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.cmdLineWithMultiIndels(): testing SnpEff by command line...");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String stdin = loadFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.vcf"));

		CommandOutput out = executeScript("bior_snpeff", stdin);

		assertEquals(out.stderr, 0, out.exit);

		printOutput(Arrays.asList(out.stdout));

		String expected = loadFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.expected.vcf"));
		assertEquals(expected, out.stdout);
	}
	
	
	// PASSED!!!
	@Test
	/** This can be useful when trying to isolate what SnpEff is trying to do, without all the 
	 *  extra junk coming from the pre and post pipeline elements.
	 *  NOTE: We can only take clean variants ONLY (none with multiple insertions/deletions, else it will hang)!!	 */
	public void snpEffExeOnly() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.snpEffExeOnly(): run only the exec part of the pipeline to isolate and test it...");

		UnixStreamCommand snpeff = new UnixStreamCommand(SNPEFFEXE.getSnpEffCommand(null), NO_CUSTOM_ENV, true, true);        	

		BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/snpeff/exeOnly_noMultiIndels.vcf"));

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
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/exeOnly_noMultiIndels.expected.vcf");
		printOutput(actualOutput);
		PipeTestUtils.assertListsEqual(expected, actualOutput);
	}
	
	


	@Test 
	/** Test a bunch of good variants where the Functional_Class is NOT equal to "NONE"
	 *  (since most have "NONE", it's good to check the more rare cases to validate the 
	 *  output from each variant matches expected which we should know because they are fairly unique)
	 *  Do a line-by-line comparison to validate that the input lines all match up with the output lines
	 */
	public void significantEffects() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.significantEffects(): testing variants that have more rare outputs so we can tell if lines match up between bior_snpeff output and the expected output from the jar command...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),false);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/funcClassNotNone.input.vcf"));
		List<String> biorActualOutput = PipeTestUtils.pipeOutputToStrings(p);
		List<String> expectedFromCmdJar = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/funcClassNotNone_cmdJar.expected.vcf");
		printOutput(biorActualOutput);
		assertCommandJarOutputEquivalentToBiorOutput(expectedFromCmdJar, biorActualOutput);
	}
	


	
	@Test
	/** Test a series of bad (or potentially bad) variants.
	 *  These shouldn't stop the pipeline, but may give odd or unexpected results */
	public void badVariants() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.badVariants(): test a vcf file that contains poorly formatted variants...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new CatPipe(),new HistoryInPipe()), new IdentityPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/badVariants.vcf"));
		List<String> actual = PipeTestUtils.pipeOutputToStrings(p);
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/snpeff/badVariants.expected.vcf");
		printOutput(actual);
		PipeTestUtils.assertListsEqual(expected, actual);
	}
	
	// PASSED!!!
	@Test  (expected = InvalidPipeInputException.class)
	/** Test a bad VCF (that it has less than 7 columns), 
	 *  which would most like occur because of spaces instead of tabs */
	public void badVcf() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.badVcf(): test a vcf file that has contains poorly formatted variants...");
		SNPEFFPipeline p = new SNPEFFPipeline(new Pipeline(new HistoryInPipe()), new IdentityPipe(),true);

		// Vcf file containing only 1 column because tabs are treated as spaces (min required is 7)
		// NOTE: Spaces instead of tabs!!!
		String vcfIn = "#CHROM  POS    ID     REF  ALT  QUAL  FILTER  INFO\n"
					 + "chr1    949608 rs1921 G    A    .     .       .";
		
		p.setStarts(Arrays.asList(vcfIn.split("\n")));
		// This should cause a failure!
		List<String> actual = PipeTestUtils.pipeOutputToStrings(p);
		// If we make it this far we did not catch the exception correctly, so fail
		fail("Error!  Expected an exception, but none occurred!");
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
	
	private void printOutput(List<String> output) {
		if(output == null || output.size() == 0) {
			System.out.println("Output: (null or emptyList)]");
			return;
		}
		
		System.out.println("Output: [");
		for(String s : output) {
			System.out.println(s);
		}
		System.out.println("]");
	}
}
