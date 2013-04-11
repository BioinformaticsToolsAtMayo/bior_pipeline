package edu.mayo.bior.pipeline.SNPEff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mayo.pipes.util.test.FileCompareUtils;
/**
 * This is a utility class that can be used for testing the SNPEFF jar output and biorsnpeff output
 * It assumes standard 8 column header for SNPEFF jar output
 * and 9 column header output with 9th column (SNPeffect in JSON) from bior_snpeffpipeline
 * @author m106573
 * 
 * Examples of how output files should look.
 * 
 * BiorSNPEFF Output 
 * #CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	SNPEff
 * 21	26960070	rs116645811	G	A	.	.	A	{"EFF":[{"Effect":"INTRON","Effect_impact":"MODIFIER","Functional_class":"NONE","Gene_name":"MRPL39","Gene_bioType":"protein_coding","Coding":"CODING","Transcript":"ENST00000352957"},{"Effect":"NON_SYNONYMOUS_CODING","Effect_impact":"MODERATE","Functional_class":"MISSENSE","Codon_change":"aCg/aTg","Amino_acid_change":"T334M","Gene_name":"MRPL39","Gene_bioType":"protein_coding","Coding":"CODING","Transcript":"ENST00000307301","Exon":"exon_21_26960013_26960101"}]}
 *
 *SNPEff Jar Output
 *
 *#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO
 *21	26960070	rs116645811	G	A	0.0	.	A;EFF=INTRON(MODIFIER||||MRPL39|protein_coding|CODING|ENST00000352957|),NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aCg/aTg|T334M|MRPL39|protein_coding|CODING|ENST00000307301|exon_21_26960013_26960101)
 *
 */
public class SNPEffOutputTest {

	@Test
	public void compareBiorToSnpJarCmd() throws IOException {
		List<String> bior = Arrays.asList("21	26960070	rs116645811	G	A	0	.	A	{\"EFF\":[{\"Effect\":\"INTRON\",\"Effect_impact\":\"MODIFIER\",\"Functional_class\":\"NONE\",\"Gene_name\":\"MRPL39\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000352957\"},{\"Effect\":\"NON_SYNONYMOUS_CODING\",\"Effect_impact\":\"MODERATE\",\"Functional_class\":\"MISSENSE\",\"Codon_change\":\"aCg/aTg\",\"Amino_acid_change\":\"T334M\",\"Gene_name\":\"MRPL39\",\"Gene_bioType\":\"protein_coding\",\"Coding\":\"CODING\",\"Transcript\":\"ENST00000307301\",\"Exon\":\"exon_21_26960013_26960101\"}]}");
		List<String> jar  = Arrays.asList("21	26960070	rs116645811	G	A	0.00	.	A;EFF=INTRON(MODIFIER||||MRPL39|protein_coding|CODING|ENST00000352957|),NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|aCg/aTg|T334M|MRPL39|protein_coding|CODING|ENST00000307301|exon_21_26960013_26960101)");
		Assert.assertTrue(testOutput(jar, bior));
	}

	public boolean testOutputFiles(String snpEffJarOutPath, String biorSnpEffOutPath) throws IOException {
		return testOutput(FileCompareUtils.loadFile(snpEffJarOutPath), FileCompareUtils.loadFile(biorSnpEffOutPath));
	}

	DecimalFormat mNumFormat = new DecimalFormat("#0.00");
	
	/**
	 * Takes two files snpeffjar output file and biorsnpeff output file and compares both
	 * @param bioroutputfilepath
	 * @param snpeffjaroutputfilepath
	 * @return boolean
	 */
	public boolean testOutput(List<String> snpEffJarOut, List<String> biorSnpEffOut) throws IOException {
		List<String> biorSnpeffOutputList = new ArrayList<String>();
		List<String> snpeffJarOutputList = new ArrayList<String>();

		for(String line : biorSnpEffOut) {
			if (line.matches("^[0-9].*$") || line.matches("^chr.*$")) {
				String[] data = line.split("\t");  
				List<SNPEffectColInfo> listcolsnp = jsonToSnpEffectCol(data[data.length-1]);
				biorSnpeffOutputList.add(variantToString(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],listcolsnp));
			}
		}	 

