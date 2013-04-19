/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.mayo.bior.pipeline.Treat;


/**
 * 
 * @author dquest
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.util.BiorProperties;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.RemoveAllJSONPipe;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryMetaData;


/**
 * 
 * @author m102417
 */
@SuppressWarnings ({"rawtypes", "unchecked"})
public class AlleleFrequenciesPipeline extends Pipeline implements Cleaner
{
	private BiorProperties	biorProps;
	private String			baseDir;
	
	private boolean	header = true;
	private int		deleteColCount = 0;
	private int		numCols = 0;
	private int		firstCol = 0;
	private int		refPos = 0;
	private int		altPos = 0;
	
	
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
	private static final int	kHapMapCols = kHapMapChbTotalCount + 1;
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
	private static final int	k1kGenomeCols = k1kGenomeAlt + 1;
	private static final String[]	k1kGenomeDrill = {"INFO.AF", "INFO.EUR_AF", "INFO.ASN_AF", "INFO.AFR_AF", "INFO.AMR_AF", 
	                             	                  "_refAllele", "_altAlleles"};
	private static final int	kRefOffset = 0;
	private static final int	kAltOffset = kRefOffset + 1;
	private static final int	kTotalOffset = kAltOffset + 1;
	private static final int	kNumOffsets = kTotalOffset + 1;
	private static final int	kNumResults = kAltOffset + 1;
	private static final String	kBlank = ".";
	private static final String[]	kBaseStrs = {"CHROM", "POS", "REF", "ALT"};
	private static final int	kChromPos = 0;
	private static final int	kPositionPos = kChromPos + 1;
	private static final int	kRefPos = kPositionPos + 1;
	private static final int	kAltPos = kRefPos + 1;
	private static final String	kEspEurMaf = "ESP6500_EUR_maf";
	private static final String	kEspAfrMaf = "ESP6500_AFR_maf";
	private static final String	kBGIFreq = "BGI200_Danish";
	private static final String[]	kHapMapFreq = {"HapMap_CEU_allele_freq", "HapMap_YRI_allele_freq", "HapMap_JPT+CHB_allele_freq"};
	private static final String[]	k1kGenomeFreq = {"1kGenome_EUR_allele_freq", "1kGenome_ASN_allele_freq", 
	                             	                 "1kGenome_AFR_allele_freq", "1kGenome_AMR_allele_freq"};
	private static final double	kPercentAdjust = 100.0;
	private static final boolean	kConvertFromPercent = true;
	private static final boolean	kDoNotConvert = false;
	private static final String	kMajorSplit = ",";
	private static final String	kMinorSplit = "/";
	
	
	/**
	 * Default constructor, IdentityPipe in and out, no cleaning
	 * 
	 * @throws IOException
	 */
	public AlleleFrequenciesPipeline () throws IOException
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
	public AlleleFrequenciesPipeline (Pipe input, Pipe output) throws IOException
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
	 * @param clean		If true, trims off the catalog data and adds one column per frequency source 
	 * (i.e. 1kGenome_EUR, etc), with the information in the TREAT format
	 * @throws IOException
	 */
	public AlleleFrequenciesPipeline (Pipe input, Pipe output, boolean clean) throws IOException
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
	 * @param clean		If true, trims off the catalog data and adds one column per frequency source 
	 * (i.e. 1kGenome_EUR, etc), with the information in the TREAT format
	 * @throws IOException
	 */
	public AlleleFrequenciesPipeline (Pipe input, Pipe output, String baseDir, boolean clean) throws IOException
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
		
		String		bgiFile = baseDir + biorProps.get ("bgiFile");
		String		espFile = baseDir + biorProps.get ("espFile");
		String		hapMapFile = baseDir + biorProps.get ("hapMapFile");
		String		genomeFile = baseDir + biorProps.get ("kGenomeFile");	// genomeFile
		String[]	bgiDrill = kBgiDrill;
		String[]	espDrill = kEspDrill;
		String[]	hapMapDrill = kHapMapDrill;
		String[]	genomeDrill = k1kGenomeDrill;
		int			posCol = -1;
		
		// ??-history better convert input to a history coming into the pipeline e.g. HistoryInPipe
		Pipeline	p = new Pipeline (input, 
									  new VCF2VariantPipe (), new SameVariantPipe (bgiFile, posCol), 
									  new DrillPipe (false, bgiDrill), 
									  new SameVariantPipe (espFile, posCol -= bgiDrill.length), 
									  new DrillPipe (false, espDrill), 
									  new SameVariantPipe (hapMapFile, posCol -= espDrill.length), 
									  new DrillPipe (false, hapMapDrill), 
									  new SameVariantPipe (genomeFile, posCol -= hapMapDrill.length), 
									  new DrillPipe (false, genomeDrill), 
									  new RemoveAllJSONPipe (), cleaner, output);
		
