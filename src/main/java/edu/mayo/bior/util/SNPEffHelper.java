package edu.mayo.bior.util;

import java.util.List;

import edu.mayo.bior.pipeline.SNPEffPipeline.SNPEffectHolder;

/**
 * SNPEffHelper helper class computes the "most-significant-effect" from all the Effects 
 * reported by SNPEff for a single variant. 
 * 
 *
 */
public class SNPEffHelper {
	
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
