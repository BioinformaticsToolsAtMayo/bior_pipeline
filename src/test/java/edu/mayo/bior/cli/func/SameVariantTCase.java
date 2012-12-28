package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class SameVariantTCase extends BaseFunctionalTest {
	
	 @Test
     public void testNoMatch() throws IOException, InterruptedException {

             String stdin = "src/test/resources/sameVariantCatalog.tsv.gz";

        String jsonVar = "BADCHR\t26960070XX\trs116645811X\tX\tY\t.\t.\t.\t{\"CHROM\":\"BADCHR\",\"POS\":\"26960070X\",\"ID\":\"rs116645811X\",\"REF\":\"X\",\"ALT\":\"Y\",\"QUAL\":\".\",\"FILTER\":\".\",\"_id\":\"rs116645811X\",\"_type\":\"variant\",\"_landmark\":\"BADCHR\",\"_refAllele\":\"X\",\"_altAlleles\":[\"Y\"],\"_minBP\":26960070X,\"_maxBP\":26960070X}";
             CommandOutput out = executeScript("bior_same_variant.sh", jsonVar, "-d", stdin);

             assertEquals(out.stderr, 0, out.exit);
             assertEquals("", out.stderr);

             String header = getHeader(out.stdout);

             // pull out just data rows
             String data = out.stdout.replace(header, "");
             String[] cols = data.split("\t");

             // If variant is not found, results in empty json string
             assertEquals("{}", cols[9].trim());
     }


     @Test
     public void testMatch() throws IOException, InterruptedException {

             String stdin = "src/test/resources/sameVariantCatalog.tsv.gz";

        String jsonVar = "21\t26960070\trs116645811\tG\tA\t.\t.\t.\t{\"CHROM\":\"21\",\"POS\":\"26960070\",\"ID\":\"rs116645811\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"_id\":\"rs116645811\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":26960070,\"_maxBP\":26960070}";
             CommandOutput out = executeScript("bior_same_variant.sh", jsonVar, "-d", stdin);

             assertEquals(out.stderr, 0, out.exit);
             assertEquals("", out.stderr);

             String header = getHeader(out.stdout);

             // pull out just data rows
             String data = out.stdout.replace(header, "");
             String[] cols = data.split("\t");

             // If variant is not found, results in empty json string
             //System.out.println(cols.length + cols[9]);
             assertEquals(10, cols.length);
             String json = cols[cols.length - 1];
             assertEquals("21", JsonPath.compile("CHROM").read(json));

        }

     @Test
     public void testMultipleDataSources() throws IOException, InterruptedException {

             String catalog1 = "src/test/resources/sameVariantCatalog.tsv.gz";

             String catalog2 = "src/test/resources/example_dbSNP_catalog.tsv.bgz";

             String jsonVar = "21\t26960070\trs116645811\tG\tA\t.\t.\t.\t{\"CHROM\":\"21\",\"POS\":\"26960070\",\"ID\":\"rs116645811\",\"REF\":\"G\",\"ALT\":\"A\",\"QUAL\":\".\",\"FILTER\":\".\",\"_id\":\"rs116645811\",\"_type\":\"variant\",\"_landmark\":\"21\",\"_refAllele\":\"G\",\"_altAlleles\":[\"A\"],\"_minBP\":26960070,\"_maxBP\":26960070}";
             CommandOutput out = executeScript("bior_same_variant.sh", jsonVar, "-d", catalog1, "-d", catalog2);

             assertEquals(out.stderr, 0, out.exit);
             assertEquals("", out.stderr);

             String header = getHeader(out.stdout);

             // pull out just data rows
             String data = out.stdout.replace(header, "");
             String[] cols = data.split("\t");

             // If variant is not found, results in empty json string
             //System.out.println(cols.length + cols[9]);

             // adding another data source should add another column
             assertEquals(11, cols.length);
             String json = cols[cols.length - 1];
             assertEquals("21", JsonPath.compile("CHROM").read(json));
        }
 
}