		for(String line : snpEffJarOut) {
			if (line.matches("^[0-9].*$") || line.matches("^chr.*$")) {
				String[] data = line.split("\t");
				String infocol;

				String[] infovalue = data[7].split("EFF=");
				if (infovalue[0].endsWith(";") && infovalue[0].length() > 1) {
					infocol = infovalue[0].substring(0,infovalue[0].lastIndexOf(";"));
				} else {
					infocol = ".";
				}

				String[] snpeffectsValue = infovalue[1].split(",");
				List<SNPEffectColInfo> snpeffectcol = new ArrayList<SNPEffectColInfo>();

				for (String snpeffect: snpeffectsValue) {
					SNPEffectColInfo snpeffectcolinf = new SNPEffectColInfo();
					String[] snpeffect1 =  snpeffect.split("\\(");
					snpeffectcolinf.setEffect(snpeffect1[0].trim());
					String[] snpeffect2 =  snpeffect1[1].split("\\|");
					snpeffectcolinf.setEffect_impact(snpeffect2[0]);
					snpeffectcolinf.setFunctional_class(snpeffect2[1]);
					snpeffectcolinf.setCodon_change(snpeffect2[2]);
					snpeffectcolinf.setAmino_acid_change(snpeffect2[3]);
					snpeffectcolinf.setGene_name(snpeffect2[4]);
					snpeffectcolinf.setGene_bioType(snpeffect2[5]);
					snpeffectcolinf.setCoding(snpeffect2[6]);
					snpeffectcolinf.setTranscript(snpeffect2[7]);
					snpeffectcolinf.setExon(snpeffect2[8].replaceAll("\\)", ""));
					snpeffectcol.add(snpeffectcolinf);
				}
				String quality = mNumFormat.format(Double.parseDouble(data[5]));
				// If it ends in ".00", then remove that since Bior just integerizes the output
				quality = quality.replace(".00", "");
				snpeffJarOutputList.add(variantToString(data[0],data[1],data[2],data[3],data[4],quality,data[6],infocol,snpeffectcol));
			}
		}

		printMismatches(biorSnpeffOutputList, snpeffJarOutputList);
		
