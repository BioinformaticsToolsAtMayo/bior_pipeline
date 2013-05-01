package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class ThousandGenomesFormatterTest extends BaseFormatterTest {
	
	private final Formatter mFormatter = new ThousandGenomesFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"1000Genomes.ASN_AF",
			"1000Genomes.AMR_AF",
			"1000Genomes.AFR_AF",
			"1000Genomes.EUR_AF"
		};	

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.THOUSAND_GENOMES, mFormatter.getJSONColumn());
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/ThousandGenomesFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"0.13",
				"0.17",
				"0.04",
				"0.21"
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/ThousandGenomesFormatter.test2.json"));		
		
		String[] expectedValues =
			{
				"0.13",
				"0.17",
				"",
				""
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}

}
