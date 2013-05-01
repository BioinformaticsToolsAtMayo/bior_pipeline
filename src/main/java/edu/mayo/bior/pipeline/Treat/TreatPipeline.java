package edu.mayo.bior.pipeline.Treat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.cmd.SNPEffCommand;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.bior.pipeline.Treat.format.BgiFormatter;
import edu.mayo.bior.pipeline.Treat.format.DbsnpFormatter;
import edu.mayo.bior.pipeline.Treat.format.EspFormatter;
import edu.mayo.bior.pipeline.Treat.format.Formatter;
import edu.mayo.bior.pipeline.Treat.format.FormatterPipeFunction;
import edu.mayo.bior.pipeline.Treat.format.HapmapFormatter;
import edu.mayo.bior.pipeline.Treat.format.HgncFormatter;
import edu.mayo.bior.pipeline.Treat.format.MirBaseFormatter;
import edu.mayo.bior.pipeline.Treat.format.NcbiGeneFormatter;
import edu.mayo.bior.pipeline.Treat.format.OmimFormatter;
import edu.mayo.bior.pipeline.Treat.format.SNPEffFormatter;
import edu.mayo.bior.pipeline.Treat.format.ThousandGenomesFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscBlacklistedFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscConservationFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscEnhancerFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscRegulationFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscRepeatFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscTfbsFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscTssFormatter;
import edu.mayo.bior.pipeline.Treat.format.UcscUniqueFormatter;
import edu.mayo.bior.pipeline.Treat.format.VEPFormatter;
import edu.mayo.bior.pipeline.Treat.format.VEPHgncFormatter;
import edu.mayo.bior.pipeline.VEP.VEPPipeline;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HCutPipe;
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
	
	private List<Formatter> mFormatters = new ArrayList<Formatter>();
	
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
		
		initFormatters();
		
		initPipes();
	}
	/**
	 * Initializes the formatters to transform the raw JSON annotation into the
	 * final output format.  The order of the formatters is significant as this
	 * carries over to the final output column order.
	 */
	private void initFormatters() {
		mFormatters.add(new BgiFormatter());
		mFormatters.add(new DbsnpFormatter());
		mFormatters.add(new EspFormatter());
		mFormatters.add(new HapmapFormatter());
		mFormatters.add(new HgncFormatter());
		mFormatters.add(new MirBaseFormatter());
		mFormatters.add(new NcbiGeneFormatter());
		mFormatters.add(new OmimFormatter());
		mFormatters.add(new SNPEffFormatter());
		mFormatters.add(new ThousandGenomesFormatter());
		mFormatters.add(new UcscBlacklistedFormatter());
		mFormatters.add(new UcscConservationFormatter());
		mFormatters.add(new UcscEnhancerFormatter());
		mFormatters.add(new UcscRegulationFormatter());
		mFormatters.add(new UcscRepeatFormatter());
		mFormatters.add(new UcscTfbsFormatter());
		mFormatters.add(new UcscTssFormatter());
		mFormatters.add(new UcscUniqueFormatter());
		mFormatters.add(new VEPFormatter());
		mFormatters.add(new VEPHgncFormatter());
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
	private void initPipes() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException
	{
		// setup catalog paths
		String baseDir 			= mProps.get ("fileBase");
		String genesFile		= baseDir + mProps.get("genesFile");
		String hgncFile			= baseDir + mProps.get("hgncFile");
		String hgncIndexFile	= baseDir + mProps.get("hgncIndexFile");
		String hgncEnsGeneIdx	= baseDir + mProps.get("hgncEnsemblGeneIndexFile");		
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
		
		// tracks the order of the added JSON columns
		List<JsonColumn> order = new ArrayList<JsonColumn>();


		// 1ST JSON column is the original variant
		order.add(JsonColumn.VARIANT);				pipes.add(new VCF2VariantPipe());
		
		order.add(JsonColumn.VEP);					pipes.add(new VEPPipeline    (new String[0], true));		
		/* add Ensembl Gene X-REF */				pipes.add(new DrillPipe      (true, new String[] {"Gene"})); 
		order.add(JsonColumn.VEP_HGNC);				pipes.add(new LookupPipe     (hgncFile, hgncEnsGeneIdx, -2));
		/* remove Ensembl Gene X-REF*/				pipes.add(new HCutPipe       (new int[] {-3}));
		
		order.add(JsonColumn.SNPEFF);				pipes.add(new SNPEFFPipeline (new String[]{SNPEffCommand.DEFAULT_GENOME_VERSION}, true));
		
		order.add(JsonColumn.DBSNP);				pipes.add(new SameVariantPipe(dbsnpFile,        order.size() * -1 + 1)); 		
		order.add(JsonColumn.COSMIC);				pipes.add(new SameVariantPipe(cosmicFile,       order.size() * -1 + 1)); 
		order.add(JsonColumn.UCSC_BLACKLISTED);		pipes.add(new OverlapPipe    (blacklistedFile,  order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_CONSERVATION);	pipes.add(new OverlapPipe    (conservationFile, order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_ENHANCER);		pipes.add(new OverlapPipe    (enhancerFile,     order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_TFBS);			pipes.add(new OverlapPipe    (tfbsFile,         order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_TSS);				pipes.add(new OverlapPipe    (tssFile,          order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_UNIQUE);			pipes.add(new OverlapPipe    (uniqueFile,       order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_REPEAT);			pipes.add(new OverlapPipe    (repeatFile,       order.size() * -1 + 1));
		order.add(JsonColumn.UCSC_REGULATION);		pipes.add(new OverlapPipe    (regulationFile,   order.size() * -1 + 1));
		order.add(JsonColumn.MIRBASE);				pipes.add(new OverlapPipe    (mirBaseFile,      order.size() * -1 + 1));

		// allele frequency annotation
		order.add(JsonColumn.BGI);					pipes.add(new OverlapPipe    (bgiFile,          order.size() * -1 + 1));
		order.add(JsonColumn.ESP);					pipes.add(new OverlapPipe    (espFile,          order.size() * -1 + 1));
		order.add(JsonColumn.HAPMAP);				pipes.add(new OverlapPipe    (hapmapFile,       order.size() * -1 + 1));
		order.add(JsonColumn.THOUSAND_GENOMES);		pipes.add(new OverlapPipe    (genomeFile,       order.size() * -1 + 1));

		// annotation requiring walking X-REFs
		order.add(JsonColumn.NCBI_GENE);			pipes.add(new OverlapPipe    (genesFile,        order.size() * -1 + 1));		
		/* add Entrez GeneID X-REF */				pipes.add(new DrillPipe      (true, new String[] {"GeneID"})); 
		order.add(JsonColumn.HGNC);					pipes.add(new LookupPipe     (hgncFile, hgncIndexFile, -2));
		/* remove Entrez GeneID X-REF*/				pipes.add(new HCutPipe       (new int[] {-3}));
		/* add OMIM ID X-REF */						pipes.add(new DrillPipe      (true, new String[] {"mapped_OMIM_ID"}));
		order.add(JsonColumn.OMIM);					pipes.add(new LookupPipe     (omimFile, omimIndexFile, -2));
		/* remove OMIM ID X-REF */					pipes.add(new HCutPipe       (new int[] {-3}));
		
		/* transform JSON cols into final output */	pipes.add(new TransformFunctionPipe(new FormatterPipeFunction(order, mFormatters)));
						
		this.setPipes(pipes);		
	}
}