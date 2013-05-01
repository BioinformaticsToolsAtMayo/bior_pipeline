package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HgncFormatterTest extends BaseFormatterTest {
	
	private final Formatter mFormatter = new HgncFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"Approved_Gene_Name",
			"Ensembl_Gene_ID",
		};	

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.HGNC, mFormatter.getJSONColumn());
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/HgncFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"alpha-1-B glycoprotein",
				"ENSG00000121410"
			
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/HgncFormatter.test2.json"));		
		
		String[] expectedValues =
			{
				"alpha-1-B glycoprotein",
				""
			
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}

}
