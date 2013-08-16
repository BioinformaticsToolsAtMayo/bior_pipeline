package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class VEPFormatter implements Formatter
{
	// JSON paths		
	// order of paths is important as this is the order
	// they will be applied in the format() method
	private List<JsonPath> mPaths =
			Arrays.asList(
				JsonPath.compile("Allele"),
				JsonPath.compile("Gene"),
				JsonPath.compile("Feature"),
				JsonPath.compile("Feature_type"),
				JsonPath.compile("Consequence"),
				JsonPath.compile("cDNA_position"),
				JsonPath.compile("CDS_position"),
				JsonPath.compile("Protein_position"),
				JsonPath.compile("Amino_acids"),
				JsonPath.compile("Codons"),
				JsonPath.compile("HGNC"),
				JsonPath.compile("SIFT_TERM"),
				JsonPath.compile("SIFT_Score"),
				JsonPath.compile("PolyPhen_TERM"),
				JsonPath.compile("PolyPhen_Score")
			);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.VEP;
	}

	public List<String> getHeaders() {
		return Arrays.asList( 
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
				);
	}
	
	public List<String> format(String json) {
		List<String> values = new ArrayList<String>();
		for (JsonPath path: mPaths)	{
			// execute drill
			values.add( FormatUtils.drill(path, json) );
		}
		return values;
	}
	
	public List<String> getJsonDrillPaths() {
		List<String> jsonPaths = new ArrayList<String>();
		for(JsonPath path : mPaths) {
			jsonPaths.add(path.getPath());
		}
		return jsonPaths;
	}
}
