/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.mayo.bior.pipeline;

import static edu.mayo.bior.pipeline.SplitFile.kReturnAll;
import java.io.*;
import java.util.*;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bsi.genomics.exometablebrowser.server.GetOpts;
import edu.mayo.bsi.genomics.exometablebrowser.server.Usage;
import edu.mayo.bsi.genomics.exometablebrowser.server.GetOpts.ArgType;
import edu.mayo.bsi.genomics.exometablebrowser.server.GetOpts.OptType;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.lookup.LookupPipe;
import edu.mayo.pipes.JSON.tabix.*;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryMetaData;


/**
 * 
 * @author Gregory Dougherty
 */
public class TreatPipeline implements Usage, Runnable
{
	private String				aVcfFile;
	private String				aBaseDir;
	private List<AlleleFreq>	theFreqs;
	private List<VariantInfo>	theInfo;
	
	private static final int	kGeneName = 0;
	private static final int	kNCBIEntrezGeneID = kGeneName + 1;
	private static final int	kNCBICols = kNCBIEntrezGeneID + 1;
	private static final String[]	kGeneDrill = {"gene", "GeneID"}; // , "note"
	private static final int	kdbSNPBuild = 0;
	private static final int	kdbSNPID = kdbSNPBuild + 1;
	private static final int	kdbSNPCols = kdbSNPID + 1;
	private static final String[]	kDbSnpDrill = {"INFO.dbSNPBuildID", "_id"};
	private static final int	kBGIMajorAllele = 0;
	private static final int	kBGIMinorAllele = kBGIMajorAllele + 1;
	private static final int	kBGIMajorFreq = kBGIMinorAllele + 1;
	private static final int	kBGIMinorFreq = kBGIMajorFreq + 1;
	private static final int	kBGICols = kBGIMinorFreq + 1;
	private static final String[]	kBgiDrill = {"major_allele", "minor_allele", "estimated_major_allele_freq", 
	                             	             "estimated_minor_allele_freq"};
	private static final int	kESPCeuCounts = 0;
	private static final int	kESPAfrCounts = kESPCeuCounts + 1;
	private static final int	kESPTotalCounts = kESPAfrCounts + 1;
	private static final int	kESPMAF = kESPTotalCounts + 1;
	private static final int	kESPRefAllele = kESPMAF + 1;
	private static final int	kESPAltAllele = kESPRefAllele + 1;
	private static final int	kESPCols = kESPAltAllele + 1;
	private static final String[]	kEspDrill = {"INFO.EA_AC", "INFO.AA_AC", "INFO.TAC", "INFO.MAF", "_refAllele", "_altAlleles"};
	private static final int	kHapMapRefAllele = 0;
	private static final int	kHapMapAltAllele = kHapMapRefAllele + 1;
	private static final int	kHapMapCeuRefFreq = kHapMapAltAllele + 1;
	private static final int	kHapMapCeuAltFreq = kHapMapCeuRefFreq + 1;
	private static final int	kHapMapYriRefFreq = kHapMapCeuAltFreq + 1;
	private static final int	kHapMapYriAltFreq = kHapMapYriRefFreq + 1;
	private static final int	kHapMapJptRefCount = kHapMapYriAltFreq + 1;
	private static final int	kHapMapJptAltCount = kHapMapJptRefCount + 1;
	private static final int	kHapMapJptTotalCount = kHapMapJptAltCount + 1;
	private static final int	kHapMapChbRefCount = kHapMapJptTotalCount + 1;
	private static final int	kHapMapChbAltCount = kHapMapChbRefCount + 1;
	private static final int	kHapMapChbTotalCount = kHapMapChbAltCount + 1;
	protected static final int	kHapMapCols = kHapMapChbTotalCount + 1;
	private static final String[]	kHapMapDrill = {"refallele", "otherallele", "CEU.refallele_freq", "CEU.otherallele_freq", 
              			        	                "YRI.refallele_freq", "YRI.otherallele_freq", "JPT.refallele_count", 
             			        	                "JPT.otherallele_count", "JPT.totalcount", "CHB.refallele_count", 
             			        	                "CHB.otherallele_count", "CHB.totalcount"};
	private static final int	k1kGenomeAll = 0;
	private static final int	k1kGenomeEUR = k1kGenomeAll + 1;
	private static final int	k1kGenomeASN = k1kGenomeEUR + 1;
	private static final int	k1kGenomeAFR = k1kGenomeASN + 1;
	private static final int	k1kGenomeAMR = k1kGenomeAFR + 1;
	private static final int	k1kGenomeRef = k1kGenomeAMR + 1;
	private static final int	k1kGenomeAlt = k1kGenomeRef + 1;
	protected static final int	k1kGenomeCols = k1kGenomeAlt + 1;
	private static final String[]	k1kGenomeDrill = {"INFO.AF", "INFO.EUR_AF", "INFO.ASN_AF", "INFO.AFR_AF", "INFO.AMR_AF", 
	                             	                  "_refAllele", "_altAlleles"};
	private static final int	kHGNCSymbol = 0;
	private static final int	kHGNCEntrezGeneID = kHGNCSymbol + 1;
	private static final int	kHGNCEnsemblGeneID = kHGNCEntrezGeneID + 1;
	protected static final int	kHGNCCols = kHGNCEnsemblGeneID + 1;
	private static final String[]	kHGNCDrill = {"Approved_Symbol", "Entrez_Gene_ID", "Ensembl_Gene_ID"};
	private static final int	kOMIMDisorder = 0;
	protected static final int	kOMIMCols = kOMIMDisorder + 1;
	private static final String[]	kOMIMDrill = {"Disorders"};
	private static final int	kBlacklistScore = 0;
	protected static final int	kBlacklistCols = kBlacklistScore + 1;
	private static final String[]	kBlacklistDrill = {"score"};
	private static final int	kConservationScore = 0;
	protected static final int	kConservationCols = kConservationScore + 1;
	private static final String[]	kConservationDrill = {"score"};
	private static final int	kEnhancerScore = 0;
	protected static final int	kEnhancerCols = kEnhancerScore + 1;
	private static final String[]	kEnhancerDrill = {"score"};
	private static final int	kTFBSScore = 0;
	protected static final int	kTFBSCols = kTFBSScore + 1;
	private static final String[]	kTFBSDrill = {"score"};
	private static final int	kTSSScore = 0;
	protected static final int	kTSSCols = kTSSScore + 1;
	private static final String[]	kTSSDrill = {"score"};
	private static final int	kUniqueScore = 0;
	protected static final int	kUniqueCols = kUniqueScore + 1;
	private static final String[]	kUniqueDrill = {"score"};
	private static final int	kRegulationName = 0;
	protected static final int	kRegulationCols = kRegulationName + 1;
	private static final String[]	kRegulationDrill = {"name"};
	private static final int	kRepeatName = 0;
	protected static final int	kRepeatCols = kRepeatName + 1;
	private static final String[]	kRepeatDrill = {"repName"};
	private static final int	kRefOffset = 0;
	private static final int	kAltOffset = kRefOffset + 1;
	private static final int	kTotalOffset = kAltOffset + 1;
	private static final int	kNumOffsets = kTotalOffset + 1;
	private static final int	kNumResults = kAltOffset + 1;
	private static final int	kCeuPop = 0;
	private static final int	kYriPop = kCeuPop + 1;
	private static final int	kAsnPop = kYriPop + 1;
	private static final String	kRestOfHeader = "	Gene	EntrezGeneID	FirstDBSNPSBuild	BGIMajorAllele	" +
												"BGIMinorAllele	BGIMajorFrequency	BGIMinorFrequency	ESP6500CEUMAF	" +
												"ESP6500AFRMAF	HapMapRefAllele	HapMapAltAllele	HapMapCEURefFrequency	" +
												"HapMapCEUAltFrequency	HapMapYRIRefFrequency	HapMapYRIAltFrequency	" +
												"HapMapJPT+CHBRefFrequency	HapMapJPT+CHBAltFrequency	1kGenome_allele_freq" +
												"	1kGenome_EUR_allele_freq	1kGenome_ASN_allele_freq	" +
												"1kGenome_AFR_allele_freq	1kGenome_AMR_allele_freq";
	private static final String	kBlank = ".";
	private static final String	kEspEurMaf = "ESP6500_EUR_maf";
	private static final String	kEspAfrMaf = "ESP6500_AFR_maf";
	private static final String[]	kBaseStrs = {"CHROM", "POS", "REF", "ALT"};
	private static final int	kChromPos = 0;
	private static final int	kPositionPos = kChromPos + 1;
	private static final int	kRefPos = kPositionPos + 1;
	private static final int	kAltPos = kRefPos + 1;
	private static final String[]	kHapMapFreq = {"HapMap_CEU_allele_freq", "HapMap_YRI_allele_freq", "HapMap_JPT+CHB_allele_freq"};
	private static final String	kBGIFreq = "BGI200_Danish";
	private static final String[]	k1kGenomeFreq = {"1kGenome_allele_freq", "1kGenome_EUR_allele_freq", "1kGenome_ASN_allele_freq", 
	                             	                 "1kGenome_AFR_allele_freq", "1kGenome_AMR_allele_freq"};
	private static final double	kPercentAdjust = 100.0;
	private static final OptType	kRequired = OptType.kRequiredParam;
	private static final OptType	kOptional = OptType.kOptionalParam;
	private static final OptType	kOnly = OptType.kOnlyParam;
	private static final String	kAppName = "TreatPipeline";
	private static final int	kVersion = 2;
	private static final int	kVersionParam = 0;
	private static final int	kVCFParam = kVersionParam + 1;
	private static final int	kBaseDirParam = kVCFParam + 1;
	private static final int	kWhichDataParam = kBaseDirParam + 1;
	private static final boolean	kConvertFromPercent = true;
	private static final boolean	kDoNotConvert = false;
	private static final boolean	kIsFirst = true;
	private static final boolean	kIsNotFirst = false;
	private static final int	kInvalidID = -1;
	private static final int	kScoreCutoff = 500;
	
	
	/**
	 * Create a Pipeline to process a VCF file with all the information BioR has to offer, and write the results to freqs
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @param freqs		Synchronized List where we'll place any AlleleFreq results found, must not be null.  
	 * Will add the AlleleFreqs at the end of the List, and a null at the end when done processing the VCF file
	 */
	public TreatPipeline (String vcfFile, String baseDir, List<AlleleFreq> freqs)
	{
		aVcfFile = vcfFile;
		aBaseDir = baseDir;
		theFreqs = freqs;
	}
	
	
	/**
	 * Create a Pipeline to process a VCF file with all the information BioR has to offer, and write the results to freqs
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @param info		Synchronized List where we'll place any VariantInfo results found, must not be null.  
	 * Will add the AlleleFreqs at the end of the List, and a null at the end when done processing the VCF file
	 */
	public TreatPipeline (String vcfFile, List<VariantInfo> info, String baseDir)
	{
		aVcfFile = vcfFile;
		aBaseDir = baseDir;
		theInfo = info;
	}
	
	
	/**
	 * Normal Constructor
	 */
	public TreatPipeline ()
	{
		super ();
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run ()
	{
		if (theFreqs != null)
			getAlleleFrequencies (aVcfFile, aBaseDir, theFreqs);
		else if (theInfo != null)
			getVariantData (aVcfFile, aBaseDir, theInfo);
	}
	
	
	/**
	 * Handle a command line invocation
	 * 
	 * @param args	The arguments
	 * @throws IOException
	 */
	public static void main (String[] args) throws IOException
	{
		String[]	parameters = getParameters (args);
		if (parameters == null)
			return;
		
		String	vcf = parameters[kVCFParam];
		String	baseDir = parameters[kBaseDirParam];
		String	whichData = parameters[kWhichDataParam];
		boolean	doFreqs = (whichData == null);
		File	theFile = new File (vcf);
		if (!theFile.exists ())
		{
			System.out.println ("Can't find the vcf file");
			return;
		}
		
		Properties	theProperties = getProperties ();
		if (theProperties == null)
		{
			System.out.println ("Could not load properties");
			return;
		}
		
		if (baseDir == null)
			baseDir = theProperties.getProperty ("fileBase");
		if (baseDir == null)
			baseDir = "";
		
		if (doFreqs)
			writeFrequencies (vcf, baseDir, theProperties);
		else
			writeVariantInfo (vcf, baseDir, theProperties);
	}
	
	
	/**
	 * Get the frequency information for the variants in vcf, and write it to stdout
	 * 
	 * @param vcf			Path to VCF file holding the variants of interest
	 * @param baseDir		Base directory for the catalog files
	 * @param theProperties	The properties from the Properties file
	 * @throws IOException
	 */
	private static void writeVariantInfo (String vcf, String baseDir, Properties theProperties) throws IOException
	{
		List<VariantInfo>	info = new ArrayList<VariantInfo> ();
		getVariantData (vcf, baseDir, theProperties, info);
		
		for (VariantInfo theVariant : info)
			System.out.println (theVariant.toString ());
	}
	
	
	/**
	 * Get the frequency information for the variants in vcf, and write it to stdout
	 * 
	 * @param vcf			Path to VCF file holding the variants of interest
	 * @param baseDir		Base directory for the catalog files
	 * @param theProperties	The properties from the Properties file
	 * @throws IOException
	 */
	@SuppressWarnings ({"rawtypes", "unchecked"})
	private static void writeFrequencies (String vcf, String baseDir, Properties theProperties) throws IOException
	{
		String		genesFile = baseDir + theProperties.getProperty ("genesFile");
		String		bgiFile = baseDir + theProperties.getProperty ("bgiFile");
		String		espFile = baseDir + theProperties.getProperty ("espFile");
		String		hapMapFile = baseDir + theProperties.getProperty ("hapMapFile");
		String		dbsnpFile = baseDir + theProperties.getProperty ("dbsnpFile");
		String		genomeFile = baseDir + theProperties.getProperty ("genomeFile");
		String[]	geneDrill = kGeneDrill;
		String[]	dbSnpDrill = kDbSnpDrill;
		String[]	bgiDrill = kBgiDrill;
		String[]	espDrill = kEspDrill;
		String[]	hapMapDrill = kHapMapDrill;
		String[]	genomeDrill = k1kGenomeDrill;
//		int[]		cut = {9};
		int			posCol = -1;
//		TransformFunctionPipe<History, History>	tPipe = new TransformFunctionPipe<History, History> (new TreatPipe ());
		Pipeline	p = new Pipeline (new CatPipe (), new HistoryInPipe (), new VCF2VariantPipe (), 
									  new OverlapPipe (genesFile), new DrillPipe (false, geneDrill), 
									  new SameVariantPipe (dbsnpFile, posCol -= geneDrill.length), 
									  new DrillPipe (false, dbSnpDrill), 
									  new SameVariantPipe (bgiFile, posCol -= dbSnpDrill.length), 
									  new DrillPipe (false, bgiDrill), 
									  new SameVariantPipe (espFile, posCol -= bgiDrill.length), 
									  new DrillPipe (false, espDrill), 
									  new SameVariantPipe (hapMapFile, posCol -= espDrill.length), 
									  new DrillPipe (false, hapMapDrill), 
									  new SameVariantPipe (genomeFile, posCol -= hapMapDrill.length), 
									  new DrillPipe (false, genomeDrill));
//									  new HCutPipe (cut), tPipe, new HistoryOutPipe (), new PrintPipe ());
		p.setStarts (Arrays.asList (vcf));
//		handleAll (p, posCol, hapMapDrill.length);
//		parseRows (p, posCol, hapMapDrill.length);
		writeRows (p, posCol, genomeDrill.length);
	}
	
	
	/**
	 * Get all the allele source results that BioR has for the variants specified in the passed in VCF File, and 
	 * put them into the passed in Synchronized List (so a different thread can extract them and do something 
	 * with them)
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @param freqs		Synchronized List where we'll place any AlleleFreq results found, must not be null.  
	 * Will add the AlleleFreqs at the end of the List, and a null at the end when done processing the VCF file
	 */
	public static void getAlleleFrequencies (String vcfFile, String baseDir, List<AlleleFreq> freqs)
	{
		if (freqs == null)
			return;
		
		if ((vcfFile == null) || vcfFile.isEmpty () || !new File (vcfFile).exists ())
		{
			freqs.add (null);	// Tell caller we're done
			return;
		}
		
		Properties	theProperties = getProperties ();
		if (theProperties == null)
		{
			System.out.println ("Could not load properties");
			freqs.add (null);	// Tell caller we're done
			return;
		}
		
		getAlleleFrequencies (vcfFile, baseDir, theProperties, freqs);
		freqs.add (null);	// Tell caller we're done
	}
	
	
	/**
	 * Get all the allele source results that BioR has for the variants specified in the passed in VCF File
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @return	List holding any AlleleFreq results found, or null if got a bad file
	 */
	public static List<AlleleFreq> getAlleleFrequencies (String vcfFile, String baseDir)
	{
		if ((vcfFile == null) || vcfFile.isEmpty () || !new File (vcfFile).exists ())
			return null;
		
		Properties	theProperties = getProperties ();
		if (theProperties == null)
		{
			System.out.println ("Could not load properties");
			return null;
		}
		
		List<AlleleFreq>	freqs = new ArrayList<AlleleFreq> ();
		getAlleleFrequencies (vcfFile, baseDir, theProperties, freqs);
		return freqs;
	}
	
	
	/**
	 * Get all the information that BioR has for the variants specified in the passed in VCF File, and 
	 * put them into the passed in Synchronized List (so a different thread can extract them and do something 
	 * with them)
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @param info		Synchronized List where we'll place any VariantInfo results found, must not be null.  
	 * Will add the VariantInfo at the end of the List, and a null at the end when done processing the VCF file
	 */
	public static void getVariantData (String vcfFile, String baseDir, List<VariantInfo> info)
	{
		if (info == null)
			return;
		
		if ((vcfFile == null) || vcfFile.isEmpty () || !new File (vcfFile).exists ())
		{
			info.add (null);	// Tell caller we're done
			return;
		}
		
		Properties	theProperties = getProperties ();
		if (theProperties == null)
		{
			System.out.println ("Could not load properties");
			info.add (null);	// Tell caller we're done
			return;
		}
		
		getVariantData (vcfFile, baseDir, theProperties, info);
		info.add (null);	// Tell caller we're done
	}
	
	/**
	 * Parse the arguments into usable parameters, returning null if there's a problem, or nothing to do
	 * 
	 * @param args	User provided command line arguments
	 * @return	The parsed arguments, or null if nothing to do
	 */
	private static final String[] getParameters (String[] args)
	{
		GetOpts.addOption ('V', "--version", ArgType.kNoArgument, kOnly);
		GetOpts.addOption ('v', "--vcfFile", ArgType.kRequiredArgument, kRequired);
		GetOpts.addOption ('b', "--baseDir", ArgType.kRequiredArgument, kOptional);
		GetOpts.addOption ('i', "--variantInfo", ArgType.kNoArgument, kOptional);
		
		String[]	parameters = GetOpts.parseArgs (args, new TreatPipeline ());
		if (parameters == null)
			return null;
		
		if (parameters[kVersionParam] != null)
		{
			printVersion ();
			return null;
		}
		
		return parameters;
	}
	
	
	/**
	 * Get all the allele source results that BioR has for the variants specified in the passed in VCF File
	 * 
	 * @param vcfFile		Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir		Path to the BioR Catalog files
	 * @param theProperties	Properties to use to get catalog names
	 * @param freqs			List to fill in with the results found
	 */
	@SuppressWarnings ({"rawtypes", "unchecked"})
	private static void getAlleleFrequencies (String vcfFile, String baseDir, Properties theProperties, List<AlleleFreq> freqs)
	{
		if (baseDir == null)
			baseDir = theProperties.getProperty ("fileBase");
		if (baseDir == null)
			baseDir = "";
		
		String		genesFile = baseDir + theProperties.getProperty ("genesFile");
		String		bgiFile = baseDir + theProperties.getProperty ("bgiFile");
		String		espFile = baseDir + theProperties.getProperty ("espFile");
		String		hapMapFile = baseDir + theProperties.getProperty ("hapMapFile");
		String		dbsnpFile = baseDir + theProperties.getProperty ("dbsnpFile");
		String		genomeFile = baseDir + theProperties.getProperty ("genomeFile");
		String[]	geneDrill = kGeneDrill;
		String[]	dbSnpDrill = kDbSnpDrill;
		String[]	bgiDrill = kBgiDrill;
		String[]	espDrill = kEspDrill;
		String[]	hapMapDrill = kHapMapDrill;
		String[]	genomeDrill = k1kGenomeDrill;
		int			posCol = -1;
		
		try
		{
			Pipeline	p = new Pipeline (new CatPipe (), new HistoryInPipe (), new VCF2VariantPipe (), 
										  new OverlapPipe (genesFile), new DrillPipe (false, geneDrill), 
										  new SameVariantPipe (dbsnpFile, posCol -= geneDrill.length), 
										  new DrillPipe (false, dbSnpDrill), 
										  new SameVariantPipe (bgiFile, posCol -= dbSnpDrill.length), 
										  new DrillPipe (false, bgiDrill), 
										  new SameVariantPipe (espFile, posCol -= bgiDrill.length), 
										  new DrillPipe (false, espDrill), 
										  new SameVariantPipe (hapMapFile, posCol -= espDrill.length), 
										  new DrillPipe (false, hapMapDrill), 
										  new SameVariantPipe (genomeFile, posCol -= hapMapDrill.length), 
										  new DrillPipe (false, genomeDrill));
			p.setStarts (Arrays.asList (vcfFile));
			
			parseRows (p, posCol, genomeDrill.length, freqs);
		}
		catch (IOException oops)
		{
			oops.printStackTrace ();
		}
	}
	
	
	/**
	 * Get all the information that BioR has for the variants specified in the passed in VCF File
	 * 
	 * @param vcfFile		Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir		Path to the BioR Catalog files
	 * @param theProperties	Properties to use to get catalog names
	 * @param info			List to fill in with the results found
	 */
	@SuppressWarnings ({"rawtypes", "unchecked"})
	private static void getVariantData (String vcfFile, String baseDir, Properties theProperties, List<VariantInfo> info)
	{
		if (baseDir == null)
			baseDir = theProperties.getProperty ("fileBase");
		if (baseDir == null)
			baseDir = "";
		
		String		genesFile = baseDir + theProperties.getProperty ("genesFile");
		String		hgncFile = baseDir + theProperties.getProperty ("hgncFile");
		String		dbsnpFile = baseDir + theProperties.getProperty ("dbsnpFile");
		String		hgncIndexFile = baseDir + theProperties.getProperty ("hgncIndexFile");
		String		omimFile = baseDir + theProperties.getProperty ("omimFile");
		String		omimIndexFile = baseDir + theProperties.getProperty ("omimIndexFile");
		String		conservationFile = baseDir + theProperties.getProperty ("conservationFile");
		String		repeatFile = baseDir + theProperties.getProperty ("repeatFile");
		String		regulationFile = baseDir + theProperties.getProperty ("regulationFile");
		String		uniqueFile = baseDir + theProperties.getProperty ("uniqueFile");
		String		tssFile = baseDir + theProperties.getProperty ("tssFile");
		String		tfbsFile = baseDir + theProperties.getProperty ("tfbsFile");
		String		enhancerFile = baseDir + theProperties.getProperty ("enhancerFile");
		String		blacklistedFile = baseDir + theProperties.getProperty ("blacklistedFile");
		String[]	geneDrill = kGeneDrill;
		String[]	hgncDrill = kHGNCDrill;
		String[]	dbSnpDrill = kDbSnpDrill;
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
		
		try
		{
			Pipeline	p = new Pipeline (new CatPipe (), new HistoryInPipe (), new VCF2VariantPipe (), 
										  new OverlapPipe (genesFile), new DrillPipe (false, geneDrill), 
										  new LookupPipe (hgncFile, hgncIndexFile, (posCol -= geneDrill.length) + 1), 
										  new DrillPipe (false, hgncDrill), 
										  new SameVariantPipe (dbsnpFile, posCol -= hgncDrill.length), 
										  new DrillPipe (false, dbSnpDrill), 
										  new LookupPipe (omimFile, omimIndexFile, (posCol -= dbSnpDrill.length) + 1), 
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
										  new DrillPipe (false, regulationDrill));
			p.setStarts (Arrays.asList (vcfFile));
			
			parseVariants (p, posCol, regulationDrill.length, info);
		}
		catch (IOException oops)
		{
			oops.printStackTrace ();
		}
	}
	
	
	/**
	 * Parse the contents of the pipeline one row at a time
	 * 
	 * @param pipeline		Pipeline that is parsing the VCF file
	 * @param posCol		Column before the start of the last set of data added
	 * @param lastColCount	Count of columns added in the last set
	 * @param freqs			List to fill in with the results found
	 */
	@SuppressWarnings ("rawtypes")
	protected static void parseRows (Pipeline pipeline, int posCol, int lastColCount, List<AlleleFreq> freqs)
	{
		boolean	header = true;
		int		numCols = -1;
		int		firstCol = -1;
		int		chromPos = -1;
		int		positionPos = -1;
		int		refPos = -1;
		int		altPos = -1;
		
		while (pipeline.hasNext ())
		{
			History	history = (History) pipeline.next ();
			if (header)
			{
				header = false;
				numCols = history.size ();
				firstCol = numCols - (lastColCount - posCol) + 1;	// Skip the history column
				
				int[]	poses = getPositions (kBaseStrs);
				
				chromPos = poses[kChromPos];
				positionPos = poses[kPositionPos];
				refPos = poses[kRefPos];
				altPos = poses[kAltPos];
			}
			
			int		startCol = firstCol;
			String	chrom = history.get (chromPos);
			int		pos = parseInt (history.get (positionPos));
			String	ref = history.get (refPos);
			String	alt = history.get (altPos);
//			String	geneName = history.get (startCol + kGeneName);
//			int		entrezGeneID = parseInt (history.get (startCol + kNCBIEntrezGeneID));
			startCol += kNCBICols;
//			int		dbSNPBuild = parseInt (history.get (startCol + kdbSNPBuild));
			startCol += kdbSNPCols;
			String	bgiMajAllele = history.get (startCol + kBGIMajorAllele);
			String	bgiMinAllele = history.get (startCol + kBGIMinorAllele);
			double	bgiMajFreq = parseDouble (history.get (startCol + kBGIMajorFreq), kDoNotConvert);
			double	bgiMinFreq = parseDouble (history.get (startCol + kBGIMinorFreq), kDoNotConvert);
			startCol += kBGICols;
			String	espMafs = history.get (startCol + kESPMAF);
			startCol += kESPCols;
			String	hapRefAllele = history.get (startCol + kHapMapRefAllele);
			String	hapAltAllele = history.get (startCol + kHapMapAltAllele);
			double	hapCeuRefFreq = parseDouble (history.get (startCol + kHapMapCeuRefFreq), kDoNotConvert);
			double	hapCeuAltFreq = parseDouble (history.get (startCol + kHapMapCeuAltFreq), kDoNotConvert);
			double	hapYriRefFreq = parseDouble (history.get (startCol + kHapMapYriRefFreq), kDoNotConvert);
			double	hapYriAltFreq = parseDouble (history.get (startCol + kHapMapYriAltFreq), kDoNotConvert);
			double[]	hapJptChbFreq = getCombinedFreq (history, startCol);
			startCol += kHapMapCols;
			double	kGenomeEURFreq = parseDouble (history.get (startCol + k1kGenomeEUR), kDoNotConvert);
			double	kGenomeASNFreq = parseDouble (history.get (startCol + k1kGenomeASN), kDoNotConvert);
			double	kGenomeAFRFreq = parseDouble (history.get (startCol + k1kGenomeAFR), kDoNotConvert);
			double	kGenomeAMRFreq = parseDouble (history.get (startCol + k1kGenomeAMR), kDoNotConvert);
			String	genomeRefAllele = history.get (startCol + k1kGenomeRef);
			String	genomeAltAllele = history.get (startCol + k1kGenomeAlt);
			
			List<AlleleFreq>	results;
			
			results = getBGIMAFs (chrom, pos, bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			if (results != null)
				freqs.addAll (results);
			results = getESPMAFs (chrom, pos, espMafs, ref, alt);
			if (results != null)
				freqs.addAll (results);
			results = getHapMapMAFs (chrom, pos, hapRefAllele, hapAltAllele, kCeuPop, hapCeuRefFreq, hapCeuAltFreq);
			if (results != null)
				freqs.addAll (results);
			results = getHapMapMAFs (chrom, pos, hapRefAllele, hapAltAllele, kYriPop, hapYriRefFreq, hapYriAltFreq);
			if (results != null)
				freqs.addAll (results);
			results = getHapMapMAFs (chrom, pos, hapRefAllele, hapAltAllele, kAsnPop, hapJptChbFreq[kRefOffset], 
									 hapJptChbFreq[kAltOffset]);
			if (results != null)
				freqs.addAll (results);
			results = get1kGenomeMAFs (chrom, pos, k1kGenomeEUR, genomeRefAllele, genomeAltAllele, kGenomeEURFreq);
			if (results != null)
				freqs.addAll (results);
			results = get1kGenomeMAFs (chrom, pos, k1kGenomeASN, genomeRefAllele, genomeAltAllele, kGenomeASNFreq);
			if (results != null)
				freqs.addAll (results);
			results = get1kGenomeMAFs (chrom, pos, k1kGenomeAFR, genomeRefAllele, genomeAltAllele, kGenomeAFRFreq);
			if (results != null)
				freqs.addAll (results);
			results = get1kGenomeMAFs (chrom, pos, k1kGenomeAMR, genomeRefAllele, genomeAltAllele, kGenomeAMRFreq);
			if (results != null)
				freqs.addAll (results);
		}
	}
	
	
	/**
	 * Parse the contents of the pipeline one row at a time
	 * 
	 * @param pipeline		Pipeline that is parsing the VCF file
	 * @param posCol		Column before the start of the last set of data added
	 * @param lastColCount	Count of columns added in the last set
	 * @param info			List to fill in with the results found
	 */
	@SuppressWarnings ("rawtypes")
	protected static void parseVariants (Pipeline pipeline, int posCol, int lastColCount, List<VariantInfo> info)
	{
		boolean	header = true;
		int		numCols = -1;
		int		firstCol = -1;
		int		chromPos = -1;
		int		positionPos = -1;
		int		refPos = -1;
		int		altPos = -1;
		
		while (pipeline.hasNext ())
		{
			History	history = (History) pipeline.next ();
			if (header)
			{
				header = false;
				numCols = history.size ();
				firstCol = numCols - (lastColCount - posCol) + 1;	// Skip the history column
				
				int[]	poses = getPositions (kBaseStrs);
				
				chromPos = poses[kChromPos];
				positionPos = poses[kPositionPos];
				refPos = poses[kRefPos];
				altPos = poses[kAltPos];
			}
			
			int		startCol = firstCol;
			String	chromosome = history.get (chromPos);
			int		pos = parseInt (history.get (positionPos));
			String	ref = history.get (refPos);
			String	alt = history.get (altPos);
			int		endPos = getEndPos (pos, ref, alt);
//			String	geneName = history.get (startCol + kGeneName);
//			int		ncbiEntrezGeneID = parseInt (history.get (startCol + kNCBIEntrezGeneID));
			startCol += kNCBICols;
			String	geneSymbol = history.get (startCol + kHGNCSymbol);
			int		entrezGeneID = parseInt (history.get (startCol + kHGNCEntrezGeneID));
			String	ensemblGeneID = history.get (startCol + kHGNCEnsemblGeneID);
			startCol += kHGNCCols;
			int		firstBuild = parseInt (history.get (startCol + kdbSNPBuild));
			String	dbSNPsID = history.get (startCol + kdbSNPID);
			startCol += kdbSNPCols;
			String	omimDisease = history.get (startCol + kOMIMDisorder);
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
			String	name = history.get (startCol + kRepeatName);
			boolean	repeat = !name.isEmpty ();
			startCol += kRepeatCols;
			name = history.get (startCol + kRegulationName);
			boolean	regulatory = !name.isEmpty ();
			startCol += kRegulationCols;
			
			info.add (new VariantInfo (chromosome, pos, endPos, ref, alt, entrezGeneID, firstBuild, geneSymbol, 
										dbSNPsID, ensemblGeneID, omimDisease, blacklisted, conserved, enhancer, 
										tfbs, tss, unique, repeat, regulatory));
		}
	}
	

	/**
	 * Parse the contents of the pipeline one row at a time
	 * 
	 * @param pipeline		Pipeline that is parsing the VCF file
	 * @param posCol		Column before the start of the last set of data added
	 * @param lastColCount	Count of columns added in the last set
	 */
	@SuppressWarnings ("rawtypes")
	protected static void writeRows (Pipeline pipeline, int posCol, int lastColCount)
	{
		boolean	header = true;
		int		numCols = -1;
		int		firstCol = -1;
		
		while (pipeline.hasNext ())
		{
			History	history = (History) pipeline.next ();
			if (header)
			{
				header = false;
				numCols = history.size ();
				firstCol = numCols - (lastColCount - posCol);
				
				List<String>	headers = History.getMetaData ().getOriginalHeader ();
				String			headerLine = null;
				
				for (String aHeader : headers)
				{
					if (headerLine != null)
						System.out.println (headerLine);	// Had a header w/ more than one line, put breaks between the lines
					
					headerLine = aHeader;
				}
				if (headerLine != null)
					System.out.print (headerLine);
				System.out.println (kRestOfHeader);
			}
			
			int		startCol = firstCol;
			boolean	first = true;
			for (int i = 0; i < startCol; ++i)
			{
				if (first)
					first = false;
				else
					System.out.print ('\t');
				System.out.print (history.get (i));
			}
			
			++startCol;	// Skip over the history
			String	geneName = history.get (startCol + kGeneName);
			int		entrezGeneID = parseInt (history.get (startCol + kNCBIEntrezGeneID));
			startCol += kNCBICols;
			int		dbSNPBuild = parseInt (history.get (startCol + kdbSNPBuild));
			startCol += kdbSNPCols;
			String	bgiMajAllele = history.get (startCol + kBGIMajorAllele);
			String	bgiMinAllele = history.get (startCol + kBGIMinorAllele);
			double	bgiMajFreq = parseDouble (history.get (startCol + kBGIMajorFreq), kDoNotConvert);
			double	bgiMinFreq = parseDouble (history.get (startCol + kBGIMinorFreq), kDoNotConvert);
			startCol += kBGICols;
			String	espMafs = history.get (startCol + kESPMAF);
			startCol += kESPCols;
			String	hapRefAllele = history.get (startCol + kHapMapRefAllele);
			String	hapAltAllele = history.get (startCol + kHapMapAltAllele);
			double	hapCeuRefFreq = parseDouble (history.get (startCol + kHapMapCeuRefFreq), kDoNotConvert);
			double	hapCeuAltFreq = parseDouble (history.get (startCol + kHapMapCeuAltFreq), kDoNotConvert);
			double	hapYriRefFreq = parseDouble (history.get (startCol + kHapMapYriRefFreq), kDoNotConvert);
			double	hapYriAltFreq = parseDouble (history.get (startCol + kHapMapYriAltFreq), kDoNotConvert);
			double[]	hapJptChbFreq = getCombinedFreq (history, startCol);
			startCol += kHapMapCols;
			double	kGenomeFreq = parseDouble (history.get (startCol + k1kGenomeAll), kDoNotConvert);
			double	kGenomeEURFreq = parseDouble (history.get (startCol + k1kGenomeEUR), kDoNotConvert);
			double	kGenomeASNFreq = parseDouble (history.get (startCol + k1kGenomeASN), kDoNotConvert);
			double	kGenomeAFRFreq = parseDouble (history.get (startCol + k1kGenomeAFR), kDoNotConvert);
			double	kGenomeAMRFreq = parseDouble (history.get (startCol + k1kGenomeAMR), kDoNotConvert);
			
			printString (geneName);
			printInt (entrezGeneID);
			printInt (dbSNPBuild);
			printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			printESPMAFs (espMafs);
			printHapMap (hapRefAllele, hapAltAllele, kIsFirst, hapCeuRefFreq, hapCeuAltFreq);
			printHapMap (hapRefAllele, hapAltAllele, kIsNotFirst, hapYriRefFreq, hapYriAltFreq);
			printHapMap (hapRefAllele, hapAltAllele, kIsNotFirst, hapJptChbFreq[kRefOffset], hapJptChbFreq[kAltOffset]);
			print1kGenome (kGenomeFreq);
			print1kGenome (kGenomeEURFreq);
			print1kGenome (kGenomeASNFreq);
			print1kGenome (kGenomeAFRFreq);
			print1kGenome (kGenomeAMRFreq);
			System.out.println ();
		}
	}
	
	
	/**
	 * Get the Ref and Alt frequencies for HapMap JPT+CHB populations
	 * 
	 * @param history	Object to get the relevant strings from
	 * @param startCol	Where to start looking in the History
	 * @return	Array of two doubles.  Both might be NaN, or double between 0 and 1
	 */
	private static double[] getCombinedFreq (History history, int startCol)
	{
		int[]	jptCounts = getCounts (history, startCol + kHapMapJptRefCount);
		int[]	chbCounts = getCounts (history, startCol + kHapMapChbRefCount);
		
		int			ref = jptCounts[kRefOffset] + chbCounts[kRefOffset];
		int			alt = jptCounts[kAltOffset] + chbCounts[kAltOffset];
		int			total = jptCounts[kTotalOffset] + chbCounts[kTotalOffset];
		double[]	results = new double[kNumResults];
		
		if ((total == 0) || (alt == 0))	// If alt is zero, there's nothing to report
			results[kRefOffset] = results[kRefOffset] = Double.NaN;
		else
		{
			results[kRefOffset] = ((double) ref) / total;
			results[kAltOffset] = ((double) alt) / total;
		}
		
		return results;
	}
	
	
	/**
	 * Get the counts for a population group
	 * 
	 * @param history	Where to get the counts from
	 * @param startCol	First column to look at
	 * @return	An array of three ints.  It will have all 0s if there are no value, but it will 
	 * always exist and have three entries
	 */
	private static int[] getCounts (History history, int startCol)
	{
		int[]	results = new int[kNumOffsets];
		
		results[kRefOffset] = parseInt (history.get (startCol + kRefOffset));
		results[kAltOffset] = parseInt (history.get (startCol + kAltOffset));
		results[kTotalOffset] = parseInt (history.get (startCol + kTotalOffset));
		
		return results;
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
	 * Calculate the end position given the starting position and the alt and ref bases
	 * 
	 * @param pos	Starting position
	 * @param ref	Reference base(s).  May be empty if an insertion, or may have one base
	 * @param alt	Alternate base(s).  May be empty if a deletion, or may have one base
	 * @return	-1 if an SNV, pos if a deletion, pos + mount inserted if an insertion
	 */
	private static int getEndPos (int pos, String ref, String alt)
	{
		int	refLen = ref.length ();
		int	altLen = alt.length ();
		
		if (refLen == altLen)
			return kInvalidID;
		
		if (refLen > altLen)	// Deletion
			return pos;
		
		return pos + altLen - refLen;	// insertion size is the number of excess characters in alt
	}
		
	
	/**
	 * Get the Ref and Alt frequencies for HapMap JPT+CHB populations
	 * 
	 * @param cols		Object to get the relevant strings from
	 * @param startCol	Where to start looking in the History
	 * @return	Array of two doubles.  Both might be NaN, or double between 0 and 1
	 */
	private static double[] getCombinedFreq (String[] cols, int startCol)
	{
		int[]	jptCounts = getCounts (cols, startCol + kHapMapJptRefCount);
		int[]	chbCounts = getCounts (cols, startCol + kHapMapChbRefCount);
		
		int			ref = jptCounts[kRefOffset] + chbCounts[kRefOffset];
		int			alt = jptCounts[kAltOffset] + chbCounts[kAltOffset];
		int			total = jptCounts[kTotalOffset] + chbCounts[kTotalOffset];
		double[]	results = new double[kNumResults];
		
		if ((total == 0) || (alt == 0))	// If alt is zero, there's nothing to report
			results[kRefOffset] = results[kRefOffset] = Double.NaN;
		else
		{
			results[kRefOffset] = ((double) ref) / total;
			results[kAltOffset] = ((double) alt) / total;
		}
		
		return results;
	}
	
	
	/**
	 * Get the counts for a population group
	 * 
	 * @param cols		Where to get the counts from
	 * @param startCol	First column to look at
	 * @return	An array of three ints.  It will have all 0s if there are no value, but it will 
	 * always exist and have three entries
	 */
	private static int[] getCounts (String[] cols, int startCol)
	{
		int[]	results = new int[kNumOffsets];
		
		results[kRefOffset] = parseInt (cols[startCol + kRefOffset]);
		results[kAltOffset] = parseInt (cols[startCol + kAltOffset]);
		results[kTotalOffset] = parseInt (cols[startCol + kTotalOffset]);
		
		return results;
	}
	
	
	/**
	 * Go through the History metadata finding which columns hold the Alt and Ref positions
	 * 
	 * @param baseStrs	Strings to look for matches for
	 * @return	Array of ints, same size as baseStrs, holding the matches, or -1 if not matched
	 */
	private static int[] getPositions (String[] baseStrs)
	{
		HistoryMetaData			metaData = History.getMetaData ();
		List<ColumnMetaData>	columns = metaData.getColumns ();
		int						numStrs = baseStrs.length;
		int[]					results = new int[numStrs];
		int						leftToFind = numStrs;
		
		for (int i = 0; i < numStrs; ++i)
			results[i] = -1;
		
		int	pos = 0;
		for (ColumnMetaData	column : columns)
		{
			String	test = column.getColumnName ().toUpperCase ();
			
			for (int i = 0; i < numStrs; ++i)
			{
				if (test.equals (baseStrs[i]))
				{
					results[i] = pos;
					--leftToFind;
					if (leftToFind == 0)
						return results;
					break;	// Strings are different, so if found a match done looking for this test string
				}
			}
			
			++pos;
		}
		
		return results;
	}


	/**
	 * Get the contents of the pipeline, and then parse them all
	 * 
	 * @param pipeline		Pipeline that is parsing the VCF file
	 * @param posCol		Column before the start of the last set of data added
	 * @param lastColCount	Count of columns added in the last set
	 */
	@SuppressWarnings ({"rawtypes", "unchecked"})
	protected static void handleAll (Pipeline pipeline, int posCol, int lastColCount)
	{
		List<String>	rows = new ArrayList<String> ();
		pipeline.fill (rows);
		
		boolean	header = true;
		int		numCols = -1;
		int		firstCol = -1;
		for (String row : rows)
		{
			String[]	cols = SplitFile.mySplit (row, "\t", kReturnAll);
			if (header)
			{
				header = false;
				numCols = cols.length;
				firstCol = numCols - ((-1 - posCol) + lastColCount);
				boolean	first = true;
				for (int i = 0; i < firstCol; ++i)
				{
					if (first)
						first = false;
					else
						System.out.print ('\t');
					System.out.print (cols[i]);
				}
				System.out.println (kRestOfHeader);
				continue;
			}
			
			int		startCol = firstCol;
			boolean	first = true;
			for (int i = 0; i < startCol; ++i)
			{
				if (first)
					first = false;
				else
					System.out.print ('\t');
				System.out.print (cols[i]);
			}
			
			String	geneName = cols[startCol + kGeneName];
			int		entrezGeneID = parseInt (cols[startCol + kNCBIEntrezGeneID]);
			startCol += kNCBICols;
			int		dbSNPBuild = parseInt (cols[startCol + kdbSNPBuild]);
			startCol += kdbSNPCols;
			String	bgiMajAllele = cols[startCol + kBGIMajorAllele];
			String	bgiMinAllele = cols[startCol + kBGIMinorAllele];
			double	bgiMajFreq = parseDouble (cols[startCol + kBGIMajorFreq], kDoNotConvert);
			double	bgiMinFreq = parseDouble (cols[startCol + kBGIMinorFreq], kDoNotConvert);
			startCol += kBGICols;
			String	espMafs = cols[startCol + kESPMAF];
			startCol += kESPCols;
			String	hapRefAllele = cols[startCol + kHapMapRefAllele];
			String	hapAltAllele = cols[startCol + kHapMapAltAllele];
			double	hapCeuRefFreq = parseDouble (cols[startCol + kHapMapCeuRefFreq], kDoNotConvert);
			double	hapCeuAltFreq = parseDouble (cols[startCol + kHapMapCeuAltFreq], kDoNotConvert);
			double	hapYriRefFreq = parseDouble (cols[startCol + kHapMapYriRefFreq], kDoNotConvert);
			double	hapYriAltFreq = parseDouble (cols[startCol + kHapMapYriAltFreq], kDoNotConvert);
			double[]	hapJptChbFreq = getCombinedFreq (cols, startCol);
			
			printString (geneName);
			printInt (entrezGeneID);
			printInt (dbSNPBuild);
			printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			printESPMAFs (espMafs);
			
			printString (geneName);
			printInt (dbSNPBuild);
			printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			printESPMAFs (espMafs);
			printHapMap (hapRefAllele, hapAltAllele, kIsFirst, hapCeuRefFreq, hapCeuAltFreq);
			printHapMap (hapRefAllele, hapAltAllele, kIsNotFirst, hapYriRefFreq, hapYriAltFreq);
			printHapMap (hapRefAllele, hapAltAllele, kIsNotFirst, hapJptChbFreq[kRefOffset], hapJptChbFreq[kAltOffset]);
			System.out.println ();
		}
	}
	
	
	/**
	 * Print out a String if it isn't blank, and then print out a tab, regardless of whether or not 
	 * the String was blank
	 * 
	 * @param theStr	String to print out, if not blank
	 */
	private static void printString (String theStr)
	{
		System.out.print ('\t');
		if (!theStr.equals (kBlank))
			System.out.print (theStr);
	}
	
	
	/**
	 * Print out an integer if it isn't 0, and then print out a tab, regardless of whether or not 
	 * the integer was 0
	 * 
	 * @param theInt	Integer to print out, if not 0
	 */
	private static void printInt (int theInt)
	{
		System.out.print ('\t');
		if (theInt > 0)
			System.out.print (theInt);
	}
	
	
	/**
	 * Print out the BGI info, or tabs if nothing to print
	 * 
	 * @param bgiMajAllele	Major / Ref Allele
	 * @param bgiMinAllele	Minor / Alt Allele
	 * @param bgiMajFreq	Major / Ref Frequency
	 * @param bgiMinFreq	Minor / Alt Frequency
	 */
	private static void printBGI (String bgiMajAllele, String bgiMinAllele, double bgiMajFreq, double bgiMinFreq)
	{
		if (!bgiMajAllele.equals (kBlank) && !bgiMinAllele.equals (kBlank))
		{
			System.out.print ('\t');
			System.out.print (bgiMajAllele);
			System.out.print ('\t');
			System.out.print (bgiMinAllele);
			System.out.print ('\t');
			if (!Double.isNaN (bgiMajFreq))	// Otherwise just do a blank
				System.out.print (bgiMajFreq);
			
			System.out.print ('\t');
			if (!Double.isNaN (bgiMinFreq))	// Otherwise just do a blank
				System.out.print (bgiMinFreq);
		}
		else
			System.out.print ("\t\t\t\t");
	}
	
	
	/**
	 * Print out the HapMap info, or tabs if nothing to print
	 * 
	 * @param hapRefAllele	Major / Ref Allele
	 * @param hapAltAllele	Minor / Alt Allele
	 * @param hapRefFreq	Major / Ref Frequency
	 * @param hapAltFreq	Minor / Alt Frequency
	 */
	private static void printHapMap (String hapRefAllele, String hapAltAllele, boolean isFirst, double hapRefFreq, double hapAltFreq)
	{
		if (!hapRefAllele.equals (kBlank) && !hapAltAllele.equals (kBlank))
		{
			if (isFirst)
			{
				System.out.print ('\t');
				System.out.print (hapRefAllele);
				System.out.print ('\t');
				System.out.print (hapAltAllele);
			}
			
			System.out.print ('\t');
			if (!Double.isNaN (hapRefFreq))	// Otherwise just do a blank
				System.out.print (hapRefFreq);
			System.out.print ('\t');
			if (!Double.isNaN (hapAltFreq))
				System.out.print (hapAltFreq);
		}
		else if (isFirst)
			System.out.print ("\t\t\t\t");
		else
			System.out.print ("\t\t");
	}
	
	
	/**
	 * Print out the Thousand Genomes info, or tabs if nothing to print
	 * 
	 * @param altFreq	Minor / Alt Frequency
	 */
	private static void print1kGenome (double altFreq)
	{
		System.out.print ('\t');
		if ((altFreq == 0.0) || (Double.isNaN (altFreq)))
			return;
		System.out.print (altFreq);
	}
	
	
	/**
	 * Print out the CEU and AFR ESP MAFs if they're there, or just tabs if they aren't
	 * 
	 * @param espMafs	String holding ESP Minor allele frequencies in the format ["CEU", "AFR", "Total"]
	 */
	private static void printESPMAFs (String espMafs)
	{
		int	len = espMafs.length ();
		if (espMafs.equals (kBlank) || (len < 4))
		{
			System.out.print ("\t\t");
			return;
		}
		
		espMafs = espMafs.substring (2, len - 2);	// Trim off [""]
		int	pos = espMafs.indexOf ('"');
		if (pos < 0)
		{
			System.out.print ("\t\t");
			return;
		}
		
		System.out.print ('\t');
		double	maf = parseDouble (espMafs.substring (0, pos), kConvertFromPercent);
		System.out.print (maf);
		System.out.print ('\t');
		
		pos = espMafs.indexOf ('"', pos + 1) + 1;
		if (pos > 0)
		{
			int	end = espMafs.indexOf ('"', pos + 1);
			if (end > 0)
			{
				maf = parseDouble (espMafs.substring (pos, end), kConvertFromPercent);
				System.out.print (maf);
			}
		}
	}
	
	
	/**
	 * Return a list holding BGI AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param chromosome	The chromosome the variant is on
	 * @param pos			The position of the variant in the chromosome
	 * @param bgiMajAllele	Major / Ref Allele
	 * @param bgiMinAllele	Minor / Alt Allele
	 * @param bgiMajFreq	Major / Ref Frequency
	 * @param bgiMinFreq	Minor / Alt Frequency
	 * @return	List of one or two AlleleFreq objects, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getBGIMAFs (String chromosome, int pos, String bgiMajAllele, String bgiMinAllele, 
												double bgiMajFreq, double bgiMinFreq)
	{
		if (bgiMajAllele.equals (kBlank) || bgiMinAllele.equals (kBlank))
			return null;
		
		boolean	hasRef = !((bgiMajFreq == 0.0) || (Double.isNaN (bgiMajFreq)));
		boolean	hasAlt = !((bgiMinFreq == 0.0) || (Double.isNaN (bgiMinFreq)));
		if (!hasRef || !hasAlt)
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = bgiMajAllele.charAt (0);
		char	minorBase = getFirstBase (bgiMinAllele);
		
		results.add (new AlleleFreq (chromosome, pos, minorBase, majorBase, bgiMinFreq, bgiMajFreq, kBGIFreq));
		
		return results;
	}
	
	
	/**
	 * Return a list holding HapMap AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param chromosome	The chromosome the variant is on
	 * @param pos			The position of the variant in the chromosome
	 * @param hapRefAllele	Major / Ref Allele
	 * @param hapAltAllele	Minor / Alt Allele
	 * @param pop			Which population the data is for
	 * @param hapRefFreq	Major / Ref Frequency
	 * @param hapAltFreq	Minor / Alt Frequency
	 * @return	List of one AlleleFreq object, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getHapMapMAFs (String chromosome, int pos, String hapRefAllele, String hapAltAllele, int pop, 
													double hapRefFreq, double hapAltFreq)
	{
		if (hapRefAllele.equals (kBlank) || hapAltAllele.equals (kBlank))
			return null;
		
		boolean	hasRef = !((hapRefFreq == 0.0) || (Double.isNaN (hapRefFreq)));
		boolean	hasAlt = !((hapAltFreq == 0.0) || (Double.isNaN (hapAltFreq)));
		if (!hasRef || !hasAlt)
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = hapRefAllele.charAt (0);
		char	minorBase = getFirstBase (hapAltAllele);
		
		results.add (new AlleleFreq (chromosome, pos, minorBase, majorBase, hapAltFreq, hapRefFreq, kHapMapFreq[pop]));
		
		return results;
	}
	
	
	/**
	 * Return a list holding Thousand Genomes AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param chromosome	The chromosome the variant is on
	 * @param pos			The position of the variant in the chromosome
	 * @param pop			Which population the data is for
	 * @param ref			Reference base, will use first char, so can not be null or empty
	 * @param alt			Alternate base, will use first char, so can not be null or empty
	 * @param altFreq		Minor / Alt Frequency
	 * @return	List of one AlleleFreq object, or null if there aren't any specified
	 */
	private static List<AlleleFreq> get1kGenomeMAFs (String chromosome, int pos, int pop, String ref, String alt, double altFreq)
	{
		if ((altFreq == 0.0) || (Double.isNaN (altFreq)))
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = ref.charAt (0);
		char	minorBase = getFirstBase (alt);
		
		results.add (new AlleleFreq (chromosome, pos, minorBase, majorBase, altFreq, 1.0 - altFreq, k1kGenomeFreq[pop]));
		
		return results;
	}
	
	
	/**
	 * Look through a String for the first A, C, G, T.  Failing to find any of those, look for hte first 
	 * letter.  Failing that, return 'A'
	 * 
	 * @param baseStr	String to look through.  Must not be null
	 * @return	A Character, guaranteed to be a capital letter
	 */
	private static final char getFirstBase (String baseStr)
	{
		baseStr = baseStr.toUpperCase ();	// Make sure everything's upper case
		int		numBases = baseStr.length ();
		char	base;
		
		for (int i = 0; i < numBases; ++i)
		{
			base = baseStr.charAt (i);
			switch (base)
			{
				case 'A':
				case 'C':
				case 'G':
				case 'T':
					return base;
			}
		}
		
		// Couldn't find an A, T, C, or G, go for any letter
		for (int i = 0; i < numBases; ++i)
		{
			base = baseStr.charAt (i);
			if (Character.isLetter (base))
				return base;
		}
		
		// Couldn't find anything, return the default
		return 'A';
	}
	
	
	/**
	 * Return a list holding CEU and AFR ESP AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param chromosome	The chromosome the variant is on
	 * @param position		The position of the variant in the chromosome
	 * @param espMafs		String holding ESP Minor allele frequencies in the format ["CEU", "AFR", "Total"]
	 * @param ref			Reference base, will use first char, so can not be null or empty
	 * @param alt			Alternate base, will use first char, so can not be null or empty
	 * @return	List of one or two AlleleFreq objects, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getESPMAFs (String chromosome, int position, String espMafs, String ref, String alt)
	{
		int	len = espMafs.length ();
		if (espMafs.equals (kBlank) || (len < 4))
			return null;
		
		espMafs = espMafs.substring (2, len - 2);	// Trim off [""]
		int	pos = espMafs.indexOf ('"');
		if (pos < 0)
			return null;
		
		double	ceuMaf = parseDouble (espMafs.substring (0, pos), kConvertFromPercent);
		double	afrMaf = Double.NaN;
		
		pos = espMafs.indexOf ('"', pos + 1) + 1;
		if (pos > 0)
		{
			int	end = espMafs.indexOf ('"', pos + 1);
			if (end > 0)
				afrMaf = parseDouble (espMafs.substring (pos, end), kConvertFromPercent);
		}
		
		// Make sure have something to report
		boolean	hasCEU = !((ceuMaf == 0.0) || (Double.isNaN (ceuMaf)));
		boolean	hasAFR = !((afrMaf == 0.0) || (Double.isNaN (afrMaf)));
		if (!hasCEU && !hasAFR)
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = ref.charAt (0);
		char	minorBase = getFirstBase (alt);
		
		if (hasCEU)
			results.add (new AlleleFreq (chromosome, position, minorBase, majorBase, ceuMaf, 1.0 - ceuMaf, kEspEurMaf));
		
		if (hasAFR)
			results.add (new AlleleFreq (chromosome, position, minorBase, majorBase, afrMaf, 1.0 - afrMaf, kEspAfrMaf));
		
		return results;
	}
	
	
	/**
	 * Parse a String, returning the int represented, or 0 if not an int
	 * 
	 * @param theInt	String to parse.  Must not be null
	 * @return	An integer, 0 if parsing failed
	 */
	private static int parseInt (String theInt)
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
	 * Parse a String, returning the double represented, or NaN if not a double
	 * 
	 * @param theDouble	String to parse.  Must not be null
	 * @return	An double, NaN if parsing failed
	 */
	private static double parseDouble (String theDouble, boolean convertFromPercent)
	{
		double	result = Double.NaN;
		if (!theDouble.equals (kBlank))
		{
			try
			{
				result = Double.parseDouble (theDouble);
				if (convertFromPercent)
					result /= kPercentAdjust;
			}
			catch (NumberFormatException oops)
			{
				// Do nothing
			}
		}
		
		return result;
	}
	
	
	/**
	 * Get the properties from the properties file
	 * 
	 * @return	The properties, or null if coulnd't load them
	 */
	private static Properties getProperties ()
	{
		Properties	theProperties = new Properties ();
		InputStream is = TreatPipeline.class.getResourceAsStream ("TreatPipeline.properties");
		
		try
		{
			theProperties.load (is);
			is.close ();
			is = null;
		}
		catch (Exception ex)
		{
			/* leave gLocalProperties empty, bail */
			ex.printStackTrace (System.err);
			try
			{
				if (is != null)
					is.close ();
			}
			catch (IOException oops)
			{
				// Don't care
			}
			return null; // We're toast
		}
		
		return theProperties;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.mayo.bsi.genomics.exometablebrowser.server.Usage#usage()
	 */
	/**
	 * Writes Usage information to a String
	 * 
	 * @return	How to use this app
	 * @see edu.mayo.bsi.genomics.exometablebrowser.server.Usage#usage()
	 */
	public String usage ()
	{
		return TreatPipeline.Usage ();
	}
	
	
	/**
	 * Writes Usage information to a String.  Exists so don't have to create an object just to print Usage
	 * 
	 * @return	How to use this app
	 */
	private static String Usage ()
	{
		StringBuilder	usage = new StringBuilder ();
		
		usage.append ("Usage: java -jar " + kAppName + ".jar <Options>\n");
		
		usage.append ("\t-v | --vcfFile: <path to vcf file>: Path of the vcf file to parse.  Required\n");
		usage.append ("\t-b | --baseDir: <Path to BioR Catalogs>: Path to the BioR Catalog bse directory.\n\t\t");
		usage.append ("Optional, the default is '/data4/bsi/refdata-new/catalogs/v1/BioR/'\n");
		usage.append ("\t-i | --variantInfo: If specified, get Variant Info.  If not, get Frequency Info\n");
		usage.append ("\t-V | --version: Prints name and version string, then exits\n");
		
		return usage.toString ();
	}


	/* (non-Javadoc)
	 * @see edu.mayo.bsi.genomics.exometablebrowser.server.Usage#name()
	 */
	/**
	 * Return the name of the application
	 * 
	 * @return	Name of the application, for use in error / usage reporting
	 * @see edu.mayo.bsi.genomics.exometablebrowser.server.Usage#name()
	 */
	public String name ()
	{
		return kAppName;
	}
	
	
	/**
	 * Print on the name and version of this application
	 */
	private static void printVersion ()
	{
		System.out.println (kAppName + " Version " + kVersion);
	}
	
}
