package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class ThousandGenomesFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"INFO.ASN_AF",
		"INFO.AMR_AF",
		"INFO.AFR_AF",
		"INFO.EUR_AF"
	};
	
	private static final JsonPath PATH_ASN = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_AMR = JsonPath.compile(JSON_DRILL_PATHS[1]);
	private static final JsonPath PATH_AFR = JsonPath.compile(JSON_DRILL_PATHS[2]);
	private static final JsonPath PATH_EUR = JsonPath.compile(JSON_DRILL_PATHS[3]);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.THOUSAND_GENOMES;
	}

	public List<String> getHeaders() {
		return Arrays.asList(
				"1000Genomes.ASN_AF",
				"1000Genomes.AMR_AF",
				"1000Genomes.AFR_AF",
				"1000Genomes.EUR_AF"
				);
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_ASN, json),
				FormatUtils.drill(PATH_AMR, json),
				FormatUtils.drill(PATH_AFR, json),
				FormatUtils.drill(PATH_EUR, json)
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	
}
