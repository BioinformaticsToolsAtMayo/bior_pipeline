package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class UcscRegulationFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_ID  = JsonPath.compile("id");	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.UCSC_REGULATION;
	}

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("UCSC.regulation");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		// execute drills
		String id  = FormatUtils.drill(PATH_ID, json);
				
		values.add(id);

		return values;
	}

}
