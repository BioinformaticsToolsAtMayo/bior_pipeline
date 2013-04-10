package edu.mayo.bior.pipeline.SNPEff;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	
	
   /**
    * Takes two files snpeffjar output file and biorsnpeff output file and compares both
    * @param bioroutputfilepath
    * @param snpeffjaroutputfilepath
    * @return boolean
    */
	public boolean testOutput(String snpeffjarfilepath,String biorsnpefffilepath) throws IOException {
		
	    List<String> biorSnpeffOutputList = new ArrayList<String>();
	    List<String> snpeffJarOutputList = new ArrayList<String>();
	    
			 BufferedReader biorsnpeffReader = null;
			 String currentline1;
			 
			 biorsnpeffReader = new BufferedReader(new FileReader(biorsnpefffilepath));
			
			 while((currentline1 = biorsnpeffReader.readLine()) != null) {
				 
				 if (currentline1.matches("^[0-9].*$") || currentline1.matches("^chr.*$")) {
					 
					 
					 String[] data = currentline1.split("\t");  
		             List<SNPEffectColInfo> listcolsnp = jsonToSnpEffectCol(data[8]);
		             biorSnpeffOutputList.add(variantToString(data[0],data[1],data[2],data[3],data[4],data[5],data[6],data[7],listcolsnp));
		           
				 }
			 }	 
		 
			
			 BufferedReader snpeffJarOutputReader = null;
			 String currentline;
			 
			 snpeffJarOutputReader = new BufferedReader(new FileReader(snpeffjarfilepath));
			
			 while((currentline = snpeffJarOutputReader.readLine()) != null) {
				 if (currentline.matches("^[0-9].*$") || currentline.matches("^chr.*$")) {
					 
				
					 String[] data = currentline.split("\t");
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
				      
					
					  snpeffJarOutputList.add(variantToString(data[0],data[1],data[2],data[3],data[4],".",data[6],infocol,snpeffectcol));
					
				 }
				 
			 }
		
			 biorsnpeffReader.close();
			 snpeffJarOutputReader.close(); 
           
           if (biorSnpeffOutputList.equals(snpeffJarOutputList)) {
        	   
        	   return true;
           } else {
        	   
        	   return false;
           }
           
		
    
      
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
	private  String variantToString(String chrm, String pos,
			     String rsid, String ref, String alt, String qual,String filter, String info, List<SNPEffectColInfo> listcolsnp) {
	 return	chrm + " " + pos + " " + rsid + " " + ref + " " + alt + " " + qual + " " + filter + " " + info + " " + this.convertListToString(listcolsnp);
		
	}

	/**
	 * Parses SNPeffect JSON column in bior output file and returns a list of SNPEffectColObjects
	 * @param json
	 * @return List<SNPEffectColInfo>
	 */

	private static List<SNPEffectColInfo> jsonToSnpEffectCol(String json) {
		  
		 
          List<SNPEffectColInfo> snpeffectList = new ArrayList<SNPEffectColInfo>();
          
           if (json.contains("EFF")) { //if multiple effects are present
           	
           JsonElement root = new JsonParser().parse(json);
           
		    
		    JsonArray root1 =  root.getAsJsonObject().get("EFF").getAsJsonArray();
		   
		    for ( JsonElement aUser : root1){
		      SNPEffectColInfo snpEffectColInfo = new SNPEffectColInfo();
		      String effect= ""; 
			   String effect_impact = ""; 
			   String functional_class = "";
			   String codon_change = "";
			   String amino_acid_change = "";
			   String gene_name="";
			   String gene_bioType = "";
			   String coding = "";
			   String transcript = "";
			   String  exon = ""; 
		    effect = aUser.getAsJsonObject().get("Effect").getAsString();
		    effect_impact = aUser.getAsJsonObject().get("Effect_impact").getAsString();
		    if (! aUser.getAsJsonObject().get("Functional_class").getAsString().equalsIgnoreCase("NONE")){
		    functional_class =  aUser.getAsJsonObject().get("Functional_class").getAsString();
		          }
		    if (aUser.getAsJsonObject().has("Codon_change")) {
		    	
		    	codon_change = aUser.getAsJsonObject().get("Codon_change").getAsString();
		    }
		    
            if (aUser.getAsJsonObject().has("Amino_acid_change")) {
		    	
		    	amino_acid_change = aUser.getAsJsonObject().get("Amino_acid_change").getAsString();
		    }
		    
            if (aUser.getAsJsonObject().has("Gene_name")) {
		    	
		    	gene_name = aUser.getAsJsonObject().get("Gene_name").getAsString();
		    }
            
            if (aUser.getAsJsonObject().has("Gene_bioType")) {
		    	
		    	gene_bioType = aUser.getAsJsonObject().get("Gene_bioType").getAsString();
		    }
            if (aUser.getAsJsonObject().has("Coding")) {
		    	
		    	coding = aUser.getAsJsonObject().get("Coding").getAsString();
		    }
            if (aUser.getAsJsonObject().has("Transcript")) {
		    	
		    	transcript = aUser.getAsJsonObject().get("Transcript").getAsString();
		    }
            if (aUser.getAsJsonObject().has("Exon")) {
		    	
		    	exon = aUser.getAsJsonObject().get("Exon").getAsString();
		    }
		 
		      
            snpEffectColInfo.setEffect(effect);
            snpEffectColInfo.setEffect_impact(effect_impact);
            snpEffectColInfo.setFunctional_class(functional_class);
            snpEffectColInfo.setCodon_change(codon_change);
            snpEffectColInfo.setAmino_acid_change(amino_acid_change);
            snpEffectColInfo.setGene_name(gene_name);
            snpEffectColInfo.setGene_bioType(gene_bioType);
            snpEffectColInfo.setCoding(coding);
            snpEffectColInfo.setTranscript(transcript);
            snpEffectColInfo.setExon(exon);
            snpeffectList.add(snpEffectColInfo);
           
          }
       } else { // When only single effect is present
       	
       	JsonElement aUser = new JsonParser().parse(json);
       	SNPEffectColInfo snpEffectColInfo = new SNPEffectColInfo();
       	String effect= ""; 
		   String effect_impact = ""; 
		   String functional_class = "";
		   String codon_change = "";
		   String amino_acid_change = "";
		   String gene_name="";
		   String gene_bioType = "";
		   String coding = "";
		   String transcript = "";
		   String  exon = ""; 
       	    effect = aUser.getAsJsonObject().get("Effect").getAsString();
		    effect_impact = aUser.getAsJsonObject().get("Effect_impact").getAsString();
		    if (! aUser.getAsJsonObject().get("Functional_class").getAsString().equalsIgnoreCase("NONE")){
		    functional_class =  aUser.getAsJsonObject().get("Functional_class").getAsString();
		          }
		    if (aUser.getAsJsonObject().has("Codon_change")) {
		    	
		    	codon_change = aUser.getAsJsonObject().get("Codon_change").getAsString();
		    }
		    
             if (aUser.getAsJsonObject().has("Amino_acid_change")) {
		    	
		    	amino_acid_change = aUser.getAsJsonObject().get("Amino_acid_change").getAsString();
		    }
		    
             if (aUser.getAsJsonObject().has("Gene_name")) {
 		    	
 		    	gene_name = aUser.getAsJsonObject().get("Gene_name").getAsString();
 		    }
             
             if (aUser.getAsJsonObject().has("Gene_bioType")) {
 		    	
 		    	gene_bioType = aUser.getAsJsonObject().get("Gene_bioType").getAsString();
 		    }
             if (aUser.getAsJsonObject().has("Coding")) {
 		    	
 		    	coding = aUser.getAsJsonObject().get("Coding").getAsString();
 		    }
             if (aUser.getAsJsonObject().has("Transcript")) {
 		    	
 		    	transcript = aUser.getAsJsonObject().get("Transcript").getAsString();
 		    }
             if (aUser.getAsJsonObject().has("Exon")) {
 		    	
 		    	exon = aUser.getAsJsonObject().get("Exon").getAsString();
 		    }
		
             snpEffectColInfo.setEffect(effect);
             snpEffectColInfo.setEffect_impact(effect_impact);
             snpEffectColInfo.setFunctional_class(functional_class);
             snpEffectColInfo.setCodon_change(codon_change);
             snpEffectColInfo.setAmino_acid_change(amino_acid_change);
             snpEffectColInfo.setGene_name(gene_name);
             snpEffectColInfo.setGene_bioType(gene_bioType);
             snpEffectColInfo.setCoding(coding);
             snpEffectColInfo.setTranscript(transcript);
             snpEffectColInfo.setExon(exon);
             snpeffectList.add(snpEffectColInfo); 
       }
		return snpeffectList;
	}
   
	/**
	 * String representation of SNPEffectColInfo Object
	 * @param snpeffectcolinf
	 * @return String representation of SNPEffectColInfo object
	 */
	
	
	private String convertToString(SNPEffectColInfo snpeffectcolinf) {
		
	 
		return snpeffectcolinf.getEffect()+"("+snpeffectcolinf.getEffect_impact() + "|" + snpeffectcolinf.getFunctional_class() + "|" + snpeffectcolinf.getCodon_change() + "|" + snpeffectcolinf.getAmino_acid_change() + "|" + snpeffectcolinf.getGene_name() + "|" + snpeffectcolinf.getGene_bioType() + "|" + snpeffectcolinf.getCoding() + "|" + snpeffectcolinf.getTranscript() + "|" + snpeffectcolinf.getExon() + ")";
		
		
		
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
