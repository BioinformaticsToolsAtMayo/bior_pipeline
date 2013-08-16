package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class BgiFormatter implements Formatter
{
	
	private static final String[] JSON_DRILL_PATHS = {
		"estimated_minor_allele_freq"
	};
	
	private static final JsonPath PATH_BGIDANISHMAF  = JsonPath.compile(JSON_DRILL_PATHS[0]);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.BGI;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList("BGI200_Danish_MAF");
	}
	
	public List<String> format(String json) {
		return Arrays.asList( FormatUtils.drill(PATH_BGIDANISHMAF, json) );
	}

	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	
	
}
