package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class CosmicFormatter implements Formatter {
	
	private static final JsonPath PATH_MUTATIONID  = JsonPath.compile("Mutation_ID");
	private static final JsonPath PATH_MUTATIONCDS   = JsonPath.compile("Mutation_CDS");
	private static final JsonPath PATH_MUTATIONAA   = JsonPath.compile("Mutation_AA");
	private static final JsonPath PATH_GRCH37STRAND   = JsonPath.compile("Mutation_GRCh37_strand");	

	public JsonColumn getJSONColumn() {
		// TODO Auto-generated method stub
		return JsonColumn.COSMIC;
	}

	public List<String> getHeaders() {
List<String> headers = new ArrayList<String>();
		
		headers.add("COSMIC.Mutation_ID");
		headers.add("COSMIC.Mutation_CDS");
		headers.add("COSMIC.Mutation_AA");
		headers.add("COSMIC.strand");
		return headers;
	}

	public List<String> format(String json) {
		
		List<String> values = new ArrayList<String>();
		
		String mutationID = FormatUtils.drill(PATH_MUTATIONID, json);
		String mutationCDS = FormatUtils.drill(PATH_MUTATIONCDS, json);
		String mutationAA = FormatUtils.drill(PATH_MUTATIONAA, json);
		String mutationStrand = FormatUtils.drill(PATH_GRCH37STRAND, json);
		values.add(mutationID);
		values.add(mutationCDS);
		values.add(mutationAA);
		values.add(mutationStrand);
		return values;
		
	}

}