		return biorSnpeffOutputList.equals(snpeffJarOutputList);
	}

	private void printMismatches(List<String> biorSnpeffOutputList,	List<String> snpeffJarOutputList) {
		System.out.println("snpeffJarOutputList size:  " + snpeffJarOutputList.size());
		System.out.println("biorSnpeffOutputList size: " + biorSnpeffOutputList.size());
		int numMatch = 0;
		int numTotal = 0;
		for(int i=0; i < snpeffJarOutputList.size(); i++) {
			numTotal++;
			if(biorSnpeffOutputList.get(i).equals(snpeffJarOutputList.get(i))) {
				numMatch++;
			} else {
				System.out.println("MISMATCH on line " + (i+1) + ":");
				System.out.println("    Bior:      " + biorSnpeffOutputList.get(i));
				System.out.println("    SnpEffJar: " + snpeffJarOutputList.get(i));
			}
		}
		System.out.println("Total: "+ numTotal + ", matches: " + numMatch);
	}


	/** This method returns the String representation of each row in vcf file.
	 * 
	 * @param chrm
	 * @param pos
	 * @param rsid
	 * @param ref
	 * @param alt
	 * @param qual
	 * @param filter
	 * @param info
	 * @param listcolsnp
	 * @return String representation of each row in vcf file
	 */
	private  String variantToString(String chrm, String pos, String rsid, String ref, String alt,
			String qual,String filter, String info, List<SNPEffectColInfo> listcolsnp)
	{
		return	chrm + " " + pos + " " + rsid + " " + ref + " " + alt + " " + qual + " " 
				+ filter + " " + info + " " + this.convertListToString(listcolsnp);
	}

	/**
	 * Parses SNPeffect JSON column in bior output file and returns a list of SNPEffectColObjects
	 * @param json
	 * @return List<SNPEffectColInfo>
	 */
	private static List<SNPEffectColInfo> jsonToSnpEffectCol(String json) {
		List<SNPEffectColInfo> snpeffectList = new ArrayList<SNPEffectColInfo>();

		//System.out.println("Converting string to json: " + json);
		
		if (json.contains("EFF")) { //if multiple effects are present
			JsonElement root = new JsonParser().parse(json);
			JsonArray root1 =  root.getAsJsonObject().get("EFF").getAsJsonArray();
			for ( JsonElement jsonElem : root1){
				snpeffectList.add(json2Obj(jsonElem));
			}
		} else { // When only single effect is present
			JsonElement jsonElem = new JsonParser().parse(json);
			snpeffectList.add(json2Obj(jsonElem)); 
		}
		return snpeffectList;
	}

	private static SNPEffectColInfo json2Obj(JsonElement jsonElem) {
		SNPEffectColInfo snpEffectColInfo = new SNPEffectColInfo();
		String functionalClass = getJson(jsonElem, "Functional_class");
		if("NONE".equalsIgnoreCase(functionalClass))
			functionalClass = "";
		snpEffectColInfo.setFunctional_class(	functionalClass);
		snpEffectColInfo.setCodon_change(		getJson(jsonElem, "Codon_change"));
		snpEffectColInfo.setAmino_acid_change(	getJson(jsonElem, "Amino_acid_change"));
		snpEffectColInfo.setGene_name(			getJson(jsonElem, "Gene_name"));
		snpEffectColInfo.setGene_bioType(		getJson(jsonElem, "Gene_bioType"));
		snpEffectColInfo.setCoding(				getJson(jsonElem, "Coding"));
		snpEffectColInfo.setTranscript(			getJson(jsonElem, "Transcript"));
		snpEffectColInfo.setExon(				getJson(jsonElem, "Exon"));
		snpEffectColInfo.setEffect(				getJson(jsonElem, "Effect"));
		snpEffectColInfo.setEffect_impact(		getJson(jsonElem, "Effect_impact"));
		return snpEffectColInfo;
	}
	
	private static String getJson(JsonElement jsonElem, String jsonPath) {
		String val = "";
		if( jsonElem.getAsJsonObject().has(jsonPath)) {
			val = jsonElem.getAsJsonObject().get(jsonPath).getAsString();
		}
		return val;
	}
	
	/**
	 * String representation of SNPEffectColInfo Object
	 * @param snpeffectcolinf
	 * @return String representation of SNPEffectColInfo object
	 */
	private String convertToString(SNPEffectColInfo snpeffectcolinf) {
		return snpeffectcolinf.getEffect()+"("+snpeffectcolinf.getEffect_impact() 
				+ "|" + snpeffectcolinf.getFunctional_class() + "|" + snpeffectcolinf.getCodon_change() 
				+ "|" + snpeffectcolinf.getAmino_acid_change() + "|" + snpeffectcolinf.getGene_name() 
				+ "|" + snpeffectcolinf.getGene_bioType() + "|" + snpeffectcolinf.getCoding() 
				+ "|" + snpeffectcolinf.getTranscript() + "|" + snpeffectcolinf.getExon() + ")";
	}

	/**
	 * String representation of list of SNPEffectColInfo objects
	 * @param snpeffectlist
	 * @return String
	 */
	private  String convertListToString(List<SNPEffectColInfo> snpeffectlist) {
		StringBuilder value = new StringBuilder();

		for(SNPEffectColInfo snpeffectcol : snpeffectlist) {
			value.append(convertToString(snpeffectcol)) ;
		}
		return value.toString();

	}
}
