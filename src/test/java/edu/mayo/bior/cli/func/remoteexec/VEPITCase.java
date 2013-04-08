package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.BaseFunctionalTest;
import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.VEP.VEPEXE;
import edu.mayo.pipes.MergePipe;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

/** Required steps before being able to run these command:
 *  1) SSH to DragonRider dev server and setup the functestuser user
 *  	a) ssh bior@biordev.mayo.edu
 *  	b) su -
 *  	c) adduser functestuser --home /home/functestuser
 *  2) Setup environment scripts
 *  	a) vi /home/functestuser/.bashrc
 *  	b) (add these variables):
 *  			$xxxx=yyyy
 *  	c) 
 * @author Michael Meiners (m054457)
 *
 */
public class VEPITCase extends RemoteFunctionalTest {

//	@BeforeClass
//	public static void beforeAllVep() {
//		System.out.println("BeforeAll - VEP");
//	}
//	
//	@AfterClass
//	public static void afterAllVep() {
//		System.out.println("AfterAll - VEP");
//	}
//	
//	@Before
//	public void beforeEachVep() {
//		System.out.println("BeforeEach - VEP");
//	}
//
//	@After
//	public void afterEachVep() {
//		System.out.println("AfterEach - VEP");
//	}
    
    	/**
	 * note: if you want to dig deep and debug this code, you probably want to set your log4j properties to:
	 * ##active
           ## log4j configuration used during build and unit tests
           #log4j.rootLogger=DEBUG, console
           #log4j.threshhold=ALL

           ## console appender logs to STDOUT
           #log4j.appender.console=org.apache.log4j.ConsoleAppender
           ##log4j.appender.console.layout=org.apache.log4j.PatternLayout
           #log4j.appender.console.layout.ConversionPattern=%d [%t] %-5p %c - %m%n
	 * 
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	//@Test
	public void testExecSNPEffPipe() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException{
		System.out.println("Test the raw output of a run on SNPEff versus the expected output (w/o header)");
		VEPEXE vep = new VEPEXE();
		//Pipe t = new TransformFunctionPipe(snp);
		Pipeline p = new Pipeline(
				new CatPipe(),               //raw file
				new HistoryInPipe(),         //get rid of the header
				new MergePipe("\t"),
				//t,
				new PrintPipe()
				//new IdentityPipe()
				);
		p.setStarts(Arrays.asList("src/test/resources/tools/vep/example.vcf"));
		//expected results
		BufferedReader br = new BufferedReader(new FileReader("src/test/resources/tools/vep/example.vcf.vep"));
                String expected = "";
                while(expected != null){
                    expected = br.readLine();
                    System.out.println(expected);
                }
//		for(int i=1;p.hasNext();i++){
//			//System.out.println(i);
//			String o = (String) p.next();       //result from the pipeline
//			String res = (String) br.readLine();//result is the output file
//			while(res.startsWith("#")){//if it is a header line, skip it
//				res = (String) br.readLine();
//			}
//			//System.out.println("CALCULATED: " + o);
//			//System.out.println("OUTPUT    : " + res);
//			assertEquals(res,o);
//			//if(i==10) break;
//		}
	}
	
	//@Test
	public void testCatalogLocationDbSnp() {
		System.out.println("VEPITCase.testCatalogLocationDbSnp(): Verify that the dbSNP catalog file exists and is > 2GB...");
		File dbSnp = new File("/data/catalogs/dbSNP/137/00-All_GRCh37.tsv.bgz");
		Assert.assertTrue(dbSnp.exists());
		long twoGB = (2L * 1024L * 1024L * 1024L);
		Assert.assertTrue(dbSnp.length() > twoGB);
		System.out.println("  Verified.");
	}
	

	
    //@Test
    public void test() throws IOException, InterruptedException {
		System.out.println("VEPITCase.test(): testing sample VEP vcf file");
		// NOTE:  This test case should only run on biordev - where it can run VEP
		String stdin = loadFile(new File("src/test/resources/tools/vep/vepsample.vcf"));

		CommandOutput out = executeScript("bior_vep", stdin);

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);

		// pull out just data rows
		String data = out.stdout.replace(header, "");

		// JSON should be added as last column (9th)
		String[] cols = data.split("\t");
		//assertEquals(9, cols.length);

		String json = cols[cols.length - 1];
		//System.out.println(json);
		assertEquals("benign(0.001)", JsonPath.compile("PolyPhen").read(json));
		assertEquals("tolerated(0.05)", JsonPath.compile("SIFT").read(json));
		assertEquals("benign", JsonPath.compile("PolyPhen_TERM").read(json));
		assertEquals("tolerated", JsonPath.compile("SIFT_TERM").read(json));

	}

	
	@Test
	public void test2() {
		
	}
}
