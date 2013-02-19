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
public class TreatPipeline implements Usage
{
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
	private static final String	kRestOfHeader = "	Gene	FirstDBSNPSBuild	BGIMajorAllele	BGIMinorAllele	" +
												"BGIMajorFrequency	BGIMinorFrequency	ESP6500CEUMAF	ESP6500AFRMAF	" +
												"HapMapRefAllele	HapMapAltAllele	HapMapRefFrequency	HapMapAltFrequency";
	private static final String	kBlank = ".";
	private static final String	kEspEurMaf = "ESP6500_EUR_maf";
	private static final String	kEspAfrMaf = "ESP6500_AFR_maf";
	private static final String[]	kBaseStrs = {"REF", "ALT"};
	private static final int	kRefPos = 0;
	private static final int	kAltPos = kRefPos + 1;
	private static final String	kHapMapFreq	= "HapMap_CEU_allele_freq";	// HapMap_YRI_allele_freq, HapMap_JPT+CHB_allele_freq
	private static final String	kBGIFreq	= "BGI200_Danish";
	private static final double	kPercentAdjust = 100.0;
	private static final OptType	kRequired = OptType.kRequiredParam;
	private static final OptType	kOptional = OptType.kOptionalParam;
	private static final OptType	kOnly = OptType.kOnlyParam;
	private static final String	kAppName = "TreatPipeline";
	private static final int	kVersion = 2;
	private static final int	kVersionParam = 0;
	private static final int	kVCFParam = kVersionParam + 1;
	private static final int	kBaseDirParam = kVCFParam + 1;
	private static final boolean	kConvertFromPercent = true;
	private static final boolean	kDoNotConvert = false;
	
	
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
	
	
	@SuppressWarnings ({"javadoc", "rawtypes", "unchecked"})
	public static void main (String[] args) throws IOException
	{
		String[]	parameters = getParameters (args);
		if (parameters == null)
			return;
		
		String	vcf = parameters[kVCFParam];
		String	baseDir = parameters[kBaseDirParam];
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
		
		String		genesFile = theProperties.getProperty ("genesFile");
		String		bgiFile = theProperties.getProperty ("bgiFile");
		String		espFile = theProperties.getProperty ("espFile");
		String		hapMapFile = theProperties.getProperty ("hapMapFile");
		String		dbsnpFile = theProperties.getProperty ("dbsnpFile");
		String[]	geneDrill = {"gene"}; // , "note"
		String[]	dbSnpDrill = {"INFO.dbSNPBuildID", "_id"};
		String[]	bgiDrill = {"major_allele", "minor_allele", "calculated_major_allele_freq", "calculated_minor_allele_freq"};
		String[]	espDrill = {"INFO.EA_AC", "INFO.AA_AC", "INFO.TAC", "INFO.MAF", "_refAllele", "_altAlleles"};
		String[]	hapMapDrill = {"refallele", "otherallele", "CEU.refallele_freq", "CEU.otherallele_freq"};
//		int[]		cut = {9};
		int			posCol = -1;
//		TransformFunctionPipe<History, History>	tPipe = new TransformFunctionPipe<History, History> (new TreatPipe ());
		Pipeline	p = new Pipeline (new CatPipe (), new HistoryInPipe (), new VCF2VariantPipe (), 
									  new OverlapPipe (baseDir + genesFile), new DrillPipe (false, geneDrill), 
									  new SameVariantPipe (baseDir + dbsnpFile, posCol -= geneDrill.length), 
									  new DrillPipe (false, dbSnpDrill), 
									  new SameVariantPipe (baseDir + bgiFile, posCol -= dbSnpDrill.length), 
									  new DrillPipe (false, bgiDrill), 
									  new SameVariantPipe (baseDir + espFile, posCol -= bgiDrill.length), 
									  new DrillPipe (false, espDrill), 
									  new SameVariantPipe (baseDir + hapMapFile, posCol -= espDrill.length), 
									  new DrillPipe (false, hapMapDrill));
//									  new HCutPipe (cut), tPipe, new HistoryOutPipe (), new PrintPipe ());
		p.setStarts (Arrays.asList (vcf));
//		handleAll (p, posCol, hapMapDrill.length);
		writeRows (p, posCol, hapMapDrill.length);
	}
	
	
	/**
	 * Get all the allele source results that BioR has for the variants specified in the passed in VCF File
	 * 
	 * @param vcfFile	Path to the VCF file.  May not be null, empty, or point to a non-existent file
	 * @param baseDir	Path to the BioR Catalog files.  If null, will use the default: /data4/bsi/refdata-new/catalogs/v1/BioR/
	 * @return	List holding any AlleleFreq results found, or null if got a bad file
	 */
	@SuppressWarnings ({"rawtypes", "unchecked"})
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
		
