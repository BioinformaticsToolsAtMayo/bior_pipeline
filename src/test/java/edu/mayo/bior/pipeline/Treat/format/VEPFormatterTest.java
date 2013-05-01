package edu.mayo.bior.pipeline.Treat.format;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class VEPFormatterTest extends BaseFormatterTest {

	private final Formatter mFormatter = new VEPFormatter();

	@Test
	public void testJSONColumn()
	{
		assertEquals(JsonColumn.VEP, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader()
	{
		String[] header =
			{
				"VEP.Allele",
				"VEP.Gene",
				"VEP.Feature",
				"VEP.Feature_type",
				"VEP.Consequence",
				"VEP.cDNA_position",
				"VEP.CDS_position",
				"VEP.Protein_position",
				"VEP.Amino_acids",
				"VEP.Codons",
				"VEP.HGNC",
				"SIFT.TERM",
				"SIFT.Score",
				"PolyPhen.TERM",
				"PolyPhen.Score"
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
		
		String json = FileUtils.readFileToString(new File("src/test/resources/treat/formatters/VEPFormatter.brca1.json"));		
		
		String[] expectedValues =
			{
				"C",
				"ENSG00000012048",
				"ENST00000352993",
				"Transcript",
				"missense_variant",
				"768",				
				"536",
				"179",
				"Y/C",
				"tAc/tGc",
				"BRCA1",
				"deleterious",
				"0.01",
				"probably_damaging",
				"0.999"		
			};
		
		validateFormattedValues(mFormatter, json, expectedValues);
	}
}
