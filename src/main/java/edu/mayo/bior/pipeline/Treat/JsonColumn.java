package edu.mayo.bior.pipeline.Treat;

/**
 * JSON columns added by the TREAT workflow.
 * 
 * @author duffp
 *
 */
public enum JsonColumn {
	VARIANT, // JSON added by VCF2VariantPipe	
	NCBI_GENE,
	HGNC,
	DBSNP_ALL,
	DBSNP_CLINVAR,
	COSMIC,
	OMIM,
	HAPMAP,
	MIRBASE,
	THOUSAND_GENOMES,
	BGI,
	ESP,
	UCSC_BLACKLISTED,
	UCSC_CONSERVATION,
	UCSC_ENHANCER,
	UCSC_TFBS,
	UCSC_TSS,
	UCSC_UNIQUE,
	UCSC_REPEAT,
	UCSC_REGULATION,
	VEP,
	VEP_HGNC, // HGNC derived from VEP's Ensembl gene ID
	SNPEFF
}