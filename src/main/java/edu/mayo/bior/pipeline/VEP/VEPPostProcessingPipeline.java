/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.VEP;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryMetaData;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.JSONUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipeline {

	private static Logger sLogger = Logger.getLogger(VEPPostProcessingPipeline.class);
			

	
	public VEPPostProcessingPipeline(){

	}

	public VEPPostProcessingPipeline(String whichPipeline){
		this.whichPipeline = whichPipeline;
	}

	/**
	 * whichPipeline - what vep pipeline do you want to execute for postprocessing?
	 * 1. CartesianProduct - get all the vep results and fan them out replicating the data before
	 * 2. WorstScenario    - do your best to pick the worst outcome and report only that
	 */
	private String whichPipeline = "CartesianProduct";

	public void setWhichPipeline(String whichPipeline) {
		this.whichPipeline = whichPipeline;
	}

	public Pipe getPipeline(Pipe input, Pipe output, boolean includeHistory){
		return getPipeline(input, output, whichPipeline, includeHistory);
	}

	public Pipe getPipeline(Pipe input, Pipe output, String whichPipeline1, boolean includeHistory){
		this.whichPipeline = whichPipeline1;
		if(whichPipeline.equalsIgnoreCase("CartesianProduct")){
			return getCartesianProductPipeline(input, output, includeHistory);
		}
		if(whichPipeline.equalsIgnoreCase("WorstScenario")){
			return getWorstCasePipeline(input, output, includeHistory);
		}
		return getCartesianProductPipeline(input, output, includeHistory);
	}


	public static void main(String[] args){
		VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
		InputStreamPipe	in 		= new InputStreamPipe();
		PrintPipe           out             = new PrintPipe();
		Pipe p = vepp.getPipeline(in, out, "WorstScenario", true);

		for(int i=0; i<args.length; i++){
			if(args[i].contains("a") ||
					args[i].contains("-a") ||
					args[i].contains("-all") ||
					args[i].contains("--all") ){
				p = vepp.getPipeline(in, out, "CartesianProduct", true);
			}
		}

		p.setStarts(Arrays.asList(System.in));
		while(p.hasNext()){
			p.next();
		}
	}

	/**
	 * Executes the given Pipe like a stream-compatible UNIX command.
	 * 
	 * @param logic A Pipe that takes a HISTORY as input and output.
	 */
	public void execute() {

		// pipes
		InputStreamPipe	in 			= new InputStreamPipe();
		PrintPipe       out        	= new PrintPipe();
		Pipe            logic       = getPipeline(in, out, whichPipeline, true);

		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
		(
				in,			// each STDIN line	--> String
				logic,		// history			--> modified history*
				new HistoryOutPipe(),	// history*			--> String
				new PrintPipe()		// String			--> STDOUT
				);

		// prime pipeline with STDIN stream
		pipeline.setStarts(Arrays.asList(System.in));

		// run pipeline
		while (pipeline.hasNext()) {
			pipeline.next();
		}		
	}

	public final static String[] headers = {
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

	/** Modify the INFO column and remove the CSQ attribute that was added by VEP, and do one of the following:
	 *  If INFO column contains ";CSQ=" (in which case something was previously in the info column): 
	 *  	Replace ";CSQ=.*" with nothing
	 *  Else (
	 *  	Replace "CSQ=.*" with "."  
	 * @author Michael Meiners (m054457)
	 * Date created: Apr 19, 2013
	 */
	public static class RemoveVepCsqFieldPipe implements PipeFunction<History, History> {
		@Override
		public History compute(History historyObj) {
			History history = (History)historyObj;
			sLogger.info("RemoveVepCsqFieldPipe.compute: (before): " + history);
			sLogger.info("RemoveVepCsqFieldPipe.compute  (header): " + History.getMetaData().getColumnHeaderRow("\t"));
			String info = history.get(7);
			if(info.contains(";CSQ="))
				info = info.replaceAll(";CSQ=.*", "");
			else if( info.contains("CSQ=") )
				info = info.replaceAll("CSQ=.*", ".");
			history.set(7, info);
			sLogger.info("RemoveVepCsqFieldPipe.compute: (after): " + history);
			return history;
		}
	}

	/**
	 * 
	 * @param input
	 * @param output
	 * @param includeHistory - if you want historyInPipe and historyOutPipe on the pipeline
	 * @return 
	 */
	public Pipe getCartesianProductPipeline(Pipe input, Pipe output, boolean isIncludeHistory){
		;
		//##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
		//example:
		//A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||

		Delim2JSONPipe pipes2json = new Delim2JSONPipe(-1, false,  headers, "|");
		Pipe fixSiftPoly = new TransformFunctionPipe<History,History>(new FixSiftandPolyphen());

		Pipe p = new Pipeline(input,//the output of vep
				isIncludeHistory ? new HistoryInPipe() : new IdentityPipe(),
				new VCF2VariantPipe(), 
				new TransformFunctionPipe(new RemoveVepCsqFieldPipe()),
				//new FindAndReplaceHPipe(8,";CSQ=.*|CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
				new DrillPipe(false, new String[] { "INFO.CSQ" } ),
				new FanPipe(),
				pipes2json,
				fixSiftPoly,
				isIncludeHistory ? new HistoryOutPipe() : new IdentityPipe(),
				output);
		return p;
	}

	
	/**
	 * 
	 * @param input
	 * @param output
	 * @param includeHistory - if you want historyInPipe and historyOutPipe on the pipeline
	 * @return 
	 */
	public Pipe getWorstCasePipeline(Pipe input, Pipe output, boolean isIncludeHistory){
		Pipe worstvep = new TransformFunctionPipe<History,History>(new PickWorstVEP());
		Pipe p = new Pipeline(input,//the output of vep
				isIncludeHistory ? new HistoryInPipe() : new IdentityPipe(),
				new VCF2VariantPipe(), 
				new TransformFunctionPipe(new RemoveVepCsqFieldPipe()),
				//new FindAndReplaceHPipe(8,"CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
				new DrillPipe(false, new String[] { "INFO.CSQ" } ),
				worstvep,
				isIncludeHistory ? new HistoryOutPipe() : new IdentityPipe(),
				output
				);
		return p;
	}

	/**
	 * pickWorstVEP attempts to select the worst case scenario of all of the CSQ outputs that are availible for a given input.
	 */
	public static class PickWorstVEP implements PipeFunction<History, History> {

		public String jsonize(String[] csq){
			JsonObject jobj = new JsonObject();
			for(int i=0; i<csq.length; i++){
				if(csq[i].length() > 0 && headers.length >= i){
					jobj.addProperty(headers[i], csq[i]);
				}
			}
			jobj.addProperty(siftterm, worstSiftTerm);
			jobj.addProperty(siftscore, worstsift);
			jobj.addProperty(polyterm, worstPolyTerm);
			jobj.addProperty(polyscore, worstPoly);
			return jobj.toString();
		}

		FixSiftandPolyphen fix = new FixSiftandPolyphen();
		String[] worstCaseCSQ;
		Double worstsift = 1.0;
		String worstSiftTerm = "";
		Double worstPoly = Double.MAX_VALUE;
		String worstPolyTerm = "";
		
		private String handleCSQ(ArrayList<String> csqs){
			boolean nohits = true;
			worstsift = 2.0;
			worstSiftTerm = "";
			worstPoly = -1.0;
			worstPolyTerm = "";
			for(int i=0; i< csqs.size(); i++){
				String csq = csqs.get(i);
				String[] split = csq.split("\\|");
				if(split.length > 13){
					//sLogger.info(csq);
					//sift
					//sLogger.info(split[12]);                        
					//polyphen
					//sLogger.info(split[13]);
					if(split[13].contains("(") && split[13].contains("(")){
						nohits = false;
						//sift
						Double sscore = fix.parseScore(split[13]);
						String sterm = fix.parseTerm(split[13]);  
						//polyphen
						Double pscore = 0.0;
						String pterm = "not_predicted";
						if(split.length > 14){
							pscore = fix.parseScore(split[14]);
							pterm = fix.parseTerm(split[14]);
						}
						//sLogger.info(sscore);
						//sLogger.info(sterm);
						//sLogger.info(pscore);
						//sLogger.info(pterm);
						//if this one beats everything we have seen on both counts...
						if(sscore <= worstsift && pscore >= worstPoly){
							worstsift = sscore;
							worstSiftTerm = sterm;
							worstPoly = pscore;
							worstPolyTerm = pterm;
							worstCaseCSQ = split;
						}
					}

				}

			}

			if(nohits == true){
				return "{}";  //no significant sift or polyphen value
			}

			String outJson = jsonize(worstCaseCSQ);
			//sLogger.info("*************  WE PICK  ****************");
			//sLogger.info(outJson);
			//sLogger.info("*************  WE PICK  ****************");

			return outJson;
		}

		private Gson gson = new Gson();
		public History compute(History history) {
			//sLogger.debug("PickWorstVEP.compute (before): " + history);
			//String headerBefore = History.getMetaData().getColumnHeaderRow("\t");
			//sLogger.debug("PickWorstVEP.compute (header): " + headerBefore);
			setup();
			String vepCsq = history.remove(history.size()-1);
			//sLogger.info(vepCsq);
			if(vepCsq.startsWith("[")) {
				ArrayList<String> csqs = gson.fromJson(vepCsq, ArrayList.class);
				String worststr = handleCSQ(csqs);
				history.add(worststr);
			}else{
				history.add("{}"); //if we did not get any matches back, thats fine, just tack on null json.
			}
			//sLogger.debug("PickWorstVEP.compute (after): " + history);
			//String headerAfter = History.getMetaData().getColumnHeaderRow("\t");
			//sLogger.debug("PickWorstVEP.compute (header-after): " + headerAfter);
			return history;
		}  

		private boolean isFirst = true;

		protected void setup(){     
			//if it is the first call to the pipe... fix the history...
			if(isFirst){
				isFirst = false;
				// Remove the column "INFO.CSQ"
				History.getMetaData().getColumns().remove(History.getMetaData().getColumns().size()-1);
				// add column meta data
				List<ColumnMetaData> cols = History.getMetaData().getColumns();
				ColumnMetaData cmd = new ColumnMetaData("VEP");
				cols.add(cmd);
				
			}        
		}
	}
	private static final String siftterm = "SIFT_TERM";
	private static final String polyterm = "PolyPhen_TERM";
	private static final String siftscore = "SIFT_Score";
	private static final String polyscore = "PolyPhen_Score";

	public static class FixSiftandPolyphen implements PipeFunction<History,History> {
		public String parseTerm(String s){
			int i = s.indexOf("(");
			String term = s.substring(0, i);
			//sLogger.info(term);
			return term;
		}
		public Double parseScore(String s){
			int i = s.indexOf("(");
			int j = s.indexOf(")");                
			String number = s.substring(i+1, j);
			//sLogger.info(number);
			return Double.parseDouble(number);
		}
		Gson gson = new Gson();
		boolean first = false;
		
		public History compute(History history) {
			//sLogger.debug("FixSiftAndPolyphen.compute (before): " + history);
			//sLogger.debug("FixSiftAndPolyphen.compute (header): " + History.getMetaData().getColumnHeaderRow("\t"));
			if(first == false){
				HistoryMetaData meta = history.getMetaData();
				ColumnMetaData cmd = new ColumnMetaData("VEP");
				List<ColumnMetaData> cols = meta.getColumns();
				cols.remove(cols.size()-1);
				cols.add(cmd);
				first = true;
			}

			//just lop off the last column... could extend later...
			String json = history.get(history.size()-1);
			HashMap hm = gson.fromJson(json, HashMap.class);
			if(hm.containsKey("SIFT")){
				String siftraw = (String) hm.get("SIFT");
				//sLogger.info(siftraw);
				if(siftraw.contains("(") && siftraw.contains(")")){
					hm.put(siftterm, parseTerm(siftraw));
					hm.put(siftscore, parseScore(siftraw));
				}
			}           
			if(hm.containsKey("PolyPhen")){
				String polyraw = (String) hm.get("PolyPhen");
				//sLogger.info(polyraw);
				parseTerm(polyraw);
				if(polyraw.contains("(") && polyraw.contains(")")){
					hm.put(polyterm, parseTerm(polyraw));
					hm.put(polyscore,parseScore(polyraw) );
				}
			}
			//String outJson = gson.toJson(hm);
			String outJson = JSONUtil.computeJSON(hm, true);
			history.remove(history.size()-1);
			history.add(outJson);
			//sLogger.debug("FixSiftAndPolyphen.compute (after): " + history);
			return history;
		}   
	}  


}
