/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HCutPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import java.io.IOException;

/**
 *
 * @author m102417
 */
public class OverlapingFeaturesPipeline extends Pipeline {

        private BiorProperties biorProps;
        private String baseDir;
        
        //properties to extract...
        private static final String[]	kGeneDrill = {"gene", "GeneID"};
        private static final String[]	kDbSnpDrill = {"INFO.dbSNPBuildID", "INFO.SSR", "INFO.SCS", "INFO.CLN", "INFO.SAO", "_id"};
        private static final String[]	kHGNCDrill = {"Approved_Symbol", "Entrez_Gene_ID", "Ensembl_Gene_ID"};
        private static final String[]	kCosmicDrill = {"Mutation_ID", "Mutation_CDS", "Mutation_AA", "Mutation_GRCh37_strand"};
        private static final String[]	kOMIMDrill = {"Disorders"};
        private static final String[]	kBlacklistDrill = {"score"};
        private static final String[]	kConservationDrill = {"score"};
        private static final String[]	kEnhancerDrill = {"score"};
        private static final String[]	kTFBSDrill = {"score"};
        private static final String[]	kTSSDrill = {"score"};
        private static final String[]	kUniqueDrill = {"score"};
        private static final String[]	kRepeatDrill = {"repName"};
        private static final String[]	kRegulationDrill = {"name"};
        
    
        public OverlapingFeaturesPipeline() throws IOException {
            biorProps = new BiorProperties();
            baseDir = biorProps.get("fileBase");
            init(new IdentityPipe(), new IdentityPipe());
	}
    
	public OverlapingFeaturesPipeline(Pipe input, Pipe output) throws IOException{
            biorProps = new BiorProperties();
            baseDir = biorProps.get("fileBase");
            init(input, output);
	}

	public void init(Pipe input, Pipe output) throws IOException { 
            
            	String		genesFile = baseDir + biorProps.get("genesFile");
		String		hgncFile = baseDir + biorProps.get("hgncFile");
		String		dbsnpFile = baseDir + biorProps.get("dbsnpFile");
		String		cosmicFile = baseDir + biorProps.get("cosmicFile");
		String		hgncIndexFile = baseDir + biorProps.get("hgncIndexFile");
		String		omimFile = baseDir + biorProps.get("omimFile");
		String		omimIndexFile = baseDir + biorProps.get("omimIndexFile");
		String		conservationFile = baseDir + biorProps.get("conservationFile");
		String		repeatFile = baseDir + biorProps.get("repeatFile");
		String		regulationFile = baseDir + biorProps.get("regulationFile");
		String		uniqueFile = baseDir + biorProps.get("uniqueFile");
		String		tssFile = baseDir + biorProps.get("tssFile");
		String		tfbsFile = baseDir + biorProps.get("tfbsFile");
		String		enhancerFile = baseDir + biorProps.get("enhancerFile");
		String		blacklistedFile = baseDir + biorProps.get("blacklistedFile");
		String[]	geneDrill = kGeneDrill;
		String[]	hgncDrill = kHGNCDrill;
		String[]	dbSnpDrill = kDbSnpDrill;
		String[]	cosmicDrill = kCosmicDrill;
		String[]	omimDrill = kOMIMDrill;
		String[]	blacklistDrill = kBlacklistDrill;
		String[]	conservationDrill = kConservationDrill;
		String[]	enhancerDrill = kEnhancerDrill;
		String[]	tfbsDrill = kTFBSDrill;
		String[]	tssDrill = kTSSDrill;
		String[]	uniqueDrill = kUniqueDrill;
		String[]	repeatDrill = kRepeatDrill;
		String[]	regulationDrill = kRegulationDrill;
		int			posCol = -1;
            
            
            //requires history as input, history as output
            Pipeline p = new Pipeline (input, 
                    new VCF2VariantPipe (), 
                    new OverlapPipe (genesFile), new DrillPipe (false, geneDrill), 
                    new LookupPipe (hgncFile, hgncIndexFile, (posCol -= geneDrill.length) + 1), 
                    new DrillPipe (false, hgncDrill), 
                    new SameVariantPipe (dbsnpFile, posCol -= hgncDrill.length), 
                    new DrillPipe (false, dbSnpDrill), 
                    new SameVariantPipe (cosmicFile, posCol -= dbSnpDrill.length), 
                    new DrillPipe (false, cosmicDrill), 
                    new LookupPipe (omimFile, omimIndexFile, (posCol -= cosmicDrill.length) + 1), 
                    new DrillPipe (false, omimDrill), 
                    new OverlapPipe (blacklistedFile, posCol -= omimDrill.length), 
                    new DrillPipe (false, blacklistDrill), 
                    new OverlapPipe (conservationFile, posCol -= blacklistDrill.length), 
                    new DrillPipe (false, conservationDrill), 
                    new OverlapPipe (enhancerFile, posCol -= conservationDrill.length), 
                    new DrillPipe (false, enhancerDrill), 
                    new OverlapPipe (tfbsFile, posCol -= enhancerDrill.length), 
                    new DrillPipe (false, tfbsDrill), 
                    new OverlapPipe (tssFile, posCol -= tfbsDrill.length), 
                    new DrillPipe (false, tssDrill), 
                    new OverlapPipe (uniqueFile, posCol -= tssDrill.length), 
                    new DrillPipe (false, uniqueDrill), 
                    new OverlapPipe (repeatFile, posCol -= uniqueDrill.length), 
                    new DrillPipe (false, repeatDrill), 
                    new OverlapPipe (regulationFile, posCol -= repeatDrill.length), 
                    new DrillPipe (false, regulationDrill),
                    new HCutPipe(new int[posCol]),
                    output
                    );
            this.setPipes(p.getPipes());
	}
}
