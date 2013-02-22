package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class LookupCommandITCase extends BaseFunctionalTest {

	//@Test
    public void testMatch() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";
		
		String inputLine = "X\t155215011\t155215912\t23270";
		
		CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", stdin);
	
	    assertEquals(out.stderr, 0, out.exit);
	    assertEquals("", out.stderr);
	
	    String header = getHeader(out.stdout);
	    System.out.println(header);
	    
        // pull out just data rows
        String data = out.stdout.replace(header, "");
        String[] cols = data.split("\t");

        System.out.println(Arrays.asList(cols));
	}
	
}
