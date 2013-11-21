package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.PathReplace;
import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.bior.pipeline.Treat.TreatPipelineSingleThread;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.metadata.Metadata;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 * Functional tests for the BioR TREAT annotation module implementation.
 * 
 * @author duffp, mmeiners
 *
 */
public class TreatITCase extends RemoteFunctionalTest
{

    @Before
    public void cleanupBefore(){
        System.out.println("Make sure you have the required catalogs installed and in your path (or mounted over SMB) before you attempt to run the TREAT/ANNOTATE TESTS");
        History.clearMetaData();
    }

    @After
    public void cleanupAfter(){
        History.clearMetaData();
    }

    //REMEMBER TO CONNECT TO THE RCF BEFORE YOU TRY TO DO THIS COMMAND!
    //CONNECTING TO THE RCF DRIVES OVER SAMBA IS DONE ON A MAC BY GOING TO FINDER -> CONNECT TO SERVER THEN:
    //smb://rcfcluster-cifs/data5/bsi/
	@Test
	public void testPipeline_SubsetConfig() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testPipeline_SubsetConfig():");
		System.out.println("Annotate pipeline with a subset config file...");
		TreatPipeline annotatePipe = new TreatPipeline("src/test/resources/treat/configtest/smallSubset.config");
		Pipeline pipes = new Pipeline(
				new CatPipe(),
				new HistoryInPipe( new ArrayList(annotatePipe.getMetadata()) ),
				annotatePipe,
				new HistoryOutPipe(),
				new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/gold.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
		List<String> expected = splitLines(FileUtils.readFileToString(new File("src/test/resources/treat/configtest/smallSubset_output.tsv")));
        actual = PathReplace.replacePathDontCare(actual);
        expected = PathReplace.replacePathDontCare(expected);
		assertLinesEqual(expected, actual);
		System.out.println("<<<<<----------- Test passed -----");
	}

