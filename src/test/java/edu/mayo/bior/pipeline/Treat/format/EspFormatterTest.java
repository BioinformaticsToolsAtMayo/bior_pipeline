package edu.mayo.bior.pipeline.Treat.format;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import org.junit.Test;
import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class EspFormatterTest extends BaseFormatterTest {
	
	private final Formatter mFormatter = new EspFormatter();

	@Test
	public void testJSONColumn() {
		assertEquals(JsonColumn.ESP, mFormatter.getJSONColumn());
	}
	
	@Test
	public void testHeader() {
		validateHeader(mFormatter, new String[] { "ESP6500.EUR_MAF", "ESP6500.AFR_MAF" } );
	}
	
	/**
	 * Tests "happy" path where JSON has all fields.
	 * @throws IOException
	 */
	@Test
	public void testGoodJson() throws IOException {
		String json = "{\"CHROM\":\"21\",\"POS\":\"9908404\",\"ID\":\".\",\"REF\":\"TG\",\"ALT\":\"T\",\"QUAL\":\".\",\"FILTER\":\"PASS\",\"INFO\":{\"DBSNP\":[\".\"],"
				+     "\"EA_AC\":[\"1\",\"3841\"],\"AA_AC\":[\"2\",\"2134\"],\"TAC\":[\"3\",\"5975\"],\"MAF\":[\"0.026\",\"0.0936\",\"0.0502\"],\"GTS\":[\"A1A1\",\"A1R\",\"RR\"],"
				+     "\"EA_GTC\":[\"0\",\"1\",\"1920\"],\"AA_GTC\":[\"0\",\"2\",\"1066\"],\"GTC\":[\"0\",\"3\",\"2986\"],\"DP\":17,\"GL\":[\".\"],\"CP\":0.1,\"CG\":0.1,\"AA\":\".\",\"CA\":[\".\"],"
				+     "\"EXOME_CHIP\":[\"no\"],\"GWAS_PUBMED\":[\".\"],\"GM\":[\".\"],\"FG\":[\"intergenic\"],\"AAC\":[\".\"],\"PP\":[\".\"],\"CDP\":[\".\"],\"GS\":[\".\"],\"PH\":[\".\"]},"
				+     "\"_id\":\".\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"TG\",\"_altAlleles\":[\"T\"],\"_minBP\":9908404,\"_maxBP\":9908405,"
				+     "\"EA\":{\"_maf\":0.000260},\"AA\":{\"_maf\":0.000936},\"ALL\":{\"_maf\":0.000502}}";
		// { EA._maf, AA._maf }
		String[] expected =  { "2.6E-4", "9.36E-4" };
		validateFormattedValues(mFormatter, json, expected);
	}

	/**
	 * Tests "sad" path where most of the JSON fields are missing.
	 * @throws IOException
	 */
	@Test
	public void testMissingJson() throws IOException	{
		// MIM_NUMBER missing from JSON;  Disorder = ""
		String json = "{\"CHROM\":\"21\",\"POS\":\"9908404\",\"ID\":\".\",\"REF\":\"TG\",\"ALT\":\"T\",\"QUAL\":\".\",\"FILTER\":\"PASS\",\"INFO\":{\"DBSNP\":[\".\"],"
				+     "\"EA_AC\":[\"1\",\"3841\"],\"AA_AC\":[\"2\",\"2134\"],\"TAC\":[\"3\",\"5975\"],\"MAF\":[\"0.026\",\"0.0936\",\"0.0502\"],\"GTS\":[\"A1A1\",\"A1R\",\"RR\"],"
				+     "\"EA_GTC\":[\"0\",\"1\",\"1920\"],\"AA_GTC\":[\"0\",\"2\",\"1066\"],\"GTC\":[\"0\",\"3\",\"2986\"],\"DP\":17,\"GL\":[\".\"],\"CP\":0.1,\"CG\":0.1,\"AA\":\".\",\"CA\":[\".\"],"
				+     "\"EXOME_CHIP\":[\"no\"],\"GWAS_PUBMED\":[\".\"],\"GM\":[\".\"],\"FG\":[\"intergenic\"],\"AAC\":[\".\"],\"PP\":[\".\"],\"CDP\":[\".\"],\"GS\":[\".\"],\"PH\":[\".\"]},"
				+     "\"_id\":\".\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"TG\",\"_altAlleles\":[\"T\"],\"_minBP\":9908404,\"_maxBP\":9908405,"
				+     "\"EA\":{\"_maf\":\"\"},\"AA\":{},\"ALL\":{\"_maf\":0.000502}}";
		// { EA._maf, AA._maf }
		String[] expected =  { "", "" };
		validateFormattedValues(mFormatter, json, expected);
	}
}
