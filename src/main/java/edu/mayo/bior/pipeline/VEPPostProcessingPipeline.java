/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.google.gson.Gson;
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
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipeline {
    
        public static void main(String[] args){
            VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
            InputStreamPipe	in 		= new InputStreamPipe();
            PrintPipe           out             = new PrintPipe();
            Pipe p = vepp.getPipeline(in, out);

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
                Pipe            logic           = getPipeline(in, out);
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
        
        public Pipe getPipeline(Pipe input, Pipe output){
                String[] drillPath = new String[1];
                drillPath[0]= "INFO.CSQ";
                DrillPipe drill = new DrillPipe(false, drillPath);
                //##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
                //example:
                //A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||
                String[] headers = {"Allele", 
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
                                    "DISTANCE",
                                    "SIFT",
                                    "PolyPhen",
                                    "CELL_TYPE"
                    };
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
            public History compute(History history) {
                    //just lop off the last column... could extend later...
                    String json = history.get(history.size()-1);
                    HashMap hm = gson.fromJson(json, HashMap.class);
                    if(hm.containsKey("SIFT")){
                        String siftraw = (String) hm.get("SIFT");
                        //System.out.println(siftraw);
                        if(siftraw.contains("(") && siftraw.contains(")")){
                            hm.put("SIFT_TERM", parseTerm(siftraw));
                            hm.put("SIFT_Score", parseScore(siftraw));
                        }
                    }           
                    if(hm.containsKey("PolyPhen")){
                        String polyraw = (String) hm.get("PolyPhen");
                        //System.out.println(polyraw);
                        parseTerm(polyraw);
                        if(polyraw.contains("(") && polyraw.contains(")")){
                            hm.put("PolyPhen_TERM", parseTerm(polyraw));
                            hm.put("PolyPhen_Score",parseScore(polyraw) );
                        }
                    }
                    String outJson = gson.toJson(hm);
                    history.remove(history.size()-1);
                    history.add(outJson);
                    return history;
            }   
        }    
    
}
