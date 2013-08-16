package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HgncFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"Approved_Name",
		"Ensembl_Gene_ID"
	};
	
	private static final JsonPath PATH_GENENAME  	 = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_ENSEMBLGENEID = JsonPath.compile(JSON_DRILL_PATHS[1]);
	public JsonColumn getJSONColumn() {
		return JsonColumn.HGNC;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList("Approved_Gene_Name",  "Ensembl_Gene_ID");
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_GENENAME, json),
				FormatUtils.drill(PATH_ENSEMBLGENEID, json)
				);
	}

	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
