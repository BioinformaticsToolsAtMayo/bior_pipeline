/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.SplitPipe;
import edu.mayo.pipes.UNIX.CatGZPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

/**
 *
 * @author m102417
 */
public class IntegrationTestSprint1 {
    
    public String geneFile = "src/test/resources/genes.tsv.bgz";
    //public String dbSNP = "src/test/resources/dbsnp20k.vcf.gz";
    public String dbSNP = "/data/dbsnp/00-All.vcf.gz";


    
    /**
     * This test, integrates several components that should work together as scripts piped together, but don't appear to work
     * from the command line (exit prematurely)
     * The script that exits is:
     * zcat 00-All.vcf.gz | bior_vcf_to_variants.sh | bior_overlap.sh -d /data/catalogs/NCBIGene/GRCh37_p10/genes.tsv.bgz | bior_drill.sh -p gene 
     */
    @Test
    public void testIntegrationOfComponentsInJVM() throws IOException{
        System.out.println("Integration Test of Several Components inside of the JVM");
        
        // pipes
	CatGZPipe               cat 	= new CatGZPipe("gzip");
	SplitPipe 		split 	= new SplitPipe("\t");
        VCF2VariantPipe         vcf2v   = new VCF2VariantPipe();
        OverlapPipe             op      = new OverlapPipe(geneFile);
	MergePipe		merge 	= new MergePipe("\t");
	PrintPipe		print	= new PrintPipe();
        Pipeline p = new Pipeline(cat, split, vcf2v, op, merge);
        p.setStarts(Arrays.asList(dbSNP));
        for(int i=0;p.hasNext();i++){
            if(i%5000==0){
                System.out.println(i);
            }
            p.next();
        }
        
    }
    
}
