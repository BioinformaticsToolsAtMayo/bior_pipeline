package edu.mayo.bior.pipeline.Treat.format;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class VEPHgncFormatterTest extends BaseFormatterTest {

	private final Formatter mFormatter = new VEPHgncFormatter();

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.VEP_HGNC, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader()
	{
		String[] header =
			{
				"UniprotID",
			};	
		validateHeader(mFormatter, header);
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * 
	 * @throws IOException
	 */
	@Test
	public void brca1Test() throws IOException
	{
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/VEPHgncFormatter.brca1.json"));		
		
		String[] expectedValues =
			{
				"P38398"
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}
}
