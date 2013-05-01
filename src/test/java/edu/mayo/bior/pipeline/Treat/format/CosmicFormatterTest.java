package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class CosmicFormatterTest extends BaseFormatterTest{
	private final Formatter mFormatter = new CosmicFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"COSMIC.Mutation_ID",
			"COSMIC.Mutation_CDS",
			"COSMIC.Mutation_AA",
			"COSMIC.strand"	
		};	

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.COSMIC, mFormatter.getJSONColumn());
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/CosmicFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"536198",
				"c.447G\u003eT",
				"p.S149S",
				"+"
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/CosmicFormatter.test2.json"));		
		
		String[] expectedValues =
			{
				
				"",
				"",
				"",
				""
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}


}
