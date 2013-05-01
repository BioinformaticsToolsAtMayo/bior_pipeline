package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class NcbiGeneFormatter implements Formatter
{
	private static final JsonPath PATH_GENEID  = JsonPath.compile("GeneID");
	private static final JsonPath PATH_GENE   = JsonPath.compile("gene");
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.NCBI_GENE;
	}
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("Entrez.GeneID");
		headers.add("Gene_Symbol");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		String geneID  = FormatUtils.drill(PATH_GENEID, json);
		String gene   = FormatUtils.drill(PATH_GENE,  json);
		// TODO: implement
		values.add(geneID);
		values.add(gene);

		return values;
	}

}
