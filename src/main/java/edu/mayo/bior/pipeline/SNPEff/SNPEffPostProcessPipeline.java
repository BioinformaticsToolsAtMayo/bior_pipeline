/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.SNPEff.SNPEffHelper.InfoFieldKey;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.HeaderPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
/**
 *
 * @author m089716
 */
public class SNPEffPostProcessPipeline {
	
	private static final Logger log = Logger.getLogger(UnixStreamCommand.class);
	
	boolean summarizeEffect = true;
	
	public static final String SNPEFF_EFFECT_METADATA_DELIMITER = "[()]";
    public static final String SNPEFF_EFFECT_METADATA_SUBFIELD_DELIMITER = "\\|";
	
	/**
	 * SNPEff outputs more than one Effect for a variant.. if 'summarizeEffect' is set to true
	 * only the 'MostSignificantEffect' is shown. Which is default behavior in TreatWorkflow.
	 * @param summarizeEffect
	 */
	public SNPEffPostProcessPipeline(boolean summarizeEffect) {
		this.summarizeEffect = summarizeEffect;
	}

	 private static String[] headers = {"Effect", 
         "Effect_impact", 
         "Functional_class",
         "Codon_change",
         "Amino_acid_change",
         "Gene_name",
         "Gene_bioType",
         "Coding",
         "Transcript",
         "Exon"
	 };
	 
	/**
	 * 
	 * @param input - the output line/string of snpeff
	 * @param exe
	 * @param transform
	 * @param output - formatted/parsed result file from snpeff
	 * @return
	 */
	public Pipe getSNPEffPostProcessPipeline(Pipe input, Pipe output) {		
        
        Pipe<History,History> transform = new TransformFunctionPipe<History,History>(new SNPEffTransformPipe(this.summarizeEffect));
         
        Pipe pipe = new Pipeline(
            input,
            new VCFProgram2HistoryPipe(), //used to remove all header lines from the input 
            transform,            
            output
        );
        
        return pipe;
	}
        
        public TransformFunctionPipe getSNPEffTransformPipe(boolean showMostSignificantEffectOnly){
            return new TransformFunctionPipe( new SNPEffTransformPipe(showMostSignificantEffectOnly) );
        }
	
	/**
	 * 
	 * @author m089716
	 *
	 */
	public static class SNPEffTransformPipe implements PipeFunction<History, History> {

		boolean showMostSignificantEffectOnly = true;
		
		public SNPEffTransformPipe(boolean showMostSignificantEffectOnly) {
			this.showMostSignificantEffectOnly = showMostSignificantEffectOnly;
		}
				
