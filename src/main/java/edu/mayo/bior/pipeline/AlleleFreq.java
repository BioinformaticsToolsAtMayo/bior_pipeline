/**
 * Exome Workflow
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2010
 *
 */
package edu.mayo.bior.pipeline;

import java.util.List;

/**
 * <p>@author Gregory Dougherty</p>
 *
 */
public class AlleleFreq 
{
	private char	minorBase;
	private char	majorBase;
	private double	minorFreq;
	private double	majorFreq;
	private String	frequencySource;
	
	
	/** Array holding the Allele Frequency Sources we recognize, and their order */
	public static final String[]	kFrequencySources = {"HapMap_CEU_allele_freq", "HapMap_YRI_allele_freq", 
	                             	                     "HapMap_JPT+CHB_allele_freq", 
	                             	                     "1kGenome_CEU_allele_freq", 
	                             	                     "1kGenome_YRI_allele_freq", 
	                             	                     "1kGenome_JPT+CHB_allele_freq", "BGI200_Danish", 
	                             	                     "ESP5400_EUR_maf", "ESP5400_AFR_maf", 
	                             	                     "ESP6500_EUR_maf", "ESP6500_AFR_maf"};
	private static final int	kHapMapCEU = 0;
	private static final int	kHapMapYRI = kHapMapCEU + 1;
	private static final int	kHapMapJPT = kHapMapYRI + 1;
	private static final int	k1kGenomeCEU = kHapMapJPT + 1;
	private static final int	k1kGenomeYRI = k1kGenomeCEU + 1;
	private static final int	k1kGenomeJPT = k1kGenomeYRI + 1;
	private static final int	kBGI200Danish = k1kGenomeJPT + 1;
	private static final int	kESP5400EUR = kBGI200Danish + 1;
	private static final int	kESP5400AFR = kESP5400EUR + 1;
	/** Array indexes of the HapMap Frequency sources */
	public static final int[]	kHapMapFrequencies = {kHapMapCEU, kHapMapYRI, kHapMapJPT};
	/** Array indexes of the Thousand Genome Frequency sources */
	public static final int[]	k1kGenomesFrequencies = {k1kGenomeCEU, k1kGenomeYRI, k1kGenomeJPT};
	/** Array indexes of the BGI 200 Frequency sources */
	public static final int[]	kBGI200Frequencies = {kBGI200Danish};
	/** Array indexes of the ESP 5400 Frequency sources */
	public static final int[]	kESP5400Frequencies = {kESP5400EUR, kESP5400AFR};
		
	private static final int	kSourceCol = 0;
	private static final int	kPopulationCol = kSourceCol + 1;
	private static final int	kRefBaseCol = kPopulationCol + 1;
	private static final int	kRefFreqCol = kRefBaseCol + 1;
	private static final int	kSNPBaseCol = kRefFreqCol + 1;
	private static final int	kSNPFreqCol = kSNPBaseCol + 1;
	private static final int	kNumCols = kSNPFreqCol + 1;
	
