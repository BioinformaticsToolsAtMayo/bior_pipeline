/**
 * bior_pipeline
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2011
 *
 */
package edu.mayo.bior.pipeline.Treat;

import java.util.List;
import com.tinkerpop.pipes.PipeFunction;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryMetaData;

/**
 * <p>@author Gregory Dougherty</p>
 *
 */
public class TreatPipe implements PipeFunction<History,History>
{
	private Cleaner	theCleaner;
	
	private static final int	kGeneName = 0;
	private static final int	kNCBICols = kGeneName + 1;
	private static final int	kdbSNPBuild = 0;
	private static final int	kdbSNPID = kdbSNPBuild + 1;
	private static final int	kdbSNPCols = kdbSNPID + 1;
	private static final int	kBGIMajorAllele = 0;
	private static final int	kBGIMinorAllele = kBGIMajorAllele + 1;
	private static final int	kBGIMajorFreq = kBGIMinorAllele + 1;
	private static final int	kBGIMinorFreq = kBGIMajorFreq + 1;
	private static final int	kBGICols = kBGIMinorFreq + 1;
	private static final int	kESPCeuCounts = 0;
	private static final int	kESPAfrCounts = kESPCeuCounts + 1;
	private static final int	kESPTotalCounts = kESPAfrCounts + 1;
	private static final int	kESPMAF = kESPTotalCounts + 1;
	private static final int	kESPRefAllele = kESPMAF + 1;
	private static final int	kESPAltAllele = kESPRefAllele + 1;
	private static final int	kESPCols = kESPAltAllele + 1;
	private static final int	kHapMapRefAllele = 0;
	private static final int	kHapMapAltAllele = kHapMapRefAllele + 1;
	private static final int	kHapMapCeuRefFreq = kHapMapAltAllele + 1;
	private static final int	kHapMapCeuAltFreq = kHapMapCeuRefFreq + 1;
	protected static final int	kHapMapCols = kHapMapCeuAltFreq + 1;
	private static final String	kBlank = ".";
	private static boolean	header = true;	
	
	
	/**
	 * The Constructor.  Gets the cleaner so can call it later
	 * 
	 * @param theCleaner	Object that will clean the data
	 */
	public TreatPipe (Cleaner theCleaner)
	{
		super ();
		this.theCleaner = theCleaner;
	}
	
	
	/* (non-Javadoc)
	 * @see com.tinkerpop.pipes.PipeFunction#compute(java.lang.Object)
	 */
	public History compute (History history)
	{
		if (theCleaner != null)
			return theCleaner.doClean (history);
		
		int	numCols = history.size ();
		int	firstCol = numCols - (kdbSNPCols + kBGICols + kESPCols + kHapMapCols);
		
		if (header)
		{
			HistoryMetaData			metaData = History.getMetaData ();
			List<ColumnMetaData>	columns = metaData.getColumns ();
			
			for (int i = numCols - 1; i >= firstCol; --i)
				columns.remove (i);
			
			header = false;
		}
		
		int	startCol = firstCol;
		for (int i = 0; i < startCol; ++i)
		{
			System.out.print (history.get (i));
			System.out.print ('\t');
		}
		
		String	geneName = history.get (startCol + kGeneName);
		startCol += kNCBICols;
		int		dbSNPBuild = parseInt (history.get (startCol + kdbSNPBuild));
		startCol += kdbSNPCols;
		String	bgiMajAllele = history.get (startCol + kBGIMajorAllele);
		String	bgiMinAllele = history.get (startCol + kBGIMinorAllele);
		double	bgiMajFreq = parseDouble (history.get (startCol + kBGIMajorFreq));
		double	bgiMinFreq = parseDouble (history.get (startCol + kBGIMinorFreq));
		startCol += kBGICols;
		String	espMafs = history.get (startCol + kESPMAF);
		startCol += kESPCols;
		String	hapRefAllele = history.get (startCol + kHapMapRefAllele);
		String	hapAltAllele = history.get (startCol + kHapMapAltAllele);
		double	hapRefFreq = parseDouble (history.get (startCol + kHapMapCeuRefFreq));
		double	hapAltFreq = parseDouble (history.get (startCol + kHapMapCeuAltFreq));
		
		for (int i = numCols - 1; i >= firstCol; --i)
			history.remove (i);
		
		printString (geneName, history);
		printInt (dbSNPBuild, history);
		printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq, history);
		printESPMAFs (espMafs, history);
		printHapMap (hapRefAllele, hapAltAllele, hapRefFreq, hapAltFreq, history);
		return history;
	}
	
	
	/**
	 * Print out a String if it isn't blank, and then print out a tab, regardless of whether or not 
	 * the String was blank
	 * 
	 * @param theStr	String to print out, if not blank
	 */
	private static void printString (String theStr, History history)
	{
		if (!theStr.equals (kBlank))
			System.out.print (theStr);
		System.out.print ('\t');
	}
	
	
	/**
	 * Print out an integer if it isn't 0, and then print out a tab, regardless of whether or not 
	 * the integer was 0
	 * 
	 * @param theInt	Integer to print out, if not 0
	 */
	private static void printInt (int theInt, History history)
	{
		if (theInt > 0)
			System.out.print (theInt);
		System.out.print ('\t');
	}
	
	
	/**
	 * Print out the BGI info, or tabs if nothing to print
	 * 
	 * @param bgiMajAllele	Major / Ref Allele
	 * @param bgiMinAllele	Minor / Alt Allele
	 * @param bgiMajFreq	Major / Ref Frequency
	 * @param bgiMinFreq	Minor / Alt Frequency
	 */
	private static void printBGI (String bgiMajAllele, String bgiMinAllele, double bgiMajFreq, double bgiMinFreq, History history)
	{
		if (!bgiMajAllele.equals (kBlank) && !bgiMinAllele.equals (kBlank))
		{
			System.out.print (bgiMajAllele);
			System.out.print ('\t');
			System.out.print (bgiMinAllele);
			System.out.print ('\t');
			if (Double.isNaN (bgiMajFreq))
				System.out.print (0.0);
			else
				System.out.print (bgiMajFreq);
			System.out.print ('\t');
			if (Double.isNaN (bgiMinFreq))
				System.out.print (0.0);
			else
				System.out.print (bgiMinFreq);
			System.out.print ('\t');
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
	private static void printHapMap (String hapRefAllele, String hapAltAllele, double hapRefFreq, double hapAltFreq, History history)
	{
		if (!hapRefAllele.equals (kBlank) && !hapAltAllele.equals (kBlank))
		{
			System.out.print (hapRefAllele);
			System.out.print ('\t');
			System.out.print (hapAltAllele);
			System.out.print ('\t');
			if (Double.isNaN (hapRefFreq))
				System.out.print (0.0);
			else
				System.out.print (hapRefFreq);
			System.out.print ('\t');
			if (Double.isNaN (hapAltFreq))
				System.out.print (0.0);
			else
				System.out.print (hapAltFreq);
			System.out.print ('\t');
		}
		else
			System.out.print ("\t\t\t\t");
	}
	
	
	/**
	 * Print out the CEU and AFR ESP MAFs if they're there, or just tabs if they aren't
	 * 
	 * @param espMafs
	 */
	private static void printESPMAFs (String espMafs, History history)
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
		
		System.out.print (espMafs.substring (0, pos));
		System.out.print ('\t');
		
		pos = espMafs.indexOf ('"', pos + 1) + 1;
		if (pos > 0)
		{
			int	end = espMafs.indexOf ('"', pos + 1);
			if (end > 0)
				System.out.print (espMafs.substring (pos, end));
		}
		System.out.print ('\t');
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
	private static double parseDouble (String theDouble)
	{
		double	result = Double.NaN;
		if (!theDouble.equals (kBlank))
		{
			try
			{
				result = Double.parseDouble (theDouble);
			}
			catch (NumberFormatException oops)
			{
				// Do nothing
			}
		}
		
		return result;
	}
	
}
