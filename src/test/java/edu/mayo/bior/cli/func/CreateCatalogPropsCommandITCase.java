package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class CreateCatalogPropsCommandITCase extends BaseFunctionalTest {
	
	@Test
	public void testCommand() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    CommandOutput out = executeScript("bior_create_catalog_props", stdin, "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);
        
        //the above command should create this file
        File dsFile = new File("src/test/resources/genes.datasource.properties");
        assertTrue(dsFile.exists());
        dsFile.deleteOnExit();
        
        //the above command should create this file
        File cpFile = new File("src/test/resources/genes.columns.properties");
        assertTrue(cpFile.exists());
        cpFile.deleteOnExit();
	}
}