	private static final String	kMajorSplit = ",";
	private static final String	kMinorSplit = "/";
	private static final int	kReturnAll = SplitFile.kReturnAll;
	private static final int	kBases = 0;
	private static final int	kFrequencies = kBases + 1;
	private static final int	kRefCol = 0;
	private static final int	kSNPCol = kRefCol + 1;
	
	
	/**
	 * Report if a list of AlleleFrequency has at least one frequency from the provided list of 
	 * sources
	 * 
	 * @param freqs		List of AlleleFrequency.  If null or empty, will return false
	 * @param sources	Indexes into the kFrequencySources array of names, the targets of the search.
	 * If null or empty, will return true. If has an invalid index, will throw an exception
	 * @return	True if found a match, else false
	 */
	public static final boolean hasSource (List<AlleleFreq> freqs, int[] sources)
	{
		if (freqs == null)
			return false;
		
		if ((sources == null) || (sources.length == 0))
			return true;	// Nothing to test against, it's guaranteed to have at least nothing :-)
		
		for (AlleleFreq aFrequency : freqs)
		{
			String	name = aFrequency.frequencySource;
			for (int source : sources)
			{
				if (name.equals (kFrequencySources[source]))
					return true;
			}
		}
		
		return false;
	}
	
	
//	/**
//	 * Update the class's list of frequency sources with the sources from the database
//	 * 
//	 * @param sources	Array of source names.  The sources must be in DB ID order, with the first 
//	 * one (i.e. index 0) the source that's DB ID 1, the second DB ID 2, etc.  If there's a gap in 
//	 * the DB ID list, caller can put null strings in those positions.  Never put empty strings in
//	 * the array
//	 */
//	public static final void setFrequencySources (String[] sources)
//	{
//		if (sources == null)
//			return;
//		
//		gFrequencySources = sources;
//	}
	
	
	/**
	 * Constructor for IsSerializable
	 */
	protected AlleleFreq ()
	{
		super ();
	}
	
	
	/**
	 * @param minorBase
	 * @param majorBase
	 * @param minorFreq
	 * @param majorFreq
	 * @param frequencySource
	 */
	public AlleleFreq (char minorBase, char majorBase, double minorFreq, double majorFreq,
							String frequencySource)
	{
//		if (gFrequencySources == null)
//			gFrequencySources = kFrequencySources;
		
		this.minorBase = minorBase;
		this.majorBase = majorBase;
		this.minorFreq = minorFreq;
		this.majorFreq = majorFreq;
		this.frequencySource = frequencySource;
	}
	
	
	/**
	 * @param frequencyInfo		String from the data file with all the information
	 * @param frequencySource	Name of the source of the frequency info
	 */
	public AlleleFreq (String frequencyInfo, String frequencySource)
	{
//		if (gFrequencySources == null)
//			gFrequencySources = kFrequencySources;
		
		String[]	categories = SplitFile.mySplit (frequencyInfo, kMajorSplit, kReturnAll);
		String[]	bases = SplitFile.mySplit (categories[kBases], kMinorSplit, kReturnAll);
		String[]	freqs = SplitFile.mySplit (categories[kFrequencies], kMinorSplit, kReturnAll);
		
		minorBase = bases[kRefCol].charAt (0);
		majorBase = bases[kSNPCol].charAt (0);
		
		minorFreq = Double.parseDouble (freqs[kRefCol]);
		majorFreq = Double.parseDouble (freqs[kSNPCol]);
		
		this.frequencySource = frequencySource;
	}

	
	/**
	 * @param frequencyInfo	String from the data file with all the information
	 * @param whichSource	Index of the source in the gFrequencySources array
	 */
	public AlleleFreq (String frequencyInfo, int whichSource)
	{
//		if (gFrequencySources == null)
//			gFrequencySources = kFrequencySources;
		
		String[]	categories = SplitFile.mySplit (frequencyInfo, kMajorSplit, kReturnAll);
		String[]	bases = SplitFile.mySplit (categories[kBases], kMinorSplit, kReturnAll);
		String[]	freqs = SplitFile.mySplit (categories[kFrequencies], kMinorSplit, kReturnAll);
		
		minorBase = bases[kRefCol].charAt (0);
		majorBase = bases[kSNPCol].charAt (0);
		
		minorFreq = Double.parseDouble (freqs[kRefCol]);
		majorFreq = Double.parseDouble (freqs[kSNPCol]);
		
//		frequencySource = gFrequencySources[whichSource];
		frequencySource = kFrequencySources[whichSource];
	}


	/**
	 * Return the number of columns this object takes
	 * 
	 * @return	a positive integer
	 */
	public static final int numColumns ()
	{
		return kNumCols;
	}
	

