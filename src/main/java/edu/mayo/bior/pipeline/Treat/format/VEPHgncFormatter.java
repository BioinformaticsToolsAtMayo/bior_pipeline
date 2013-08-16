package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class VEPHgncFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = { "UniProt_ID" };
	// JSON paths
	private static final JsonPath PATH_UNIPROT_ID  = JsonPath.compile(JSON_DRILL_PATHS[0]);	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.VEP_HGNC;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList( "UniprotID" );
	}
	
	public List<String> format(String json)	{
		return Arrays.asList( FormatUtils.drill(PATH_UNIPROT_ID, json) );
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	
}
