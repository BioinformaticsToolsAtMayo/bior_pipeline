package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class DbsnpClinvarFormatter implements Formatter
{
	// JSON paths
	private static final JsonPath PATH_CLNSIG  = JsonPath.compile("INFO.CLNSIG[0]");
	private static final JsonPath PATH_CLNDBN  = JsonPath.compile("INFO.CLNDBN[0]");	

	public JsonColumn getJSONColumn() {
		return JsonColumn.DBSNP_CLINVAR;
	}	
	
	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		headers.add("dbSNP.ClinicalSig");
		headers.add("dbSNP.DiseaseVariant");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();

		// execute drills
		String clnsig   = FormatUtils.drill(PATH_CLNSIG,  json);
		String clndbn   = FormatUtils.drill(PATH_CLNDBN,  json);
				
		values.add(translateCLNSIG(clnsig));
		values.add(translateCLNDBN(clndbn));
		
		return values;
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
		}
		catch (NumberFormatException e)
		{
			return clnsig;
		}
		
		switch (code)
		{
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
		
		if ((clndbn.length() > 0) && (clndbn.charAt(0) != '.'))
		{
			return "1";
		}
		else
		{
			return "0";
		}
	}
}
