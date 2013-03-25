package edu.mayo.bior.pipeline.SNPEff;

import java.util.List;


/**
 * SNPEffHelper helper class computes the "most-significant-effect" from all the Effects 
 * reported by SNPEff for a single variant. 
 * 
 *
 */
public class SNPEffHelper {
	
	public enum InfoFieldKey {
        EFFECT_KEY            ("SNPEFF_EFFECT"),
        IMPACT_KEY            ("SNPEFF_IMPACT"),
        FUNCTIONAL_CLASS_KEY  ("SNPEFF_FUNCTIONAL_CLASS"),
        CODON_CHANGE_KEY      ("SNPEFF_CODON_CHANGE"),
        AMINO_ACID_CHANGE_KEY ("SNPEFF_AMINO_ACID_CHANGE"),
        GENE_NAME_KEY         ("SNPEFF_GENE_NAME"),
        GENE_BIOTYPE_KEY      ("SNPEFF_GENE_BIOTYPE"),    
        CODING				  ("SNPEFF_CODING"),
        TRANSCRIPT_ID_KEY     ("SNPEFF_TRANSCRIPT_ID"),
        EXON_ID_KEY           ("SNPEFF_EXON_ID");

        // Actual text of the key
        private final String keyName;

        InfoFieldKey ( String keyName) {
            this.keyName = keyName;
        }

        public String getKeyName() {
            return keyName;
        }
	}
	
	// SnpEff labels each effect as either LOW, MODERATE, or HIGH impact, or as a MODIFIER.
    public enum EffectImpact {
        MODIFIER  (0),
        LOW       (1),
        MODERATE  (2),
        HIGH      (3);

        private final int severityRating;

        EffectImpact ( int severityRating ) {
            this.severityRating = severityRating;
        }

        public boolean isHigherImpactThan ( EffectImpact other ) {
            return this.severityRating > other.severityRating;
        }

        public boolean isSameImpactAs ( EffectImpact other ) {
            return this.severityRating == other.severityRating;
        }
    }

   

    // SnpEff assigns a functional class to each effect.
    public enum EffectFunctionalClass {
        NONE     (0),
        SILENT   (1),
        MISSENSE (2),
        NONSENSE (3);

        private final int priority;

        EffectFunctionalClass ( int priority ) {
            this.priority = priority;
        }

        public boolean isHigherPriorityThan ( EffectFunctionalClass other ) {
            return this.priority > other.priority;
        }
    }
    
    /**
     * 
     * @param all effects returned by SNPEff
     * @return the most-significant-helper fromt he above effects
     * Refer to 
     * https://github.com/broadgsa/gatk/blob/master/public/java/src/org/broadinstitute/sting/gatk/walkers/annotator/SnpEff.java
     * for the business-logic to get the most-significant-effect. Implemented the same here.
     * 
     */
    public static SNPEffectHolder getMostSignificantEffect ( List<SNPEffectHolder> effects ) {
    	SNPEffectHolder mostSignificantEffect = null;

        for ( SNPEffectHolder effect : effects ) {
            if ( mostSignificantEffect == null ||
                 effect.isHigherImpactThan(mostSignificantEffect) ) {

                mostSignificantEffect = effect;
            }
        }

        return mostSignificantEffect;
    }
	
}
