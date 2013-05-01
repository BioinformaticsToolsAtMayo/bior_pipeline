package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class MirBaseFormatter implements Formatter
{
	private static final JsonPath PATH_MIRBASEID  = JsonPath.compile("ID");
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.MIRBASE;
	}
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("miRBASE.ID");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		String mirBaseId = FormatUtils.drill(PATH_MIRBASEID, json);
		values.add(mirBaseId);

		return values;
	}

}
