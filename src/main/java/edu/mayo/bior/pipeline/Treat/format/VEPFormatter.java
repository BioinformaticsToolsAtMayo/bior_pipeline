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

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("VEP.Allele");
		headers.add("VEP.Gene");
		headers.add("VEP.Feature");
		headers.add("VEP.Feature_type");
		headers.add("VEP.Consequence");
		headers.add("VEP.cDNA_position");
		headers.add("VEP.CDS_position");
		headers.add("VEP.Protein_position");
		headers.add("VEP.Amino_acids");
		headers.add("VEP.Codons");
		headers.add("VEP.HGNC");
		headers.add("SIFT.TERM");
		headers.add("SIFT.Score");
		headers.add("PolyPhen.TERM");
		headers.add("PolyPhen.Score");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		
		List<String> values = new ArrayList<String>();
		
		for (JsonPath path: mPaths)
		{
			// execute drill
			String value = FormatUtils.drill(path, json);
			
			// save value
			values.add(value);
		}
		
		return values;
	}

}
