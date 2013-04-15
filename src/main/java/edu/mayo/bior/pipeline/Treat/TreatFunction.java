/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import com.tinkerpop.pipes.PipeFunction;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import java.util.List;

/**
 *
 * Treat is a stand along application for variant annotation.  Variants come in
 * as strings in VCF format (VCF header is stripped) and annotation is provided
 * in the TREAT.xls format - this is a tab delimited file with columns defined below.
 *  
 * @author dquest
 */
public class TreatFunction implements PipeFunction<String,History>{

    int count =0;
    
    private ColumnMetaData cmd(String s){
        return new ColumnMetaData(s);
    }
    
    private void setTreatMetadata(){
        List<ColumnMetaData> columns = History.getMetaData().getColumns();
        //strip out all columns --- they are wrong and this is an end user application
        columns.removeAll(columns);
                
        //IGV Link  --depricated
        columns.add(cmd("Chr"));                        //input
        columns.add(cmd("Position"));                   //input
        columns.add(cmd("Ref"));                        //input VCF Standard
        columns.add(cmd("Alt"));                        //input VCF Standard
        columns.add(cmd("Quality"));                    //input
        columns.add(cmd("dbSNP130/132/135"));           //dbSNP - rsID
        columns.add(cmd("dbSNP130/132/135Alleles"));    //dbSNP
        columns.add(cmd("DiseaseVariant"));             //dbSNP
        columns.add(cmd("HapMap_CEU_allele_freq"));     //HapMap
        columns.add(cmd("1kgenome_CEU_allele_freq"));   //1000Genome
        columns.add(cmd("HapMap_YRI_allele_freq"));     //HapMap
        columns.add(cmd("1kgenome_YRI_allele_freq"));   //1000Genome
        columns.add(cmd("HapMap_JPT+CHB_allele_freq")); //HapMap
        columns.add(cmd("1kgenome_JPT+CHB_allele_freq"));//1000Genome
        columns.add(cmd("BGI200_Danish"));               //BGI
        columns.add(cmd("COSMIC"));                      //COSMIC
        columns.add(cmd("ESP6500_EUR_maf"));             //ESP
        columns.add(cmd("ESP6500_AFR_maf"));             //ESP
        //InCaptureKit   --depricated  //Following columns are in GenomeGPS
        //#AlternateHits --depricated
        //GenotypeClass  --depricated
        //Alt-SupportedReads --depricated
        //Ref-SupportedReads --depricated
        //ReadDepth      --depricated
        //CloseToIndel   --depricated
        columns.add(cmd("Codons"));         //VEP - SIFT
        columns.add(cmd("Transcript_ID"));  //VEP - SIFT
        //Protein_ID     --depricated
        columns.add(cmd("Substitution"));   //VEP - SIFT
        //Region        --depricated
        columns.add(cmd("SNP_Type"));       //VEP - SIFT
        columns.add(cmd("Prediction"));     //VEP - SIFT
        columns.add(cmd("Score"));          //VEP - SIFT
        //Median_Info    --depricated
        columns.add(cmd("Gene_ID"));        //NCBIGene
        columns.add(cmd("Gene_Name"));      //NCBIGene
        columns.add(cmd("OMIM_Disease"));   //OMIM
        columns.add(cmd("Average_Allele_Freqs"));//HAPMAP
        //User Comment   --depricated
        //SynonymousCodonUsage  --depricated
        //Difference     --depricated
        columns.add(cmd("BlacklistedRegion"));//UCSC
        columns.add(cmd("Alignability_Uniquness"));//UCSC
        columns.add(cmd("Repeat_Region"));//UCSC
        columns.add(cmd("miRbase"));    //miRBASE
        columns.add(cmd("SNP_SuspectRegion"));//dbSNP
        columns.add(cmd("SNP_ClinicalSig"));//dbSNP
        columns.add(cmd("conservation"));//UCSC
        columns.add(cmd("regulation")); //UCSC
        columns.add(cmd("tfbs"));       //UCSC
        columns.add(cmd("tss"));        //UCSC
        columns.add(cmd("enhancer"));   //UCSC
        columns.add(cmd("UniprotID"));  //HGNC
        columns.add(cmd("polyphen2"));  //VEP
        columns.add(cmd("Homozygous")); //SNPEFF
        columns.add(cmd("Bio_type"));   //SNPEFF
        columns.add(cmd("accession"));  //SNPEFF
        columns.add(cmd("Exon_ID"));    //SNPEFF
        columns.add(cmd("Exon_Rank"));  //SNPEFF
        columns.add(cmd("functionGVS"));//SNPEFF
        columns.add(cmd("aminoAcids")); //SNPEFF
        columns.add(cmd("proteinPosition"));//SNPEFF
        columns.add(cmd("Codon_Degeneracy"));//SNPEFF
        columns.add(cmd("geneList"));//SNPEFF
        columns.add(cmd("Entrez_id"));//SNPEFF
        columns.add(cmd("Gene_title")); //NCBIGene
        //Tissue_specificity --depricated
        //pathway            --depricated
        //GeneCards          --depricated
        //Kaviar_Variants    --depricated
      
    }
    
    /**
     * compute takes in a VCF line (NO HEADER), and shreds it into a TREAT line
     * Columns in the input (e.g. sample columns) that are not needed for annotation
     * are thrown away... this way it works JUST LIKE THE LEGACY TREAT COMMAND.
     * Outputs from compute can not be compared to TREAT because TREAT processing
     * was wrong in many respects, most notably same variant did not compare alleles.
     * This version of TREAT removes many of the errors, and also drops columns
     * that where validated by the BioR working group as not being used often.
     * @param VCFLine
     * @return 
     */
    public History compute(String VCFLine) {
        
        History xlsLine = new History();
        //Compute Treat and put the result in xlsLine (it is just an array of strings, history is getting set above)
        
        if(count == 0){
            setTreatMetadata();
        }
        count++;
        
        return xlsLine;
    }
    
}
