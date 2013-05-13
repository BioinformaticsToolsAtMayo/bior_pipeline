package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HapmapFormatterTest extends BaseFormatterTest {
	
	private final Formatter mFormatter = new HapmapFormatter();

	@Test
	public void testJSONColumn() {
		assertEquals(JsonColumn.HAPMAP, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader() {
		validateHeader(mFormatter, new String[] {
				"HapMap.CEU_MAF",
				"HapMap.YRI_MAF",
				"HapMap.JPT_MAF",
				"HapMap.CHB_MAF"
		} );
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * @throws IOException
	 */
	@Test
	public void testGoodJson() throws IOException {
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/HapmapFormatter.test1.json"));		
		String[] expected =  { "0.102", "0.077", "0.427",  "0.485"};
		validateFormattedValues(mFormatter, json, expected);
	}

	/**
	 * Tests "sad" path where most of the JSON fields are missing.
	 * @throws IOException
	 */
	@Test
	public void testMissingJson() throws IOException	{
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/HapmapFormatter.test1.json"));
		// Remove the other freq for CEU
		json = json.replace("\"otherallele_freq\": 0.102,\n", "");
		String[] expected =  { ".", "0.077", "0.427", "0.485" };
		validateFormattedValues(mFormatter, json, expected);
	}
}
