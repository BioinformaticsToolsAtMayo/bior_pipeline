package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class EspFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"EA._maf",
		"AA._maf"
	};

	// Compile the json paths initially as these will be called repeatedly - once per line
	// Here we want European and African MinorAlleleFrequency
	// Ex:  ..."EA":{"_maf":[0.000260]},"AA":{"_maf":[0.000936]},"ALL":{"_maf":[0.000502]}}
	private static final JsonPath PATH_EUROPEAN_MAF = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_AFRICAN_MAF  = JsonPath.compile(JSON_DRILL_PATHS[1]);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.ESP;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList("ESP6500.EUR_MAF", "ESP6500.AFR_MAF");
	}
	
	public List<String> format(String json)	{
		return Arrays.asList(
				FormatUtils.drill(PATH_EUROPEAN_MAF, json),
				FormatUtils.drill(PATH_AFRICAN_MAF,  json)
			);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
