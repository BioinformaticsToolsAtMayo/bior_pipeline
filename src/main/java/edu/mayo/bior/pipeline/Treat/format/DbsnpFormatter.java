package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"ID",
		"INFO.dbSNPBuildID",
		"INFO.SSR",
		"INFO.SAO"
	};
	
	// JSON paths
	private static final JsonPath PATH_RSID  = JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_BUILD = JsonPath.compile(JSON_DRILL_PATHS[1]);
	private static final JsonPath PATH_SSR   = JsonPath.compile(JSON_DRILL_PATHS[2]);
	private static final JsonPath PATH_SAO   = JsonPath.compile(JSON_DRILL_PATHS[3]);

	public JsonColumn getJSONColumn() {
		return JsonColumn.DBSNP_ALL;
	}	
	
	public List<String> getHeaders()
	{
		return Arrays.asList(
				"rsID",
				"dbSNP.build",
				"dbSNP.SuspectRegion",
				"dbSNP.SNP_Allele_Origin"
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}	

	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_RSID, json),
				FormatUtils.drill(PATH_BUILD, json),
				 FormatUtils.drill(PATH_SSR,  json),
				 FormatUtils.drill(PATH_SAO, json)
	//			translateSSR( FormatUtils.drill(PATH_SSR,  json) ),
	//			translateSAO( FormatUtils.drill(PATH_SAO, json) )
				);
	}
	
	/**
	 * Translates code value from INFO.SSR field.
	 * 
	 * @param ssrCode
	 * @return
	 */
	private String translateSSR(String ssrCode)
	{		
		int code;
		try	{
			code = Integer.parseInt(ssrCode);
		}catch (NumberFormatException e) {
			return ssrCode;
		}

		switch (code) {
			case 0:  return "unspecified";
			case 1:  return "Paralog";
			case 2:  return "byEST";
			case 3:  return "Para_EST";
			case 4:  return "oldAlign";
			case 5:  return "other";
			default: return ssrCode;
		}
	}
	
	/**
	 * Translates code value from INFO.SAO field.
	 * 
	 * @param saoCode
	 * @return
	 */
	private String translateSAO(String saoCode)
	{		
		int code;
		try	{
			code = Integer.parseInt(saoCode);
		}catch (NumberFormatException e) {
			return saoCode;
		}

		switch (code) {
			case 0:  return "unspecified";
			case 1:  return "Germline";
			case 2:  return "Somatic";
			case 3:  return "Both";
			default: return saoCode;
		}
	}

}
