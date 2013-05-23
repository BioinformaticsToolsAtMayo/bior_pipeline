package edu.mayo.bior.pipeline.Treat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.cmd.SNPEffCommand;
import edu.mayo.bior.pipeline.SNPEff.SNPEFFPipeline;
import edu.mayo.bior.pipeline.Treat.format.BgiFormatter;
import edu.mayo.bior.pipeline.Treat.format.CosmicFormatter;
import edu.mayo.bior.pipeline.Treat.format.DbsnpClinvarFormatter;
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
import edu.mayo.bior.util.BiorProperties.Key;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.CompressPipe;
import edu.mayo.pipes.history.HCutPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.util.FieldSpecification;
import edu.mayo.pipes.util.FieldSpecification.FieldDirection;

/**
 * BioR implementation of TREAT annotation module.
 *  
 * @author Greg Dougherty, duffp, Mike Meiners
 *
 */
public class TreatPipeline extends Pipeline<History, History>
{

	private BiorProperties	mProps;	
	
	private List<String> mConfigColumnsToOutput;
	
	/**
	 * Constructor
	 * 
	 * @throws IOException
	 * @throws AbnormalExitException 
	 * @throws TimeoutException 
	 * @throws BrokenBarrierException 
	 * @throws InterruptedException 
	 */
	public TreatPipeline() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		this(null);
	}
	
	public TreatPipeline(String configFilePath) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		mProps = new BiorProperties ();
		mConfigColumnsToOutput = loadConfig(configFilePath);
		validateConfigFileColumns(mConfigColumnsToOutput);
		initPipes();
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
		List<Pipe> pipes = new ArrayList<Pipe>();
		
		// tracks the order of the added JSON columns
		List<JsonColumn> order = new ArrayList<JsonColumn>();

		//		 	 ColOrder, JsonColumnName,				PipesList	PipeToAdd
		//			---------  -------------------			----------	--------------------------------------------------------
		// 1ST JSON column is the original variant
			order.add(JsonColumn.VARIANT);			pipes.add(new VCF2VariantPipe());
		if(isNeedPipe(new VEPFormatter())) {
			order.add(JsonColumn.VEP);				pipes.add(new VEPPipeline (new String[0], true) );
		}
		// Since the drill and cut are for HGNC lookup, we must check if HGNC is needed before we perform the drill and cut
		if(isNeedPipe(new VEPHgncFormatter()) ) {
			/* add Ensembl Gene X-REF col	*/		pipes.add(new DrillPipe (true, new String[] {"Gene"}) ); 
			order.add(JsonColumn.VEP_HGNC);			pipes.add(new LookupPipe (getFile(Key.hgncFile), getFile(Key.hgncEnsemblGeneIndexFile), -2) );
			/* remove Ensembl Gene X-REF col */		pipes.add(new HCutPipe (new int[] {-3}));
		}
		// Since SNPEff takes a long time to load, AND that load is in the constructor, let's check if we need it first before calling the constructor
		if(isNeedPipe(new SNPEffFormatter()) )	{
			order.add(JsonColumn.SNPEFF);			pipes.add(new SNPEFFPipeline (new String[]{SNPEffCommand.DEFAULT_GENOME_VERSION}, true));
		}
		// Using 1-order.size because we don't know how many columns the user passed in.
		// We want to reference the vcf2variant column, but it is easier to reference it from the end
		if(isNeedPipe(new DbsnpFormatter())) { 
			order.add(JsonColumn.DBSNP_ALL);		pipes.add(new SameVariantPipe(getFile(Key.dbsnpFile),    	1-order.size())); 	
		}
		if(isNeedPipe(new DbsnpClinvarFormatter())) {
			order.add(JsonColumn.DBSNP_CLINVAR);	pipes.add(new SameVariantPipe(getFile(Key.dbsnpClinvarFile),1-order.size()));
		}
		if(isNeedPipe(new CosmicFormatter())) {
			order.add(JsonColumn.COSMIC);			pipes.add(new SameVariantPipe(getFile(Key.cosmicFile),      1-order.size()));
		}
		if(isNeedPipe(new UcscBlacklistedFormatter())) {
			order.add(JsonColumn.UCSC_BLACKLISTED);	pipes.add(new OverlapPipe(getFile(Key.blacklistedFile),  	1-order.size()));
		}
		if(isNeedPipe(new UcscConservationFormatter())) {
			order.add(JsonColumn.UCSC_CONSERVATION);pipes.add(new OverlapPipe(getFile(Key.conservationFile), 	1-order.size()));
		}
		if(isNeedPipe(new UcscEnhancerFormatter())) {
			order.add(JsonColumn.UCSC_ENHANCER);	pipes.add(new OverlapPipe(getFile(Key.enhancerFile),     	1-order.size()));
		}
		if(isNeedPipe(new UcscTfbsFormatter())) {
			order.add(JsonColumn.UCSC_TFBS);		pipes.add(new OverlapPipe(getFile(Key.tfbsFile),         	1-order.size()));
		}
		if(isNeedPipe(new UcscTssFormatter())) {
			order.add(JsonColumn.UCSC_TSS);			pipes.add(new OverlapPipe(getFile(Key.tssFile),          	1-order.size()));
		}
		if(isNeedPipe(new UcscUniqueFormatter())) {
			order.add(JsonColumn.UCSC_UNIQUE);		pipes.add(new OverlapPipe(getFile(Key.uniqueFile),       	1-order.size()));
		}
		if(isNeedPipe(new UcscRepeatFormatter())) {
			order.add(JsonColumn.UCSC_REPEAT);		pipes.add(new OverlapPipe(getFile(Key.repeatFile),       	1-order.size()));
		}
		if(isNeedPipe(new UcscRegulationFormatter())) {
			order.add(JsonColumn.UCSC_REGULATION);	pipes.add(new OverlapPipe(getFile(Key.regulationFile),   	1-order.size()));
		}
		if(isNeedPipe(new MirBaseFormatter())) {
			order.add(JsonColumn.MIRBASE);			pipes.add(new OverlapPipe(getFile(Key.mirBaseFile),      	1-order.size()));
		}
		if(isNeedPipe(new BgiFormatter())) {
			// allele frequency annotation
			order.add(JsonColumn.BGI);				pipes.add(new SameVariantPipe(getFile(Key.bgiFile),        	1-order.size()));
		}
		if(isNeedPipe(new EspFormatter())) {
			order.add(JsonColumn.ESP);				pipes.add(new SameVariantPipe(getFile(Key.espFile),        	1-order.size()));
		}
		if(isNeedPipe(new HapmapFormatter())) {
			order.add(JsonColumn.HAPMAP);			pipes.add(new SameVariantPipe(getFile(Key.hapMapFile),     	1-order.size()));
		}
		if(isNeedPipe(new ThousandGenomesFormatter())) {
			order.add(JsonColumn.THOUSAND_GENOMES);	pipes.add(new SameVariantPipe(getFile(Key.kGenomeFile),    	1-order.size()));
		}
		if(isNeedPipe(new NcbiGeneFormatter())) {
			// annotation requiring walking X-REFs
			order.add(JsonColumn.NCBI_GENE);		pipes.add(new OverlapPipe(getFile(Key.genesFile),        	1-order.size()));		
		}
		if(isNeedPipe(new HgncFormatter())) {
			/* add Entrez GeneID X-REF */			pipes.add(new DrillPipe(true, new String[] {"GeneID"})); 
			order.add(JsonColumn.HGNC);				pipes.add(new LookupPipe(getFile(Key.hgncFile), getFile(Key.hgncIndexFile), -2));
			/* remove Entrez GeneID X-REF*/			pipes.add(new HCutPipe(new int[] {-3}));
		}
		if(isNeedPipe(new OmimFormatter()) ) {
			/* add OMIM ID X-REF */					pipes.add(new DrillPipe(true, new String[] {"mapped_OMIM_ID"}));
			order.add(JsonColumn.OMIM);				pipes.add(new LookupPipe(getFile(Key.omimFile), getFile(Key.omimIndexFile), -2));
			/* remove OMIM ID X-REF */				pipes.add(	new HCutPipe(new int[] {-3}));
		}
		
		FormatterPipeFunction formatterPipe = new FormatterPipeFunction(order, mConfigColumnsToOutput);
		/* transform JSON cols into final output */	pipes.add(new TransformFunctionPipe(formatterPipe));

		/* specify final output cols to compress */	
		FieldSpecification fSpec = new FieldSpecification(formatterPipe.getColumnsAdded().size() + "-", FieldDirection.RIGHT_TO_LEFT);
		/* compress to have 1-to-1 */				pipes.add(new CompressPipe(fSpec, "|"));

		this.setPipes(pipes);		
	}


	/** Do we need to add this pipe?  Yes, if any of its columns are in the config file
	   (or config properties are null, which signifies that the user wants ALL columns)
	   (or if the columnFormatter is null, which means it is a necessary pipe) */
	private boolean isNeedPipe(Formatter colFormatter) {
		if( colFormatter == null || mConfigColumnsToOutput == null )
			return true;
		// Else we need to loop thru the columns this pipe would add.
		// If any are in the config file, then we need it 
		for(String colFromPipe : colFormatter.getHeaders()) {
			if(mConfigColumnsToOutput.contains(colFromPipe))
				return true;
		}
		
		// There are a few dependencies:
		//  - Add Vep 		if VepHgnc is wanted    	(vepHgnc depends on vep)
		//  - Add NcbiGene 	if Hgnc is wanted			(Hgnc    depends on NcbiGene)
		//	- Add NcbiGene AND Hgnc  if Omim is wanted	(Omim	 depends on BOTH NcbiGene AND Hgnc)
		if(colFormatter instanceof VEPFormatter  &&  isNeedPipe(new VEPHgncFormatter()))
			return true;
		if(colFormatter instanceof NcbiGeneFormatter  &&  isNeedPipe(new HgncFormatter()))
			return true;
		if(colFormatter instanceof NcbiGeneFormatter  &&  isNeedPipe(new OmimFormatter())) 
			return true;
		if(colFormatter instanceof HgncFormatter &&  isNeedPipe(new OmimFormatter())) 
			return true;

		
		// None are in the config file, so safe to bypass pipe
		return false;
	}

	private String getFile(Key propKey) {
		String path =  mProps.get(Key.fileBase) + mProps.get(propKey);
		return path;
	}
	
	/** Load the config file that contains the columns that bior_annotate is to keep */
	private List<String> loadConfig(String configFilePath) throws IOException {
		if(configFilePath == null || configFilePath.length() == 0)
			return null;
		
		List<String> configCols = Files.readLines(new File(configFilePath), Charsets.UTF_8);
		// Remove those starting with "#"
		for(int i=configCols.size()-1; i>=0; i--) {
			if(configCols.get(i).startsWith("#") || configCols.get(i).trim().length() == 0)
				configCols.remove(i);
		}
		return configCols;
	}
	

	/** Throw an exception if the config file column is not one of the possible ones */
	private void validateConfigFileColumns(List<String> configFileCols) {
		if(configFileCols == null)
			return;  // No config file specified - OK
		
		if(configFileCols.size() == 0) {
			final String MSG = "Error: The config file does not contain any output columns.  Please add some columns to output.  Or, to add all columns, do not add the config file option.";
			throw new IllegalArgumentException(MSG);
		}

		List<String> allCols = FormatterPipeFunction.getAllPossibleColumns();
		StringBuffer errMsg = new StringBuffer();
		for(String configCol : configFileCols) {
			if( ! allCols.contains(configCol) )
				errMsg.append("    " + configCol + "\n");
		}
		if(errMsg.length() > 0) {
			errMsg.insert(0, "Error: these columns specified in the config file are not recognized:\n");
			throw new IllegalArgumentException(errMsg.toString());
		}
	}
	


}