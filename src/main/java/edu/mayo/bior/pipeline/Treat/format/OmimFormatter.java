package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class OmimFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"MIM_Number",
		"Disorders"
	};
	
	// Compile the json paths initially as these will be called repeatedly - once per line
	private static final JsonPath PATH_ID      = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_DISEASE = JsonPath.compile(JSON_DRILL_PATHS[1]);

	public JsonColumn getJSONColumn() {
		return JsonColumn.OMIM;
	}
	
	public List<String> getHeaders()  {
		return Arrays.asList("OMIM.ID", "OMIM.Disease");
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
			FormatUtils.drill(PATH_ID, json),
			FormatUtils.drill(PATH_DISEASE, json)
		);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
