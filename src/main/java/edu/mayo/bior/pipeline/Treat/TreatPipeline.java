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
		addPipeIfNeeded(order, JsonColumn.VARIANT,			pipes,		new VCF2VariantPipe () );
		addPipeIfNeeded(order, JsonColumn.VEP,				pipes,		new VEPPipeline (new String[0], true) );
		/* add Ensembl Gene X-REF -- NOTE: No col - will be deleted */
															pipes.add(  new DrillPipe (true, new String[] {"Gene"}) ); 
		addPipeIfNeeded(order, JsonColumn.VEP_HGNC,			pipes,		new LookupPipe (getFile(Key.hgncFile), getFile(Key.hgncEnsemblGeneIndexFile), -2) );
		/* remove Ensembl Gene X-REF (removing a col - no header needed */
															pipes.add(	new HCutPipe (new int[] {-3}));
		addPipeIfNeeded(order, JsonColumn.SNPEFF, 			pipes, 		new SNPEFFPipeline (new String[]{SNPEffCommand.DEFAULT_GENOME_VERSION}, true));
		addPipeIfNeeded(order, JsonColumn.DBSNP_ALL, 		pipes,		new SameVariantPipe(getFile(Key.dbsnpFile),        	1 - order.size())); 		
		addPipeIfNeeded(order, JsonColumn.DBSNP_CLINVAR,	pipes, 		new SameVariantPipe(getFile(Key.dbsnpClinvarFile), 	1 - order.size())); 		
		addPipeIfNeeded(order, JsonColumn.COSMIC,			pipes, 		new SameVariantPipe(getFile(Key.cosmicFile),       	1 - order.size())); 
		addPipeIfNeeded(order, JsonColumn.UCSC_BLACKLISTED,	pipes,		new OverlapPipe(getFile(Key.blacklistedFile),  		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_CONSERVATION,pipes,		new OverlapPipe(getFile(Key.conservationFile), 		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_ENHANCER,	pipes, 		new OverlapPipe(getFile(Key.enhancerFile),     		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_TFBS,		pipes,		new OverlapPipe(getFile(Key.tfbsFile),         		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_TSS,			pipes,		new OverlapPipe(getFile(Key.tssFile),          		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_UNIQUE,		pipes,		new OverlapPipe(getFile(Key.uniqueFile),       		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_REPEAT,		pipes,		new OverlapPipe(getFile(Key.repeatFile),       		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.UCSC_REGULATION,	pipes,		new OverlapPipe(getFile(Key.regulationFile),   		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.MIRBASE,			pipes,		new OverlapPipe(getFile(Key.mirBaseFile),      		1 - order.size()));

		// allele frequency annotation
		addPipeIfNeeded(order, JsonColumn.BGI,				pipes,		new OverlapPipe(getFile(Key.bgiFile),          		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.ESP,				pipes,		new OverlapPipe(getFile(Key.espFile),          		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.HAPMAP,			pipes,		new OverlapPipe(getFile(Key.hapMapFile),       		1 - order.size()));
		addPipeIfNeeded(order, JsonColumn.THOUSAND_GENOMES,	pipes,		new OverlapPipe(getFile(Key.kGenomeFile),       	1 - order.size()));

		// annotation requiring walking X-REFs
		addPipeIfNeeded(order, JsonColumn.NCBI_GENE,		pipes,		new OverlapPipe(getFile(Key.genesFile),        		1 - order.size()));		
		/* add Entrez GeneID X-REF */								
															pipes.add(	new DrillPipe(true, new String[] {"GeneID"})); 
		addPipeIfNeeded(order, JsonColumn.HGNC,				pipes,		new LookupPipe(getFile(Key.hgncFile), getFile(Key.hgncIndexFile), -2));
		/* remove Entrez GeneID X-REF*/						pipes.add(	new HCutPipe(new int[] {-3}));
		/* add OMIM ID X-REF */								pipes.add(	new DrillPipe(true, new String[] {"mapped_OMIM_ID"}));
		addPipeIfNeeded(order, JsonColumn.OMIM,				pipes,		new LookupPipe(getFile(Key.omimFile), getFile(Key.omimIndexFile), -2));
		/* remove OMIM ID X-REF */							pipes.add(	new HCutPipe(new int[] {-3}));
		
		// Get the number of columns we will add (after the JSON columns)
		
		FormatterPipeFunction formatterPipe = new FormatterPipeFunction(order, mConfigColumnsToOutput);
		/* transform JSON cols into final output */			pipes.add(new TransformFunctionPipe(formatterPipe));

		/* specify final output cols to compress */	
		FieldSpecification fSpec = new FieldSpecification(formatterPipe.getColumnsAdded().size() + "-", FieldDirection.RIGHT_TO_LEFT);
		/* compress to have 1-to-1 */						pipes.add(new CompressPipe(fSpec, "|"));

		this.setPipes(pipes);		
	}
	
	
	/** Add pipe to be processed, if the column is in the user-specified config file
	 * @param jsonColOrderList  List of JSON column names for pipes that are added
	 * @param jsonColName       Name of JSON column to add (if pipe is added) 
	 * @param colFormatter		The formatter associated with the pipe (null if there is no associated formatter)
	 * @param pipesList			List of pipes - pipeToAdd will be added to this list if necessary
	 * @param pipeToAdd			If this pipe is necessary (based on columns user selects in the config file, then add to pipesList
	 * @return 1 if pipe added (WITH JSON HEADER), 0 if not
	 */
	private void addPipeIfNeeded(List<JsonColumn> jsonColOrder,  JsonColumn jsonColumn,
	 List<Pipe> pipesList,  Pipe pipeToAdd) 
	{
		Formatter colFormatter = jsonColToFormatter(jsonColumn);
		if( isPipeNeeded(colFormatter) ) {
			if( jsonColumn == null )
				jsonColOrder.add(jsonColumn);
			else
				jsonColOrder.add(colFormatter.getJSONColumn());
			pipesList.add(pipeToAdd);
		}
	}

	/** If the jsonCol is null as it will be for JsonColumn.VariantToJson, 
	 * then just return null Formatter (it will only add the column header)
	 * @param jsonCol
	 * @return
	 */
	private Formatter jsonColToFormatter(JsonColumn jsonCol) {
		Formatter formatter = null;
		
		if(     JsonColumn.VEP.equals(jsonCol))				formatter = new VEPFormatter();
		else if(JsonColumn.VEP_HGNC.equals(jsonCol))		formatter = new VEPHgncFormatter();
		else if(JsonColumn.SNPEFF.equals(jsonCol))			formatter = new SNPEffFormatter();
		else if(JsonColumn.DBSNP_ALL.equals(jsonCol))		formatter = new DbsnpFormatter();
		else if(JsonColumn.DBSNP_CLINVAR.equals(jsonCol))	formatter = new DbsnpClinvarFormatter();
		else if(JsonColumn.COSMIC.equals(jsonCol))			formatter = new CosmicFormatter();
		else if(JsonColumn.UCSC_BLACKLISTED.equals(jsonCol)) formatter= new UcscBlacklistedFormatter();
		else if(JsonColumn.UCSC_CONSERVATION.equals(jsonCol)) formatter=new UcscConservationFormatter();
		else if(JsonColumn.UCSC_ENHANCER.equals(jsonCol))	formatter = new UcscEnhancerFormatter();
		else if(JsonColumn.UCSC_TFBS.equals(jsonCol))		formatter = new UcscTfbsFormatter();
		else if(JsonColumn.UCSC_TSS.equals(jsonCol))		formatter = new UcscTssFormatter();
		else if(JsonColumn.UCSC_UNIQUE.equals(jsonCol))		formatter = new UcscUniqueFormatter();
		else if(JsonColumn.UCSC_REPEAT.equals(jsonCol))		formatter = new UcscRepeatFormatter();
		else if(JsonColumn.UCSC_REGULATION.equals(jsonCol))	formatter = new UcscRegulationFormatter();
		else if(JsonColumn.MIRBASE.equals(jsonCol))			formatter = new MirBaseFormatter();

		// allele frequency annotation
		else if(JsonColumn.BGI.equals(jsonCol))				formatter = new BgiFormatter();
		else if(JsonColumn.ESP.equals(jsonCol))				formatter = new EspFormatter();
		else if(JsonColumn.HAPMAP.equals(jsonCol))			formatter = new HapmapFormatter();
		else if(JsonColumn.THOUSAND_GENOMES.equals(jsonCol)) formatter= new ThousandGenomesFormatter();

		// annotation requiring walking X-REFs
		else if(JsonColumn.NCBI_GENE.equals(jsonCol))		formatter = new NcbiGeneFormatter();
		else if(JsonColumn.HGNC.equals(jsonCol))			formatter = new HgncFormatter();
		else if(JsonColumn.OMIM.equals(jsonCol))			formatter = new OmimFormatter();
		
		return formatter;
	}
	

	/** Do we need to add this pipe?  Yes, if any of its columns are in the config file
	   (or config properties are null, which signifies that the user wants ALL columns)
	   (or if the columnFormatter is null, which means it is a necessary pipe) */
	private boolean isPipeNeeded(Formatter colFormatter) {
		if( colFormatter == null || mConfigColumnsToOutput == null )
			return true;
		// Else we need to loop thru the columns this pipe would add.
		// If any are in the config file, then we need it 
		for(String colFromPipe : colFormatter.getHeaders()) {
			if(mConfigColumnsToOutput.contains(colFromPipe))
				return true;
		}
		// None are in the config file, so safe to bypass pipe
		return false;
	}

	private String getFile(Key propKey) {
		return mProps.get(Key.fileBase) + mProps.get(propKey);
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
		
		if(configFileCols.size() == 0)
			throw new IllegalArgumentException("Error: The config file does not contain any output columns.  Please add some columns to output.  Or, to add all columns, do not add the config file option.");

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