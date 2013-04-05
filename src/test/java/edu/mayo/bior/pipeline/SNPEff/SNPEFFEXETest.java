/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.SNPEff;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
public class SNPEFFEXETest {
    
    public SNPEFFEXETest() {
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

    /**
     * Test of getSnpEffCommand method, of class SNPEFFEXE.
     */
    @Test
    public void testCanCreateSeqChange() throws Exception {
        System.out.println("canCreateSeqChange");
        SNPEFFEXE snp = new SNPEFFEXE(true);
        String f = "src/test/resources/tools/snpeff/variantsWithMultipleInsertionsDeletions.vcf";
        BufferedReader br = new BufferedReader(new FileReader(f)); 
        String line = "";
        String s;
        boolean status;
        while(line != null){
            line = br.readLine();
            
            if(line != null){
                //System.out.println(line);
                s = snp.canCreateSeqChange(line);
                //System.out.println(s);
                if(s==null){
                    status = false;
                }else{
                    status = true;
                }
                assertEquals(true, status);
            }
            
        }

    }


}
