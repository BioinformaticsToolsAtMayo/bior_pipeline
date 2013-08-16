package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class CosmicFormatter implements Formatter {
	
	private static final String[] JSON_DRILL_PATHS = {
		"Mutation_ID",
		"Mutation_CDS",
		"Mutation_AA",
		"Mutation_GRCh37_strand"
	};
		
	private static final JsonPath PATH_MUTATIONID  	= JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_MUTATIONCDS  = JsonPath.compile(JSON_DRILL_PATHS[1]);
	private static final JsonPath PATH_MUTATIONAA   = JsonPath.compile(JSON_DRILL_PATHS[2]);
	private static final JsonPath PATH_GRCH37STRAND = JsonPath.compile(JSON_DRILL_PATHS[3]);	

	public JsonColumn getJSONColumn() {
		return JsonColumn.COSMIC;
	}

	public List<String> getHeaders() {
		return Arrays.asList(
				"COSMIC.Mutation_ID",
				"COSMIC.Mutation_CDS",
				"COSMIC.Mutation_AA",
				"COSMIC.strand"
				);
	}

	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_MUTATIONID, json),
				FormatUtils.drill(PATH_MUTATIONCDS, json),
				FormatUtils.drill(PATH_MUTATIONAA, json),
				FormatUtils.drill(PATH_GRCH37STRAND, json)
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
