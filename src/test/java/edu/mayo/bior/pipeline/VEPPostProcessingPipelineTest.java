/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline;

import edu.mayo.bior.pipeline.VEP.VEPPostProcessingPipeline;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.transform.TransformFunctionPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.pipeline.UnixStreamPipelineTest.AppendSuffixPipe;
import edu.mayo.bior.pipeline.VEP.VEPPostProcessingPipeline.FixSiftandPolyphen;
import edu.mayo.pipes.JSON.Delim2JSONPipe;
import edu.mayo.pipes.JSON.DrillPipe;
import edu.mayo.pipes.JSON.FanPipe;
import edu.mayo.pipes.InputStreamPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.UNIX.GrepPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.FindAndReplaceHPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
//import edu.mayo.pipes.util.CatalogUtils;

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

    @Test
    public void testSteveCancerDataset(){
        System.out.println("Test to make sure Steve's cancer file works...");
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline("WorstScenario");
        Pipe p;
        String expected = "chr1	75175886	rs3819946	T	C	3746.59	PASS	AC=52;AF=0.553;AN=94;DB;DP=11646;DS;Dels=0.00;ED=0;ESP_MAF.MAF=13.0581,44.8933,23.8428;FS=0.000;HRun=1;InbreedingCoeff=-0.8077;SNPEFF_AMINO_ACID_CHANGE=I176V;SNPEFF_AMINO_ACID_LENGTH=295;SNPEFF_CODON_CHANGE=Att/Gtt;SNPEFF_EFFECT=NON_SYNONYMOUS_CODING;SNPEFF_EXON_ID=NM_001130043.ex.3;SNPEFF_FUNCTIONAL_CLASS=MISSENSE;SNPEFF_GENE_NAME=CRYZ;SNPEFF_IMPACT=MODERATE;SNPEFF_TRANSCRIPT_ID=NM_001130043;set=variant23-variant24-variant29-variant28-variant25-variant69-variant61-variant63-variant71-variant31-variant36-variant37-variant8-variant6-variant9-variant41-variant42-variant40-variant45-variant4-variant43-variant44-variant49-variant48-variant60-variant15-variant14-variant16-variant56-variant57-variant58-variant11-variant10-variant13-variant12;thousGenomes.AF=0.42;.	GT:AD:DP:GQ:PL	./.	./.	./.	./.	./.	./.	./.	./.	./.	0/1:78,96:174:99:2255,0,1922	0/1:79,83:162:99:2404,0,2188	./.	./.	0/1:81,82:163:99:2617,0,2314	./.	./.	./.	0/1:49,36:85:99:999,0,1557	0/1:69,57:127:99:1723,0,2015	./.	./.	./.	./.	0/1:65,59:125:99:1732,0,1862	./.	0/1:110,123:233:99:3747,0,3358	./.	./.	0/1:71,70:141:99:1907,0,1882	0/1:101,69:171:99:1898,0,2730	0/1:53,82:135:99:2534,0,1433	./.	./.	0/1:44,34:78:99:1050,0,1317	./.	0/1:44,40:84:99:1281,0,1277	./.	0/1:67,64:131:99:1702,0,1858	./.	./.	./.	0/1:59,53:112:99:1547,0,1690	0/1:130,98:229:99:2963,0,3886	0/1:51,39:90:99:1042,0,1364	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	0/1:55,48:103:99:1248,0,1452	./.	0/1:44,55:100:99:1768,0,1302	./.	0/1:35,53:88:99:1293,0,951	0/1:64,97:161:99:2992,0,1748	./.	./.	./.	./.	0/1:67,67:134:99:1882,0,1801	0/1:57,51:108:99:1418,0,1522	1/1:0,75:75:99:2483,193,0	./.	0/1:30,29:59:99:830,0,755	./.	./.	0/1:40,27:67:99:642,0,1234	1/1:0,166:166:99:5667,436,0	1/1:0,156:156:99:5120,406,0	./.	./.	./.	./.	./.	./.	./.	./.	./.	0/1:75,81:158:99:2522,0,2172	./.	0/1:83,57:142:99:1661,0,2333	0/1:82,73:155:99:2407,0,2435	./.	./.	./.	./.	./.	./.	0/1:80,79:159:99:2231,0,2380	./.	./.	0/1:78,96:174:99:3062,0,2319	./.	1/1:0,68:68:99:2733,202,0	./.	./.	0/1:47,40:87:99:1145,0,1321	./.	0/1:72,61:133:99:2028,0,1940	0/1:68,65:133:99:1900,0,1980	0/1:88,74:162:99:2325,0,2591	./.	./.	./.	./.	./.	./.	./.	0/1:51,48:100:99:1430,0,1537	./.	0/1:83,76:159:99:2171,0,2250	./.	./.	./.	./.	./.	./.	./.	./.	1/1:0,108:108:99:3352,253,0	./.	0/1:128,118:247:99:3298,0,3778	./.	./.	0/1:118,122:240:99:3270,0,3170	./.	./.	./.	0/1:126,114:240:99:3326,0,3638	0/1:72,65:137:99:1718,0,1928	./.	./.	./.	./.	0/1:61,76:138:99:2186,0,1456	./.	./.	./.	./.	./.	./.	./.	./.	./.	./.	0/1:118,96:214:99:2950,0,3256	./.	./.	0/1:78,85:163:99:2220,0,2111	./.	./.	./.	{\"Allele\":\"C\",\"Gene\":\"ENSG00000116791\",\"Feature\":\"ENST00000370871\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"606\",\"CDS_position\":\"526\",\"Protein_position\":\"176\",\"Amino_acids\":\"I/V\",\"Codons\":\"Att/Gtt\",\"HGNC\":\"CRYZ\",\"SIFT\":\"tolerated(1)\",\"PolyPhen\":\"benign(0.002)\",\"SIFT_TERM\":\"tolerated\",\"SIFT_Score\":1.0,\"PolyPhen_TERM\":\"benign\",\"PolyPhen_Score\":0.002}";
        Pipe testP = new Pipeline(new GrepPipe(".*\\{.+}.*"),
                                  //new PrintPipe()
                                  new IdentityPipe()
                );
        p = vepp.getPipeline(new CatPipe(), 
                testP,
                true);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/cancer.vcf.vep"));
        //CatalogUtils.saveToFile(CatalogUtils.pipeOutputToStrings(p), "src/test/resources/tempOutput/vepPostProc.vcf.vep");
        for(int i=0; p.hasNext(); i++){
            //System.out.println(i);
            String next = (String) p.next(); 
            if(i==0){
                printAnyDifference(expected, next, 0);
                assertEquals(expected, next);
            }
        }
    }
    
    @Test
    public void testGetWorstPipeline(){
        System.out.println("Test GetPipeline: VEPPostProcessingPipeline...");
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline("WorstScenario");
        Pipe p;
        String expected = "17	41197702	rs80357183	T	A	.	.	.	{\"Allele\":\"A\",\"Gene\":\"ENSG00000012048\",\"Feature\":\"ENST00000491747\",\"Feature_type\":\"Transcript\",\"Consequence\":\"missense_variant\",\"cDNA_position\":\"2372\",\"CDS_position\":\"2273\",\"Protein_position\":\"758\",\"Amino_acids\":\"H/L\",\"Codons\":\"cAc/cTc\",\"HGNC\":\"BRCA1\",\"SIFT\":\"deleterious(0)\",\"PolyPhen\":\"probably_damaging(0.952)\",\"SIFT_TERM\":\"deleterious\",\"SIFT_Score\":0.0,\"PolyPhen_TERM\":\"probably_damaging\",\"PolyPhen_Score\":0.952}";
        Pipe testP = new Pipeline(new GrepPipe(".*\\{.+}.*"),
                                  //new PrintPipe()
                                  new IdentityPipe()
                );
        p = vepp.getPipeline(new CatPipe(), 
                testP,
                true
                );
        //p.setStarts(Arrays.asList("src/test/resources/tools/vep/vep.vcf"));
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/dbSNPS_overlap_BRCA1.vcf.vep"));
        for(int i=0; p.hasNext(); i++){
            String next = (String) p.next(); 
            if(i==0){
                assertEquals(expected, next);
            }
        }
    }
    
    @Test
    public void testGetWorstPipeline2(){
        System.out.println("Test GetPipeline (worst) VEPPostProcessingPipeline...  ");
        Pipe verify = new Pipeline(new CatPipe(), new GrepEPipe("#.*"));
        String[] path = {"SIFT_TERM","SIFT_Score","PolyPhen_TERM","PolyPhen_Score"};
        Pipe testP = new Pipeline( 
        		new GrepEPipe("#.*"),
        		//new GrepPipe(".*\\{.+}.*"),//if you don't want the ones that don't have a match
        		new HistoryInPipe(),
        		new DrillPipe(false, path),
        		new MergePipe("\t"),
        		//new PrintPipe()
        		new IdentityPipe()
        		);
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline("WorstScenario");
        Pipe p = vepp.getPipeline(new CatPipe(), testP, true);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf.vep"));
        verify.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf.vep.drill"));
        for(int i=0; p.hasNext(); i++){
            String next = (String) p.next();
            String expected = (String)verify.next();
            printAnyDifference(expected, next, i);
            assertEquals(expected, next);
        }
    }
    
    private void printAnyDifference(String expected, String actual, int line) {
        if(! expected.equals(actual)) {
        	System.out.println("Difference on line " + (line+1));
        	System.out.println("Expected: " + expected);
        	System.out.println("Was:      " + actual);
        	int idxOfDiff = Math.min(expected.length(), actual.length());
        	for(int i=0; i < idxOfDiff; i++) {
        		if( expected.charAt(i) != actual.charAt(i) ) {
        			idxOfDiff = i;
        			break;
        		}
        	}
        	StringBuilder sp = new StringBuilder();
        	while(sp.length() < idxOfDiff) {
        		if(actual.charAt(sp.length()) == '\t')
        			sp.append("\t");
        		else
        			sp.append(" ");
        	}
        	System.out.println("Diff at:  " + sp.toString() + "X");
        	System.out.println("Diff idx = " + idxOfDiff);
        }
    }
    
    /**
     * test case from michael zimmermann
     */
