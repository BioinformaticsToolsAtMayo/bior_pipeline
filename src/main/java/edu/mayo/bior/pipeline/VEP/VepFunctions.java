package edu.mayo.bior.pipeline.VEP;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Michael Meiners (m054457)
 * Date created: Apr 25, 2013
 */
public class VepFunctions {
	private final static String[] headers = {
		"Allele",
		"Gene",
		"Feature",
		"Feature_type",
		"Consequence",
		"cDNA_position",
		"CDS_position",
		"Protein_position",
		"Amino_acids",
		"Codons",
		"Existing_variation",
		"HGNC",
		"DISTANCE",
		"SIFT",
		"PolyPhen",
		"CELL_TYPE"
	};
	
	class Effect {
		public String term;
		public Double score;
	}
	
	/** Turn CSQ result ("CSQ=x|y|z") into json ("{CSQ:{"a":"x","b":"y","c":"z"}}")  */
	public JsonArray vepCsqToJsonList(String vepCsq) {		
		JsonArray jsonArray = new JsonArray();		
		if (vepCsq.contains("VEPERR")) {
			JsonObject jObj = new JsonObject();
			jObj.addProperty("VEPMessage", "VEPERRORMessage");
			jObj.addProperty("Status", "VEP failed to assign function to this variant");
			jsonArray.add(jObj);
		} else {
			String[] vepCsqItems = vepCsq.replace("CSQ=", "").split(",");
			for(String csqItem : vepCsqItems) {
				jsonArray.add(vepCsqToJson(csqItem));
			}
		}
		
		return jsonArray;
			
	}
	
	
	/** Turn CSQ result into JSON
	 *  Ex CSQ: "A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg||MRPL39||tolerated(0.05)|benign(0.001)|"
	 *  Ex JSON:"{"Allele":"A","Gene":"ENSG00000154719","Feature":"ENST00000307301","Feature_type":"Transcript","Consequence":"missense_variant","cDNA_position":"1043","CDS_position":"1001","Protein_position":"334","Amino_acids":"T/M","Codons":"aCg/aTg","HGNC":"MRPL39","SIFT":"tolerated(0.05)","PolyPhen":"benign(0.001)","SIFT_TERM":"tolerated","SIFT_Score":0.05,"PolyPhen_TERM":"benign","PolyPhen_Score":0.001}"  */
	private JsonObject vepCsqToJson(String vepCsqItem) {
		String[] split = vepCsqItem.split("\\|");
		JsonObject jsonObj = new JsonObject();

		// NOTE: the split ignores any empty columns at the end, so there may be fewer than 16 after split 
		// For ex: "a||b|||" will only have 3 columns
		for(int i=0; i < Math.min(split.length, headers.length); i++) {
			if( ! "".equals(split[i]))
				jsonObj.addProperty(headers[i], split[i]);
		}
		
		boolean isSiftPresent = false;
		if( split.length > 13 && split[13].contains("(") && split[13].contains(")") ) {
			isSiftPresent = true;
			Effect sift = parseEffect(split[13]);
			jsonObj.addProperty("SIFT_TERM", sift.term);
			jsonObj.addProperty("SIFT_Score", sift.score);
		}
		
		// TODO: These had been the defaults - are they necessary?  Should we populate polyphen with defaults if not given?
		//Double pscore = 0.0;
		//String pterm = "not_predicted";
		if( split.length > 14 && split[14].contains("(") && split[14].contains(")") ) {
			Effect polyphen = parseEffect(split[14]);
			jsonObj.addProperty("PolyPhen_TERM", polyphen.term);
			jsonObj.addProperty("PolyPhen_Score", polyphen.score);
		} // else if( isSiftPresent ) {
			// set default values for polyphen
		//}
		return jsonObj;
	}

	/** Convert string (ex: "tolerated(0.05)") to an Effect object */
	private Effect parseEffect(String effStr) {
		int idxOpenParen = effStr.indexOf("(");
		int idxCloseParen= effStr.indexOf(")");
		
		if(idxOpenParen == -1 || idxCloseParen == -1 )
			return null;
		
		Effect effect = new Effect();
		effect.term = effStr.substring(0, idxOpenParen);
		effect.score = Double.parseDouble(effStr.substring(idxOpenParen+1, idxCloseParen));
		return effect;
	}
	
	/** Given a list of vepCsqOutputs as JSON strings, return the one that has the worst outcome */
	public JsonObject getWorstCase(JsonArray jsonArray) {
		Double worstSift = 1.0;
		Double worstPoly = Double.MAX_VALUE;

		// NOTE: if the worstJson object is never set, it will be converted to string "{}"
		JsonObject worstJson = new JsonObject();
		
		// TODO: What if EITHER sift or polyphen is not given???????????
		for(int i=0; i < jsonArray.size(); i++) {
			JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();
			JsonElement siftScore = jsonObj.get("SIFT_Score");
			JsonElement polyphenScore = jsonObj.get("PolyPhen_Score");
			// Ignore this item if it does not have a score to compare
			if(siftScore == null || polyphenScore == null)
				continue;
			// If we don't have a worst yet, then add this one, else compare the scores to see if this one's is worse
			if(worstJson.entrySet().isEmpty()  ||  (siftScore.getAsDouble() <= worstSift && polyphenScore.getAsDouble() >= worstPoly) ){
				worstSift = siftScore.getAsDouble();
				worstPoly = polyphenScore.getAsDouble();
				worstJson = jsonObj;
			}
		}
		
		return worstJson;
	}
	
}
