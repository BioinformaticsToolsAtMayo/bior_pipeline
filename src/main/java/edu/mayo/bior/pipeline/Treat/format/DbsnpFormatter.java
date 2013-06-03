package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_RSID  = JsonPath.compile("ID");
	private static final JsonPath PATH_BUILD = JsonPath.compile("INFO.dbSNPBuildID");
	private static final JsonPath PATH_SSR   = JsonPath.compile("INFO.SSR");
	private static final JsonPath PATH_SAO   = JsonPath.compile("INFO.SAO");

	public JsonColumn getJSONColumn() {
		return JsonColumn.DBSNP_ALL;
	}	
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("rsID");
		headers.add("dbSNP.build");
		headers.add("dbSNP.SuspectRegion");
		headers.add("dbSNP.SNP_Allele_Origin");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();

		// execute drills
		String rsID  = FormatUtils.drill(PATH_RSID, json);
		String build = FormatUtils.drill(PATH_BUILD, json);
		String ssr   = FormatUtils.drill(PATH_SSR,  json);
		String sao   = FormatUtils.drill(PATH_SAO, json);
				
		values.add(rsID);
		values.add(build);
		values.add(translateSSR(ssr));
		values.add(translateSAO(sao));

		return values;
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
		try
		{
			code = Integer.parseInt(ssrCode);
		}
		catch (NumberFormatException e)
		{
			return ssrCode;
		}

		switch (code)
		{
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
		try
		{
			code = Integer.parseInt(saoCode);
		}
		catch (NumberFormatException e)
		{
			return saoCode;
		}

		switch (code)
		{
			case 0:  return "unspecified";
			case 1:  return "Germline";
			case 2:  return "Somatic";
			case 3:  return "Both";
			default: return saoCode;
		}
	}	
}
