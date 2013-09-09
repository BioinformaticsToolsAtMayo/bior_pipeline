package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.After;
import org.junit.Before;

/**
 * @author Michael Meiners (m054457)
 * Date created: Apr 26, 2013
 */
public class ManyCmdsITCase extends RemoteFunctionalTest {
  
    public final String VCF_IN 		= "src/test/resources/tools/manycmds/manycmds.input.vcf";
    public final String TREAT_IN = "src/test/resources/tools/manycmds/treatInputData";
    public final String DBSNP_CATALOG     	= "src/test/resources/treat/brca1.dbsnp.tsv.gz";
    public final String GENES_CATALOG   	= "src/test/resources/genes.tsv.bgz";
    private HashMap<String,String> hm = new HashMap();
    
    @Before
    public void setUp()throws IOException{
        SNPEFFPipelineITCase snpeff = new SNPEFFPipelineITCase();
        snpeff.setUp();
        VEPPipelineITCase vep = new VEPPipelineITCase();
        vep.setUp();
        File in = new File(VCF_IN);
        File dbSNP = new File(DBSNP_CATALOG);
        File gene = new File(GENES_CATALOG);
        hm.put("REPLACEMEmanycmds.input.vcf", in.getCanonicalPath());
        hm.put("REPLACEMEbrca1.dbsnp.tsv.gz", dbSNP.getCanonicalPath());
        hm.put("REPLACEMEgenes.tsv.bgz", gene.getCanonicalPath());
        hm.put("REPLACEMEsnpeff.datasource.properties", snpeff.getDataSourceProps().getCanonicalPath());
        hm.put("REPLACEMEsnpeff.column.properties", snpeff.getColumnProps().getCanonicalPath());
        hm.put("REPLACEMEvep.datasource.properties", vep.getDataSourceProps().getCanonicalPath());
        hm.put("REPLACEMEvep.column.properties", vep.getColumnProps().getCanonicalPath());
    }
    
    @After
    public void tearDown()
    {
	History.clearMetaData();
    }
    
    public String replaceHash(String input) {
        String output = input;
        for(String key : hm.keySet() ){
            if(input.contains(key)){
                output = output.replaceAll(key, hm.get(key));
            }
        }
        return output;
    }

    

	@Test
	public void testManyCmds() throws IOException, InterruptedException { 
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("ManyCmdsITCase.testManyCmds(): Test many commands piped into one another");
		
		// GOAL:
		// 1) Take a small list of variants as input
		// 2) Use bior_vcf_to_json to convert to json
		// 3) Do bior_same_variant against dbSnp (small dbSnp with BRCA1 variants)
		// 4) Do bior_overlap to find genes associated with those variants
		// Commands:
		// 		bior_vcf_to_json
		//		bior_same_variant
		//		bior_overlap
		//		bior_drill
		//		bior_lookup
		//		bior_vep
		//		bior_snpeff
		// NOT used:
		//		bior_index
		//		bior_squish
		//      bior_pretty_print

		String cmd = String.format(
				"cat %s  | bior_vcf_to_tjson  | bior_same_variant -d %s  | bior_overlap -d %s  | " +
				"bior_drill -p HGNC  | bior_lookup -d %s -p HGNC  | bior_vep | bior_snpeff | bior_drill -p Effect_impact ",
				VCF_IN,
				DBSNP_CATALOG,
				GENES_CATALOG,
				GENES_CATALOG
			);
		System.out.println("Command: " + cmd);
		CommandOutput out = executeScriptWithPipes(cmd);
	        
//		assertEquals(out.stderr,"SNPEFF Starting up... this will take about a minute");
		assertEquals(0, out.exit);
		
//		assertEquals(out.stderr,"SNPEFF Starting up... this will take about a minute ");
		assertEquals(0, out.exit);
		
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
		List<String> expectedIN = FileCompareUtils.loadFile("src/test/resources/tools/manycmds/manycmds.out.vcf");
                ArrayList<String> expectedFixed = new ArrayList<String>();
                for(String s : expectedIN){
                    if(s.startsWith("##BIOR")){
                        s = s.replaceAll("Path=.*","Path=@@PATH>");
                        s = s.replaceAll("DataSourceProperties=.*>.*","DataSourceProperties=@@PATH>");
                    }
                    expectedFixed.add(this.replaceHash(s));
                }
        ArrayList<String> actualFixed = new ArrayList<String>();
        for(String s : actual){
            if(s.startsWith("##BIOR")){
                s = s.replaceAll("Path=.*","Path=@@PATH>");
                s = s.replaceAll("DataSourceProperties=.*>.*","DataSourceProperties=@@PATH>");
            }
            actualFixed.add(s);
        }
	
		VEPCommandITCase.printComparison(null, expectedFixed, actualFixed);

		PipeTestUtils.assertListsEqual(expectedFixed, actualFixed);
	}
	
	@Test 
	public void testAnnotateWithVCFiser() throws IOException, InterruptedException {
		System.out.println("_____________________---------------");
		System.out.println("----Test Annotate with vcfiser------");
		
		String cmd = String.format(" cat %s | bior_annotate | bior_tjson_to_vcf",TREAT_IN);
		System.out.println("Command: " + cmd);
		CommandOutput out = executeScriptWithPipes(cmd);
		assertEquals(0,out.exit);
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
		List<String> expectedIN = FileCompareUtils.loadFile("src/test/resources/tools/manycmds/treatVcfiserOutput");
                ArrayList<String> expectedFixed = new ArrayList<String>();
                for(String s : expectedIN){
                    expectedFixed.add(this.replaceHash(s));
                }
        
//       VEPCommandITCase.printComparison(null, expectedFixed, actual); 
        TreatITCase.assertLinesEqual(expectedFixed, actual);
//      PipeTestUtils.assertListsEqual(expectedFixed, actual);
		
	}
}
