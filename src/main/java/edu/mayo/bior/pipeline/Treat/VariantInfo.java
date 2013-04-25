/**
 * bior_pipeline
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2011
 *
 */
package edu.mayo.bior.pipeline.Treat;

/**
 * Class to get non-Allele Frequency variant information from the TreatPipeline to the Table Browser
 * <p>@author Gregory Dougherty</p>
 *
 */
public class VariantInfo
{
	private String	chromosome;
	private String	ref;
	private String	alt;
	private int		startPos;
	private int		endPos;
	private int		entrezGeneID;
	private int		firstBuild;
	private String	dbSNPsID;
	private String	suspectRegion;
	private String	clinicalSig;
	private String	alleleOrigin;
	private boolean	diseaseVariant;
	private String	geneSymbol;
	private String	ensemblGeneID;
	private int		mutationID;
	private String	cosmicCDS;
	private String	cosmicAA;
	private boolean	strand;
	private String	omimDisease;
	private boolean	blacklisted;
	private boolean	conserved;
	private boolean	enhancer;
	private boolean	tfbs;
	private boolean	tss;
	private boolean	unique;
	private boolean	repeat;
	private boolean	regulatory;
	private String	landmark;
	private String	type;
	private int		minBP;
	private int		maxBP;
	private boolean	miRStrand;
	private String	acc;
	private String	miRBId;
	
	
	/**
	 * Constructor for VariantInfo
	 * 
	 * @param chromosome	Chromosome of the variant this frequency is for
	 * @param startPos		Where in the chromosome the variant starts
	 * @param endPos		Where in the chromosome the variant ends, if it is an indel.  -1 if it is an SNV
	 * @param ref			The reference base(s)
	 * @param alt			The alternate base(s)
	 * @param entrezGeneID	The EntrezGeneID, or -1 if none
	 * @param firstBuild	First time it appeared in dbSNPs, -1 if not in
	 * @param dbSNPsID		dbSNPs ID if it has one, or null if it doesn't
	 * @param suspectRegion	String giving one of 6 possible regions
	 * @param clinicalSig	String giving one of 9 possible clinical significance values
	 * @param alleleOrigin	String giving one of 4 (to be 7) possible allelic origin values
	 * @param diseaseVariant	True if variant is know to cause a disease, else false
	 * @param geneSymbol	HGNC Approved Gene Symbol of the gene the Variant is in, null if not in one
	 * @param ensemblGeneID	EnsemblGeneID if it has one, or null if it doesn't
	 * @param mutationID	Cosmic mutation ID, or -1
	 * @param cosmicCDS		Cosmic: nucleotide sequence change
	 * @param cosmicAA		Cosmic: Peptide sequence change
	 * @param strand		Cosmic: NCBI provided + (true) or - (false) strand
	 * @param omimDisease	OMIM disease if it has one, or null if it doesn't
	 * @param blacklisted	True if blacklist score is above the cutoff, false otherwise
	 * @param conserved		True if conserved score is above the cutoff, false otherwise
	 * @param enhancer		True if enhancer score is above the cutoff, false otherwise
	 * @param tfbs			True if tfbs score is above the cutoff, false otherwise
	 * @param tss			True if tss score is above the cutoff, false otherwise
	 * @param unique		True if unique score is above the cutoff, false otherwise
	 * @param repeat		True if repeat score is above the cutoff, false otherwise
	 * @param regulatory	True if regulatory score is above the cutoff, false otherwise
	 * @param landmark		miRBase Landmark, a Chromosome specifier
	 * @param type			miRBase type, generally "miRNA"
	 * @param minBP			Location on the Chromosome where the miRBase item starts
	 * @param maxBP			Location on the Chromosome where the miRBase item ends
	 * @param miRStrand		Which strand of the Chromosome where the miRBase item ends
	 * @param acc			The accession for the entry in miRBase
	 * @param id			The identifier for the entry in miRBase
	 */
	public VariantInfo (String chromosome, int startPos, int endPos, String ref, String alt, int entrezGeneID, 
						int firstBuild, String dbSNPsID, String suspectRegion, String clinicalSig, 
						String alleleOrigin, boolean diseaseVariant, String geneSymbol, String ensemblGeneID, 
						int mutationID, String cosmicCDS, String cosmicAA, boolean strand, String omimDisease, 
						boolean blacklisted, boolean conserved, boolean enhancer, boolean tfbs, 
						boolean tss, boolean unique, boolean repeat, boolean regulatory, String landmark, 
						String type, int minBP, int maxBP, boolean miRStrand, String acc, String id)
	{
		this.chromosome = chromosome;
		this.startPos = startPos;
		this.endPos = endPos;
		this.ref = ref;
		this.alt = alt;
		this.entrezGeneID = entrezGeneID;
		this.firstBuild = firstBuild;
		this.geneSymbol = geneSymbol;
		this.dbSNPsID = dbSNPsID;
		this.ensemblGeneID = ensemblGeneID;
		this.mutationID = mutationID;
		this.cosmicCDS = cosmicCDS;
		this.cosmicAA = cosmicAA;
		this.strand = strand;
		this.omimDisease = omimDisease;
		this.blacklisted = blacklisted;
		this.conserved = conserved;
		this.enhancer = enhancer;
		this.tfbs = tfbs;
		this.tss = tss;
		this.unique = unique;
		this.repeat = repeat;
		this.regulatory = regulatory;
		this.landmark = landmark;
		this.type = type;
		this.minBP = minBP;
		this.maxBP = maxBP;
		this.miRStrand = miRStrand;
		this.acc = acc;
		this.miRBId = id;
	}
	
	
	/**
	 * @return the chromosome
	 */
	public final String getChromosome ()
	{
		return chromosome;
	}
	
	
	/**
	 * @return the ref
	 */
	public final String getRef ()
	{
		return ref;
	}
	
	
	/**
	 * @return the alt
	 */
	public final String getAlt ()
	{
		return alt;
	}
	
	
	/**
	 * @return the startPos
	 */
	public final int getStartPos ()
	{
		return startPos;
	}
	
	
	/**
	 * @return the endPos
	 */
	public final int getEndPos ()
	{
		return endPos;
	}
	
	
	/**
	 * @return the entrezGeneID
	 */
	public final int getEntrezGeneID ()
	{
		return entrezGeneID;
	}
	
	
	/**
	 * @return the firstBuild
	 */
	public final int getFirstBuild ()
	{
		return firstBuild;
	}
	
	
	/**
	 * @return the dbSNPsID
	 */
	public final String getDbSNPsID ()
	{
		return dbSNPsID;
	}
	
	
	/**
	 * @return the suspectRegion
	 */
	public final String getSuspectRegion ()
	{
		return suspectRegion;
	}
	
	
	/**
	 * @return the clinicalSig
	 */
	public final String getClinicalSig ()
	{
		return clinicalSig;
	}
	
	
	/**
	 * @return the alleleOrigin
	 */
	public final String getAlleleOrigin ()
	{
		return alleleOrigin;
	}
	
	
	/**
	 * @return the diseaseVariant
	 */
	public final boolean isDiseaseVariant ()
	{
		return diseaseVariant;
	}
	
	
	/**
	 * @return the geneSymbol
	 */
	public final String getGeneSymbol ()
	{
		return geneSymbol;
	}
	
	
	/**
	 * @return the ensemblGeneID
	 */
	public final String getEnsemblGeneID ()
	{
		return ensemblGeneID;
	}
	
	
	/**
	 * @return the mutationID
	 */
	public final int getMutationID ()
	{
		return mutationID;
	}
	
	
	/**
	 * @return the cosmicCDS
	 */
	public final String getCosmicCDS ()
	{
		return cosmicCDS;
	}
	
	
	/**
	 * @return the cosmicAA
	 */
	public final String getCosmicAA ()
	{
		return cosmicAA;
	}
	
	
	/**
	 * @return the strand
	 */
	public final boolean isStrand ()
	{
		return strand;
	}
	
	
	/**
	 * @return the omimDisease
	 */
	public final String getOMIMDisease ()
	{
		return omimDisease;
	}
	
	
	/**
	 * @return the blacklisted
	 */
	public final boolean isBlacklisted ()
	{
		return blacklisted;
	}
	
	
	/**
	 * @return the conserved
	 */
	public final boolean isConserved ()
	{
		return conserved;
	}
	
	
	/**
	 * @return the enhancer
	 */
	public final boolean isEnhancer ()
	{
		return enhancer;
	}
	
	
	/**
	 * @return the tfbs
	 */
	public final boolean isTfbs ()
	{
		return tfbs;
	}
	
	
	/**
	 * @return the tss
	 */
	public final boolean isTss ()
	{
		return tss;
	}
	
	
	/**
	 * @return the unique
	 */
	public final boolean isUnique ()
	{
		return unique;
	}
	
	
	/**
	 * @return the repeat
	 */
	public final boolean isRepeat ()
	{
		return repeat;
	}
	
	
	/**
	 * @return the regulatory
	 */
	public final boolean isRegulatory ()
	{
		return regulatory;
	}
	
	
	/**
	 * @return the omimDisease
	 */
	public final String getOmimDisease ()
	{
		return omimDisease;
	}
	
	
	/**
	 * @return the landmark
	 */
	public final String getLandmark ()
	{
		return landmark;
	}
	
	
	/**
	 * @return the type
	 */
	public final String getType ()
	{
		return type;
	}
	
	
	/**
	 * @return the minBP
	 */
	public final int getMinBP ()
	{
		return minBP;
	}
	
	
	/**
	 * @return the maxBP
	 */
	public final int getMaxBP ()
	{
		return maxBP;
	}
	
	
	/**
	 * @return the miRStrand
	 */
	public final boolean isMiRStrand ()
	{
		return miRStrand;
	}
	
	
	/**
	 * @return the acc
	 */
	public final String getAcc ()
	{
		return acc;
	}
	
	
	/**
	 * @return the id
	 */
	public final String getId ()
	{
		return miRBId;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode ()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (blacklisted ? 1231 : 1237);
		result = prime * result + ((chromosome == null) ? 0 : chromosome.hashCode ());
		result = prime * result + ((ref == null) ? 0 : ref.hashCode ());
		result = prime * result + ((alt == null) ? 0 : alt.hashCode ());
		result = prime * result + (conserved ? 1231 : 1237);
		result = prime * result + ((dbSNPsID == null) ? 0 : dbSNPsID.hashCode ());
		result = prime * result + ((suspectRegion == null) ? 0 : suspectRegion.hashCode ());
		result = prime * result + ((clinicalSig == null) ? 0 : clinicalSig.hashCode ());
		result = prime * result + ((alleleOrigin == null) ? 0 : alleleOrigin.hashCode ());
		result = prime * result + (diseaseVariant ? 1231 : 1237);
		result = prime * result + endPos;
		result = prime * result + (enhancer ? 1231 : 1237);
		result = prime * result + ((ensemblGeneID == null) ? 0 : ensemblGeneID.hashCode ());
		result = prime * result + entrezGeneID;
		result = prime * result + firstBuild;
		result = prime * result + ((geneSymbol == null) ? 0 : geneSymbol.hashCode ());
		result = prime * result + ((omimDisease == null) ? 0 : omimDisease.hashCode ());
		result = prime * result + (regulatory ? 1231 : 1237);
		result = prime * result + (repeat ? 1231 : 1237);
		result = prime * result + startPos;
		result = prime * result + (tfbs ? 1231 : 1237);
		result = prime * result + (tss ? 1231 : 1237);
		result = prime * result + (unique ? 1231 : 1237);
		result = prime * result + mutationID;	// Ignore rest of Cosmic, they all key on mutationID
		result = prime * result + ((miRBId == null) ? 0 : miRBId.hashCode ());
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
		
		if (!(obj instanceof VariantInfo))
			return false;
		
		VariantInfo other = (VariantInfo) obj;
		if (blacklisted != other.blacklisted)
			return false;
		
		if (diseaseVariant != other.diseaseVariant)
			return false;
		
		if (conserved != other.conserved)
			return false;
		
		if (endPos != other.endPos)
			return false;
		
		if (enhancer != other.enhancer)
			return false;
		
		if (entrezGeneID != other.entrezGeneID)
			return false;
		
		if (firstBuild != other.firstBuild)
			return false;
		
		if (regulatory != other.regulatory)
			return false;
		
		if (repeat != other.repeat)
			return false;
		
		if (startPos != other.startPos)
			return false;
		
		if (tfbs != other.tfbs)
			return false;
		
		if (tss != other.tss)
			return false;
		
		if (unique != other.unique)
			return false;
		
		if (mutationID != other.mutationID)	// Ignore rest of Cosmic, they all key on mutationID
			return false;
		
		if (miRBId == null)	// Ignore rest of miRBase, they all key on id
		{
			if (other.miRBId != null)
				return false;
		}
		else if (!miRBId.equals (other.miRBId))
			return false;
		
		if (chromosome == null)
		{
			if (other.chromosome != null)
				return false;
		}
		else if (!chromosome.equals (other.chromosome))
			return false;
		
		if (ref == null)
		{
			if (other.ref != null)
				return false;
		}
		else if (!ref.equals (other.ref))
			return false;
		
		if (alt == null)
		{
			if (other.alt != null)
				return false;
		}
		else if (!alt.equals (other.alt))
			return false;
		
		if (dbSNPsID == null)
		{
			if (other.dbSNPsID != null)
				return false;
		}
		else if (!dbSNPsID.equals (other.dbSNPsID))
			return false;
		
		if (ensemblGeneID == null)
		{
			if (other.ensemblGeneID != null)
				return false;
		}
		else if (!ensemblGeneID.equals (other.ensemblGeneID))
			return false;
		
		if (geneSymbol == null)
		{
			if (other.geneSymbol != null)
				return false;
		}
		else if (!geneSymbol.equals (other.geneSymbol))
			return false;
		
		if (omimDisease == null)
		{
			if (other.omimDisease != null)
				return false;
		}
		else if (!omimDisease.equals (other.omimDisease))
			return false;
		
		if (suspectRegion == null)
		{
			if (other.suspectRegion != null)
				return false;
		}
		else if (!suspectRegion.equals (other.suspectRegion))
			return false;
		
		if (clinicalSig == null)
		{
			if (other.clinicalSig != null)
				return false;
		}
		else if (!clinicalSig.equals (other.clinicalSig))
			return false;
		
		if (alleleOrigin == null)
		{
			if (other.alleleOrigin != null)
				return false;
		}
		else if (!alleleOrigin.equals (other.alleleOrigin))
			return false;
		
		return true;
	}
	
	
	/**
	 * Return a tab delimited string with the columns in the order of {@link #toTabString ()} 
	 * 
	 * @return	Tab delimited string, no tab at beginning or end
	 */
	public static String tabHeader ()
	{
		StringBuilder builder = new StringBuilder ();
		
		builder.append ("chromosome\tref\talt\tstartPos\tendPos\tentrezGeneID\tdbSNPsID\tfirstBuild\t");
		builder.append ("alleleOrigin\tclinicalSig\tsuspectRegion\tdiseaseVariant\t");
		builder.append ("geneSymbol\tensemblGeneID\tmutationID\tcosmicCDS\tcosmicAA\tstrand\tOMIMDisease\t");
		builder.append ("blacklisted\tconserved\tenhancer\ttfbs\ttss\tunique\trepeat\tregulatory\t");
		builder.append ("landmark\ttype\tmnBP\tmaxBP\tstrand\tacc\tID");
		
		return builder.toString ();
	}
	
	
	/**
	 * Return a tab delimited string with the columns in the order of {@link #tabHeader ()} 
	 * 
	 * @return	Tab delimited string, no tab at beginning or end
	 */
	public String toTabString ()
	{
		StringBuilder builder = new StringBuilder ();
		
		builder.append (chromosome);
		builder.append ("\t");
		builder.append (ref);
		builder.append ("\t");
		builder.append (alt);
		builder.append ("\t");
		builder.append (startPos);
		builder.append ("\t");
		builder.append (endPos);
		builder.append ("\t");
		builder.append (entrezGeneID);
		builder.append ("\t");
		builder.append (dbSNPsID);
		builder.append ("\t");
		builder.append (firstBuild);
		builder.append ("\t");
		builder.append (alleleOrigin);
		builder.append ("\t");
		builder.append (clinicalSig);
		builder.append ("\t");
		builder.append (suspectRegion);
		builder.append ("\t");
		builder.append (diseaseVariant);
		builder.append ("\t");
		builder.append (geneSymbol);
		builder.append ("\t");
		builder.append (ensemblGeneID);
		builder.append ("\t");
		if (mutationID > 0)
		{
			builder.append (mutationID);
			builder.append ("\t");
			builder.append (cosmicCDS);
			builder.append ("\t");
			builder.append (cosmicAA);
			if (strand)
				builder.append ("\t+\t");
			else
				builder.append ("\t-\t");
		}
		else
			builder.append ("-1\t\t\t\t");
		
		builder.append (omimDisease);
		builder.append ("\t");
		builder.append (blacklisted);
		builder.append ("\t");
		builder.append (conserved);
		builder.append ("\t");
		builder.append (enhancer);
		builder.append ("\t");
		builder.append (tfbs);
		builder.append ("\t");
		builder.append (tss);
		builder.append ("\t");
		builder.append (unique);
		builder.append ("\t");
		builder.append (repeat);
		builder.append ("\t");
		builder.append (regulatory);
		if (miRBId != null)
		{
			builder.append ("\t");
			builder.append (landmark);
			builder.append ("\t");
			builder.append (type);
			builder.append ("\t");
			builder.append (minBP);
			builder.append ("\t");
			builder.append (maxBP);
			if (miRStrand)
				builder.append ("\t+\t");
			else
				builder.append ("\t-\t");
			builder.append (acc);
			builder.append ("\t");
			builder.append (miRBId);
		}
		else
			builder.append ("\t\t\t\t\t\t\t");
		
		return builder.toString ();
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString ()
	{
		StringBuilder builder = new StringBuilder ();
		
		builder.append ("VariantInfo [chromosome = ");
		builder.append (chromosome);
		builder.append (", (");
		builder.append (ref);
		builder.append (" -> ");
		builder.append (alt);
		builder.append ("), [");
		builder.append (startPos);
		if (endPos >= 0)
		{
			builder.append (", ");
			builder.append (endPos);
		}
		builder.append ("], entrezGeneID = ");
		builder.append (entrezGeneID);
		builder.append (", dbSNPsID = ");
		builder.append (dbSNPsID);
		builder.append (", firstBuild = ");
		builder.append (firstBuild);
		builder.append (", alleleOrigin = ");
		builder.append (alleleOrigin);
		builder.append (", clinicalSig = ");
		builder.append (clinicalSig);
		builder.append (", suspectRegion = ");
		builder.append (suspectRegion);
		builder.append (", diseaseVariant = ");
		builder.append (diseaseVariant);
		builder.append (", geneSymbol = ");
		builder.append (geneSymbol);
		builder.append (", ensemblGeneID = ");
		builder.append (ensemblGeneID);
		builder.append (", mutationID = ");
		if (mutationID > 0)
		{
			builder.append (mutationID);
			builder.append (", cosmicCDS = ");
			builder.append (cosmicCDS);
			builder.append (", cosmicAA = ");
			builder.append (cosmicAA);
			builder.append (", strand = ");
			if (strand)
				builder.append ("+");
			else
				builder.append ("-");
		}
		else
			builder.append ("-1");
		builder.append (", omimDisease = ");
		builder.append (omimDisease);
		builder.append (", blacklisted = ");
		builder.append (blacklisted);
		builder.append (", conserved = ");
		builder.append (conserved);
		builder.append (", enhancer = ");
		builder.append (enhancer);
		builder.append (", tfbs = ");
		builder.append (tfbs);
		builder.append (", tss = ");
		builder.append (tss);
		builder.append (", unique = ");
		builder.append (unique);
		builder.append (", repeat = ");
		builder.append (repeat);
		builder.append (", regulatory = ");
		builder.append (regulatory);
		builder.append ("]");
		if (miRBId != null)
		{
			builder.append (", landmark = ");
			builder.append (landmark);
			builder.append (", type = ");
			builder.append (type);
			builder.append (", minBP = ");
			builder.append (minBP);
			builder.append (", maxBP = ");
			builder.append (maxBP);
			if (miRStrand)
				builder.append (", miRStrand = '+'");
			else
				builder.append (", miRStrand = '-'");
			builder.append (", acc = ");
			builder.append (acc);
			builder.append (", miRBId = ");
			builder.append (miRBId);
		}
		else
			builder.append (", miRBId = Nothing");
		
		return builder.toString ();
	}
	
}
