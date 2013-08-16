package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class UcscRegulationFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = { "id" };
	
	// JSON paths
	private static final JsonPath PATH_ID  = JsonPath.compile(JSON_DRILL_PATHS[0]);	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.UCSC_REGULATION;
	}

	public List<String> getHeaders() {
		return Arrays.asList( "UCSC.regulation" );
	}
	
	public List<String> format(String json) {
		return Arrays.asList( FormatUtils.drill(PATH_ID, json) );
	}

	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	
}
