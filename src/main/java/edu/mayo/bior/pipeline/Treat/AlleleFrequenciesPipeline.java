/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

/**
 *
 * @author dquest
 */

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.RemoveAllJSONPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import java.io.IOException;

/**
 *
 * @author m102417
 */
public class AlleleFrequenciesPipeline extends Pipeline {
    
        private BiorProperties biorProps;
        private String baseDir;
        
        public AlleleFrequenciesPipeline() throws IOException {
                biorProps = new BiorProperties();
                baseDir = biorProps.get("fileBase");
		init(new IdentityPipe(), new IdentityPipe());
	}
    
	public AlleleFrequenciesPipeline(Pipe input, Pipe output) throws IOException{
                biorProps = new BiorProperties();
                baseDir = biorProps.get("fileBase");
		init(input, output);
	}

        private static final String[]	geneDrill = {"gene", "GeneID"}; // , "note"
        private static final String[]	dbSnpDrill = {"INFO.dbSNPBuildID", "INFO.SSR", "INFO.SCS", "INFO.CLN", "INFO.SAO", "_id"};
        private static final String[]	bgiDrill = {"estimated_major_allele_freq", "estimated_minor_allele_freq"};
        private static final String[]	espDrill = {"INFO.EA_AC[0]","INFO.EA_AC[1]", "INFO.AA_AC[0]","INFO.AA_AC[1]", "INFO.TAC[0]", "INFO.TAC[1]", "INFO.MAF[0]", "INFO.MAF[1]", "INFO.MAF[2]"};
        private static final String[]	hapMapDrill = {"CEU.refallele_freq", "CEU.otherallele_freq", "YRI.refallele_freq", "YRI.otherallele_freq", "JPT.refallele_count", "JPT.otherallele_count", "JPT.totalcount", "CHB.refallele_count",  "CHB.otherallele_count", "CHB.totalcount"};
        private static final String[]	kGenomeDrill = {"INFO.AF", "INFO.EUR_AF", "INFO.ASN_AF", "INFO.AFR_AF", "INFO.AMR_AF"};
        
        /**
         * 
         * @param input - assumes that somehow input is converted to a history 
         * @param output 
         */
	public void init(Pipe input, Pipe output)  throws IOException{
            
            	String		genesFile = baseDir + biorProps.get("genesFile");
		String		bgiFile = baseDir + biorProps.get("bgiFile");
		String		espFile = baseDir + biorProps.get("espFile");
		String		hapMapFile = baseDir + biorProps.get("hapMapFile");
		String		dbsnpFile = baseDir + biorProps.get("dbsnpFile");
                String		kGenomeFile = baseDir + biorProps.get("kGenomeFile");
                
                
                int			posCol = -1;
            
            Pipeline p = new Pipeline(
				input,//??-history better convert it to a history comming into the pipeline e.g. HistoryInPipe				 
                    		new VCF2VariantPipe (), 
                                new OverlapPipe (genesFile), new DrillPipe (false, geneDrill), 
                                new SameVariantPipe (dbsnpFile, posCol -= geneDrill.length), 
                                new DrillPipe (false, dbSnpDrill), 
                                new SameVariantPipe (bgiFile, posCol -= dbSnpDrill.length), 
                                new DrillPipe (false, bgiDrill), 
                                new SameVariantPipe (espFile, posCol -= bgiDrill.length), 
                                new DrillPipe (false, espDrill), 
                                new SameVariantPipe (hapMapFile, posCol -= espDrill.length), 
                                new DrillPipe (false, hapMapDrill), 
                                new SameVariantPipe (kGenomeFile, posCol -= hapMapDrill.length), 
                                new DrillPipe (false, kGenomeDrill),
                                new RemoveAllJSONPipe(),
//				new HCutPipe (cut), tPipe, new HistoryOutPipe (), new PrintPipe ()
				output
				);
            this.setPipes(p.getPipes());
	}
              
    
}
