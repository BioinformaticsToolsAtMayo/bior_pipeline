package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_RSID  = JsonPath.compile("ID");
	private static final JsonPath PATH_SSR   = JsonPath.compile("INFO.SSR");
	private static final JsonPath PATH_SCS   = JsonPath.compile("INFO.SCS");
	private static final JsonPath PATH_CLN   = JsonPath.compile("INFO.CLN");	

	public JsonColumn getJSONColumn() {
		return JsonColumn.DBSNP;
	}	
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("rsID");
		headers.add("dbSNP.SuspectRegion");
		headers.add("dbSNP.ClinicalSig");
		headers.add("dbSNP.DiseaseVariant");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();

		// execute drills
		String rsID  = FormatUtils.drill(PATH_RSID, json);
		String ssr   = FormatUtils.drill(PATH_SSR,  json);
		String scs   = FormatUtils.drill(PATH_SCS,  json);
		String cln   = FormatUtils.drill(PATH_CLN,  json);
				
		values.add(rsID);
		values.add(translateSSR(ssr));
		values.add(translateSCS(scs));
		values.add(cln);
		
		return values;
	}

	/**
	 * Translates code value from INFO.SCS field.
	 * 
	 * @param scsCode
	 * @return
	 */
	private String translateSCS(String scsCode)
	{
		int code;
		try
		{
			code = Integer.parseInt(scsCode);
		}
		catch (NumberFormatException e)
		{
			return "";
		}
		
		switch (code)
		{
			case 0:  return "unknown";
			case 1:  return "untested";
			case 2:  return "non-pathogenic";
			case 3:  return "probable-non-pathogenic";
			case 4:  return "probable-pathogenic";
			case 5:  return "pathogenic";
			case 6:  return "drug-response";
			case 7:  return "histocompatibility";
			case 8:  return "other";		
			default: return "";
		}
	}
	
	/**
	 * Translates code value from INFO.SSR field.
	 * 
	 * @param scsCode
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
			return "";
		}

		switch (code)
		{
			case 0:  return "unspecified";
			case 1:  return "Paralog";
			case 2:  return "byEST";
			case 3:  return "Para_EST";
			case 4:  return "oldAlign";
			case 5:  return "other";
			default: return "";
		}
	}	
}
