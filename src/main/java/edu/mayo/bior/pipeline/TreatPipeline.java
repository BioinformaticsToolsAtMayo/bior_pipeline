/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import java.io.IOException;
import java.util.Arrays;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HCutPipe;
import edu.mayo.pipes.history.HistoryInPipe;

/**
 *
 * @author m102417
 */
public class TreatPipeline {
    
    public static void main(String[] args) throws IOException{
        System.out.println("Welcome to treat");
        String vcf = "src/test/resources/tools/vep/example.vcf";
        
        String genesFile = "src/test/resources/genes.tsv.bgz";
        String[] geneDrill = {"gene", "note"};
        int[] cut = {9};
        Pipeline p = new Pipeline(new CatPipe(),
                                  new HistoryInPipe(),
                                  new VCF2VariantPipe(),
                                  new OverlapPipe(genesFile),
                                  new DrillPipe(false, geneDrill),
                                  new HCutPipe(cut),
                                  new PrintPipe());
        p.setStarts(Arrays.asList(vcf));
        for(int i = 0; p.hasNext(); i++){
            p.next();
        }
    }
    
}
