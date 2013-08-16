package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class MirBaseFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = { "ID" };

	private static final JsonPath PATH_MIRBASEID  = JsonPath.compile( JSON_DRILL_PATHS[0] );
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.MIRBASE;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList("miRBASE.ID");
	}
	
	public List<String> format(String json)	{
		return Arrays.asList( FormatUtils.drill(PATH_MIRBASEID, json) );
	}

	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
