/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.bior.pipeline.VEPPostProcessingPipeline.FixSiftandPolyphen;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import java.util.Arrays;
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
    public void testExecute() {
        System.out.println("execute VEP PostProcessing Pipeline");
        String[] drillPath = new String[1];
        drillPath[0]= "INFO.CSQ";
        DrillPipe drill = new DrillPipe(false, drillPath);
        //##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|SIFT|PolyPhen|CELL_TYPE">
        String[] headers = {"Allele", 
                            "Gene", 
                            "Feature",
                            "Feature_type",
                            "Consequence",
                            "cDNA_position",
                            "CDS_position",
                            "Protein_position",
                            "Amino_acids",
                            "Codons",
                            "Existing_variation",
                            "DISTANCE",
                            "SIFT",
                            "PolyPhen",
                            "CELL_TYPE"
            };//A|ENSG00000260583|ENST00000567517|Transcript|upstream_gene_variant|||||||4432|||
        Delim2JSONPipe pipes2json = new Delim2JSONPipe(-1, false,  headers, "|");
        Pipe fixSiftPoly = new TransformFunctionPipe<History,History>(new FixSiftandPolyphen());
        
        Pipe p = new Pipeline(new CatPipe(),//the output of vep
                              new HistoryInPipe(),
                              new VCF2VariantPipe(), 
                              new FindAndReplaceHPipe(8,"CSQ=.*","."),//this is probably not the correct regular expression... I think it will modify the original INFO column if they had stuff in there
                              drill,
                              new FanPipe(),
                              pipes2json,
                              fixSiftPoly,
                              new PrintPipe());
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/vep.vcf"));
        while(p.hasNext()){
            p.next();
        }
//        Pipe<History, History> logic = null;
//        VEPPostProcessingPipeline instance = new VEPPostProcessingPipeline();
//        instance.execute(logic);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
}
