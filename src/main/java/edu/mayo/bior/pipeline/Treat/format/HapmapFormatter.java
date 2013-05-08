package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class HapmapFormatter implements Formatter {
	// Compile the json paths initially as these will be called repeatedly - once per line
	// Here we want Population information
	// Ex:  ..."CEU":{...,"otherallele_freq":0.25,...}, "CHB":{...,"otherallele_freq":0.21,...},
	//         "JPT":{...,"otherallele_freq":0.02,...}, "YRI":{...,"otherallele_freq":0.05,...}

	private static final JsonPath PATH_CEU_MAF  = JsonPath.compile("CEU.otherallele_freq");
	private static final JsonPath PATH_CHB_MAF  = JsonPath.compile("CHB.otherallele_freq");
	private static final JsonPath PATH_JPT_MAF  = JsonPath.compile("JPT.otherallele_freq");
	private static final JsonPath PATH_YRI_MAF  = JsonPath.compile("YRI.otherallele_freq");

	public JsonColumn getJSONColumn() {
		return JsonColumn.HAPMAP;
	}
	
	public List<String> getHeaders() {
		return Arrays.asList(
				"HapMap.CEU_MAF",
				"HapMap.CHB_MAF",
				"HapMap.JPT_MAF",
				"HapMap.YRI_MAF"
				);
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_CEU_MAF, json),
				FormatUtils.drill(PATH_CHB_MAF, json),
				FormatUtils.drill(PATH_JPT_MAF, json),
				FormatUtils.drill(PATH_YRI_MAF, json)
				);
	}
}
