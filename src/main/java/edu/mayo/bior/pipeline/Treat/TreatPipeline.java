package edu.mayo.bior.pipeline.Treat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.util.BiorProperties;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.History;

/**
 * BioR implementation of TREAT annotation module.
 *  
 * @author Greg Dougherty, duffp
 *
 */
public class TreatPipeline extends Pipeline<History, History>
{

	private BiorProperties	mProps;	
	
	/**
	 * Constructor
	 * 
	 * @throws IOException
	 * @throws AbnormalExitException 
	 * @throws TimeoutException 
	 * @throws BrokenBarrierException 
	 * @throws InterruptedException 
	 */
	public TreatPipeline() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException
	{
		mProps = new BiorProperties ();
		
		init();
	}
	
	/**
	 * Initializes what pipes will be used for this pipeline.
	 * 
	 * @throws IOException
	 * @throws AbnormalExitException 
	 * @throws TimeoutException 
	 * @throws BrokenBarrierException 
	 * @throws InterruptedException 
	 */
	private void init() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException
	{
		// setup catalog paths
		String baseDir 			= mProps.get ("fileBase");
		String genesFile		= baseDir + mProps.get("genesFile");
		String hgncFile			= baseDir + mProps.get("hgncFile");
		String hgncIndexFile	= baseDir + mProps.get("hgncIndexFile");
		String omimFile			= baseDir + mProps.get("omimFile");
		String omimIndexFile	= baseDir + mProps.get("omimIndexFile");
		String dbsnpFile		= baseDir + mProps.get("dbsnpFile");
		String cosmicFile		= baseDir + mProps.get("cosmicFile");
		String blacklistedFile	= baseDir + mProps.get("blacklistedFile");	
		String conservationFile	= baseDir + mProps.get("conservationFile");
		String enhancerFile		= baseDir + mProps.get("enhancerFile");
		String tfbsFile			= baseDir + mProps.get("tfbsFile");
		String tssFile			= baseDir + mProps.get("tssFile");
		String uniqueFile		= baseDir + mProps.get("uniqueFile");
		String repeatFile		= baseDir + mProps.get("repeatFile");
		String regulationFile	= baseDir + mProps.get("regulationFile");
		String mirBaseFile		= baseDir + mProps.get("mirBaseFile");
		String bgiFile			= baseDir + mProps.get("bgiFile");
		String espFile			= baseDir + mProps.get("espFile");
		String hapmapFile		= baseDir + mProps.get("hapMapFile");
		String genomeFile		= baseDir + mProps.get("kGenomeFile");
		
		List<Pipe> pipes = new ArrayList<Pipe>();

//		/* ?.  VCF columns to Variant column */			pipes.add(new VEPPipeline   (new String[0], true));		
//		/* ?.  VCF columns to Variant column */			pipes.add(new SNPEFFPipeline(new String[0], true));		

		/* 1.  VCF columns to Variant column */			pipes.add(new VCF2VariantPipe());

		/* 2.  same variant w/ dbSNP */					pipes.add(new SameVariantPipe(dbsnpFile,        pipes.size() * -1)); 		
		/* 3.  same variant w/ COSMIC */				pipes.add(new SameVariantPipe(cosmicFile,       pipes.size() * -1)); 
		/* 4.  overlap Variant w/ UCSC blacklist */		pipes.add(new OverlapPipe    (blacklistedFile,  pipes.size() * -1));
		/* 5.  overlap Variant w/ UCSC conservation */	pipes.add(new OverlapPipe    (conservationFile, pipes.size() * -1));
		/* 6.  overlap Variant w/ UCSC enhancer */		pipes.add(new OverlapPipe    (enhancerFile,     pipes.size() * -1));
		/* 7.  overlap Variant w/ UCSC tfbs */			pipes.add(new OverlapPipe    (tfbsFile,         pipes.size() * -1));
		/* 8.  overlap Variant w/ UCSC tss */			pipes.add(new OverlapPipe    (tssFile,          pipes.size() * -1));
		/* 9.  overlap Variant w/ UCSC unique */		pipes.add(new OverlapPipe    (uniqueFile,       pipes.size() * -1));
		/* 10. overlap Variant w/ UCSC repeat */		pipes.add(new OverlapPipe    (repeatFile,       pipes.size() * -1));
		/* 11. overlap Variant w/ UCSC regulation */	pipes.add(new OverlapPipe    (regulationFile,   pipes.size() * -1));
		/* 12. overlap Variant w/ miRBase */			pipes.add(new OverlapPipe    (mirBaseFile,      pipes.size() * -1));

		// allele frequency annotation
		/* 13. same variant w/ BGI */					pipes.add(new OverlapPipe    (bgiFile,          pipes.size() * -1));
		/* 14. same variant w/ ESP */					pipes.add(new OverlapPipe    (espFile,          pipes.size() * -1));
		/* 15. same variant w/ Hapmap */				pipes.add(new OverlapPipe    (hapmapFile,       pipes.size() * -1));
		/* 16. same variant w/ 1k Genomes */			pipes.add(new OverlapPipe    (genomeFile,       pipes.size() * -1));

		// annotaiton requiring walking X-REFs
		/* 17. overlap Variant w/ NCBI Gene */			pipes.add(new OverlapPipe    (genesFile,        pipes.size() * -1));		
		/* 18. drill X-REF Entrez Gene ID */		 	pipes.add(new DrillPipe      (true, new String[] {"GeneID"})); 
		/* 19. lookup HGNC using Entrez Gene ID */		pipes.add(new LookupPipe     (hgncFile, hgncIndexFile, -2));
		/* 20. drill X-REF OMIM ID */					pipes.add(new DrillPipe      (true, new String[] {"mapped_OMIM_ID"}));
		/* 21. lookup OMIM using OMIM ID */				pipes.add(new LookupPipe     (omimFile, omimIndexFile, -2));
		
		this.setPipes(pipes);		
	}
}