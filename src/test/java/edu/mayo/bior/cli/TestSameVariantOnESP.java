/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.cli;

import com.tinkerpop.pipes.util.Pipeline;
import edu.mayo.pipes.JSON.tabix.SameVariantPipe;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepPipe;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import java.io.IOException;
import java.util.Arrays;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author m102417
 */
public class TestSameVariantOnESP {
    
    public TestSameVariantOnESP() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void sameVariantTest() throws IOException {
        String greg = "src/test/resources/gregExample.vcf";
        Pipeline p = new Pipeline(new CatPipe(),
                new GrepPipe("865628"),
                new HistoryInPipe (), 
                new VCF2VariantPipe (),
                new SameVariantPipe("src/test/resources/ESPFuncTest.tsv.bgz"),
                new MergePipe("\t")//, //new GrepPipe("865628")
                //new PrintPipe()
                );
        p.setStarts(Arrays.asList(greg));
        while(p.hasNext()){
            String s = (String) p.next();
            String[] split = s.split("\t");
            for(int j = 0; j<split.length; j++){
                //if(j<9 || j > 297){
                    System.out.println(split[j]);
                //}
            }
        }
    
    }
}
