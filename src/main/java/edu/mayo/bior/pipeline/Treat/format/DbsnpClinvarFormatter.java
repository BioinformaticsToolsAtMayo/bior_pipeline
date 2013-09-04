package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpClinvarFormatter implements Formatter
{
	
	private static final String[] JSON_DRILL_PATHS = {
		"INFO.CLNSIG[0]",
		"INFO.CLNDBN[0]"
	};
		
	// JSON paths
	private static final JsonPath PATH_CLNSIG  = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_CLNDBN  = JsonPath.compile(JSON_DRILL_PATHS[1]);	

	public JsonColumn getJSONColumn() {
		return JsonColumn.DBSNP_CLINVAR;
	}	
	
	public List<String> getHeaders() {
		return Arrays.asList(
				"dbSNP.ClinicalSig",
				"dbSNP.DiseaseVariant"
				);
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				 FormatUtils.drill(PATH_CLNSIG,  json),
				 FormatUtils.drill(PATH_CLNDBN,  json)
		//		translateCLNSIG( FormatUtils.drill(PATH_CLNSIG,  json) ),
		//		translateCLNDBN( FormatUtils.drill(PATH_CLNDBN,  json) )
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	


	/**
	 * Translates code value from INFO.CLNSIG field.
	 * 
	 * @param clnsig
	 * @return
	 */
	private String translateCLNSIG(String clnsig)
	{
		int code;
		try
		{
			code = Integer.parseInt(clnsig);
		} catch (NumberFormatException e) {
			return clnsig;
		}
		
		switch (code) {
			case 0:    return "unknown";
			case 1:    return "untested";
			case 2:    return "non-pathogenic";
			case 3:    return "probable-non-pathogenic";
			case 4:    return "probable-pathogenic";
			case 5:    return "pathogenic";
			case 6:    return "drug-response";
			case 7:    return "histocompatibility";
			case 255:  return "other";		
			default:   return clnsig;
		}
	}
	
	/**
	 * Translate INFO.CLNDBN into a 0/1 flag of not present or present.
	 * @param clndbn
	 * @return
	 */
	private String translateCLNDBN(String clndbn)
	{	
		clndbn = clndbn.trim();
		
		if ((clndbn.length() > 0) && (clndbn.charAt(0) != '.'))	{
			return "1";
		} else {
			return "0";
		}
	}
}
