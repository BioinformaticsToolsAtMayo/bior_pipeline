package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class UcscRepeatFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_SCORE  = JsonPath.compile("swScore");	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.UCSC_REPEAT;
	}

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("UCSC.Repeat_Region");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		// execute drills
		String swScore  = FormatUtils.drill(PATH_SCORE, json);
				
		values.add(swScore);

		return values;
	}

}
