/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.tabix.OverlapPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.SplitPipe;
import edu.mayo.pipes.UNIX.CatGZPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author m102417
 */
public class IntegrationTestSprint1 {
    
    public String geneFile = "src/test/resources/genes.tsv.bgz";
    //public String dbSNP = "src/test/resources/dbsnp20k.vcf.gz";
    public String dbSNP = "src/test/resources/dbsnp20k.vcf.gz";


    public final List<String> outputMeta = Arrays.asList(
            "##fileformat=VCFv4.0",
            "##fileDate=20120616",
            "##source=dbSNP",
            "##dbSNP_BUILD_ID=137",
            "##reference=GRCh37.p5",
            "##phasing=partial",
            "##variationPropertyDocumentationUrl=ftp://ftp.ncbi.nlm.nih.gov/snp/specs/dbSNP_BitField_latest.pdf",
            "##FILTER=<ID=NC,Description=\"Inconsistent Genotype Submission For At Least One Sample\">",
            "##BIOR=<ID=\"bior.ToTJson\",Operation=\"bior_vcf_to_json\",DataType=\"JSON\",ShortUniqueName=\"ToTJson\">",
            "##BIOR=<ID=\"bior.dbsnp20k.vcf.gz\",Operation=\"bior_overlap\",DataType=\"JSON\",ShortUniqueName=\"dbsnp20k.vcf.gz\",Path=\"src/test/resources/dbsnp20k.vcf.gz\">",
            "##BIOR=<ID=\"bior.dbsnp20k.vcf.gz.gene\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"gene\",FieldDescription=\"\",ShortUniqueName=\"dbsnp20k.vcf.gz\",Path=\"src/test/resources/dbsnp20k.vcf.gz\">",
            "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	bior.ToTJson	bior.dbsnp20k.vcf.gz.gene",
            "1	11014	rs28484712	G	A	.	.	RSPOS=11014;dbSNPBuildID=125;SSR=0;SAO=0;VP=050000000005000002000100;WGT=1;VC=SNV;ASP;OTHERKG	{\"CHROM\":\"1\",\"POS\":\"11014\",\"ID\":\"rs28484712\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"RSPOS\":11014,\"dbSNPBuildID\":125,\"SSR\":0,\"SAO\":0,\"VP\":\"050000000005000002000100\",\"WGT\":1,\"VC\":\"SNV\",\"ASP\":true,\"OTHERKG\":true},\"_id\":\"rs28484712\",\"_type\":\"variant\",\"_landmark\":\"1\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":11014,\"_maxBP\":11014}	LOC100506145",
            "1	11022	rs28775022	G	A	.	.	RSPOS=11022;dbSNPBuildID=125;SSR=0;SAO=0;VP=050000000005000002000100;WGT=1;VC=SNV;ASP;OTHERKG	{\"CHROM\":\"1\",\"POS\":\"11022\",\"ID\":\"rs28775022\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"RSPOS\":11022,\"dbSNPBuildID\":125,\"SSR\":0,\"SAO\":0,\"VP\":\"050000000005000002000100\",\"WGT\":1,\"VC\":\"SNV\",\"ASP\":true,\"OTHERKG\":true},\"_id\":\"rs28775022\",\"_type\":\"variant\",\"_landmark\":\"1\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":11022,\"_maxBP\":11022}	LOC100506145",
            "1	11081	rs10218495	G	T	.	.	RSPOS=11081;dbSNPBuildID=119;SSR=0;SAO=0;VP=050000000009000102000100;WGT=1;VC=SNV;CFL;GNO;OTHERKG	{\"CHROM\":\"1\",\"POS\":\"11081\",\"ID\":\"rs10218495\",\"REF\":\"G\",\"ALT\":\"T\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"RSPOS\":11081,\"dbSNPBuildID\":119,\"SSR\":0,\"SAO\":0,\"VP\":\"050000000009000102000100\",\"WGT\":1,\"VC\":\"SNV\",\"CFL\":true,\"GNO\":true,\"OTHERKG\":true},\"_id\":\"rs10218495\",\"_type\":\"variant\",\"_landmark\":\"1\",\"_refAllele\":\"G\",\"_altAlleles\":[\"T\"],\"_minBP\":11081,\"_maxBP\":11081}	LOC100506145",
            "1	12214	rs202068986	C	G	.	.	RSPOS=12214;dbSNPBuildID=137;SSR=0;SAO=0;VP=050000000a05000002000100;WGT=1;VC=SNV;NSM;REF;ASP;OTHERKG	{\"CHROM\":\"1\",\"POS\":\"12214\",\"ID\":\"rs202068986\",\"REF\":\"C\",\"ALT\":\"G\",\"QUAL\":\".\",\"FILTER\":\".\",\"INFO\":{\"RSPOS\":12214,\"dbSNPBuildID\":137,\"SSR\":0,\"SAO\":0,\"VP\":\"050000000a05000002000100\",\"WGT\":1,\"VC\":\"SNV\",\"NSM\":true,\"REF\":true,\"ASP\":true,\"OTHERKG\":true},\"_id\":\"rs202068986\",\"_type\":\"variant\",\"_landmark\":\"1\",\"_refAllele\":\"C\",\"_altAlleles\":[\"G\"],\"_minBP\":12214,\"_maxBP\":12214}	DDX11L1"
    );
    
    /**
     * This test, integrates several components that should work together as scripts piped together, but don't appear to work
     * from the command line (exit prematurely)
     * The script that exits is:
     * zcat 00-All.vcf.gz | bior_vcf_to_tjson | bior_overlap -d /data/catalogs/NCBIGene/GRCh37_p10/genes.tsv.bgz | grep -v "##INFO.*" | grep -v "{}" | bior_drill -p gene 
     */
    @Test
    public void testIntegrationOfComponentsInJVM() throws IOException{
        System.out.println("Integration Test of Several Components inside of the JVM");
        String[] dpath = new String[]{"gene"};
        Metadata tojson = new Metadata("bior_vcf_to_json");
        Metadata overlap = new Metadata(dbSNP, "bior_overlap");
        Metadata drill = new Metadata(-1, "bior_drill",false, dpath);
        ArrayList<Metadata> ops = new ArrayList<Metadata>();
        ops.add(tojson);
        ops.add(overlap);
        ops.add(drill);
        
        Pipeline p = new Pipeline(
                new CatPipe(),  //updates from greg allow it to handle zipped files
                new HistoryInPipe(ops),
                new VCF2VariantPipe(),
                new OverlapPipe(geneFile),
                new DrillPipe(false, dpath),
                new HistoryOutPipe(),
                new GrepEPipe("##INFO.*"), // remove all INFO rows
                new GrepEPipe("\\.$")    // remove non-matching rows
                );
        p.setStarts(Arrays.asList(dbSNP));
        for(int i=0;i<outputMeta.size();i++){
            String s = (String) p.next();
            assertEquals(outputMeta.get(i),s.trim());
        }
        
    }
    
}
