package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class CreateCatalogPropsCommandITCase extends BaseFunctionalTest {
	
	//@Test
	public void testCommand() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    CommandOutput out = executeScript("bior_create_catalog_props", "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);
        System.out.println("header="+header);
        
	}
}
