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

		CommandOutput out = executeScript("bior_vcf_to_json", stdin);

		assertEquals("STDERR:"+out.stderr+"\n"+"STDOUT:"+out.stdout, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
	        System.out.println(header);	
		assertEquals(
				"##fileformat=VCFv4.0" +"\n" +
				"##fileDate=20090805" +"\n" +
				"##source=myImputationProgramV3.1" +"\n" +
				"##reference=1000GenomesPilot-NCBI36" +"\n" +
				"##phasing=partial" +"\n" +
				"##INFO=<ID=NS,Number=1,Type=Integer,Description=\"Number of Samples With Data\">" +"\n" +
				"##INFO=<ID=DP,Number=1,Type=Integer,Description=\"Total Depth\">" +"\n" +
				"##INFO=<ID=AF,Number=.,Type=Float,Description=\"Allele Frequency\">" +"\n" +
				"##INFO=<ID=AA,Number=1,Type=String,Description=\"Ancestral Allele\">" +"\n" +
				"##INFO=<ID=DB,Number=0,Type=Flag,Description=\"dbSNP membership, build 129\">" +"\n" +
				"##INFO=<ID=H2,Number=0,Type=Flag,Description=\"HapMap2 membership\">" +"\n" +
				"#CHROM POS     ID        REF ALT    QUAL FILTER INFO"+"\tVCF2VariantPipe"+"\n", 
				header);
		
		// pull out just data rows
		String data = out.stdout.replace(header, "");
		
		// JSON should be added as last column (9th)
		String[] cols = data.split("\t");
		System.out.println("Columns should be 9 but it is " + cols.length);
		assertEquals(9, cols.length);
		
		String json = cols[cols.length - 1];

        assertEquals("20",			JsonPath.compile("CHROM").read(json));
        assertEquals("14370",		JsonPath.compile("POS").read(json));
        assertEquals(3,				JsonPath.compile("INFO.NS").read(json));
	}
}
