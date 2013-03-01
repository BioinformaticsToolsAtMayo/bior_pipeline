package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class LookupCommandITCase extends BaseFunctionalTest {

	@Test
	public void testMatch() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\t18500";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    String json = cols[cols.length - 1];
	    
	    assertEquals("PARP4P1", JsonPath.compile("gene").read(json));	    
	    assertEquals("347613", JsonPath.compile("GeneID").read(json));
	}
	
	@Test
	public void testNoMatch() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\t9999999";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    assertEquals("{}", cols[4].trim());
	}
	
	@Test
	public void testInvalidInput() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    assertEquals("{}", cols[4].trim());
	}
	
	@Test
	public void testDefaultIndexFileNotExist() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNCId", "-d", stdin);
        assertEquals(out.stderr, 1, out.exit);
        assertTrue(out.stderr.contains("The built-in index for")  &&  ! out.stderr.contains("The index file path you specified"));
	}
	
	@Test
	public void testUserIndexFileNotExist() throws IOException, InterruptedException {
		final String STDIN = "src/test/resources/genes.tsv.bgz";
		final String USER_IDX = "doesNotExist.h2.db";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNCId", "-d", STDIN, "-i", USER_IDX);
        assertEquals(out.stderr, 1, out.exit);
        assertTrue( ! out.stderr.contains("The built-in index for")  &&  out.stderr.contains("The index file path you specified does not exist:"));
	}

	
}
