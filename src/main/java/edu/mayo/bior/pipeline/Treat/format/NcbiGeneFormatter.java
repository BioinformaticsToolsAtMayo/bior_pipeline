package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class NcbiGeneFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = { 
		"GeneID",
		"gene"
	};
	
	private static final JsonPath PATH_GENEID = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_GENE   = JsonPath.compile(JSON_DRILL_PATHS[1]);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.NCBI_GENE;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList(
				"Entrez.GeneID",
				"Gene_Symbol"
				);
	}
	
	public List<String> format(String json)	{
		return Arrays.asList(
				FormatUtils.drill(PATH_GENEID, json),
				FormatUtils.drill(PATH_GENE,  json)
				);
	}

	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
