package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class SNPEffFormatter implements Formatter
{
	
	private static final JsonPath PATH_EFFECT  = JsonPath.compile("Effect");
	private static final JsonPath PATH_EFFECTIMPACT   = JsonPath.compile("Effect_impact");
	private static final JsonPath PATH_FUNCTIONALCLASS  = JsonPath.compile("Functional_class");
	private static final JsonPath PATH_CODONCHANGE   = JsonPath.compile("Codon_change");
	private static final JsonPath PATH_AMINOACID  = JsonPath.compile("Amino_acid_change");
	private static final JsonPath PATH_GENENAME   = JsonPath.compile("Gene_name");
	private static final JsonPath PATH_GENEBIOTYPE  = JsonPath.compile("Gene_bioType");
	private static final JsonPath PATH_CODING  = JsonPath.compile("Coding");
	private static final JsonPath PATH_TRANSCRIPT  = JsonPath.compile("Transcript");
	private static final JsonPath PATH_EXON   = JsonPath.compile("Exon");
	private static final JsonPath PATH_HGNC  = JsonPath.compile("HGNC");

	
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.SNPEFF;
	}

	public List<String> getHeaders()
	{
		List<String> headers = new ArrayList<String>();
		
		// TODO implement
		headers.add("SNPEFF.Effect");
		headers.add("SNPEFF.Effect_impact");
		headers.add("SNPEFF.Functional_class");
		headers.add("SNPEFF.Codon_change");
		headers.add("SNPEFF.Amino_acid_change");
		headers.add("SNPEFF.Gene_name");
		headers.add("SNPEFF.Gene_bioType");
		headers.add("SNPEFF.Coding");
		headers.add("SNPEFF.Transcript");
		headers.add("SNPEFF.Exon");
		
		return headers;
	}
	
	public List<String> format(String json)
	{
		List<String> values = new ArrayList<String>();
		
		String effect = FormatUtils.drill(PATH_EFFECT, json);
		String effectImpact = FormatUtils.drill(PATH_EFFECTIMPACT, json);
		String functionalClass = FormatUtils.drill(PATH_FUNCTIONALCLASS, json);
		String codonChange = FormatUtils.drill(PATH_CODONCHANGE, json);
		String aminoacidChange = FormatUtils.drill(PATH_AMINOACID, json);
		String geneName = FormatUtils.drill(PATH_GENENAME, json);
		String geneBioType = FormatUtils.drill(PATH_GENEBIOTYPE, json);
		String coding = FormatUtils.drill(PATH_CODING, json);
		String transcipt = FormatUtils.drill(PATH_TRANSCRIPT, json);
        String exon = FormatUtils.drill(PATH_EXON, json);
		values.add(effect);
		values.add(effectImpact);
		values.add(functionalClass);
		values.add(codonChange);
		values.add(aminoacidChange);
		values.add(geneName);
		values.add(geneBioType);
		values.add(coding);
		values.add(transcipt);
		values.add(exon);

		return values;
	}

}
