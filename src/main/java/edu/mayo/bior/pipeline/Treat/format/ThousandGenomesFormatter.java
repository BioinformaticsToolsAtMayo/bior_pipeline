package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class ThousandGenomesFormatter implements Formatter
{
	public JsonColumn getJSONColumn() {
		return JsonColumn.THOUSAND_GENOMES;
	}

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("TODO_" + getClass().getName());
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		// TODO: implement
		values.add("TODO");

		return values;
	}

}
