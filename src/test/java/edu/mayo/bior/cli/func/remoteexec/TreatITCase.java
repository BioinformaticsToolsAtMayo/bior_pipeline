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

import edu.mayo.bior.cli.PathReplace;
import edu.mayo.bior.util.ClasspathUtil;
import edu.mayo.pipes.UNIX.GrepEPipe;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.util.metadata.Metadata;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
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
				new HistoryInPipe( new ArrayList(annotatePipe.getMetdata()) ),
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
				new HistoryInPipe(new ArrayList<Metadata>(anno.getMetdata())),
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
				new HistoryInPipe( new ArrayList(treatPipe.getMetdata()) ),
				treatPipe,
				new HistoryOutPipe()
				//new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/10000.1tomany.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
		// Just check that we didn't get a hang (may want to verify the first and last lines match)
		System.out.println("Actual size: " + actual.size());
		assertEquals(10002, actual.size());
		List<String> linesIn = FileCompareUtils.loadFile("src/test/resources/treat/10000.1tomany.vcf");
        this.compareListsNoHeader(actual, linesIn,false);
		System.out.println("<<<<<----------- Test passed -----");
	}

	
	
	@Test
    public void testCmd_WithSmallSubsetConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithSmallSubsetConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile (small subset)...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/smallSubset_output.tsv"));
		
		String configFilePath = "src/test/resources/treat/configtest/smallSubset.config";
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);
        List<String> e = splitLines(expected);
        List<String> s = splitLines(out.stdout);
		compareListsNoHeader(e, s, true);
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		System.out.println("<<<<<----------- Test passed -----");
    }
	

	@Test
    public void testCmd_WithSmallSubsetDependenciesConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithSmallSubsetDependenciesConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile (small subset with some data sources dependent on others before it)...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/subset_output.tsv"));
		
		String configFilePath = "src/test/resources/treat/configtest/subset.config";
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);
        List<String> e = splitLines(expected);
        List<String> r = splitLines(out.stdout);
        compareListsNoHeader(e,r,true);
		//assertMatch(splitLines(expected), splitLines(out.stdout));
		System.out.println("<<<<<----------- Test passed -----");
    }

	@Test
    public void testCmd_WithAllConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_WithAllConfigFile():");
		System.out.println("AnnotateCommand With ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/gold_output.tsv"));
		
		String configFilePath = "src/test/resources/treat/configtest/all.config";
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l", "-c", configFilePath); //with 'config' option

		// TEMP - dump to file to look at output later
		//FileUtils.write(new File("treatAllColsConfig.tsv"), out.stdout);

		if (out.exit != 0)
			fail(out.stderr);

		//assertLinesEqual(splitLines(expected), splitLines(out.stdout));
		//assertMatch(splitLines(expected), splitLines(out.stdout));
        compareListsNoHeader(splitLines(expected),splitLines(out.stdout),true);
		System.out.println("<<<<<----------- Test passed -----");
    }
	
    @Test
    public void testCmd_NoConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
		System.out.println("\n-------------------------------------------------------->>>>>");
		System.out.println("Testing: testCmd_NoConfigFile():");
		System.out.println("AnnotateCommand Without ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String expected = FileUtils.readFileToString(new File("src/test/resources/treat/gold_output.tsv"));
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput, "-l"); //with 'config' option

		if (out.exit != 0)
			fail(out.stderr);
		
		//assertLinesEqual(splitLines(expected), splitLines(out.stdout));
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

        // execute command with config file option - default
        CommandOutput out = executeScript("bior_annotate", goldInput, "-c", configFilePath); //with 'config' option
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

        // execute command with config file option - default
        CommandOutput out = executeScript("bior_annotate", goldInput, "-c", configFilePath); //with 'config' option
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

        // execute command with config file option - default
        CommandOutput out = executeScript("bior_annotate", goldInput, "-c", configFilePath); //with 'config' option
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
    
	private List<String> splitLines(String s) throws IOException {
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
    */
    public void compareListsNoHeader(List<String> expected, List<String> results, boolean biorLinesOnly){
        String headerSignature = "#";
        if(biorLinesOnly) headerSignature = "##BIOR";
        int i = 0;
        int j = 0;
        while( i< expected.size() && j<results.size()){
            String e = expected.get(i);
            String r = expected.get(j);
            while(e.startsWith(headerSignature)){
                i++;
                e = expected.get(i);
            }
            while(r.startsWith(headerSignature)){
                j++;
                r = expected.get(j);
            }
            assertEquals(e,r);
            i++;
            j++;
        }

    }

}
