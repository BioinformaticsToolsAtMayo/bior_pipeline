package edu.mayo.bior.pipeline.VEP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Michael Meiners (m054457)
 * Date created: Apr 26, 2013
 */
public class VepFunctionsTest {
	private Gson mGson = new Gson();
	VepFunctions mVepFunc = new VepFunctions();


	@Test
	public void parseEffects() {
		String actual = mVepFunc.vepCsqToJsonList(
				"CSQ=" +
				"A|ENSG00000154719|ENST00000352957|Transcript|intron_variant|||||||MRPL39||||," + 
				"A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||||," +
				"A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||tolerated(0.05)|benign(0.001)|"
				).toString();
		String expected = 
			 "[{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000352957\",\"Feature_type\":\"Transcript\",\"Consequence\":\"intron_variant\",\"HGNC\":\"MRPL39\"},"
			+ "{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000307301\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"1043\",\"CDS_position\":\"1001\",\"Protein_position\":\"334\",\"Amino_acids\":\"T/M\",\"Codons\":\"aCg/aTg\",\"HGNC\":\"MRPL39\"},"
			+ "{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000307301\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"1043\",\"CDS_position\":\"1001\",\"Protein_position\":\"334\",\"Amino_acids\":\"T/M\",\"Codons\":\"aCg/aTg\",\"HGNC\":\"MRPL39\",\"SIFT\":\"tolerated(0.05)\",\"PolyPhen\":\"benign(0.001)\",\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":0.05,\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.001}"
			+ "]";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getWorstBlank() {
		JsonArray jsonArray = mVepFunc.vepCsqToJsonList("CSQ=");
		String actual = mVepFunc.getWorstCase(jsonArray).toString();
		String expected = "{}";
		assertEquals(expected, actual);
	}
	
	@Test
	public void getWorstNoScores() {
		JsonArray jsonArray = mVepFunc.vepCsqToJsonList(
			"CSQ=" +
			"A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||LINC00515|4432|||," +
			"A|ENSG00000154719|ENST00000352957|Transcript|intron_variant|||||||MRPL39||||," + 
			"A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||||");
		String actual = mVepFunc.getWorstCase(jsonArray).toString();
		String expected = "{}";
		assertEquals(expected, actual);
	}
	
	@Test
	/** 3 scores given for SIFT and Polyphen, but only one can be chosen */
	public void getWorstOf3Scores() {
		JsonArray jsonArray = mVepFunc.vepCsqToJsonList(
				"CSQ=" +
				"A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||LINC00515|4432||missense(0.30)|tolerated(0.04)|," +
				"A|ENSG00000154719|ENST00000352957|Transcript|intron_variant|||||||MRPL39||tolerated(0.31)|benign(0.24)|," + 
				"A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||tolerated(0.05)|benign(0.001)|");
			String actual = mVepFunc.getWorstCase(jsonArray).toString();
			String expected = "{\"Allele\":\"A\",\"Gene\":\"ENSG00000154719\",\"Feature\":\"ENST00000352957\",\"Feature_type\":\"Transcript\",\"Consequence\":\"intron_variant\",\"HGNC\":\"MRPL39\","
				+ "\"SIFT\":\"tolerated(0.31)\",\"PolyPhen\":\"benign(0.24)\","
				+ "\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":0.31,"
				+ "\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.24}";
			assertEquals(expected, actual);
	}

	@Test
	public void getWorstNoSiftScores() {
		JsonArray jsonArray = mVepFunc.vepCsqToJsonList(
				"CSQ=A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39|||benign(0.001)|");
			String actual = mVepFunc.getWorstCase(jsonArray).toString();
			String expected = "{}";
			assertEquals(expected, actual);
	}
	
	@Test
	public void getWorstNoPolyphenScores() {
		JsonArray jsonArray = mVepFunc.vepCsqToJsonList(
				"CSQ=A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||||");
			String actual = mVepFunc.getWorstCase(jsonArray).toString();
			String expected = "{}";
			assertEquals(expected, actual);
	}
	
	private JsonObject toJsonObj(String jsonStr) {
		return new JsonObject().get(jsonStr).getAsJsonObject();
	}
	
	private JsonArray toJsonArray(String jsonArrayStr) {
		return (JsonArray)(new JsonParser().parse(jsonArrayStr));
	}
}
