/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.mayo.bior.pipeline.Treat;


import java.io.IOException;
import java.util.List;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.RemoveAllJSONPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryMetaData;


/**
 * Get the variant information for the TREAT workflow from BioR
 * 
 * @author m102417
 */
@SuppressWarnings ({"rawtypes", "unchecked"})
public class OverlapingFeaturesPipeline extends Pipeline implements Cleaner
{
	private BiorProperties	biorProps;
	private String			baseDir;
	
	private boolean	header = true;
	private int		deleteColCount = 0;
	private int		numCols = 0;
	private int		firstCol = 0;
	// properties to extract...
	private static final int	kGeneName = 0;
	private static final int	kNCBIEntrezGeneID = kGeneName + 1;
	private static final int	kNCBICols = kNCBIEntrezGeneID + 1;
	private static final String[]	kGeneDrill = {"gene", "GeneID"}; // , "note"
	private static final int	kdbSNPBuild = 0;
	private static final int	kdbSNPSuspect = kdbSNPBuild + 1;
	private static final int	kdbSNPClinical = kdbSNPSuspect + 1;
	private static final int	kdbSNPDisease = kdbSNPClinical + 1;
	private static final int	kdbSNPAllele = kdbSNPDisease + 1;
	private static final int	kdbSNPID = kdbSNPAllele + 1;
	private static final int	kdbSNPRef = kdbSNPID + 1;
	private static final int	kdbSNPAlt = kdbSNPRef + 1;
	private static final int	kdbSNPCols = kdbSNPAlt + 1;
	private static final String[]	kDbSnpDrill = {"INFO.dbSNPBuildID", "INFO.SSR", "INFO.SCS", "INFO.CLN", "INFO.SAO", "_id", 
	                             	               "REF", "ALT"};
	private static final String[]	kDbSnpSuspectLookup = {"unspecified", "Paralog", "byEST", "Para_EST", "oldAlign", "other"};
	private static final String[]	kDbSnpClinicalLookup = {"unknown", "untested", "non-pathogenic", "probable-non-pathogenic", 
	                             	                        "probable-pathogenic", "pathogenic", "drug-response", 
	                             	                        "histocompatibility", "other"};
	private static final String[]	kDbSnpAlleleLookup = {"unspecified", "Germline", "Somatic", "Both", "not-tested", 
	                             	                      "tested-inconclusive", "other"};
	private static final int	kHGNCSymbol = 0;
	private static final int	kHGNCEntrezGeneID = kHGNCSymbol + 1;
	private static final int	kHGNCEnsemblGeneID = kHGNCEntrezGeneID + 1;
	private static final int	kHGNCCols = kHGNCEnsemblGeneID + 1;
	private static final String[]	kHGNCDrill = {"Approved_Symbol", "Entrez_Gene_ID", "Ensembl_Gene_ID"};
	private static final int	kCosmicID = 0;
	private static final int	kCosmicCDS = kCosmicID + 1;
	private static final int	kCosmicAA = kCosmicCDS + 1;
	private static final int	kCosmicStrand = kCosmicAA + 1;
	private static final int	kCosmicCols = kCosmicStrand + 1;
	private static final String[]	kCosmicDrill = {"Mutation_ID", "Mutation_CDS", "Mutation_AA", "Mutation_GRCh37_strand"};
	private static final int	kOMIMDisorder = 0;
	private static final int	kOMIMCols = kOMIMDisorder + 1;
	private static final String[]	kOMIMDrill = {"Disorders"};
	private static final int	kBlacklistScore = 0;
	private static final int	kBlacklistCols = kBlacklistScore + 1;
	private static final String[]	kBlacklistDrill = {"score"};
	private static final int	kConservationScore = 0;
	private static final int	kConservationCols = kConservationScore + 1;
	private static final String[]	kConservationDrill = {"score"};
	private static final int	kEnhancerScore = 0;
	private static final int	kEnhancerCols = kEnhancerScore + 1;
	private static final String[]	kEnhancerDrill = {"score"};
	private static final int	kTFBSScore = 0;
	private static final int	kTFBSCols = kTFBSScore + 1;
	private static final String[]	kTFBSDrill = {"score"};
	private static final int	kTSSScore = 0;
	private static final int	kTSSCols = kTSSScore + 1;
	private static final String[]	kTSSDrill = {"score"};
	private static final int	kUniqueScore = 0;
	private static final int	kUniqueCols = kUniqueScore + 1;
	private static final String[]	kUniqueDrill = {"score"};
	private static final int	kRegulationName = 0;
	private static final int	kRegulationCols = kRegulationName + 1;
	private static final String[]	kRegulationDrill = {"name"};
	private static final int	kRepeatName = 0;
	private static final int	kRepeatCols = kRepeatName + 1;
	private static final String[]	kRepeatDrill = {"repName"};
	private static final String	kBlank = ".";
	private static final int	kScoreCutoff = 500;
	private static final String	kPlusStrand = "+";
	
	
	/**
	 * Default constructor, IdentityPipe in and out, no cleaning
	 * 
	 * @throws IOException
	 */
	public OverlapingFeaturesPipeline () throws IOException
	{
		biorProps = new BiorProperties ();
		baseDir = biorProps.get ("fileBase");
		init (new IdentityPipe (), new IdentityPipe (), false);
	}
	
	
	/**
	 * Standard Constructor, gets data from input, sends it to output, doesn't do any cleaning
	 * 
	 * @param input		Where gets vcf information from
	 * @param output	Where sends the resulting information
	 * @throws IOException
	 */
	public OverlapingFeaturesPipeline (Pipe input, Pipe output) throws IOException
	{
		biorProps = new BiorProperties ();
		baseDir = biorProps.get ("fileBase");
		init (input, output, false);
	}
	
	
	/**
	 * Full constructor, gets data from input, sends it to output, does cleaning if clean is true
	 * 
	 * @param input		Where gets vcf information from
	 * @param output	Where sends the resulting information
	 * @param clean		If true, trims off the catalog data and adds the information in the TREAT format
	 * @throws IOException
	 */
	public OverlapingFeaturesPipeline (Pipe input, Pipe output, boolean clean) throws IOException
	{
		biorProps = new BiorProperties ();
		baseDir = biorProps.get ("fileBase");
		init (input, output, clean);
	}
	
	
	/**
	 * Full constructor, gets data from input, sends it to output, does cleaning if clean is true
	 * 
	 * @param input		Where gets vcf information from
	 * @param output	Where sends the resulting information
	 * @param baseDir	Base directory that holds all the catalog files, or null to use the one from properties
	 * @param clean		If true, trims off the catalog data and adds the information in the TREAT format
	 * @throws IOException
	 */
	public OverlapingFeaturesPipeline (Pipe input, Pipe output, String baseDir, boolean clean) throws IOException
	{
		biorProps = new BiorProperties ();
		if (baseDir != null)
			this.baseDir = baseDir;
		init (input, output, clean);
	}
	
	
	/**
	 * Sets up the catalogs and drills necessary to get the data
	 * 
	 * @param input		assumes that somehow input is converted to a history
	 * @param output	Where the results will go
	 * @param clean		If true, trims off the catalog data and adds one column per frequency source 
	 * (i.e. 1kGenome_EUR, etc), with the information in the TREAT format
	 * @throws IOException
	 */
	public void init (Pipe input, Pipe output, boolean clean) throws IOException
	{
		Pipe	cleaner;
		if (clean)
			cleaner = new TransformFunctionPipe<History, History> (new TreatPipe (this));
		else
			cleaner = new IdentityPipe<History> ();
		
		if (baseDir == null)
			baseDir = biorProps.get ("fileBase");
		if (baseDir == null)
			baseDir = "";
		
		String		genesFile = baseDir + biorProps.get ("genesFile");
		String		hgncFile = baseDir + biorProps.get ("hgncFile");
		String		dbsnpFile = baseDir + biorProps.get ("dbsnpFile");
		String		cosmicFile = baseDir + biorProps.get ("cosmicFile");
		String		hgncIndexFile = baseDir + biorProps.get ("hgncIndexFile");
		String		omimFile = baseDir + biorProps.get ("omimFile");
		String		omimIndexFile = baseDir + biorProps.get ("omimIndexFile");
		String		conservationFile = baseDir + biorProps.get ("conservationFile");
		String		repeatFile = baseDir + biorProps.get ("repeatFile");
		String		regulationFile = baseDir + biorProps.get ("regulationFile");
		String		uniqueFile = baseDir + biorProps.get ("uniqueFile");
		String		tssFile = baseDir + biorProps.get ("tssFile");
		String		tfbsFile = baseDir + biorProps.get ("tfbsFile");
		String		enhancerFile = baseDir + biorProps.get ("enhancerFile");
		String		blacklistedFile = baseDir + biorProps.get ("blacklistedFile");
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
		
		// requires history as input, history as output
		Pipeline	p = new Pipeline (input, new VCF2VariantPipe (), 
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
									  new RemoveAllJSONPipe (), cleaner, output);
		
		this.setPipes (p.getPipes ());
		deleteColCount = repeatDrill.length - posCol;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.mayo.bior.pipeline.Treat.Cleaner#doClean(edu.mayo.pipes.history.History)
	 */
	public History doClean (History history)
	{
		if (header)
		{
			header = false;
			numCols = history.size ();
			firstCol = numCols - deleteColCount + 1;	// Skip the history column
			
			// Now clean up the history metadata
			HistoryMetaData			metaData = History.getMetaData ();
			List<ColumnMetaData>	columns = metaData.getColumns ();
			
			for (int i = numCols - 1; i >= firstCol; --i)
				columns.remove (i);
			
			addVariantColumns (columns);
		}
		
		int		startCol = firstCol;
//		String	geneName = history.get (startCol + kGeneName);
//		int		ncbiEntrezGeneID = parseInt (history.get (startCol + kNCBIEntrezGeneID));
		startCol += kNCBICols;
		String	geneSymbol = getString (history.get (startCol + kHGNCSymbol));
		int		entrezGeneID = parseInt (history.get (startCol + kHGNCEntrezGeneID));
		String	ensemblGeneID = getString (history.get (startCol + kHGNCEnsemblGeneID));
		startCol += kHGNCCols;
		int		firstBuild = parseInt (history.get (startCol + kdbSNPBuild));
		String	suspectRegion = lookupString (history.get (startCol + kdbSNPSuspect), kDbSnpSuspectLookup);
		String	clinicalSig = lookupString (history.get (startCol + kdbSNPClinical), kDbSnpClinicalLookup);
		String	alleleOrigin = lookupString (history.get (startCol + kdbSNPAllele), kDbSnpAlleleLookup);
		boolean	diseaseVariant = Boolean.parseBoolean (history.get (startCol + kdbSNPDisease));
		String	dbSNPsID = getString (history.get (startCol + kdbSNPID));
		String	dbSNPsRef = getString (history.get (startCol + kdbSNPRef));
		String	dbSNPsAlt = getString (history.get (startCol + kdbSNPAlt));
		startCol += kdbSNPCols;
		int		mutationID = parseInt (history.get (startCol + kCosmicID));
		String	cosmicCDS = history.get (startCol + kCosmicCDS);
		String	cosmicAA = history.get (startCol + kCosmicAA);
		boolean	strand = kPlusStrand.equals (history.get (startCol + kCosmicStrand));
		startCol += kCosmicCols;
		String	omimDisease = getString (history.get (startCol + kOMIMDisorder));
		startCol += kOMIMCols;
		boolean	blacklisted = isAboveCutoff (history.get (startCol + kBlacklistScore));
		startCol += kBlacklistCols;
		boolean	conserved = isAboveCutoff (history.get (startCol + kConservationScore));
		startCol += kConservationCols;
		boolean	enhancer = isAboveCutoff (history.get (startCol + kEnhancerScore));
		startCol += kEnhancerCols;
		boolean	tfbs = isAboveCutoff (history.get (startCol + kTFBSScore));
		startCol += kTFBSCols;
		boolean	tss = isAboveCutoff (history.get (startCol + kTSSScore));
		startCol += kTSSCols;
		boolean	unique = isAboveCutoff (history.get (startCol + kUniqueScore));
		startCol += kUniqueCols;
		String	name = getString (history.get (startCol + kRepeatName));
		boolean	repeat = !isEmpty (name);
		startCol += kRepeatCols;
		name = getString (history.get (startCol + kRegulationName));
		boolean	regulatory = !isEmpty (name);
		startCol += kRegulationCols;
		
		// Got the information we needed, now clear it all out
		for (int i = startCol - 1; i >= firstCol; --i)
			history.remove (i);
		
		addDbSNPs (dbSNPsID, dbSNPsRef, dbSNPsAlt, history);
		addString (ensemblGeneID, history);
		addString (geneSymbol, history);
		addNonZeroInt (entrezGeneID, history);
		addString (omimDisease, history);
		addCosmic (mutationID, cosmicCDS, cosmicAA, strand, history);
		addBoolean (blacklisted, history);
		addBoolean (unique, history);
		addBoolean (repeat, history);
		addBoolean (diseaseVariant, history);
		history.add (kBlank);	// TODO mirBase
//		history.add ("" + theInt);	TODO mirBase
		addString (suspectRegion, history);
		addString (clinicalSig, history);
		history.add (kBlank);	// TODO polyphen2
		addString (alleleOrigin, history);
		addNonZeroInt (firstBuild, history);
		history.add (kBlank);	// TODO UniprotID
		
		addBoolean (conserved, history);
		addBoolean (regulatory, history);
		addBoolean (tfbs, history);
		addBoolean (tss, history);
		addBoolean (enhancer, history);
		
		return history;
	}
	
	
	private static final String[]	kSNVHeader = {"dbSNP135", "dbSNP135Alleles", "Gene ID", "Gene Name", "Entrez_id", "OMIM Disease"};
	private static final String[]	kSNVHeaderGGPS = {"COSMIC", "BlacklistedRegion", "Alignability/Uniquness", 
	                             	                  "Repeat_Region", "DiseaseVariant", "miRbase", "SNP_SuspectRegion", 
	                             	                  "SNP_ClinicalSig", "polyphen2", "Variant_AlleleOrigin", 
	                             	                  "First_dbSNP_Build", "UniprotID"};
	protected static final String[]	kSNVHeaderSift = {"Codons", "Transcript ID", "Protein ID", "Substitution", 
	                             	                  "Region", "SNP Type", "Prediction", "Score", "Median Info", 
	                             	                  "Gene ID", "Gene Name", "OMIM Disease", "Average Allele Freqs", 
	                             	                  "User Comment", "SynonymousCodonUsage", "Difference"};
	private static final String[]	kSNVHeaderUCSC = {"conservation", "regulation", "tfbs", "tss", "enhancer"};
	protected static final String	kSNVHeaderSSeq = "# inDBSNPOrNot	accession	functionGVS	" +
													 "aminoAcids	proteinPosition	polyPhen	geneList	" +
													 "Entrez_id	Gene_title	closest_transcript_id	" +
													 "Tissue_specificity	pathway	GeneCards	Kaviar_Variants";
	protected static final String[]	kSNVHeaderSEffect = {"Homozygous", "Bio_type", "accession", "Exon_ID", "Exon_Rank", 
	                             	                     "Effect", "aminoAcids", "proteinPosition", "Codon_Degeneracy", 
	                             	                     "geneList", "Entrez_id", "Gene_title", "Tissue_specificity", 
	                             	                     "pathway", "GeneCards", "Kaviar_Variants"};
	
	
	/**
	 * Add the Variant information columns to the history metadata
	 * 
	 * @param columns	Where to add them
	 */
	private void addVariantColumns (List<ColumnMetaData> columns)
	{
		for (String columnName : kSNVHeader)
			columns.add (new ColumnMetaData (columnName));
		
		for (String columnName : kSNVHeaderGGPS)
			columns.add (new ColumnMetaData (columnName));
		
		for (String columnName : kSNVHeaderUCSC)
			columns.add (new ColumnMetaData (columnName));
	}
	
	
	/**
	 * Add the dbSNPs information, or maybe two blank strings
	 * 
	 * @param dbSNPsID	dbSNPs ID
	 * @param dbSNPsRef	The dbSNPs ref base
	 * @param dbSNPsAlt	The dbSNPs alt base
	 * @param history	The history object to add to
	 */
	private void addDbSNPs (String dbSNPsID, String dbSNPsRef, String dbSNPsAlt, History history)
	{
		history.add (dbSNPsID);
		
		if (!isEmpty (dbSNPsRef))
			history.add (dbSNPsRef + "/" + dbSNPsAlt);
		else
			history.add (kBlank);
	}
	
	
	/**
	 * Add an int if it's greater than 0, or a blank string if it isn't
	 * 
	 * @param theInt	Int to add
	 * @param history	The history object to add to
	 */
	private void addNonZeroInt (int theInt, History history)
	{
		if (theInt > 0)
			history.add ("" + theInt);
		else
			history.add (kBlank);
	}
	
	
	/**
	 * Add aString if it's not null or empty, or a blank string if it is
	 * 
	 * @param theString	String to add
	 * @param history	The history object to add to
	 */
	private void addString (String theString, History history)
	{
		if (!isEmpty (theString))
			history.add (theString);
		else
			history.add (kBlank);
	}
	
	
	/**
	 * Add a boolean as either 1 (true) or 0 (false)
	 * 
	 * @param theBool	Value to use
	 * @param history	The history object to add to
	 */
	private void addBoolean (boolean theBool, History history)
	{
		if (theBool)
			history.add ("1");
		else
			history.add ("0");
	}
	
	
	/**
	 * Add a Cosmic entry, or else a blank string
	 * 
	 * @param mutationID	The mutation ID, if not greater than 0 will return a blank string
	 * @param cosmicCDS		the CDS value
	 * @param cosmicAA		The Amino Acid change
	 * @param strand		The strand, true for plus
	 * @param history		The history object to add to
	 */
	private void addCosmic (int mutationID, String cosmicCDS, String cosmicAA, boolean strand, History history)
	{
		if (mutationID <= 0)
		{
			history.add (kBlank);
			return;
		}
		
		StringBuilder	result = new StringBuilder ();
		
		result.append (mutationID);
		result.append (';');
		result.append (cosmicCDS);
		result.append (';');
		result.append (cosmicAA);
		if (strand)
			result.append (";+");
		else
			result.append (";-");
		
		history.add (result.toString ());
	}
	
	
	/**
	 * Test to see if a string is an int greater than or equal to the cutoff score
	 * 
	 * @param testStr	String to test
	 * @return	True if it is, false if it isn't a valid int, or the int is too small
	 */
	private static final boolean isAboveCutoff (String testStr)
	{
		int	score = parseInt (testStr);
		return score >= kScoreCutoff;
	}
	
	
	/**
	 * Test a String, if it's not empty, and not ".", return it, otherwise return null
	 * 
	 * @param theString	String to test
	 * @return	A String, or null
	 */
	private static final boolean isEmpty (String theString)
	{
		return ((theString == null) || theString.isEmpty ());
	}
	
	
	/**
	 * Test a String, if it's not empty, and not ".", return it, otherwise return null
	 * 
	 * @param theString	String to test
	 * @return	A String, or null
	 */
	private static final String getString (String theString)
	{
		if ((theString == null) || theString.isEmpty ())
			return null;
		
		if (theString.equals (kBlank))
			return null;
		
		return theString;
	}
	
	
	/**
	 * Parse a String, returning the int represented, or 0 if not an int
	 * 
	 * @param theInt	String to parse.  Must not be null
	 * @return	An integer, 0 if parsing failed
	 */
	private static final int parseInt (String theInt)
	{
		int	result = 0;
		if (!theInt.equals (kBlank))
		{
			try
			{
				result = Integer.parseInt (theInt);
			}
			catch (NumberFormatException oops)
			{
				// Do nothing
			}
		}
		
		return result;
	}
	
	
	/**
	 * Parse a String, getting an int.  If that int gives a String from theLookup, will return it.  If 
	 * String doesn't parse to an int, or the int is negative or >= theLookup.length (), returns theLookup[0]
	 * 
	 * @param theInt	String to parse.  Must not be null
	 * @param theLookup	Array to get strings from.  Must not be null or of length 0
	 * @return	A string from theLookup
	 */
	private static final String lookupString (String theInt, String[] theLookup)
	{
		int	result = 0;
		if (!theInt.equals (kBlank))
		{
			try
			{
				result = Integer.parseInt (theInt);
			}
			catch (NumberFormatException oops)
			{
				// Do nothing
			}
		}
		
		if (result > theLookup.length)
			return theLookup[0];
		
		return theLookup[result];
	}
	
}
