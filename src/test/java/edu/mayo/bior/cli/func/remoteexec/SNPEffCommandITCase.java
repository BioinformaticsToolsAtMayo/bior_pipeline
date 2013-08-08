package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;

public class SNPEffCommandITCase extends RemoteFunctionalTest
{
	private File dataSourceProps;
	private File columnProps;		

	@Before
	public void setUp()
	{
		File biorLiteHome = new File(sHomePath);
		dataSourceProps = new File(biorLiteHome, "conf/tools/snpeff.datasource.properties");
		columnProps     = new File(biorLiteHome, "conf/tools/snpeff.columns.properties");		
	}

	/**
	 * Loads the file and does a search&replace on any dynamic stuff.
	 * @param f
	 * @return
	 * @throws IOException
	 */
	private String loadExpectedOutputFile(File f) throws IOException
	{
		String expectedContent = FileUtils.readFileToString(f);
		expectedContent = expectedContent.replace("@@SRC_PROP_PATH@@", dataSourceProps.getCanonicalPath());
		expectedContent = expectedContent.replace("@@COL_PROP_PATH@@", columnProps.getCanonicalPath());
		
		return expectedContent;
	}	
	
	@Test
	public void blackListedFlag() throws IOException, InterruptedException
	{
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String stdin = loadFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.vcf"));

		// Add the "-fi" flag
		CommandOutput out = executeScript("bior_snpeff", stdin, "-genome_version", "GRCh37.64", "-fi");
		Assert.assertTrue(out.stderr.contains("Unrecognized option: -fi"));
	}
	
	@Test
	/** Test the command line for SNPEff */
	public void cmdLineWithMultiIndels() throws IOException, InterruptedException
	{
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String stdin = loadFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.vcf"));

		CommandOutput out = executeScript("bior_snpeff", stdin, "-genome_version", "GRCh37.64");

		assertEquals(out.stderr, 0, out.exit);

		String expected = loadExpectedOutputFile(new File("src/test/resources/tools/snpeff/variantsSingleAndMultiChange.expected.vcf"));
		assertEquals(expected, out.stdout);
	}

}