       // @Override
        public History compute(History history) {
            //throw new UnsupportedOperationException("Not supported yet.");
        	
        	String parsedEffValue = this.parseEFFColumnData(history);
        	
        	//add the parsed-effect-value as a json string to the end of history
        	history.add(parsedEffValue);
        	
        	return history;
        }
        
        
        /**
         * 
         * @param history
         * @return parses the EFF column data from the SNPEff output file and returns either 
         * the 'most-significant-effect-as-a-json-string or all-effects-as-a-string-of-json-arrays 
         */
        private String parseEFFColumnData(History history) {
        	
        	String rawEff="";
        	String parsedEff="";
        	
        	Map<String, String> splitEffectCoreValues;
        	List<SNPEffectHolder> snpEffectHolderObjs = new ArrayList<SNPEffectHolder>();
        	
        	SNPEffectHolder snpEffectHolder = null; 
        	
        	String attrib_effect = "";
        	
        	try {
	        	if (history.size() >= 7) {
	        		if (history.get(7)!=null && !history.get(7).equals("")) {
	        			if (history.get(7).contains("EFF=")) {        			
	        		
		        			rawEff = history.get(7); //last column has EFF
		        			String rawEffValue = rawEff.substring(rawEff.indexOf("EFF=")+4, rawEff.length());
		        			
		        			List<String> allEffects = null;
		        			
		        			try {
		        				allEffects = Arrays.asList(rawEffValue.split(",")); //EXON(|||), NON(|||), etc
		        			} catch (Exception ex) {
		                		log.error("SNPEffPostProcess Failed with message:: Effect values are not wellformed!!" + ex.getMessage());
		                	}
		        			
		        			for (String effect : allEffects) {
		        				attrib_effect = effect.substring(0, effect.indexOf("("));
		        				//System.out.println("attrib_effect="+attrib_effect);
		        				
		        				splitEffectCoreValues = new HashMap<String, String>();
		        				
		        				//System.out.println(InfoFieldKey.EFFECT_KEY.getKeyName());
		        				splitEffectCoreValues.put(InfoFieldKey.EFFECT_KEY.getKeyName(), attrib_effect);
		        				
		        				String effectCoreValues = effect.substring(effect.indexOf("(")+1, effect.indexOf(")"));
		        				//System.out.println("core="+effectCoreValues);
		        				
		        				String[] splitValues = effectCoreValues.split(SNPEFF_EFFECT_METADATA_SUBFIELD_DELIMITER);
		        				//System.out.println(Arrays.asList(splitValues));
		        				
		        				for(int i=0;i<=splitValues.length;i++) {
		        					splitEffectCoreValues.put(InfoFieldKey.IMPACT_KEY.getKeyName(), splitValues[0]);
		        					
		        					if (splitValues.length > 1) {
		        						splitEffectCoreValues.put(InfoFieldKey.FUNCTIONAL_CLASS_KEY.getKeyName(), splitValues[1]);
		        					} else {        						
		        						splitEffectCoreValues.put(InfoFieldKey.FUNCTIONAL_CLASS_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 2) {
		        						splitEffectCoreValues.put(InfoFieldKey.CODON_CHANGE_KEY.getKeyName(), splitValues[2]);
		        					} else {
		        						splitEffectCoreValues.put(InfoFieldKey.CODON_CHANGE_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 3) {
		        						splitEffectCoreValues.put(InfoFieldKey.AMINO_ACID_CHANGE_KEY.getKeyName(), splitValues[3]);
		        					} else {
		        						splitEffectCoreValues.put(InfoFieldKey.AMINO_ACID_CHANGE_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 4) {
		        						splitEffectCoreValues.put(InfoFieldKey.GENE_NAME_KEY.getKeyName(), splitValues[4]);
		        					} else {
		        						splitEffectCoreValues.put(InfoFieldKey.GENE_NAME_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 5) {
		        						splitEffectCoreValues.put(InfoFieldKey.GENE_BIOTYPE_KEY.getKeyName(), splitValues[5]);
		        					} else {
		        						splitEffectCoreValues.put(InfoFieldKey.GENE_BIOTYPE_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 6) {
		        						splitEffectCoreValues.put(InfoFieldKey.CODING.getKeyName(), splitValues[6]);
		        					} else {        						
		        						splitEffectCoreValues.put(InfoFieldKey.CODING.getKeyName(), "");
		        					}        					
		        					
		        					if (splitValues.length > 7) {
		        						splitEffectCoreValues.put(InfoFieldKey.TRANSCRIPT_ID_KEY.getKeyName(), splitValues[7]);
		        					} else {        						
		        						splitEffectCoreValues.put(InfoFieldKey.TRANSCRIPT_ID_KEY.getKeyName(), "");
		        					}
		        					
		        					if (splitValues.length > 8) {        						
		        						splitEffectCoreValues.put(InfoFieldKey.EXON_ID_KEY.getKeyName(), splitValues[8]);
		        					} else {        						
		        						//In the Effect, ExonId is the last column and is sometimes empty. In that case, add it explicitly 
		        						splitEffectCoreValues.put(InfoFieldKey.EXON_ID_KEY.getKeyName(), "");
		        					}
		        				}
		        				
		        				snpEffectHolder = new SNPEffectHolder(splitEffectCoreValues);
		        				snpEffectHolderObjs.add(snpEffectHolder);
		        			}
		        			
		        			if (this.showMostSignificantEffectOnly) {
		        				// Add only annotations for one of the most biologically-significant effect from this set:
		        				SNPEffectHolder mostSignificantEffect = SNPEffHelper.getMostSignificantEffect(snpEffectHolderObjs);
		        				//System.out.println("mostSignificantEffect="+mostSignificantEffect.toString());    
		        				parsedEff = jsonize(mostSignificantEffect.getAnnotationAsList());
		        			} else {
		        				// get individual effects, add them to an array, build a json array (using jsonize below)
		        				String outJson = "";
		        				List<String> resultsJsonStrings = new ArrayList<String>();
		        				for(SNPEffectHolder snpEffectHolderObj : snpEffectHolderObjs) {
		        					outJson = jsonize(snpEffectHolderObj.getAnnotationAsList());
		        					resultsJsonStrings.add(outJson);
		        				}
		        				
		        				parsedEff = buildJsonArray(resultsJsonStrings);
		        			}
	        			} else if (history.get(7).contains("SNPEFFERR=")) {
		        			// Parse the error message
		        			String message = history.get(7).substring(history.get(7).indexOf("SNPEFFERR="), history.get(7).length());
		        			//System.out.println(message);	
		        			if (!message.equals("")) {
		        				JsonObject jObj = new JsonObject();
		        				jObj.addProperty("SNPEffMessage", message);
		        				jObj.addProperty("Status", "SNPEff failed to assign function to this variant");
		        				
		        				//System.out.println(jObj.toString());
		        				parsedEff = jObj.toString();
		        			} else {
		        				log.error("SNPEffPostProcess failed with message::Cannot retrieve error message from SNPEff results!");
		        			}    			
	        			
	        			} else {
	        				log.error("SNPEffPostProcess failed with message::INFO column does not have valid EFF or SNPEFFERROR to parse!");
	        			}
	        			
	        		} else {
	        			log.error("SNPEffPostProcess failed with message::Cannot Process SNPEff Result, INFO column not found!");
	        		}
	        	} else {
	        		log.error("SNPEffPostProcess failed with message::Cannot Process SNPEff Result, INFO column not found!");
	        	}
	        	
        	} catch (Exception ex) {
        		log.error("SNPEffPostProcess Failed with message::" + ex.getMessage());
        	}
        	
        	return parsedEff;
        }
       

		/**
         * Convert List<String> to Json
         * @param EFF = [[UPSTREAM, MODIFIER, , , , , LINC00515, antisense, NON_CODING, ENST00000567517, ]]
         * @return as below:
         * {"Effect":"UPSTREAM","Effect_impact":"MODIFIER","Gene_name":"LINC00515","Gene_bioType":"antisense","Coding":"NON_CODING","Transcript":"ENST00000567517"}
         */
        private String jsonize(List<String> eff){
            JsonObject jObj = new JsonObject();
            
            try {
	            for(int i=0; i<eff.size(); i++){            	
	                if(eff.get(i).length() > 0 && headers.length >= i){
	                	//System.out.println(headers[i]);
	                    jObj.addProperty(headers[i], eff.get(i));
	                }
	            }            
            } catch (Exception ex) {
            	log.error(ex.getMessage());
            }
            
            return jObj.toString();
        }
        
        /**
         * Convert List<json strings> to an "Array of Json" 
         * @param resultsJsonStrings = "resultsJsonStrings" contains individual json strings, this method constructs a JSON formatted arry with those values
         * @return as below:         * 
         * "EFF":
         * [
         * 		{
         * 			"Effect":"INTRON"
         * 			"Effect_Impact":"MODERATE"
         * 			...
         * 		},
         * 		{
         * 			"Effect":"INTRON"
         * 			"Effect_Impact":"MODERATE"
         * 			...
         * 		}
         * ]
         */
        private String buildJsonArray(List<String> resultsJsonStrings) {
        	
        	// TODO use json dom to generate json array, instead of building the array manually
        	
        	StringBuilder arrayOfJsons = new StringBuilder();
        	
        	arrayOfJsons.append("{"); //"{":
        	arrayOfJsons.append("\""); //"EFF":
        	arrayOfJsons.append("EFF"); //"EFF":
        	arrayOfJsons.append("\""); //"EFF":
        	arrayOfJsons.append(":"); //"EFF":
        	arrayOfJsons.append("["); //"[":
        	
        	for(ListIterator<String> it = resultsJsonStrings.listIterator(); it.hasNext() ;) {
        		//outJson has one set of results.. 
        		arrayOfJsons.append(it.next());
        		
        		if (it.hasNext()) {
        			arrayOfJsons.append(",");
        		}
        	}
        	
        	arrayOfJsons.append("]"); //"]":
        	arrayOfJsons.append("}"); //"{":
        	
        	return arrayOfJsons.toString();
        }
	}

    
}
