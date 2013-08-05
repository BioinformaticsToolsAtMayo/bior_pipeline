package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class LookupCommandITCase extends BaseFunctionalTest {

	@Test
	public void testMatch() throws IOException, InterruptedException
	{
		String catRelativePath = "src/test/resources/genes.tsv.bgz";
        String catCanonicalPath = (new File(catRelativePath)).getCanonicalPath();        
        
	    String stdin = 
	    		"#CHROM\tSTART\tEND\tHGNC_ID\n" +
	    		"Y\t28740815\t28780802\t18500";

	    CommandOutput out = executeScript("bior_lookup", stdin, "-p", "HGNC", "-d", catRelativePath);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);
        String[] headerLines = header.split("\n");
        assertEquals(2, headerLines.length);
        assertEquals(String.format("##BIOR=<ID=\"bior.genes\",Operation=\"bior_lookup\",DataType=\"JSON\",ShortUniqueName=\"genes\",Path=\"%s\"", catCanonicalPath), headerLines[0]);
        assertEquals("#CHROM\tSTART\tEND\tHGNC_ID\tbior.genes", headerLines[1]);         

        // pull out just data rows
        // Ex output: Y	28740815	28780802	{"_type":"gene","_landmark":"Y","_strand":"-","_minBP":28740815,"_maxBP":28780802,
        //           "gene":"PARP4P1","gene_synonym":"ADPRTL1P; PARP4P; PARP4PY1","note":"poly (ADP-ribose) polymerase family, member 4 pseudogene 1; Derived by automated computational analysis using gene prediction method: Curated Genomic.",
        //           "pseudo":"","GeneID":"347613","HGNC":"18500"}
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    String json = cols[cols.length - 1];
	    
	    assertEquals("PARP4P1", JsonPath.compile("gene").read(json));	    
	    assertEquals("347613", JsonPath.compile("GeneID").read(json));
	}

	@Test
	public void testNoMatch() throws IOException, InterruptedException
	{
		String catRelativePath = "src/test/resources/genes.tsv.bgz";
        String catCanonicalPath = (new File(catRelativePath)).getCanonicalPath();        

	    String inputLine =
	    		"#CHROM\tSTART\tEND\tHGNC_ID\n" +
	    		"Y\t28740815\t28780802\t9999999";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", catRelativePath);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);
        String[] headerLines = header.split("\n");
        assertEquals(2, headerLines.length);
        assertEquals(String.format("##BIOR=<ID=\"bior.genes\",Operation=\"bior_lookup\",DataType=\"JSON\",ShortUniqueName=\"genes\",Path=\"%s\"", catCanonicalPath), headerLines[0]);
        assertEquals("#CHROM\tSTART\tEND\tHGNC_ID\tbior.genes", headerLines[1]);         

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    assertEquals("{}", cols[4].trim());
	    
	    
	}	
	
	@Test
	public void testMatchCaseInsensitive() throws IOException, InterruptedException {
	    String inputLine = "Parp4p1"; // Actual name is: "PARP4P1";
	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "gene", "-d", "src/test/resources/genes.tsv.bgz" );
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    String json = cols[cols.length - 1];
	    
	    assertEquals("PARP4P1", JsonPath.compile("gene").read(json));	    
	}
	
	@Test
	public void testMatchCaseSensitive() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "C1ORF159"; // Actual name is: "C1orf159";
	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "gene", "-d", stdin, "-s");
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);

        // pull out just data rows
	    String data = out.stdout.replace(header, "");
	    String[] cols = data.split("\t");

	    String json = cols[cols.length - 1].trim();
	    
	    // Should NOT be found since the case didn't match that in the database
	    assertEquals("{}", json);
	}	
	
	@Test
	public void testInvalidInput() throws IOException, InterruptedException {
		String catRelativePath = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNC", "-d", catRelativePath);
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
		String catRelativePath = "src/test/resources/genes.tsv.bgz";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNCId", "-d", catRelativePath);
        assertEquals(out.stderr, 1, out.exit);
        assertTrue(out.stderr.contains("The built-in index for")  &&  ! out.stderr.contains("The index file path you specified"));
	}
	
	@Test
	public void testUserIndexFileNotExist() throws IOException, InterruptedException {
		final String catRelativePath = "src/test/resources/genes.tsv.bgz";
		final String USER_IDX = "doesNotExist.h2.db";

	    String inputLine = "Y\t28740815\t28780802\tJUNK";

	    CommandOutput out = executeScript("bior_lookup", inputLine, "-p", "HGNCId", "-d", catRelativePath, "-i", USER_IDX);
        assertEquals(out.stderr, 1, out.exit);
        assertTrue( ! out.stderr.contains("The built-in index for")  &&  out.stderr.contains("The index file path you specified does not exist:"));
	}

	
}
