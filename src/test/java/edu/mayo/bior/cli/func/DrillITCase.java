package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

public class DrillITCase extends BaseFunctionalTest {

	// header line added from running command prior to drill
	private final String prevCmdHeader = 
			"##BIOR=<ID=\"bior.JSON_COL\",Operation=\"bior_same_variant\",DataType=\"JSON\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">"; 
	
	private final String stdin =
			prevCmdHeader + "\n" +			
			"#bior.JSON_COL" + "\n" + 
			"{" +
					"\"key1\":\"string_value1\"," +
					"\"key2\": true," +
					"\"key3\": 1" +
			"}";	
	
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
		
		CommandOutput out = executeScript("bior_drill", stdin, "-p", "key1", "-p", "key2", "-p", "key3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
        String[] headerLines = header.split("\n");
        assertEquals(5, headerLines.length);
        assertEquals(prevCmdHeader, headerLines[0]);		
		assertEquals("##BIOR=<ID=\"bior.JSON_COL.key1\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"key1\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">", headerLines[1]);
		assertEquals("##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"key2\",Path=\"/path/to/catalog\">", headerLines[2]);
		assertEquals("##BIOR=<ID=\"bior.JSON_COL.key3\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"key3\",Path=\"/path/to/catalog\">", headerLines[3]);
		assertEquals("#bior.JSON_COL.key1\tbior.JSON_COL.key2\tbior.JSON_COL.key3", headerLines[4]);

		// pull out just data rows		
		String data = out.stdout.replace(header, "");		
		String[] cols = data.split("\t");
		
		assertEquals(3, cols.length);

        assertEquals("string_value1",	cols[0].trim());
        assertEquals("true",			cols[1].trim());
        assertEquals("1",				cols[2].trim());
	}	

	@Test
	public void testKeepJson() throws IOException, InterruptedException {
		
		CommandOutput out = executeScript("bior_drill", stdin, "-k", "-p", "key2");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
        String[] headerLines = header.split("\n");
        assertEquals(3, headerLines.length);
        assertEquals(prevCmdHeader, headerLines[0]);		
		assertEquals("##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"key2\",Path=\"/path/to/catalog\">", headerLines[2]);
		assertEquals("#bior.JSON_COL.key2\tbior.JSON_COL", headerLines[3]);

		// pull out just data rows
		String data = out.stdout.replace(header, "");		
		
		// pull out just json
		String expectedJson = stdin.replace(getHeader(stdin), "");
		
		// JSON should be added as last column (4th)
		String[] cols = data.split("\t");
		
		assertEquals(2, cols.length);

        assertEquals("true",			cols[0].trim());
        assertEquals(expectedJson,		cols[1].trim());
	}
	
	@Test
	public void testCatalogWithProps() throws IOException, InterruptedException {
        String catRelativePath  = "src/test/resources/metadata/00-All_GRCh37.tsv.bgz";
        String catCanonicalPath = (new File(catRelativePath)).getCanonicalPath();
		
		String prevCmdHeader = 
				String.format("##BIOR=<ID=\"bior.dbSNP137\",Operation=\"bior_same_variant\",DataType=\"JSON\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p10\",Path=\"%s\">", catCanonicalPath); 
		
		String stdin =
				prevCmdHeader + "\n" +			
				"#bior.JSON_COL" + "\n" + 
				"{" +
					"\"INFO\": {" +
						"\"RSPOS\": 10145," +
						"\"dbSNPBuildID\": 134" +
						"}," +
				"}";	
		
		CommandOutput out = executeScript("bior_drill", stdin, "-p", "INFO.RSPOS", "-p", "INFO.dbSNPBuildID");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
        String[] headerLines = header.split("\n");
        assertEquals(4, headerLines.length);
        assertEquals(prevCmdHeader, headerLines[0]);		
		assertEquals(String.format("##BIOR=<ID=\"bior.dbSNP137.INFO.RSPOS\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"INFO.RSPOS\",FieldDescription=\"Chromosome position reported in dbSNP\",Path=\"%s\">", catCanonicalPath), headerLines[1]);
		assertEquals(String.format("##BIOR=<ID=\"dbSNP137.INFO.dbSNPBuildID\",Operation=\"bior_drill\",DataType=\"STRING\",Field=\"INFO.dbSNPBuildID\",FieldDescription=\"First dbSNP build for RS\",Path=\"%s\">", catCanonicalPath), headerLines[2]);
		assertEquals("#bior.dbSNP137.INFO.RSPOS\tbior.dbSNP137.INFO.dbSNPBuildID", headerLines[3]);

		// pull out just data rows		
		String data = out.stdout.replace(header, "");		
		String[] cols = data.split("\t");
		
		assertEquals(2, cols.length);

        assertEquals("10145",	cols[0].trim());
        assertEquals("134",		cols[1].trim());
	}	
}
