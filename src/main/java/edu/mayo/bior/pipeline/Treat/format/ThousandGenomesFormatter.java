package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class ThousandGenomesFormatter implements Formatter
{
	private static final JsonPath PATH_ASN  = JsonPath.compile("INFO.ASN_AF");
	private static final JsonPath PATH_AMR   = JsonPath.compile("INFO.AMR_AF");
	private static final JsonPath PATH_AFR  = JsonPath.compile("INFO.AFR_AF");
	private static final JsonPath PATH_EUR   = JsonPath.compile("INFO.EUR_AF");
	public JsonColumn getJSONColumn() {
		return JsonColumn.THOUSAND_GENOMES;
	}

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("1000Genomes.ASN_AF");
		headers.add("1000Genomes.AMR_AF");
		headers.add("1000Genomes.AFR_AF");
		headers.add("1000Genomes.EUR_AF");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		String asnAf  = FormatUtils.drill(PATH_ASN, json);
		String amrAf  = FormatUtils.drill(PATH_AMR, json);
		String afrAf  = FormatUtils.drill(PATH_AFR, json);
		String eurAf  = FormatUtils.drill(PATH_EUR, json);
		values.add(asnAf);
		values.add(amrAf);
		values.add(afrAf);
		values.add(eurAf);

		return values;
	}

}
