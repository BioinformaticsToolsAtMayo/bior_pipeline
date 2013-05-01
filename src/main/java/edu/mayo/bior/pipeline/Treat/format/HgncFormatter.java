package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HgncFormatter implements Formatter
{
	
	private static final JsonPath PATH_GENENAME  = JsonPath.compile("Approved_Name");
	private static final JsonPath PATH_ENSEMBLGENEID   = JsonPath.compile("Ensembl_Gene_ID");
	public JsonColumn getJSONColumn() {
		return JsonColumn.HGNC;
	}
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("Approved_Gene_Name");
        headers.add("Ensembl_Gene_ID");		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		String geneName  = FormatUtils.drill(PATH_GENENAME, json);
		String ensemblGeneID   = FormatUtils.drill(PATH_ENSEMBLGENEID,  json);
		values.add(geneName);
		values.add(ensemblGeneID);

		return values;
	}

}
