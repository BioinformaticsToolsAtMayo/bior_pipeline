package edu.mayo.bior.pipeline.Treat.format;

import java.util.Arrays;
import java.util.List;

import com.jayway.jsonpath.JsonPath;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

public class SNPEffFormatter implements Formatter
{
	private static final String[] JSON_DRILL_PATHS = {
		"Effect",
		"Effect_impact",
		"Functional_class",
		"Codon_change",
		"Amino_acid_change",
		"Gene_name",
		"Gene_bioType",
		"Coding",
		"Transcript",
		"Exon",
		"HGNC"
	};
	
	private static final JsonPath PATH_EFFECT  			= JsonPath.compile(JSON_DRILL_PATHS[0]);
	private static final JsonPath PATH_EFFECTIMPACT   	= JsonPath.compile(JSON_DRILL_PATHS[1]);
	private static final JsonPath PATH_FUNCTIONALCLASS  = JsonPath.compile(JSON_DRILL_PATHS[2]);
	private static final JsonPath PATH_CODONCHANGE   	= JsonPath.compile(JSON_DRILL_PATHS[3]);
	private static final JsonPath PATH_AMINOACID  		= JsonPath.compile(JSON_DRILL_PATHS[4]);
	private static final JsonPath PATH_GENENAME   		= JsonPath.compile(JSON_DRILL_PATHS[5]);
	private static final JsonPath PATH_GENEBIOTYPE 		= JsonPath.compile(JSON_DRILL_PATHS[6]);
	private static final JsonPath PATH_CODING  			= JsonPath.compile(JSON_DRILL_PATHS[7]);
	private static final JsonPath PATH_TRANSCRIPT  		= JsonPath.compile(JSON_DRILL_PATHS[8]);
	private static final JsonPath PATH_EXON   			= JsonPath.compile(JSON_DRILL_PATHS[9]);
	private static final JsonPath PATH_HGNC  			= JsonPath.compile(JSON_DRILL_PATHS[10]);
	
	public JsonColumn getJSONColumn() {
		return JsonColumn.SNPEFF;
	}

	public List<String> getHeaders() {
		return Arrays.asList(
				"SNPEFF.Effect",
				"SNPEFF.Effect_impact",
				"SNPEFF.Functional_class",
				"SNPEFF.Codon_change",
				"SNPEFF.Amino_acid_change",
				"SNPEFF.Gene_name",
				"SNPEFF.Gene_bioType",
				"SNPEFF.Coding",
				"SNPEFF.Transcript",
				"SNPEFF.Exon"
				);
	}
	
	public List<String> format(String json) {
		return Arrays.asList(
				FormatUtils.drill(PATH_EFFECT, json),
				FormatUtils.drill(PATH_EFFECTIMPACT, json),
				FormatUtils.drill(PATH_FUNCTIONALCLASS, json),
				FormatUtils.drill(PATH_CODONCHANGE, json),
				FormatUtils.drill(PATH_AMINOACID, json),
				FormatUtils.drill(PATH_GENENAME, json),
				FormatUtils.drill(PATH_GENEBIOTYPE, json),
				FormatUtils.drill(PATH_CODING, json),
				FormatUtils.drill(PATH_TRANSCRIPT, json),
				FormatUtils.drill(PATH_EXON, json)
				);
	}
	
	public List<String> getJsonDrillPaths() {
		return Arrays.asList(JSON_DRILL_PATHS);
	}
}
