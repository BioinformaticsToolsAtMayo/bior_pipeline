/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.jayway.jsonpath.JsonPath;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipelineTest.AppendSuffixPipe;
import edu.mayo.bior.pipeline.VEPPostProcessingPipeline.FixSiftandPolyphen;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author m102417
 */
public class VEPPostProcessingPipelineTest {
    
    public VEPPostProcessingPipelineTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of execute method, of class VEPPostProcessingPipeline.
     */
    @Test
    public void testGetPipeline() {
        System.out.println("Test GetPipeline: VEPPostProcessingPipeline...");
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
        Pipe p;
		p = vepp.getPipeline(new CatPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/vep.vcf"));
        String s="";
        while(p.hasNext()){
            s = (String) p.next().toString();            
        }
        //System.out.println(s);
        String[] arrResult = s.split("\t");
        assertEquals("ENSG00000073150", JsonPath.compile("Gene").read(arrResult[8]));
    }
    
    /**
     * Tests replace "CSQ=*" to "."
     */
    @Test
    public void testFindReplacePass() {
        System.out.println("Test FindReplace: VEPPostProcessingPipeline...");
        
        String csqHeader = "##INFO=<ID=CSQ,Number=.,Type=String,Description=\"Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE\">";
        String colHeader = "#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO";
        String vcfsample = "21	26960070	rs116645811	G	A	.	.	CSQ=A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||,A|ENSG00000154719|ENST00000352957|Transcript|intron_variant||||||||||,A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg|||tolerated(0.05)|benign(0.001)|";
        
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
        Pipe p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList(csqHeader, colHeader, vcfsample));
        String result = "";
        while(p.hasNext()){
            result = (String) p.next();            
        }
        //System.out.println("...........RESULT........\n"+result);
        String[] arrResult = result.split("\t");        
        //System.out.println(arrResult[7]);
        assertEquals(".", arrResult[7]);        
    }
    
    /**
     * Tests replace "CSQ=*" to "."
     */
    @Test
    public void testFindReplaceFail() {
        System.out.println("Test FindReplace Fail: VEPPostProcessingPipeline...");
        
        String vcfsample = "21	26960070	rs116645811	G	A	.	.	CSSQ=A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||,A|ENSG00000154719|ENST00000352957|Transcript|intron_variant||||||||||,A|ENSG00000154719|ENST00000307301|Transcript|missense_variant|1043|1001|334|T/M|aCg/aTg|||tolerated(0.05)|benign(0.001)|";
        
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
        Pipe p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList(vcfsample));
        String result = "";
        while(p.hasNext()){
            result = (String) p.next();            
        }
        String[] arrResult = result.split("\t");        
        //System.out.println(arrResult[7]);
        assertFalse(arrResult[7].equals("."));  //cannot be ".", since "CSQ" will not be found              

        vcfsample = "21	26960070	rs116645811	G	A	.	.	";  // NO "CSQ"
        p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe());
        p.setStarts(Arrays.asList(vcfsample));        
        while(p.hasNext()){
            result = (String) p.next();            
        }
        arrResult = result.split("\t");        
        //System.out.println(arrResult[7]);
        assertFalse(arrResult[7].equals("."));  //cannot be ".", since "CSQ" will not be found                           
        
    }
    
    /**
     * Test of execute method, of class VEPPostProcessingPipeline.
     */
    @Test
    public void testFixSiftandPolyphen() {
        System.out.println("Test FixSiftandPolyphen:VEPPostProcessingPipeline...");

        FixSiftandPolyphen fix = new FixSiftandPolyphen();
        
        double siftScore = fix.parseScore("tolerated(0.65)");
        assertEquals(0.65, siftScore, 0.0);

        double siftScore2 = fix.parseScore("tolerated(0)");
        //System.out.println(fix.parseScore("tolerated(0)"));
        assertEquals(0.0, siftScore2, 0.0);
        
        String siftPrediction = fix.parseTerm("tolerated(0.65)");
        assertEquals("tolerated", siftPrediction);
        
        String siftPrediction2 = fix.parseTerm("tolerated((0.65)"); //extra brackets
        assertEquals("tolerated", siftPrediction2);       
           
    }    
}
