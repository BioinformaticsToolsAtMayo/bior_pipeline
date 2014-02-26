package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.mayo.pipes.util.test.PipeTestUtils;

public class DrillITCase extends BaseFunctionalTest {

	// header line added from running command prior to drill
	private final String prevCmdHeader = 
			"##BIOR=<ID=\"bior.JSON_COL\",Operation=\"bior_same_variant\",DataType=\"JSON\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">"; 
	
	private final String stdin =
			prevCmdHeader + "\n" +			
			"#bior.JSON_COL" + "\n" + 
			"{" +
					"\"key1\":\"string_value1\"," +
					"\"key2\":true," +
					"\"key3\":1" +
			"}";	
	
	
	private final String[] headerMeta = {
	  prevCmdHeader,
      "##BIOR=<ID=\"bior.JSON_COL.key1\",Operation=\"bior_drill\",Field=\"key1\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
	  "##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",Field=\"key2\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
	  "##BIOR=<ID=\"bior.JSON_COL.key3\",Operation=\"bior_drill\",Field=\"key3\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
	};
	
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testNormalPath");
        CommandOutput out = executeScript("bior_drill", stdin, "-p", "key1", "-p", "key2", "-p", "key3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected = Arrays.asList(
				headerMeta[0], headerMeta[1], headerMeta[2], headerMeta[3], 
				"#bior.JSON_COL.key1\tbior.JSON_COL.key2\tbior.JSON_COL.key3",
				"string_value1\ttrue\t1"
				);
		PipeTestUtils.assertListsEqual(expected, actual);
	}	

	@Test
	public void testKeepJson() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testKeepJson");
        CommandOutput out = executeScript("bior_drill", stdin, "-k", "-p", "key3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected = Arrays.asList(
				headerMeta[0], headerMeta[3], 
				"#bior.JSON_COL.key3\tbior.JSON_COL",
				"1\t{\"key1\":\"string_value1\",\"key2\":true,\"key3\":1}"
				);
		PipeTestUtils.assertListsEqual(expected, actual);
	}
	
	@Test
	public void testCatalogWithProps() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testCatalogWithProps");
        String catRelativePath  = "src/test/resources/metadata/00-All_GRCh37.tsv.bgz";
        String catCanonicalPath = (new File(catRelativePath)).getCanonicalPath();
		
		String prevCmdHeader = 
				String.format("##BIOR=<ID=\"bior.dbSNP137\",Operation=\"bior_same_variant\",DataType=\"JSON\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p10\",Path=\"%s\">", catCanonicalPath); 
		
		String stdin =
				prevCmdHeader + "\n" +			
				"#bior.dbSNP137" + "\n" + 
				"{" +
					"\"INFO\":{" +
						"\"RSPOS\":10145," +
						"\"dbSNPBuildID\":134" +
						"}" +
				"}";	
				
		CommandOutput out = executeScript("bior_drill", stdin, "-p", "INFO.RSPOS", "-p", "INFO.dbSNPBuildID");
		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected=Arrays.asList(
				prevCmdHeader,
				String.format("##BIOR=<ID=\"bior.dbSNP137.INFO.RSPOS\",Operation=\"bior_drill\",Field=\"INFO.RSPOS\",DataType=\"Integer\",Number=\"1\",FieldDescription=\"Chromosome position reported in dbSNP\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p10\",Path=\"%s\">", catCanonicalPath),
				String.format("##BIOR=<ID=\"bior.dbSNP137.INFO.dbSNPBuildID\",Operation=\"bior_drill\",Field=\"INFO.dbSNPBuildID\",DataType=\"Integer\",Number=\"1\",FieldDescription=\"First dbSNP build for RS\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p10\",Path=\"%s\">", catCanonicalPath),
				"#bior.dbSNP137.INFO.RSPOS\tbior.dbSNP137.INFO.dbSNPBuildID",
				"10145\t134"
				);
		PipeTestUtils.assertListsEqual(expected, actual);
	}	
	
	
	@Test
    /** Test multiple JSON paths drilled, positive column # for the JSON */
    public void testMultiPath_PosCol() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testMultiPath_PosCol(): Test multiple JSON paths drilled, specifying a positive column number");
        
        
    	final String STDIN = prevCmdHeader + "\n"
        			+ "#CHROM\tPOS\tbior.JSON_COL\tINFO\n"
        			+ "1\t100\t{\"key1\":11,\"key2\":22,\"key3\":33}\tsomeInfo";	

        CommandOutput out = executeScript("bior_drill", STDIN, "-p", "key1", "-p", "key2", "-p", "key3", "-c", "3", "--log");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected = Arrays.asList(
				prevCmdHeader,
				"##BIOR=<ID=\"bior.JSON_COL.key1\",Operation=\"bior_drill\",Field=\"key1\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",Field=\"key2\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key3\",Operation=\"bior_drill\",Field=\"key3\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"#CHROM\tPOS\tINFO\tbior.JSON_COL.key1\tbior.JSON_COL.key2\tbior.JSON_COL.key3",
				"1\t100\tsomeInfo\t11\t22\t33"
				);
		PipeTestUtils.assertListsEqual(expected, actual);
	}	

	@Test
    /** Test multiple JSON paths drilled, negative column # for the JSON */
    public void testMultiPath_NegCol() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testMultiPath_NegCol(): Test multiple JSON paths drilled, specifying a negative column number");
        
        
    	final String STDIN = prevCmdHeader + "\n"
        			+ "#CHROM\tPOS\tbior.JSON_COL\tINFO\n"
        			+ "1\t100\t{\"key1\":11,\"key2\":22,\"key3\":33}\tsomeInfo";	

        CommandOutput out = executeScript("bior_drill", STDIN, "-p", "key1", "-p", "key2", "-p", "key3", "-c", "-2", "--log");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected = Arrays.asList(
				prevCmdHeader,
				"##BIOR=<ID=\"bior.JSON_COL.key1\",Operation=\"bior_drill\",Field=\"key1\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",Field=\"key2\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key3\",Operation=\"bior_drill\",Field=\"key3\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"#CHROM\tPOS\tINFO\tbior.JSON_COL.key1\tbior.JSON_COL.key2\tbior.JSON_COL.key3",
				"1\t100\tsomeInfo\t11\t22\t33"
				);
		PipeTestUtils.assertListsEqual(expected, actual);

	}
	
	@Test
    /** Test multiple JSON paths drilled, negative column # for the JSON, and keep the JSON column (which will remove it from its current position and append it on the end) */
    public void testMultiPath_NegCol_keepJson() throws IOException, InterruptedException {
        System.out.println("DrillITCase.testMultiPath_NegCol_keepJson(): Test multiple JSON paths drilled, specifying a negative column number, and keeping the JSON column");
        
        
    	final String STDIN = prevCmdHeader + "\n"
        			+ "#CHROM\tPOS\tbior.JSON_COL\tINFO\n"
        			+ "1\t100\t{\"key1\":11,\"key2\":22,\"key3\":33}\tsomeInfo";	

        CommandOutput out = executeScript("bior_drill", STDIN, "-p", "key1", "-p", "key2", "-p", "key3", "-c", "-2", "-k", "--log");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		List<String> actual = Arrays.asList(out.stdout.split("\n"));
		List<String> expected = Arrays.asList(
				prevCmdHeader,
				"##BIOR=<ID=\"bior.JSON_COL.key1\",Operation=\"bior_drill\",Field=\"key1\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key2\",Operation=\"bior_drill\",Field=\"key2\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"##BIOR=<ID=\"bior.JSON_COL.key3\",Operation=\"bior_drill\",Field=\"key3\",DataType=\"String\",Number=\".\",FieldDescription=\"\",ShortUniqueName=\"JSON_COL\",Path=\"/path/to/catalog\">",
				"#CHROM\tPOS\tINFO\tbior.JSON_COL.key1\tbior.JSON_COL.key2\tbior.JSON_COL.key3\tbior.JSON_COL",
				"1\t100\tsomeInfo\t11\t22\t33\t{\"key1\":11,\"key2\":22,\"key3\":33}"
				);
		PipeTestUtils.assertListsEqual(expected, actual);

	}	
}
