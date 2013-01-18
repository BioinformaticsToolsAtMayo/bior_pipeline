package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class ESPITCase extends BaseFunctionalTest {
	
	//@Test
	public void test() throws IOException, InterruptedException {
        String stdin = loadFile(new File("src/test/resources/miniCatalog/espsample.vcf"));

        CommandOutput out = executeScript("bior_vep", stdin);

        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
        String data = out.stdout.replace(header, "");

        // JSON should be added as last column (9th)
        String[] cols = data.split("\t");
		
	}
}
