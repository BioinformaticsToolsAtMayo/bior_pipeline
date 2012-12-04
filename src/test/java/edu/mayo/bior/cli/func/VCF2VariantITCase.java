package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class VCF2VariantITCase extends BaseFunctionalTest {

	@Test
	public void test() throws IOException, InterruptedException {
		
		String stdin = loadFile(new File("src/test/resources/test.vcf"));

		CommandOutput out = executeScript("bior_vcf_to_variants.sh", stdin);

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		// JSON should be added as last column (9th)
		String[] cols = out.stdout.split("\t");
		
		assertEquals(9, cols.length);
		
		String json = cols[cols.length - 1];

        assertEquals("20",			JsonPath.compile("CHROM").read(json));
        assertEquals("14370",		JsonPath.compile("POS").read(json));
        assertEquals(3,				JsonPath.compile("INFO.NS").read(json));
	}

}