//        @Test
//    public void testGetWorstPipeline3(){
//        System.out.println("Test GetPipeline (worst) VEPPostProcessingPipeline from michael zimmermann...");
//        Pipe verify = new Pipeline(new CatPipe(), new GrepEPipe("#.*"));
//        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline("WorstScenario");
//        String[] path = {"SIFT_TERM","SIFT_Score","PolyPhen_TERM","PolyPhen_Score"};
//        Pipe p;
//                Pipe testP = new Pipeline( new GrepEPipe("#.*"),
//                                  //new GrepPipe(".*\\{.+}.*"),//if you don't want the ones that don't have a match
//                                  new HistoryInPipe(),
//                                  new DrillPipe(false, path),
//                                  new MergePipe("\t"),
//                                  //new PrintPipe()
//                                  new IdentityPipe()
//                );
//        p = vepp.getPipeline(new CatPipe(), testP);
//        p.setStarts(Arrays.asList("src/test/resources/tools/vep/mexample.vcf.vep"));
//        verify.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf.vep.drill"));
//        for(int i=0; p.hasNext(); i++){
//            String next = (String) p.next(); 
//            //String expected = (String)verify.next();
//            //assertEquals(expected, next);
//            
//        }
//    }

    /**
     * Test of execute method, of class VEPPostProcessingPipeline.
     */
    @Test
    public void testGetPipeline() {
        System.out.println("Test GetPipeline: VEPPostProcessingPipeline...");
        VEPPostProcessingPipeline vepp = new VEPPostProcessingPipeline();
        Pipe p;
		p = vepp.getPipeline(new CatPipe(), new IdentityPipe(), true);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf.vep"));
        String s="";
        while(p.hasNext()){
            s = (String) p.next().toString();            
        }
        //System.out.println(s);
        String[] arrResult = s.split("\t");
        assertEquals("ENSG00000073150", JsonPath.compile("Gene").read(arrResult[8]));
        //System.out.println(arrResult[8]);
        assertEquals("PANX2", JsonPath.compile("HGNC").read(arrResult[8]));
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
        Pipe p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe(), true);
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
        Pipe p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe(), true);
        p.setStarts(Arrays.asList(vcfsample));
        String result = "";
        while(p.hasNext()){
            result = (String) p.next();            
        }
        String[] arrResult = result.split("\t");        
        //System.out.println(arrResult[7]);
        assertFalse(arrResult[7].equals("."));  //cannot be ".", since "CSQ" will not be found              

        vcfsample = "21	26960070	rs116645811	G	A	.	.	";  // NO "CSQ"
        p = vepp.getPipeline(new IdentityPipe(), new IdentityPipe(), true);
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