		if (baseDir == null)
			baseDir = theProperties.getProperty ("fileBase");
		if (baseDir == null)
			baseDir = "";
		String		genesFile = theProperties.getProperty ("genesFile");
		String		bgiFile = theProperties.getProperty ("bgiFile");
		String		espFile = theProperties.getProperty ("espFile");
		String		hapMapFile = theProperties.getProperty ("hapMapFile");
		String		dbsnpFile = theProperties.getProperty ("dbsnpFile");
		String[]	geneDrill = {"gene"}; // , "note"
		String[]	dbSnpDrill = {"INFO.dbSNPBuildID", "_id"};
		String[]	bgiDrill = {"major_allele", "minor_allele", "calculated_major_allele_freq", "calculated_minor_allele_freq"};
		String[]	espDrill = {"INFO.EA_AC", "INFO.AA_AC", "INFO.TAC", "INFO.MAF", "_refAllele", "_altAlleles"};
		String[]	hapMapDrill = {"refallele", "otherallele", "CEU.refallele_freq", "CEU.otherallele_freq"};
		int			posCol = -1;
		
		try
		{
			Pipeline	p = new Pipeline (new CatPipe (), new HistoryInPipe (), new VCF2VariantPipe (), 
										  new OverlapPipe (baseDir + genesFile), new DrillPipe (false, geneDrill), 
										  new SameVariantPipe (baseDir + dbsnpFile, posCol -= geneDrill.length), 
										  new DrillPipe (false, dbSnpDrill), 
										  new SameVariantPipe (baseDir + bgiFile, posCol -= dbSnpDrill.length), 
										  new DrillPipe (false, bgiDrill), 
										  new SameVariantPipe (baseDir + espFile, posCol -= bgiDrill.length), 
										  new DrillPipe (false, espDrill), 
										  new SameVariantPipe (baseDir + hapMapFile, posCol -= espDrill.length), 
										  new DrillPipe (false, hapMapDrill));
			p.setStarts (Arrays.asList (vcfFile));
			
			List<AlleleFreq>	freqs = parseRows (p, posCol, hapMapDrill.length);
			
			return freqs;
		}
		catch (IOException oops)
		{
			oops.printStackTrace ();
			return null;
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
	protected static List<AlleleFreq> parseRows (Pipeline pipeline, int posCol, int lastColCount)
	{
		List<AlleleFreq>	freqs = new ArrayList<AlleleFreq> ();
		boolean	header = true;
		int		numCols = -1;
		int		firstCol = -1;
		int		refPos = -1;
		int		altPos = -1;
		
		while (pipeline.hasNext ())
		{
			History	history = (History) pipeline.next ();
			if (header)
			{
				header = false;
				numCols = history.size ();
				firstCol = (numCols - ((-1 - posCol) + lastColCount)) + 1;	// Skip the history column
				
				int[]	poses = getPositions (kBaseStrs);
				
				refPos = poses[kRefPos];
				altPos = poses[kAltPos];
			}
			
			int		startCol = firstCol;
			String	ref = history.get (refPos);
			String	alt = history.get (altPos);
//			String	geneName = history.get (startCol + kGeneName);
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
			double	hapRefFreq = parseDouble (history.get (startCol + kHapMapCeuRefFreq), kConvertFromPercent);
			double	hapAltFreq = parseDouble (history.get (startCol + kHapMapCeuAltFreq), kConvertFromPercent);
			
			List<AlleleFreq>	results;
			
			results = getBGIMAFs (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			if (results != null)
				freqs.addAll (results);
			results = getESPMAFs (espMafs, ref, alt);
			if (results != null)
				freqs.addAll (results);
			results = getHapMapMAFs (hapRefAllele, hapAltAllele, hapRefFreq, hapAltFreq);
			if (results != null)
				freqs.addAll (results);
		}
		
		return freqs;
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
			double	hapRefFreq = parseDouble (history.get (startCol + kHapMapCeuRefFreq), kConvertFromPercent);
			double	hapAltFreq = parseDouble (history.get (startCol + kHapMapCeuAltFreq), kConvertFromPercent);
			
			printString (geneName);
			printInt (dbSNPBuild);
			printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			printESPMAFs (espMafs);
			printHapMap (hapRefAllele, hapAltAllele, hapRefFreq, hapAltFreq);
			System.out.println ();
		}
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
			startCol += kNCBICols;
			int		dbSNPBuild = parseInt (cols[startCol + kdbSNPBuild]);
			startCol += kdbSNPCols;
			String	bgiMajAllele = cols[startCol + kBGIMajorAllele];
			String	bgiMinAllele = cols[startCol + kBGIMinorAllele];
			double	bgiMajFreq = parseDouble (cols[startCol + kBGIMajorFreq], kConvertFromPercent);
			double	bgiMinFreq = parseDouble (cols[startCol + kBGIMinorFreq], kConvertFromPercent);
			startCol += kBGICols;
			String	espMafs = cols[startCol + kESPMAF];
			startCol += kESPCols;
			String	hapRefAllele = cols[startCol + kHapMapRefAllele];
			String	hapAltAllele = cols[startCol + kHapMapAltAllele];
			double	hapRefFreq = parseDouble (cols[startCol + kHapMapCeuRefFreq], kConvertFromPercent);
			double	hapAltFreq = parseDouble (cols[startCol + kHapMapCeuAltFreq], kConvertFromPercent);
			
			printString (geneName);
			printInt (dbSNPBuild);
			printBGI (bgiMajAllele, bgiMinAllele, bgiMajFreq, bgiMinFreq);
			printESPMAFs (espMafs);
			printHapMap (hapRefAllele, hapAltAllele, hapRefFreq, hapAltFreq);
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
			if (Double.isNaN (bgiMajFreq))
				System.out.print (0.0);
			else
				System.out.print (bgiMajFreq);
			System.out.print ('\t');
			if (Double.isNaN (bgiMinFreq))
				System.out.print (0.0);
			else
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
	private static void printHapMap (String hapRefAllele, String hapAltAllele, double hapRefFreq, double hapAltFreq)
	{
		if (!hapRefAllele.equals (kBlank) && !hapAltAllele.equals (kBlank))
		{
			System.out.print ('\t');
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
		}
		else
			System.out.print ("\t\t\t\t");
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
	 * @param bgiMajAllele	Major / Ref Allele
	 * @param bgiMinAllele	Minor / Alt Allele
	 * @param bgiMajFreq	Major / Ref Frequency
	 * @param bgiMinFreq	Minor / Alt Frequency
	 * @return	List of one or two AlleleFreq objects, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getBGIMAFs (String bgiMajAllele, String bgiMinAllele, double bgiMajFreq, double bgiMinFreq)
	{
		if (bgiMajAllele.equals (kBlank) || bgiMinAllele.equals (kBlank))
			return null;
		
		boolean	hasRef = !((bgiMajFreq == 0.0) || (Double.isNaN (bgiMajFreq)));
		boolean	hasAlt = !((bgiMinFreq == 0.0) || (Double.isNaN (bgiMinFreq)));
		if (!hasRef || !hasAlt)
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = bgiMajAllele.charAt (0);
		char	minorBase = bgiMinAllele.charAt (0);
		
		results.add (new AlleleFreq (minorBase, majorBase, bgiMinFreq, bgiMajFreq, kBGIFreq));
		
		return results;
	}
	
	
	/**
	 * Return a list holding HapMap AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param hapRefAllele	Major / Ref Allele
	 * @param hapAltAllele	Minor / Alt Allele
	 * @param hapRefFreq	Major / Ref Frequency
	 * @param hapAltFreq	Minor / Alt Frequency
	 * @return	List of one or two AlleleFreq objects, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getHapMapMAFs (String hapRefAllele, String hapAltAllele, double hapRefFreq, double hapAltFreq)
	{
		if (hapRefAllele.equals (kBlank) || hapAltAllele.equals (kBlank))
			return null;
		
		boolean	hasRef = !((hapRefFreq == 0.0) || (Double.isNaN (hapRefFreq)));
		boolean	hasAlt = !((hapAltFreq == 0.0) || (Double.isNaN (hapAltFreq)));
		if (!hasRef || !hasAlt)
			return null;
		
		List<AlleleFreq>	results = new ArrayList<AlleleFreq> ();
		char	majorBase = hapRefAllele.charAt (0);
		char	minorBase = hapAltAllele.charAt (0);
		
		results.add (new AlleleFreq (minorBase, majorBase, hapAltFreq, hapRefFreq, kHapMapFreq));
		
		return results;
	}
	
	
	/**
	 * Return a list holding CEU and AFR ESP AlleleFreq objects if they're there, or null if they aren't
	 * 
	 * @param espMafs	String holding ESP Minor allele frequencies in the format ["CEU", "AFR", "Total"]
	 * @param ref		Reference base, will use first char, so can not be null or empty
	 * @param alt		Alternate base, will use first char, so can not be null or empty
	 * @return	List of one or two AlleleFreq objects, or null if there aren't any specified
	 */
	private static List<AlleleFreq> getESPMAFs (String espMafs, String ref, String alt)
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
		char	minorBase = alt.charAt (0);
		
		if (hasCEU)
			results.add (new AlleleFreq (minorBase, majorBase, ceuMaf, 1.0 - ceuMaf, kEspEurMaf));
		
		if (hasAFR)
			results.add (new AlleleFreq (minorBase, majorBase, afrMaf, 1.0 - afrMaf, kEspAfrMaf));
		
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
		GetOpts.addOption ('v', "--vcfFile", ArgType.kRequiredArgument, kRequired);
		GetOpts.addOption ('b', "--baseDir", ArgType.kRequiredArgument, kOptional);
	 */
	private static String Usage ()
	{
		StringBuilder	usage = new StringBuilder ();
		
		usage.append ("Usage: java -jar " + kAppName + ".jar <Options>\n");
		
		usage.append ("\t-v | --vcfFile: <path to vcf file>: Path of the vcf file to parse.  Required\n");
		usage.append ("\t-b | --baseDir: <Path to BioR Catalogs>: Path to the BioR Catalog bse directory.\n\t\t");
		usage.append ("Optional, the default is '/data4/bsi/refdata-new/catalogs/v1/BioR/'\n");
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
