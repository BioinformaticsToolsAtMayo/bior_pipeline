package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class VEPHgncFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_UNIPROT_ID  = JsonPath.compile("UniProt_ID");	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.VEP_HGNC;
	}
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("UniprotID");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		// execute drills
		String uniprotID  = FormatUtils.drill(PATH_UNIPROT_ID, json);
				
		values.add(uniprotID);

		return values;
	}

}
