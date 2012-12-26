package edu.mayo.bior.cli.func;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class SameVariantTCase extends BaseFunctionalTest {

	@Test
	public void test() throws IOException, InterruptedException {
		String stdin = loadFile(new File("src/test/resources/testData/sameVariantCatalog.tsv.gz"));

		CommandOutput out = executeScript("bior_vcf_to_json.sh", stdin);
		
		String header = getHeader(out.stdout);
		
		System.out.println("HEADER=\n"+header);
	}
}
