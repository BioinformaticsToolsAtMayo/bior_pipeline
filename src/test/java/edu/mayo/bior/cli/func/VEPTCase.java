package edu.mayo.bior.cli.func;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class VEPTCase extends BaseFunctionalTest {

	 @Test
     public void test() throws IOException, InterruptedException {

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

}