	// Test with 15 columns (8 regular, 1 format, 6 sample)
	@Test
	public void testPipeline_SubsetConfigManyCols() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testPipeline_SubsetConfigManyCols():");
		System.out.println("Annotate pipeline with a subset config file, but with additional columns beyond the INFO col...");
		TreatPipeline annotatePipe = new TreatPipeline("src/test/resources/treat/configtest/smallSubset.config");
		Pipeline pipes = new Pipeline(
				new HistoryInPipe( new ArrayList(annotatePipe.getMetadata()) ),
				annotatePipe,
				new HistoryOutPipe(),
				new PrintPipe()
				);
		List<String> input = Arrays.asList(
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	Sample1	Sample2	Sample3	Sample4	Sample5	Sample6",
				"1	216424275	rs696723	C	G	1.1	1.2	SomeVal=true	1.0	2.0	3.0	4.0	5.0	6.0",
				"1	10593296	rs2480782	G	T	2.1	2.2	SomeVal=false	1.0	2.0	3.0	4.0	5.0	6.0"
				);
		List<String> expected = Arrays.asList(
				"##BIOR=<ID=\"rsID\",Operation=\"bior_annotate\",DataType=\"String\",Field=\"ID\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\",FieldDescription=\"Semi-colon separated list of unique identifiers.  If this is a dbSNP variant, the rs number(s) should be used.  (VCF field)\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p5\",Description=\"NCBI's dbSNP Variant Database\",Path=\"/data5Local/bsi/catalogs/bior/v1/dbSNP/137/00-All_GRCh37.tsv.bgz\">",
				"##BIOR=<ID=\"dbSNP.SuspectRegion\",Operation=\"bior_annotate\",DataType=\"Integer\",Field=\"INFO.SSR\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\",FieldDescription=\"Variant Suspect Reason Codes (may be more than one value added together) 0 - unspecified, 1 - Paralog, 2 - byEST, 4 - oldAlign, 8 - Para_EST, 16 - 1kg_failed, 1024 - other\",ShortUniqueName=\"dbSNP137\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p5\",Description=\"NCBI's dbSNP Variant Database\",Path=\"/data5Local/bsi/catalogs/bior/v1/dbSNP/137/00-All_GRCh37.tsv.bgz\">",
				"##BIOR=<ID=\"dbSNP.ClinicalSig\",Operation=\"bior_annotate\",DataType=\"String\",Field=\"INFO.CLNSIG\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\",FieldDescription=\"Variant Clinical Significance, 0 - unknown, 1 - untested, 2 - non-pathogenic, 3 - probable-non-pathogenic, 4 - probable-pathogenic, 5 - pathogenic, 6 - drug-response, 7 - histocompatibility, 255 - other\",ShortUniqueName=\"dbSNP137_clinvar\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p5\",Description=\"Variations from common_all.vcf.gz that are clinical\",Path=\"/data5Local/bsi/catalogs/bior/v1/dbSNP/137/clinvar_20130226_GRCh37.tsv.bgz\">",
				"##BIOR=<ID=\"dbSNP.DiseaseVariant\",Operation=\"bior_annotate\",DataType=\"String\",Field=\"INFO.CLNDBN\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\",FieldDescription=\"Variant disease name\",ShortUniqueName=\"dbSNP137_clinvar\",Source=\"dbSNP\",Version=\"137\",Build=\"GRCh37.p5\",Description=\"Variations from common_all.vcf.gz that are clinical\",Path=\"/data5Local/bsi/catalogs/bior/v1/dbSNP/137/clinvar_20130226_GRCh37.tsv.bgz\">",
				"##BIOR=<ID=\"1000Genomes.EUR_AF\",Operation=\"bior_annotate\",DataType=\"Float\",Field=\"INFO.EUR_AF\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\",FieldDescription=\"Allele Frequency for samples from EUR based on AC/AN\",ShortUniqueName=\"1kG_3\",Source=\"1000Genomes\",Version=\"3\",Build=\"GRCh37\",Description=\"1000 Genomes Project goal is to find most genetic variants that have frequencies of at least 1% in the populations studied.\",Path=\"/data5Local/bsi/catalogs/bior/v1/1000_genomes/20110521/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37.tsv.bgz\">",
				"#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	Sample1	Sample2	Sample3	Sample4	Sample5	Sample6	rsID	dbSNP.SuspectRegion	dbSNP.ClinicalSig	dbSNP.DiseaseVariant	1000Genomes.EUR_AF",
				"1	216424275	rs696723	C	G	1.1	1.2	SomeVal=true	1.0	2.0	3.0	4.0	5.0	6.0	rs696723	0	1	.	0.01",
				"1	10593296	rs2480782	G	T	2.1	2.2	SomeVal=false	1.0	2.0	3.0	4.0	5.0	6.0	rs2480782	0	.	.	0.07"
				);
		pipes.setStarts(input);
		List<String> actual = PipeTestUtils.getResults(pipes);
        actual = PathReplace.replacePathDontCare(actual);
        expected = PathReplace.replacePathDontCare(expected);
		assertLinesEqual(expected, actual);
		System.out.println("<<<<<----------- Test passed -----");
	}

    //OMIM.ID
    //# Hgnc - Ensembl Id
    //Ensembl_Gene_ID
    //# VEP HGNC - UniprotId
    //    UniprotID
    //# dbSNP - rsId
    //    rsID
	@Test
	public void testPipeline_subsetWithDependencies() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException
	{
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testPipeline_subsetWithDependencies():");
		System.out.println("Annotate pipeline with columns that have dependencies on other data sources (and are in a different order in the config file vs what bior_annotate expects)...");
        TreatPipeline anno = new TreatPipeline("src/test/resources/treat/configtest/subset.config");
        String generatedCommand = anno.getGeneratedCommand();
        System.out.println(generatedCommand);
        Pipeline pipes = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(new ArrayList<Metadata>(anno.getMetadata())),
				anno,
				new HistoryOutPipe(),
                new GrepEPipe("##BIOR") //don't care about BIOR headers for this test
				//new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/gold.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
		System.out.println("Actual size: " + actual.size());
		List<String> expected = splitLines(FileUtils.readFileToString(new File("src/test/resources/treat/configtest/subset_output.tsv")));
		assertLinesEqual(expected, actual);
		System.out.println("<<<<<----------- Test passed -----");
	}
	

	@Test
	/** This test is mainly to check that fanout does not cause hangs on a huge number of fanout lines */
	public void testPipeline_rsIdOnly_10000() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, URISyntaxException
	{
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testPipeline_rsIdOnly_10000():");
		System.out.println("Annotate pipeline with a single output column (dbsnp rsId)...");
		TreatPipeline treatPipe = new TreatPipeline("src/test/resources/treat/configtest/dbsnpOnly.config");
		Pipeline pipes = new Pipeline(
				new CatPipe(),
				new HistoryInPipe( new ArrayList(treatPipe.getMetadata()) ),
				treatPipe,
				new HistoryOutPipe()
				//new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/10000.1tomany.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
		// Just check that we didn't get a hang (may want to verify the first and last lines match)
		System.out.println("Actual size: " + actual.size());
		assertEquals(10002, actual.size());
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/treat/expected/expected.10000.1tomany.out");
        this.compareListsNoHeader(expected, actual, false);
		System.out.println("<<<<<----------- Test passed -----");
	}

	
	
	@Test
    public void testCmd_WithSmallSubsetConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithSmallSubsetConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile (small subset)...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		
		String configFilePath = "src/test/resources/treat/configtest/smallSubset.config";
		
		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-m", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);
		
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/smallSubset_output.tsv"));
        List<String> expectedList = splitLines(expected);
        List<String> actualList = splitLines(out.stdout);
		compareListsNoHeader(expectedList, actualList, true);
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		System.out.println("<<<<<----------- Test passed -----");
    }
	
	@Test
    public void testCmd_smallSubsetConfig_statusFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_smallSubsetConfig_statusFile():");
		System.out.println("AnnotateCommand With ConfigFile (small subset), and requesting status written to a file at end...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		
		String configFilePath = "src/test/resources/treat/configtest/smallSubset.config";
		File statusFile       = new File("src/test/resources/treat/temp/status.out");
		if( statusFile.exists() )
			statusFile.delete();
		
		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-c", configFilePath, "-s", statusFile.getCanonicalPath()); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);

		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/smallSubset_output.tsv"));
		compareListsNoHeader(splitLines(expected), splitLines(out.stdout), true);
		
		// Also compare the status output
		String actualStatus = FileUtils.readFileToString(statusFile);
		String expectedStatus = 
					 "numLinesIn=25\n"
				+ 	 "numLinesOut=25\n"
				+ 	 "numLinesBadData=0\n"
				+ 	 "isSuccessful=true\n";
		assertEquals(expectedStatus, actualStatus);
		if( statusFile.exists() )
			statusFile.delete();
		
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		System.out.println("<<<<<----------- Test passed -----");
    }

	
	@Test
    public void testCmd_WithSmallSubsetDependenciesConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithSmallSubsetDependenciesConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile (small subset with some data sources dependent on others before it)...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		
		String configFilePath = "src/test/resources/treat/configtest/subset.config";
		
		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-m", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);

		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/subset_output.tsv"));
        compareListsNoHeader(splitLines(expected), splitLines(out.stdout), true);
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		System.out.println("<<<<<----------- Test passed -----");
    }

	@Test
    public void testCmd_WithAllConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithAllConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		
		String configFilePath = "src/test/resources/treat/configtest/all.config";
		
		// execute command with config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-m", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);

		//assertLinesEqual(splitLines(expected), splitLines(out.stdout));
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/gold_output.tsv"));
        compareListsNoHeader(splitLines(expected),splitLines(out.stdout),true);
		System.out.println("<<<<<----------- Test passed -----");
    }
	
    @Test
    public void testCmd_NoConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_NoConfigFile():");
		System.out.println("AnnotateCommand Without ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		
		// execute command with NO config file option - default (and as multi-process)
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-m");

		if (out.exit != 0)
			fail(out.stderr);
		
		//assertLinesEqual(splitLines(expected), splitLines(out.stdout));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/gold_output.tsv"));
        compareListsNoHeader(splitLines(expected),splitLines(out.stdout),true);
		System.out.println("<<<<<----------- Test passed -----");
    }
    
	/** empty config file with no columns - expect an error message*/
    @Test
    public void testEmptyConfigFile() throws IOException, InterruptedException {
		System.out.println("\n-------------------------------------------------------->>>>>");
        System.out.println("Testing: testEmptyConfigFile(): AnnotateCommand ConfigFile - empty config file");

        String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
        String configFilePath = "src/test/resources/treat/configtest/empty.config";

        // execute command with config file option - default (and as multi-process)
        CommandOutput out = executeScript("bior_annotate", goldInput, "-m", "-c", configFilePath); //with 'config' option
        assertEquals(out.stderr, 1, out.exit);
        assertTrue(out.stderr.contains("does not exist (or is empty). Please specify a valid config file path."));
		System.out.println("<<<<<----------- Test passed -----");
    }

	/** invalid columns in the config file  - expect an error message */
    @Test
    public void testAllInvalidColumns() throws IOException, InterruptedException {
		System.out.println("\n-------------------------------------------------------->>>>>");
        System.out.println("Testing: testAllInvalidColumns(): AnnotateCommand ConfigFile - all invalid columns");

        String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
        String configFilePath = "src/test/resources/treat/configtest/all_invalid.config";

        // execute command with config file option - default (and as multi-process)
        CommandOutput out = executeScript("bior_annotate", goldInput, "-m", "-c", configFilePath); //with 'config' option
        assertEquals(out.stderr, 1, out.exit);
        assertTrue(out.stderr.contains("these columns specified in the config file are not recognized:\n    INVALID1\n    INVALID2\n    INVALID3"));
		System.out.println("<<<<<----------- Test passed -----");
    }
    
	/** some invalid columns in the config file - expect an error message */
    @Test
    public void testSomeInvalidColumns() throws IOException, InterruptedException {
		System.out.println("\n-------------------------------------------------------->>>>>");
        System.out.println("Testing: testSomeInvalidColumns(): AnnotateCommand ConfigFile - some columns are invalid");

        String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
        String configFilePath = "src/test/resources/treat/configtest/some_invalid.config";

        // execute command with config file option - default (and as multi-process)
        CommandOutput out = executeScript("bior_annotate", goldInput, "-m", "-c", configFilePath); //with 'config' option
        assertEquals(out.stderr, 1, out.exit);
        assertTrue(out.stderr.contains("these columns specified in the config file are not recognized:\n    INVALID1\n    INVALID2"));
		System.out.println("<<<<<----------- Test passed -----");
    }
    
    @Test
    public void testSplit() throws IOException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testSplit():");
    	String str = "line1\nline2\r\nline3\nline4";
    	List<String> expected = Arrays.asList("line1", "line2", "line3", "line4");
    	assertEquals(expected, splitLines(str));
		System.out.println("<<<<<----------- Test passed -----");
    }
    
	public static List<String> splitLines(String s) throws IOException {
		return Arrays.asList(s.split("\r\n|\n|\r"));
	}
	
	
	
	/** Compare all lines and columns.
	 * If there are '*' characters in the expected output, then don't compare these */
	public static void assertLinesEqual(List<String> expected, List<String> actual) {
		int numDiffs = 0;
		System.out.println("# Lines: (expected: " + expected.size() + ", actual: " + actual.size() + ")");
		for(int i=0; i < Math.max(expected.size(), actual.size()); i++) {
			String lineExpect = expected.size() > i ? expected.get(i) : "";
			String lineActual = actual.size()   > i ? actual.get(i)   : "";
			
			boolean foundColMismatch = false;
			
			int maxCols = Math.max(lineExpect.split("\t").length, lineActual.split("\t").length);
			String[] expectCols = lineExpect.split("\t", maxCols);
			String[] actualCols = lineActual.split("\t", maxCols);
			StringBuilder expectedStr = new StringBuilder("Expected: ");
			StringBuilder actualStr   = new StringBuilder("Actual:   ");
			StringBuilder diffStr     = new StringBuilder("Diff:     ");
			for(int j = 0; j < maxCols; j++) {
				String expCol = expectCols.length > j ? expectCols[j] : "";
				String actCol = actualCols.length > j ? actualCols[j] : "";

				int maxLen = Math.max(expCol.length(), actCol.length());
				
				boolean isEqual = "*".equals(expCol) || expCol.equals(actCol);
				if( ! isEqual ) 
					foundColMismatch = true; 
				
				// Expected, Actual, Diff should all be same length (add 2 extra spaces to end)
				expectedStr.append(expCol + StringUtils.repeat(" ", (maxLen+2)-expCol.length()));
				actualStr.append(  actCol + StringUtils.repeat(" ", (maxLen+2)-actCol.length()));
				diffStr.append(	   StringUtils.repeat( (isEqual ? " " : "^"), maxLen) + "  " );
			}
			
			if(foundColMismatch)
			{
				numDiffs++;
				System.out.println("--- Line " + (i+1) + " - Diff -----------------------------------");
				System.out.println(expectedStr);
				System.out.println(actualStr);
				System.out.println(diffStr);
				System.out.println("Actl/tab: " + lineActual);
			}
		}
		if(numDiffs == 0)
			System.out.println("  (All lines are the same)");
		else
			System.out.println("  (# of lines different: " + numDiffs + ")");
		
		assertEquals(
			"Number of differences between actual and expected (out of " + Math.max(expected.size(), actual.size()) + ")  ",
			0, numDiffs);
	}
	
	public static String getLineDiff(String lineExpect, String lineActual, int lineNum) {
		final String EOL = System.getProperty("line.separator");
		if(lineExpect == null && lineActual == null) 
			return ""; // OK - both null
		else if( lineExpect == null || lineActual == null) {
			return  "--- Line " + lineNum + " - Diff ---" + EOL
				+  	"Expected: " + (lineExpect == null ? "(null)" : lineExpect) + EOL
				+ 	"Actual:   " + (lineActual == null ? "(null)" : lineActual) + EOL;
		}
		
		StringBuilder diff = new StringBuilder();
		int maxCols = Math.max(lineExpect.split("\t").length, lineActual.split("\t").length);
		String[] expectCols = lineExpect.split("\t", maxCols);
		String[] actualCols = lineActual.split("\t", maxCols);
		diff.append("--- Line " + lineNum + " - Diff ---" + EOL);
		StringBuilder expectedStr = new StringBuilder("Expected: ");
		StringBuilder actualStr   = new StringBuilder("Actual:   ");
		StringBuilder diffStr     = new StringBuilder("Diff:     ");
		for(int j = 0; j < maxCols; j++) {
			String expCol = expectCols.length > j ? expectCols[j] : "";
			String actCol = actualCols.length > j ? actualCols[j] : "";
			String delim = j < maxCols-1 ? "\t" : "";
			expectedStr.append(expCol + delim);
			actualStr.append(  actCol + delim);
			int maxLen = Math.max(expCol.length(), actCol.length());
			int numDelims = (maxLen / 8) + 1;
			boolean isEqual = "*".equals(expCol) || expCol.equals(actCol);
			diffStr.append( isEqual ? StringUtils.repeat("\t", numDelims) : StringUtils.repeat("^", maxLen)+"\t" );
		}
		diff.append(expectedStr + EOL);
		diff.append(actualStr + EOL);
		diff.append("Actl/tab: " + lineActual + EOL);
		diff.append(diffStr + EOL);

		return diff.toString();
	}

    /**
     * In some cases, we don't want to test if annotate header lines are correct, so we remove them in both expected and output before we compare output
	   @param isIgnoreBiorHeaderLinesOnly  If true, ignores only lines starting with ##BIOR.  If false, ignores ALL header lines (those beginning with #)
    */
    public static void compareListsNoHeader(List<String> expectedIn, List<String> resultsIn, boolean isIgnoreBiorHeaderLinesOnly){
        String prefixToIgnore = "#";
        if(isIgnoreBiorHeaderLinesOnly) 
        	prefixToIgnore = "##BIOR";
        
        List<String> expected = removeLinesStartingWith(expectedIn, prefixToIgnore);
        List<String> results  = removeLinesStartingWith(resultsIn, prefixToIgnore);

        assertEquals("Lists are not the same size (after removing header lines)", expected.size(), results.size());

        for(int i=0; i < expected.size(); i++) {
            assertEquals("Lines were not equal.  (Line " + (i+1) + "):"
            		+ "\nExpected: " + expected.get(i)
            		+ "\nActual:   " + results.get(i),
            		expected.get(i), results.get(i));
        }
    }
    
    /** Creates a new list that does not contain any of the lines starting with prefix */
    private static List<String> removeLinesStartingWith(List<String> lines, String prefix) {
    	List<String> out = new ArrayList<String>();
    	for(String line : lines) {
    		if( ! line.startsWith(prefix) )
    			out.add(line);
    	}
    	return out;
    }

}
