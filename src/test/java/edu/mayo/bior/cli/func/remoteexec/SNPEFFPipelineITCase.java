package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.bior.pipeline.SNPEff.SNPEffOutputTest;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.test.PipeTestUtils;

public class SNPEFFPipelineITCase extends RemoteFunctionalTest
{
	private File dataSourceProps;
	private File columnProps;		
	private Metadata metadata;
	
	@Before
	public void setUp() throws IOException
	{
		File biorLiteHome = new File(sHomePath);
		dataSourceProps = new File(biorLiteHome, "conf/tools/snpeff.datasource.properties");
		columnProps     = new File(biorLiteHome, "conf/tools/snpeff.columns.properties");
		metadata = new Metadata(dataSourceProps.getCanonicalPath(), columnProps.getCanonicalPath(), "bior_snpeff");		
	}	

	@After
	public void tearDown()
	{
		History.clearMetaData();
	}

	/**
	 * Loads the file and does a search&replace on any dynamic stuff.
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private List<String> loadExpectedOutputFile(File f) throws IOException
	{
		String expectedContent = FileUtils.readFileToString(f);
		expectedContent = expectedContent.replace("@@SRC_PROP_PATH@@", dataSourceProps.getCanonicalPath());
		expectedContent = expectedContent.replace("@@COL_PROP_PATH@@", columnProps.getCanonicalPath());
		
		return Arrays.asList(expectedContent.split("\n"));
	}	
	
	@Test
	/** Test the treat input VCF */
	public void treatVcf() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException
	{
		SNPEFFPipeline p = new SNPEFFPipeline(new String[]{"GRCh37.64"}, new Pipeline(new CatPipe(),new HistoryInPipe(metadata)), new HistoryOutPipe(), true);
		p.setStarts(Arrays.asList("src/test/resources/tools/treat/treatInput.vcf"));
		List<String> actual = PipeTestUtils.getResults(p);
		List<String> expected = loadExpectedOutputFile(new File("src/test/resources/tools/snpeff/treat.expected.vcf"));
		PipeTestUtils.assertListsEqual(expected, actual);
	}	

	@Test 
	/** Test a bunch of good variants where the Functional_Class is NOT equal to "NONE"
	 *  (since most have "NONE", it's good to check the more rare cases to validate the 
	 *  output from each variant matches expected which we should know because they are fairly unique)
	 *  Do a line-by-line comparison to validate that the input lines all match up with the output lines
	 */
	public void significantEffects() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.significantEffects(): testing variants that have more rare outputs so we can tell if lines match up between bior_snpeff output and the expected output from the jar command...");
		SNPEFFPipeline p = new SNPEFFPipeline(new String[]{"GRCh37.64"}, new Pipeline(new CatPipe(),new HistoryInPipe(metadata)), new HistoryOutPipe(), false);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/significantEffects.input.vcf"));
		List<String> biorActualOutput = PipeTestUtils.pipeOutputToStrings2(p);
		List<String> expectedFromCmdJar = loadExpectedOutputFile(new File("src/test/resources/tools/snpeff/significantEffects_cmdJar.expected.vcf"));
		//printOutput(biorActualOutput);
		assertCommandJarOutputEquivalentToBiorOutput(expectedFromCmdJar, biorActualOutput);
	}
	


	// PASSED!!!
	@Test
	/** Test a series of bad (or potentially bad) variants.
	 *  These shouldn't stop the pipeline, but may give odd or unexpected results */
	public void badVariants() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.badVariants(): test a vcf file that contains poorly formatted variants...");
		SNPEFFPipeline p = new SNPEFFPipeline(new String[]{"GRCh37.64"}, new Pipeline(new CatPipe(),new HistoryInPipe(metadata)), new HistoryOutPipe(),true);
		p.setStarts(Arrays.asList("src/test/resources/tools/snpeff/badVariants.vcf"));
		List<String> actual = PipeTestUtils.getResults(p);
		List<String> expected = loadExpectedOutputFile(new File("src/test/resources/tools/snpeff/badVariants.expected.vcf"));
                for (String ac : actual)	{	
              System.out.println(ac);
}
                printOutput(actual);
		PipeTestUtils.assertListsEqual(expected, actual);
	}
	
	// PASSED!!!
	@Test  (expected = InvalidPipeInputException.class)
	/** Test a bad VCF (that it has less than 7 columns), 
	 *  which would most like occur because of spaces instead of tabs */
	public void badVcf() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		System.out.println("\n-----------------------------------------------------");
		System.out.println("SNPEffITCase.badVcf(): test a vcf file that has contains poorly formatted variants...");
		SNPEFFPipeline p = new SNPEFFPipeline(new String[]{"GRCh37.64"}, new Pipeline(new HistoryInPipe(metadata)), new HistoryOutPipe(),true);

		// Vcf file containing only 1 column because tabs are treated as spaces (min required is 7)
		// NOTE: Spaces instead of tabs!!!
		String vcfIn = "#CHROM  POS    ID     REF  ALT  QUAL  FILTER  INFO\n"
					 + "chr1    949608 rs1921 G    A    .     .       .";
		
		p.setStarts(Arrays.asList(vcfIn.split("\n")));
		// This should cause a failure!
		List<String> actual = PipeTestUtils.getResults(p);
		// If we make it this far we did not catch the exception correctly, so fail
		fail("Error!  Expected an exception, but none occurred!");
	}
	
	/** Programmatically assert that the output from the command-line-jar call matches up with the 
    	results from running the bior command.  We will have to compare SnpEff output such as
			EFF=DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000393691|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000461221|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000468300|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000471181|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000491747|),DOWNSTREAM(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000493795|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000309486|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000346315|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000351666|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000352993|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000354071|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000357654|),UTR_3_PRIME(MODIFIER||||BRCA1|protein_coding|CODING|ENST00000412061|)
    	to
			{"Effect":"DOWNSTREAM","Effect_impact":"MODIFIER","Functional_class":"NONE","Gene_name":"BRCA1","Gene_bioType":"protein_coding","Coding":"CODING","Transcript":"ENST00000393691"}
	 * @throws IOException 
	 */
	private void assertCommandJarOutputEquivalentToBiorOutput(List<String> cmdJarExpectedOutput, List<String> biorActualOutput) throws IOException {
		SNPEffOutputTest compare = new SNPEffOutputTest();
		boolean isSame = compare.testOutput(cmdJarExpectedOutput, biorActualOutput);
		Assert.assertTrue(isSame);
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
