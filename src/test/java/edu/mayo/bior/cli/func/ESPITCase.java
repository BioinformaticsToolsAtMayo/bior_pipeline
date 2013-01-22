package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class ESPITCase extends BaseFunctionalTest {
	
	 @Test
     public void testVariantFound() throws IOException, InterruptedException {
	     String espsample = "src/test/resources/miniCatalog/espsample.tsv.bgz";
	
	     String jsonVar = "21\t26960070\trs116645811\tG\tA\t.\t.\t.\t{\"CHROM\":\"21\",\"POS\":\"26960070\",\"ID\":\"rs116645811\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"_id\":\"rs116645811\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":26960070,\"_maxBP\":26960070}";
	
	     CommandOutput out = executeScript("bior_same_variant", jsonVar, "-d", espsample);
	
	     //System.out.println(out.stderr);
	
	     assertEquals(out.stderr, 0, out.exit);
	     assertEquals("", out.stderr);
	
	     String header = getHeader(out.stdout);
	     //System.out.println("Header="+header);
	
	     // pull out just data rows
	     String data = out.stdout.replace(header, "");
	     //System.out.println("data=\n"+data);
	
	     // JSON should be added as last column (9th)
	     String[] cols = data.split("\t");
	     //System.out.println(cols.length);
	     //assertEquals(10, cols.length);
	     String json = cols[cols.length - 1];
	     //System.out.println("json="+json);
	     assertEquals("21", JsonPath.compile("CHROM").read(json));
	
	     //These are retrieved from the catalog - info column
	     //System.out.println(JsonPath.compile("INFO.DBSNP").read(json));
	     assertEquals("[\"dbSNP_132\"]", JsonPath.compile("INFO.DBSNP").read(json).toString());
	     assertEquals("[\"possibly-damaging\",\".\"]", JsonPath.compile("INFO.PH").read(json).toString());
     }

}
