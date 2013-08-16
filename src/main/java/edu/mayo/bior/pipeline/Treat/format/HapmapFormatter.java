package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HapmapFormatter implements Formatter {
	
	private static final String[] JSON_DRILL_PATHS = {
		"CEU.otherallele_freq",
		"CHB.otherallele_freq",
		"JPT.otherallele_freq",
		"YRI.otherallele_freq"
	};
	
	// Compile the json paths initially as these will be called repeatedly - once per line
	// Here we want Population information
	// Ex:  ..."CEU":{...,"otherallele_freq":0.25,...}, "CHB":{...,"otherallele_freq":0.21,...},
	//         "JPT":{...,"otherallele_freq":0.02,...}, "YRI":{...,"otherallele_freq":0.05,...}

	private static final JsonPath PATH_CEU_MAF  = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_CHB_MAF  = JsonPath.compile(JSON_DRILL_PATHS[1]);
	private static final JsonPath PATH_JPT_MAF  = JsonPath.compile(JSON_DRILL_PATHS[2]);
	private static final JsonPath PATH_YRI_MAF  = JsonPath.compile(JSON_DRILL_PATHS[3]);

	public JsonColumn getJSONColumn() {
		return JsonColumn.HAPMAP;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList(
				"HapMap.CEU_MAF",
				"HapMap.YRI_MAF",
				"HapMap.JPT_MAF",
				"HapMap.CHB_MAF"
				);
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_CEU_MAF, json),
				FormatUtils.drill(PATH_YRI_MAF, json),
				FormatUtils.drill(PATH_JPT_MAF, json),
				FormatUtils.drill(PATH_CHB_MAF, json)
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

}
