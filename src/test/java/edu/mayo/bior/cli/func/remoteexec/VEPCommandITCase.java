package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
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
public class VEPCommandITCase extends RemoteFunctionalTest {

	private final String VEPDIR = "src/test/resources/tools/vep/";
	
	private File dataSourceProps;
	private File columnProps;		

	@Before
	public void setUp()
	{
		File biorLiteHome = new File(sHomePath);
		dataSourceProps = new File(biorLiteHome, "conf/tools/vep.datasource.properties");
		columnProps     = new File(biorLiteHome, "conf/tools/vep.columns.properties");		
	}
	
	/**
	 * Gets the ##BIOR metadata line for the header that corresponds to running bior_vep
	 * @return
	 * @throws IOException
	 */
	private String getMetadataLine() throws IOException
	{
		return String.format(
				"##BIOR=<ID=\"bior.vep\",Operation=\"bior_vep\",DataType=\"JSON\",ShortUniqueName=\"vep\",Description=\"Tool from Ensembl that predicts the functional consequences of known and unknown variants.\",Version=\"2.7\",Build=\"Ensembl Release 69\",DataSourceProperties=\"%s\",ColumnProperties=\"%s\">",
				dataSourceProps.getCanonicalPath(),
				columnProps.getCanonicalPath());		
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
	/** Test the whole bior_vep command with fanout of multiple effects
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void cmdFanout() throws IOException, InterruptedException {
		System.out.println("-----------------------------------------");
		System.out.println("VEPITCase.cmdFanout(): testing sample VEP vcf file");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String vcfIn = loadFile(new File(VEPDIR + "vepsample.vcf"));

		CommandOutput out = executeScript("bior_vep", vcfIn, "--all");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
				
		List<String> expected = loadExpectedOutputFile(new File(VEPDIR + "vepsample.expected.fanout2.vcf"));
		
		//printComparison(null, expected, actual);
		
		// The output should contain some sift and polyphen scores
		assertTrue("VEP lines should begin with CSQ JSON structure (without ['s): [\n" + actualStr + "]", actualStr.contains("{\"CSQ\":[{"));
		assertTrue("VEP lines should end with }]}: [\n" + actualStr + "]", actualStr.trim().endsWith("}]}"));
		assertTrue(actualStr, actualStr.contains("\"PolyPhen\":\"benign(0.001)\""));
		assertTrue(actualStr, actualStr.contains("\"SIFT\":\"tolerated(0.05)\""));
		assertTrue(actualStr, actualStr.contains("\"PolyPhen_TERM\":\"benign\""));
		assertTrue(actualStr, actualStr.contains("\"SIFT_TERM\":\"tolerated\""));

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
		List<String> expected = loadExpectedOutputFile(new File(VEPDIR + "vepsample.expected.worstonly.vcf"));

		//printComparison(null, expected, actual);

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
	public void testHeader() throws IOException, InterruptedException, URISyntaxException
	{
		// NOTE:  This test case should only run on biordev - where it can run VEP		
		String stdin = 
				"##fileformat=VCFv4.0" + "\n" +
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO" + "\n" +
				"21	26960070	rs116645811	G	A	.	.	.";

		CommandOutput out = executeScript("bior_vep", stdin, "--log");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String[] outputLines = out.stdout.split("\n");

		assertEquals(4, outputLines.length);
		assertEquals("##fileformat=VCFv4.0", outputLines[0]);
		assertEquals(getMetadataLine(), outputLines[1]);		
		assertEquals("#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	bior.vep", outputLines[2]);
	}	
	
	public static void printComparison(Pipeline pipeline, List<String> expected, List<String> actual) {
		if(pipeline != null)
			PipeTestUtils.walkPipeline(pipeline, 0);
		
		System.out.println("Expected: -----------------");
		PipeTestUtils.printLines(expected);
		System.out.println("Actual: -------------------");
		PipeTestUtils.printLines(actual);
		System.out.println("---------------------------");
	}


	@Test
	/** 
	 * Tests bypass logic for bior_vep.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void bypass() throws IOException, InterruptedException
	{
		System.out.println("VEPITCase.bypass(): Running the command line call and only getting the worst effect per variant as output");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String vcfIn = loadFile(new File(VEPDIR + "bypass.vcf"));

		CommandOutput out = executeScript("bior_vep", vcfIn);

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String actualStr = out.stdout;
		List<String> lines = Arrays.asList(actualStr.split("\n"));
		
		assertEquals(6, lines.size());

		List<String> dataLines = lines.subList(2, lines.size());		
		for (String line: dataLines)
		{
			String[] cols = line.split("\t");
			assertEquals(9, cols.length);
			assertEquals("{}", cols[8]);
		}
	}

    public File getDataSourceProps() {
        return dataSourceProps;
    }

    public void setDataSourceProps(File dataSourceProps) {
        this.dataSourceProps = dataSourceProps;
    }

    public File getColumnProps() {
        return columnProps;
    }

    public void setColumnProps(File columnProps) {
        this.columnProps = columnProps;
    }
        
        
}
