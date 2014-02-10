package edu.mayo.bior.cli.cmd;

/**
 * @author Michael Meiners (m054457)
 * Date created: Oct 28, 2013
 */
public class Cmds {
	public enum Names {
		bior_vcf_to_tjson,
		bior_vep,
		bior_snpeff,
		bior_lookup,
		bior_same_variant,
		bior_overlap,
		bior_drill,
		bior_compress,
		bior_trim_spaces,
		
		// Others not used yet by internal commands
		bior_annotate,
		bior_create_catalog_props,
		bior_bed_to_tjson,
		bior_create_config_for_tab_to_tjson,
		bior_tab_to_tjson, 
		bior_pretty_print,
		bior_tjson_to_vcf,                    
		bior_create_catalog,
		bior_index_catalog
	};

}
