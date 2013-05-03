package edu.mayo.bior.pipeline.Treat.format;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpClinvarFormatterTest extends BaseFormatterTest {

	private final Formatter mFormatter = new DbsnpClinvarFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"dbSNP.ClinicalSig",
			"dbSNP.DiseaseVariant"
		};	

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.DBSNP_CLINVAR, mFormatter.getJSONColumn());
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/DbsnpClinvarFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"pathogenic",
				"1"
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
		
		String json = "{}";		
		
		String[] expectedValues =
			{
				".",
				"0"
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}
}
