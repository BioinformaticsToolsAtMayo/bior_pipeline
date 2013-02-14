/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

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
import com.google.gson.JsonObject;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipeline {
    
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
        
        public Pipe getPipeline(Pipe input, Pipe output){
            return getPipeline(input, output, whichPipeline);
        }
        
        public Pipe getPipeline(Pipe input, Pipe output, String whichPipeline){
            this.whichPipeline = whichPipeline;
            if(whichPipeline.equalsIgnoreCase("CartesianProduct")){
                return getCartesianProductPipeline(input, output);
            }
            if(whichPipeline.equalsIgnoreCase("WorstScenario")){
                return getWorstCasePipeline(input, output);
            }
            return getCartesianProductPipeline(input, output);
        }
        
    
        public static void main(String[] args){
            VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
            InputStreamPipe	in 		= new InputStreamPipe();
            PrintPipe           out             = new PrintPipe();
            Pipe p = vepp.getPipeline(in, out, "WorstScenario");
            
            for(int i=0; i<args.length; i++){
                if(args[i].contains("a") ||
                     args[i].contains("-a") ||
                     args[i].contains("-all") ||
                     args[i].contains("--all") ){
                     p = vepp.getPipeline(in, out, "CartesianProduct");
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
		InputStreamPipe	in 		= new InputStreamPipe();
                PrintPipe       out             = new PrintPipe();
                Pipe            logic           = getPipeline(in, out, whichPipeline);
		HistoryOutPipe	historyOut      = new HistoryOutPipe();
		PrintPipe	print           = new PrintPipe();
		
		// pipeline definition
		Pipe<InputStream, List<String>> pipeline = new Pipeline<InputStream, List<String>>
			(
					in,			// each STDIN line	--> String
					logic,		// history			--> modified history*
					historyOut,	// history*			--> String
					print		// String			--> STDOUT
			);
		
		// prime pipeline with STDIN stream
                pipeline.setStarts(Arrays.asList(System.in));

                // run pipeline
                while (pipeline.hasNext()) {
                        pipeline.next();
                }		
	}
        
        private static String[] headers = {"Allele", 
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
        
        public Pipe getCartesianProductPipeline(Pipe input, Pipe output){
                String[] drillPath = new String[1];
                drillPath[0]= "INFO.CSQ";
                DrillPipe drill = new DrillPipe(false, drillPath);
                //##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
                //example:
                //A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||

                Delim2JSONPipe pipes2json = new Delim2JSONPipe(-1, false,  headers, "|");
                Pipe fixSiftPoly = new TransformFunctionPipe<History,History>(new FixSiftandPolyphen());

                Pipe p = new Pipeline(input,//the output of vep
                                    new HistoryInPipe(),
                                    new VCF2VariantPipe(), 
                                    new FindAndReplaceHPipe(8,"CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
                                    drill,
                                    new FanPipe(),
                                    pipes2json,
                                    fixSiftPoly,
                                    new HistoryOutPipe(),
                                    output);
                return p;
        }
        
        public Pipe getWorstCasePipeline(Pipe input, Pipe output){
                String[] drillPath = new String[1];
                drillPath[0]= "INFO.CSQ";
                DrillPipe drill = new DrillPipe(false, drillPath);
                Pipe worstvep = new TransformFunctionPipe<History,History>(new PickWorstVEP());
                Pipe p = new Pipeline(input,//the output of vep
                    new HistoryInPipe(),
                    new VCF2VariantPipe(), 
                    new FindAndReplaceHPipe(8,"CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
                    drill,
                    worstvep,
                    new HistoryOutPipe(),
                    output);
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
                        //System.out.println(csq);
                        //sift
                        //System.out.println(split[12]);                        
                        //polyphen
                        //System.out.println(split[13]);
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
                            //System.out.println(sscore);
                            //System.out.println(sterm);
                            //System.out.println(pscore);
                            //System.out.println(pterm);
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
                //System.out.println("*************  WE PICK  ****************");
                //System.out.println(outJson);
                //System.out.println("*************  WE PICK  ****************");
                
                return outJson;
            }
            
            private Gson gson = new Gson();
            public History compute(History a) {
                setup();
                String get = a.remove(a.size()-1);
                //System.out.println(get);
                if(get.startsWith("[")){
                    ArrayList<String> csqs = gson.fromJson(get, ArrayList.class);
                    String worststr = handleCSQ(csqs);
                    a.add(worststr);
                }else{
                    a.add("{}"); //if we did not get any matches back, thats fine, just tack on null json.
                }
                return a;
            }  
            
            private boolean isFirst = true;
            
            protected void setup(){     
                //if it is the first call to the pipe... fix the history...
                if(isFirst){
                    isFirst = false;
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
                //System.out.println(term);
                return term;
            }
            public Double parseScore(String s){
                int i = s.indexOf("(");
                int j = s.indexOf(")");                
                String number = s.substring(i+1, j);
                //System.out.println(number);
                return Double.parseDouble(number);
            }
            Gson gson = new Gson();
            boolean first = false;
            public History compute(History history) {
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
                        //System.out.println(siftraw);
                        if(siftraw.contains("(") && siftraw.contains(")")){
                            hm.put(siftterm, parseTerm(siftraw));
                            hm.put(siftscore, parseScore(siftraw));
                        }
                    }           
                    if(hm.containsKey("PolyPhen")){
                        String polyraw = (String) hm.get("PolyPhen");
                        //System.out.println(polyraw);
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
                    return history;
            }   
        }  
        

}
