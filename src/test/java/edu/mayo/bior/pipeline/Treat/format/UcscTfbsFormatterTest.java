package edu.mayo.bior.pipeline.Treat.format;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class UcscTfbsFormatterTest extends BaseFormatterTest {

	private final Formatter mFormatter = new UcscTfbsFormatter();

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.UCSC_TFBS, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader()
	{
		String[] header =
			{
				"UCSC.tfbs"
			};	
		validateHeader(mFormatter, header);
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void test1() throws IOException
	{
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/UcscTfbsFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"842"
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}
}
