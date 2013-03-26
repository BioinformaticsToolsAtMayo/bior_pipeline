/**
 * bior_pipeline
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2011
 *
 */
package edu.mayo.bior.pipeline;

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
	private String	geneSymbol;
	private String	dbSNPsID;
	private String	ensemblGeneID;
	private String	omimDisease;
	private boolean	blacklisted;
	private boolean	conserved;
	private boolean	enhancer;
	private boolean	tfbs;
	private boolean	tss;
	private boolean	unique;
	private boolean	repeat;
	private boolean	regulatory;
	
	
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
	 * @param geneSymbol	HGNC Approved Gene Symbol of the gene the Variant is in, null if not in one
	 * @param dbSNPsID		dbSNPs ID if it has one, or null if it doesn't
	 * @param ensemblGeneID	EnsemblGeneID if it has one, or null if it doesn't
	 * @param omimDisease	OMIM disease if it has one, or null if it doesn't
	 * @param blacklisted	True if blacklist score is above the cutoff, false otherwise
	 * @param conserved		True if conserved score is above the cutoff, false otherwise
	 * @param enhancer		True if enhancer score is above the cutoff, false otherwise
	 * @param tfbs			True if tfbs score is above the cutoff, false otherwise
	 * @param tss			True if tss score is above the cutoff, false otherwise
	 * @param unique		True if unique score is above the cutoff, false otherwise
	 * @param repeat		True if repeat score is above the cutoff, false otherwise
	 * @param regulatory	True if regulatory score is above the cutoff, false otherwise
	 */
	public VariantInfo (String chromosome, int startPos, int endPos, String ref, String alt, int entrezGeneID, int firstBuild, 
						String geneSymbol, String dbSNPsID, String ensemblGeneID, String omimDisease, boolean blacklisted, 
						boolean conserved, boolean enhancer, boolean tfbs, boolean tss, boolean unique, boolean repeat, 
						boolean regulatory)
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
		this.omimDisease = omimDisease;
		this.blacklisted = blacklisted;
		this.conserved = conserved;
		this.enhancer = enhancer;
		this.tfbs = tfbs;
		this.tss = tss;
		this.unique = unique;
		this.repeat = repeat;
		this.regulatory = regulatory;
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
	 * @return the geneSymbol
	 */
	public final String getGeneSymbol ()
	{
		return geneSymbol;
	}
	
	
	/**
	 * @return the dbSNPsID
	 */
	public final String getDbSNPsID ()
	{
		return dbSNPsID;
	}
	
	
	/**
	 * @return the ensemblGeneID
	 */
	public final String getEnsemblGeneID ()
	{
		return ensemblGeneID;
	}
	
	
	/**
	 * @return the omimDisease
	 */
	public final String getOmimDisease ()
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
		
		return true;
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
		builder.append (", firstBuild = ");
		builder.append (firstBuild);
		builder.append (", geneSymbol = ");
		builder.append (geneSymbol);
		builder.append (", dbSNPsID = ");
		builder.append (dbSNPsID);
		builder.append (", ensemblGeneID = ");
		builder.append (ensemblGeneID);
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
		return builder.toString ();
	}
	
}
