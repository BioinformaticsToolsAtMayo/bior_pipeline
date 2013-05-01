package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class BgiFormatter implements Formatter
{
	
	private static final JsonPath PATH_BGIDANISHMAF  = JsonPath.compile("estimated_minor_allele_freq");
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.BGI;
	}
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("BGI200_Danish_MAF");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		String danishMAF  = FormatUtils.drill(PATH_BGIDANISHMAF, json);
		values.add(danishMAF);

		return values;
	}
	
}
