/**
 * Exome Workflow
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2010
 *
 */
package edu.mayo.bior.pipeline.Treat;


/**
 * <p>@author Gregory Dougherty</p>
 *
 */
public class AlleleFreq 
{
	private String	chromosome;
	private int		startPos;
	private char	minorBase;
	private char	majorBase;
	private double	minorFreq;
	private double	majorFreq;
	private String	frequencySource;
	
	
	private static final String	kMajorSplit = ",";
	private static final String	kMinorSplit = "/";
	
	
	/**
	 * Constructor for AlleleFreq
	 * 
	 * @param chromosome	Chromosome of the variant this frequency is for
	 * @param startPos		Where in the chromosome the variant starts
	 * @param minorBase		The Alt base for the variant
	 * @param majorBase		The Ref base for the variant
	 * @param minorFreq		The frequency (range 0.0 - 1.0) with which the Alt base is seen in this population by this frequency source
	 * @param majorFreq		The frequency (range 0.0 - 1.0) with which the Ref base is seen in this population by this frequency source
	 * @param frequencySource	The population and frequency compiler
	 */
	public AlleleFreq (String chromosome, int startPos, char minorBase, char majorBase, 
						double minorFreq, double majorFreq, String frequencySource)
	{
		this.chromosome = chromosome;
		this.startPos = startPos;
		this.minorBase = minorBase;
		this.majorBase = majorBase;
		this.minorFreq = minorFreq;
		this.majorFreq = majorFreq;
		this.frequencySource = frequencySource;
	}
	
	
	/**
	 * @return the chromosome
	 */
	public final String getChromosome ()
	{
		return chromosome;
	}
	
	
	/**
	 * @return the startPos
	 */
	public final int getStartPos ()
	{
		return startPos;
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
		result = prime * result + ((frequencySource == null) ? 0 : frequencySource.hashCode ());
		result = prime * result + ((chromosome == null) ? 0 : chromosome.hashCode ());
		result = prime * result + startPos;
		result = prime * result + majorBase;
		result = prime * result + minorBase;
		long temp;
		temp = getHash (majorFreq);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (startPos != other.startPos)
			return false;
		
		if (majorBase != other.majorBase)
			return false;
		
		if (minorBase != other.minorBase)
			return false;
		
		if (frequencySource == null)
		{
			if (other.frequencySource != null)
				return false;
		}
		else if (!frequencySource.equals (other.frequencySource))
			return false;
		
		if (chromosome == null)
		{
			if (other.chromosome != null)
				return false;
		}
		else if (!chromosome.equals (other.chromosome))
			return false;
		
		if (getHash (majorFreq) != getHash (other.majorFreq))
			return false;
		
		if (getHash (minorFreq) != getHash (other.minorFreq))
			return false;
		
		return true;
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