	/**
	 * Return the number of columns this object takes
	 * 
	 * @return	a positive integer
	 */
	public static final int numColumnsNoSource ()
	{
		return kNumCols - (kPopulationCol + 1);	// Remove the source columns
	}
	
	
	/**
	 * @return the minorBase
	 */
	public final char getMinorBase ()
	{
		return minorBase;
	}
	
	
	/**
	 * @return the majorBase
	 */
	public final char getMajorBase ()
	{
		return majorBase;
	}
	
	
	/**
	 * @return the minorFreq
	 */
	public final double getMinorFreq ()
	{
		return minorFreq;
	}
	
	
	/**
	 * @return the majorFreq
	 */
	public final double getMajorFreq ()
	{
		return majorFreq;
	}
	
	
	/**
	 * @return the frequencySource
	 */
	public final String getFrequencySource ()
	{
		return frequencySource;
	}
	
	
	/**
	 * Report the 0 based position of the object's frequencySource
	 * 
	 * @param reference	If not null, then the order things need to go in
	 * @return	A number, -1 if there's a problem, 0 or more otherwise
	 */
	public final int getFrequencySourcePosition (String[] reference)
	{
		if (frequencySource != null)
		{
			if (reference == null)
				reference = kFrequencySources;
			
			for (int i = 0; i < reference.length; ++i)
			{
				String	source = reference[i];
				
				if ((source != null) && (frequencySource.equalsIgnoreCase (source)))
					return i;
			}
		}
		
		return -1;
	}
	
	
	/**
	 * GWT does not support Double.doubleToLongBits (double value);  So wrote this function to 
	 * provide a quick and dirty hash for doubles.  As our numbers are percentage numbers, multiplying
	 * them by 1000 then converting them to ints should give us decent discriminatory ability
	 * 
	 * @param value	The value to hash
	 * @return	(int) (value * 1000)
	 */
	private static final int getHash (double value)
	{
		return (int) (value * 1000);
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ( (frequencySource == null) ? 0 : frequencySource.hashCode ());
		result = prime * result + majorBase;
		long temp;
		temp = getHash (majorFreq);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + minorBase;
		temp = getHash (minorFreq);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals (Object obj)
	{
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof AlleleFreq))
			return false;
		
		AlleleFreq other = (AlleleFreq) obj;
		if (frequencySource == null)
		{
			if (other.frequencySource != null)
				return false;
		}
		else if (!frequencySource.equals (other.frequencySource))
			return false;
		
		if (majorBase != other.majorBase)
			return false;
		
		if (minorBase != other.minorBase)
			return false;
		
		if (getHash (majorFreq) != getHash (other.majorFreq))
			return false;
		
		if (getHash (minorFreq) != getHash (other.minorFreq))
			return false;
		
		return true;
	}
	
	
	/**
	 * Convert the AlleleFrequency back into the String it came from
	 * 
	 * @return	String in the format we read when importing an AlleleFrequency
	 */
	public String toExportString ()
	{
		StringBuilder	result = new StringBuilder ();
		
		result.append (minorBase);
		result.append (kMinorSplit);
		result.append (majorBase);
		
		result.append (kMajorSplit);
		
		result.append (minorFreq);
		result.append (kMinorSplit);
		result.append (majorFreq);
		
		return result.toString ();
	}
	
	
	/**
	 * Write out all the fields, not just the ones that should be exported
	 * 
	 * @return	A string holding the source name if there is one, the minor and major bases, and the 
	 * minor and major frequencies
	 */
	public String toString ()
	{
		StringBuilder	results = new StringBuilder ();
		
		if (frequencySource != null)
		{
			results.append (frequencySource);
			results.append ('\t');
		}
		
		results.append (minorBase);
		results.append (kMinorSplit);
		results.append (majorBase);
		
		results.append (kMajorSplit);
		
		results.append (minorFreq);
		results.append (kMinorSplit);
		results.append (majorFreq);
		
		return results.toString ();
	}
	
}
