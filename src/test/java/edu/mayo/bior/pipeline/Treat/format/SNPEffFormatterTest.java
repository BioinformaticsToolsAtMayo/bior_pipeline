package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class SNPEffFormatterTest extends BaseFormatterTest{
	private final Formatter mFormatter = new SNPEffFormatter();

	private static final String[] EXPECTED_HEADER =
		{
			"SNPEFF.Effect",
			"SNPEFF.Effect_impact",
			"SNPEFF.Functional_class",
			"SNPEFF.Codon_change",
			"SNPEFF.Amino_acid_change",
			"SNPEFF.Gene_name",
			"SNPEFF.Gene_bioType",
			"SNPEFF.Coding",
			"SNPEFF.Transcript",
			"SNPEFF.Exon"
		};	

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.SNPEFF, mFormatter.getJSONColumn());
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/SNPEffFormatter.test1.json"));		
		
		String[] expectedValues =
			{
				"STOP_GAINED",
				"HIGH",
				"NONSENSE",
				"Cga/Tga",
				"R191*",
				"TPTE",
				"protein_coding",
				"CODING",
				"ENST00000342420",
				"exon_21_10942711_10942774"
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/SNPEffFormatter.test2.json"));		
		
		String[] expectedValues =
			{
				"INTERGENIC",
				"MODIFIER",
				"NONE",
				"",
				"",
				"",
				"",
				"",
				"",
				""
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}

}
