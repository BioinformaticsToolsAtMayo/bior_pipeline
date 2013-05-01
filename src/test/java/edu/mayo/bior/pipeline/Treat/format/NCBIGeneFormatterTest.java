package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class NCBIGeneFormatterTest extends BaseFormatterTest {
	private final Formatter mFormatter = new NcbiGeneFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"Entrez.GeneID",
			"Gene_Symbol",
		};	
	
	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.NCBI_GENE, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader()
	{
		validateHeader(mFormatter, EXPECTED_HEADER);
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test1() throws IOException
	{
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/EntrezGeneFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"100506145",
				"LOC100506145",
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}

	/**
	 * Tests "sad" path where most of the JSON fields are missing.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test2() throws IOException
	{
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/EntrezGeneFormatter.test2.json"));		
		
		String[] expectedValues =
			{
				"100506145",
				"",
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}

}
