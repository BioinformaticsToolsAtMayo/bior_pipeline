package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class OmimFormatterTest extends BaseFormatterTest {
	
	private final Formatter mFormatter = new OmimFormatter();


	@Test
	public void testJSONColumn() {
		assertEquals(JsonColumn.OMIM, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader() {
		validateHeader(mFormatter, new String[] { "OMIM.ID",  "OMIM.Disease" } );
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * @throws IOException
	 */
	@Test
	public void testGoodJson() throws IOException {
		String json = "{\"Chromosome.Map_Entry_Number\":1.1,\"MonthEntered\":9,\"Day\":11,\"Year\":95,\"Cytogenetic_location\":\"1pter-p36.13\",\"GeneSymbols\":\"CCV\",\"Gene_Status\":\"P\",\"Title\":\"Cataract, congenital, Volkmann type\",\"Title_cont\":\"\",\"MIM_Number\":115665,\"Method\":\"Fd\",\"Comments\":\"\",\"Disorders\":\"Cataract, congenital, Volkmann type (2)\",\"Disorders_cont\":\" \"}";
		// { MIM_NUMBER, Disorder }
		String[] expected =  { "115665", "Cataract, congenital, Volkmann type (2)" };
		validateFormattedValues(mFormatter, json, expected);
	}

	/**
	 * Tests "sad" path where most of the JSON fields are missing.
	 * @throws IOException
	 */
	@Test
	public void testMissingJson() throws IOException	{
		// MIM_NUMBER missing from JSON;  Disorder = ""
		String json = "{\"Chromosome.Map_Entry_Number\":1.2,\"MonthEntered\":9,\"Day\":25,\"Year\":1,\"Cytogenetic_location\":\"1p36.23\",\"GeneSymbols\":\"ENO1, PPH, MPB1\",\"Gene_Status\":\"C\",\"Title\":\"Enolase-1, alpha\",\"Title_cont\":\"\",\"Method\":\"S, F, R, REa\",\"Comments\":\"\",\"Disorders\":\"\",\"Disorders_cont\":\" \",\"Mouse_correlate\":\"4(Eno1)\"}";
		// { MIM_NUMBER, Disorder }
		String[] expected =  { "", "" };
		validateFormattedValues(mFormatter, json, expected);
	}
}
