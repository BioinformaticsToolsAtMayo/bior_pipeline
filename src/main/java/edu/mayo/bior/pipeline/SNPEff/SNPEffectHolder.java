package edu.mayo.bior.pipeline.SNPEff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.mayo.bior.pipeline.SNPEff.SNPEffHelper.InfoFieldKey;



/**
 * Holds each SNPEff effect result as an object
 * @author m089716
 *
 */
public class SNPEffectHolder {        	
	
	private String Effect; 
	private String Codon_change;
	private String Amino_acid_change;
	private String Gene_name;
	private String Gene_bioType;
	private String Coding;
	private String Transcript;
	private String Exon;        
	private SNPEffHelper.EffectImpact impact;
	private SNPEffHelper.EffectFunctionalClass effectFunctionalClass;
	
	public SNPEffectHolder(Map<String, String> splitEffectCoreValues) {
		Effect = splitEffectCoreValues.get(InfoFieldKey.EFFECT_KEY.getKeyName());
		
		impact = SNPEffHelper.EffectImpact.valueOf(splitEffectCoreValues.get(InfoFieldKey.IMPACT_KEY.getKeyName()));
		
		if (splitEffectCoreValues.get(InfoFieldKey.FUNCTIONAL_CLASS_KEY.getKeyName()).trim().length() > 0 ) {
		effectFunctionalClass = SNPEffHelper.EffectFunctionalClass.valueOf(splitEffectCoreValues.get(InfoFieldKey.FUNCTIONAL_CLASS_KEY.getKeyName()));
		} else {
			effectFunctionalClass = SNPEffHelper.EffectFunctionalClass.NONE;
		}
		//System.out.println("effectFunctionalClass="+effectFunctionalClass.name());
		
		Codon_change = splitEffectCoreValues.get(InfoFieldKey.CODON_CHANGE_KEY.getKeyName());
		Amino_acid_change = splitEffectCoreValues.get(InfoFieldKey.AMINO_ACID_CHANGE_KEY.getKeyName());
		Gene_name = splitEffectCoreValues.get(InfoFieldKey.GENE_NAME_KEY.getKeyName());
		Gene_bioType = splitEffectCoreValues.get(InfoFieldKey.GENE_BIOTYPE_KEY.getKeyName());
		Coding = splitEffectCoreValues.get(InfoFieldKey.CODING.getKeyName());
		Transcript = splitEffectCoreValues.get(InfoFieldKey.TRANSCRIPT_ID_KEY.getKeyName());
		Exon = splitEffectCoreValues.get(InfoFieldKey.EXON_ID_KEY.getKeyName());
	}

	public boolean isHigherImpactThan ( SNPEffectHolder other ) {
        // If one effect is within a coding gene and the other is not, the effect that is
        // within the coding gene has higher impact:

        if ( isCoding() && ! other.isCoding() ) {
            return true;
        }
        else if ( ! isCoding() && other.isCoding() ) {
            return false;
        }

        // Otherwise, both effects are either in or not in a coding gene, so we compare the impacts
        // of the effects themselves. Effects with the same impact are tie-broken using the
        // functional class of the effect:

        if ( impact.isHigherImpactThan(other.impact) ) {
            return true;
        }
        else if ( impact.isSameImpactAs(other.impact) ) {
            return effectFunctionalClass.isHigherPriorityThan(other.effectFunctionalClass);
        }

        return false;
    }	
	
	public boolean isCoding() {
        return this.Coding == "CODING";
    }

//	@Override
//	public String toString() {
//		return "Effect="+Effect+
//				",Effect_impact="+impact.name()+
//				",Functional_class="+effectFunctionalClass.name()+
//				",Codon_change="+Codon_change+
//				",Amino_acid_change="+Amino_acid_change+
//				",TranscriptId="+Transcript+
//				",ExonId="+Exon;
//	}
	
	/**
	 * 
	 * @param snpEffectHolder
	 * @return value of the object snpEffectHolder as a List<String>
	 */
	public List<String> getAnnotationAsList() {
		List<String> mse = new ArrayList<String>();
		mse.add(this.getEffect());
		mse.add(this.getEffect_impact());
		mse.add(this.getFunctional_class());
		mse.add(this.getCodon_change());
		mse.add(this.getAmino_acid_change());
		//mse.add(this.getAmino_acid_length());
		mse.add(this.getGene_name());
		mse.add(this.getGene_bioType());
		mse.add(this.getCoding());
		mse.add(this.getTranscript());
		mse.add(this.getExon());
		
		return mse;
	}

	public String getEffect() {
		return Effect;
	}

	public String getEffect_impact() {
		return impact.name();
	}

	public String getFunctional_class() {
		return effectFunctionalClass.name();
	}

	public String getCodon_change() {
		return Codon_change;
	}

	public String getAmino_acid_change() {
		return Amino_acid_change;
	}

	public String getGene_name() {
		return Gene_name;
	}

	public String getGene_bioType() {
		return Gene_bioType;
	}

	public String getCoding() {
		return Coding;
	}

	public String getTranscript() {
		return Transcript;
	}

	public String getExon() {
		return Exon;
	}
}   