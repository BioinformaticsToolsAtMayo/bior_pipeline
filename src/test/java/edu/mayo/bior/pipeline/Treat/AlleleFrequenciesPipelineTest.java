/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 *
 * @author m102417
 */
public class AlleleFrequenciesPipelineTest {
    
    public AlleleFrequenciesPipelineTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        System.out.println("AlleleFrequenciesPipelineTest ===================================================");
        System.out.println("NOTE: If this test fails, you may need to construct the ~/bior.properties file.");
        System.out.println("An example can be found in src/main/resources/bior.properties");
        System.out.println("See BiorProperties.java for more info.");
        System.out.println("By default, it looks for these files (from bior.properties):");
        System.out.println("    fileBase=/data4/bsi/refdata-new/catalogs/v1/");
        System.out.println("    bgiFile=BGI/hg19/LuCAMP_200exomeFinal.maf_GRCh37.tsv.bgz");
        System.out.println("    espFile=ESP/build37/ESP6500SI_GRCh37.tsv.bgz");
        System.out.println("    hapMapFile=hapmap/2010-08_phaseII+III/allele_freqs_GRCh37.tsv.bgz");
        System.out.println("    kGenomeFile=1000_genomes/20110521/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37.tsv.gz");
        System.out.println("Try changing these values to:");
        System.out.println("    fileBase=src/test/resources/treat");
        System.out.println("    bgiFile=brca1.bgi.tsv.gz");
        System.out.println("    espFile=brca1.ESP.tsv.gz");
        System.out.println("    hapMapFile=brca1.hapmap.tsv.gz");
        System.out.println("    kGenomeFile=brca1.1000_genomes.tsv.gz");
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

	
    /**
     * Test my code
     * 
     * @throws IOException
     */
	@SuppressWarnings ({"rawtypes", "unchecked"})
    //@Test
	public void gregTest () throws IOException
	{
		Pipe input = new Pipeline (new CatPipe (), new HistoryInPipe ());
		Pipe output = new Pipeline (new HistoryOutPipe (), new PrintPipe ());
		
		AlleleFrequenciesPipeline p = new AlleleFrequenciesPipeline(input, output, "/Users/m082166/Documents/BioR/", true);
		p.setStarts(Arrays.asList ("src/test/resources/tools/vep/example.vcf"));
		List<String> actual = PipeTestUtils.getResults(p); 
		// TODO: Load expected from file.  
		// TODO: NOTE: if we are using the tabix files under src/test/resources/treat, we should focus on BRCA variants 
		List<String> expected = new ArrayList<String>(); //FileCompareUtils.loadFile("");
		PipeTestUtils.assertListsEqual(expected, actual);
	}

    
    /**
     * Test of init method, of class AlleleFrequenciesPipeline.
     */
    //@Test
    public void testInit() throws Exception {
        System.out.println("Testing Allele Frequencies Pipeline");
        Pipe input = new Pipeline(new CatPipe(), new HistoryInPipe());
        Pipe output = new Pipeline(new HistoryOutPipe(), new PrintPipe());
        AlleleFrequenciesPipeline p = new AlleleFrequenciesPipeline(input, output, false);
        p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
		List<String> actual = PipeTestUtils.getResults(p); 
		// TODO: Load expected from file.  
		// TODO: NOTE: if we are using the tabix files under src/test/resources/treat, we should focus on BRCA variants 
		List<String> expected = new ArrayList<String>(); //FileCompareUtils.loadFile("");
		PipeTestUtils.assertListsEqual(expected, actual);
    }
}