		this.setPipes (p.getPipes ());
		deleteColCount = genomeDrill.length - posCol;
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
			
			int[]	poses = getPositions (kBaseStrs);
			
			refPos = poses[kRefPos];
			altPos = poses[kAltPos];
			
			// Now clean up the history metadata
			HistoryMetaData			metaData = History.getMetaData ();
			List<ColumnMetaData>	columns = metaData.getColumns ();
			
			for (int i = numCols - 1; i >= firstCol; --i)
				columns.remove (i);
			
			addAlleleColumns (columns);
		}
		
		int		startCol = firstCol;
		String	ref = history.get (refPos);
		String	alt = history.get (altPos);
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
		startCol += k1kGenomeCols;
		
		// Got the information we needed, now clear it all out
		for (int i = startCol - 1; i >= firstCol; --i)
			history.remove (i);
		
		history.addAll (getESPMAFs (espMafs, ref, alt));
		history.add (getBGIMAFs (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq));
		
		history.add (getHapMapMAFs (hapRefAllele, hapAltAllele, hapCeuRefFreq, hapCeuAltFreq));
		history.add (getHapMapMAFs (hapRefAllele, hapAltAllele, hapYriRefFreq, hapYriAltFreq));
		history.add (getHapMapMAFs (hapRefAllele, hapAltAllele, hapJptChbFreq[kRefOffset], hapJptChbFreq[kAltOffset]));
		
		history.add (get1kGenomeMAFs (genomeRefAllele, genomeAltAllele, kGenomeEURFreq));
		history.add (get1kGenomeMAFs (genomeRefAllele, genomeAltAllele, kGenomeASNFreq));
		history.add (get1kGenomeMAFs (genomeRefAllele, genomeAltAllele, kGenomeAFRFreq));
		history.add (get1kGenomeMAFs (genomeRefAllele, genomeAltAllele, kGenomeAMRFreq));
		
		return history;
	}
	
	
	/**
	 * Add the Allele Frequency Source columns to the history metadata
	 * 
	 * @param columns	Where to add them
	 */
	private void addAlleleColumns (List<ColumnMetaData> columns)
	{
		columns.add (new ColumnMetaData (kEspEurMaf));
		columns.add (new ColumnMetaData (kEspAfrMaf));
		columns.add (new ColumnMetaData (kBGIFreq));
		
		for (String columnName : kHapMapFreq)
			columns.add (new ColumnMetaData (columnName));
		
		for (String columnName : k1kGenomeFreq)
			columns.add (new ColumnMetaData (columnName));
	}
	
	
	/**
	 * Return a list holding CEU and AFR ESP frequency results if they're there, or two empty strings if they aren't
	 * 
	 * @param espMafs	String holding ESP Minor allele frequencies in the format ["CEU", "AFR", "Total"]
	 * @param ref		Reference base, will use first char, so can not be null or empty
	 * @param alt		Alternate base, will use first char, so can not be null or empty
	 * @return	List of two Strings, possibly with frequency data in them in the format "Alt/Ref,MinorAF/MajorAF"
	 */
	private List<String> getESPMAFs (String espMafs, String ref, String alt)
	{
		int	len = espMafs.length ();
		if (espMafs.equals (kBlank) || (len < 4))
			return makeEmptiesList (2);
		
		espMafs = espMafs.substring (2, len - 2);	// Trim off [""]
		int	pos = espMafs.indexOf ('"');
		if (pos < 0)
			return makeEmptiesList (2);
		
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
			return makeEmptiesList (2);
		
		List<String>	results = new ArrayList<String> (2);
		char			majorBase = ref.charAt (0);
		char			minorBase = getFirstBase (alt);
		if (hasCEU)
			results.add (makeAlleleFrequency (majorBase, minorBase, ceuMaf, 1.0 - ceuMaf));
		else
			results.add (kBlank);
		
		if (hasAFR)
			results.add (makeAlleleFrequency (majorBase, minorBase, afrMaf, 1.0 - afrMaf));
		else
			results.add (kBlank);
		
		return results;
	}
	
	
	/**
	 * Return a list holding BGI Danish frequency results if they're there, or one empty string if they aren't
	 * 
	 * @param chromosome	The chromosome the variant is on
	 * @param pos			The position of the variant in the chromosome
	 * @param bgiMajAllele	Major / Ref Allele
	 * @param bgiMinAllele	Minor / Alt Allele
	 * @param bgiMajFreq	Major / Ref Frequency
	 * @param bgiMinFreq	Minor / Alt Frequency
	 * @return	A String, possibly with frequency data in the format "Alt/Ref,MinorAF/MajorAF"
	 */
	private String getBGIMAFs (String bgiMajAllele, String bgiMinAllele, double bgiMajFreq, double bgiMinFreq)
	{
		if (bgiMajAllele.equals (kBlank) || bgiMinAllele.equals (kBlank))
			return kBlank;
		
		boolean	hasRef = !((bgiMajFreq == 0.0) || (Double.isNaN (bgiMajFreq)));
		boolean	hasAlt = !((bgiMinFreq == 0.0) || (Double.isNaN (bgiMinFreq)));
		if (!hasRef || !hasAlt)
			return kBlank;
		
		char	majorBase = bgiMajAllele.charAt (0);
		char	minorBase = getFirstBase (bgiMinAllele);
		
		return makeAlleleFrequency (majorBase, minorBase, bgiMinFreq, bgiMajFreq);
	}
	
	
	/**
	 * Return a list holding HapMap frequency results if they're there, or one empty string if they aren't
	 * 
	 * @param hapRefAllele	Major / Ref Allele
	 * @param hapAltAllele	Minor / Alt Allele
	 * @param hapRefFreq	Major / Ref Frequency
	 * @param hapAltFreq	Minor / Alt Frequency
	 * @return	A String, possibly with frequency data in the format "Alt/Ref,MinorAF/MajorAF"
	 */
	private String getHapMapMAFs (String hapRefAllele, String hapAltAllele, double hapRefFreq, double hapAltFreq)
	{
		if (hapRefAllele.equals (kBlank) || hapAltAllele.equals (kBlank))
			return kBlank;
		
		boolean	hasRef = !((hapRefFreq == 0.0) || (Double.isNaN (hapRefFreq)));
		boolean	hasAlt = !((hapAltFreq == 0.0) || (Double.isNaN (hapAltFreq)));
		if (!hasRef || !hasAlt)
			return kBlank;
		
		char	majorBase = hapRefAllele.charAt (0);
		char	minorBase = getFirstBase (hapAltAllele);
		
		return makeAlleleFrequency (majorBase, minorBase, hapAltFreq, hapRefFreq);
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
	private String get1kGenomeMAFs (String ref, String alt, double altFreq)
	{
		if ((altFreq == 0.0) || (Double.isNaN (altFreq)))
			return kBlank;
		
		char	majorBase = ref.charAt (0);
		char	minorBase = getFirstBase (alt);
		
		return makeAlleleFrequency (majorBase, minorBase, altFreq, 1.0 - altFreq);
	}
	
	
	/**
	 * Go through the History metadata finding which columns hold the Alt and Ref positions
	 * 
	 * @param baseStrs	Strings to look for matches for
	 * @return	Array of ints, same size as baseStrs, holding the matches, or -1 if not matched
	 */
	private static final int[] getPositions (String[] baseStrs)
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
	 * Get the Ref and Alt frequencies for HapMap JPT+CHB populations
	 * 
	 * @param history	Object to get the relevant strings from
	 * @param startCol	Where to start looking in the History
	 * @return	Array of two doubles.  Both might be NaN, or double between 0 and 1
	 */
	private static final double[] getCombinedFreq (History history, int startCol)
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
	private static final int[] getCounts (History history, int startCol)
	{
		int[]	results = new int[kNumOffsets];
		
		results[kRefOffset] = parseInt (history.get (startCol + kRefOffset));
		results[kAltOffset] = parseInt (history.get (startCol + kAltOffset));
		results[kTotalOffset] = parseInt (history.get (startCol + kTotalOffset));
		
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
	 * Make an allele frequency string
	 * 
	 * @param ref	The reference char
	 * @param alt	The alternate char
	 * @param minor	The minor allele frequency, a number between 0 and 1
	 * @param major	The major allele frequency, a number between 0 and 1
	 * @return	String of frequency data in them in the format "Alt/Ref,MinorAF/MajorAF"
	 */
	private static final String makeAlleleFrequency (char ref, char alt, double minor, double major)
	{
		StringBuilder	result = new StringBuilder ();
		
		result.append (alt);
		result.append (kMinorSplit);
		result.append (ref);
		result.append (kMajorSplit);
		result.append (minor);
		result.append (kMinorSplit);
		result.append (major);
		
		return result.toString ();
	}
	
	
	/**
	 * Make a List with the specified number of blank Strings in it
	 * 
	 * @param count	Number of blank strings to add to the list
	 * @return	List with Math.max(0, count) blank strings in it
	 */
	private static final List<String> makeEmptiesList (int count)
	{
		List<String>	results = new ArrayList<String> (count);
		
		for (int i = 0; i < count; ++i)
			results.add (kBlank);
		
		return results;
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
	 * Parse a String, returning the double represented, or NaN if not a double
	 * 
	 * @param theDouble	String to parse.  Must not be null
	 * @return	An double, NaN if parsing failed
	 */
	private static final double parseDouble (String theDouble, boolean convertFromPercent)
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
	
}
