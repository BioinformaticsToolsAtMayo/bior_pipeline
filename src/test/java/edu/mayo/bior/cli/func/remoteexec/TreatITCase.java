package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.bior.pipeline.Treat.TreatPipeline;
import edu.mayo.exec.AbnormalExitException;
import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.HistoryInPipe;
import edu.mayo.pipes.history.HistoryOutPipe;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 * Functional tests for the BioR TREAT annotation module implementation.
 * 
 * @author duffp
 *
 */
public class TreatITCase extends RemoteFunctionalTest
{
	
	@Test
	public void treatPipeline() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		Pipeline pipes = new Pipeline(
				new CatPipe(),
				new HistoryInPipe(),
				new TreatPipeline(),//"src/test/resources/treat/configtest/subset.config"),
				new HistoryOutPipe(),
				new PrintPipe()
				);
		pipes.setStarts(Arrays.asList("src/test/resources/treat/gold.vcf"));
		List<String> actual = PipeTestUtils.getResults(pipes);
	}

	@Test
	public void gold() throws IOException, InterruptedException
	{
		String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String goldOutput = FileUtils.readFileToString(new File("src/test/resources/treat/gold_output.tsv"));
		
		CommandOutput out = executeScript("bior_annotate", goldInput, "--log");

		// check that command completed successfully
		if (out.exit != 0)
		{
			fail(out.stderr);
		}
		
		List<String> expectedLines = splitLines(goldOutput);
		List<String> actualLines = splitLines(out.stdout);

		// check number of rows
		assertEquals(
			String.format("# of rows did not match.", expectedLines.size(), actualLines.size()), 
			expectedLines.size(),
			actualLines.size());
		
		for (int i=0; i < expectedLines.size(); i++)
		{
			final int lineNum = i + 1;
			
			String expectedLine = expectedLines.get(i);
			String actualLine   = actualLines.get(i);
			
			String[] expectedCols = expectedLine.split("\t");
			String[] actualCols   = actualLine.split("\t");
			
			// check number of columns
			assertEquals(
				String.format("# of columns did not match for line %s.", lineNum, expectedCols.length, actualCols.length), 
				expectedCols.length,
				actualCols.length);
			
			// compare column-by-column
			for (int col=0; col < expectedCols.length; col++)
			{
				final int colNum = col + 1;
				
				String expectedVal = expectedCols[col];
				String actualVal   = actualCols[col]; 
				
				// compare ONLY if the value is not "*"
				if (expectedVal.equals("*") == false)
				{
					// compare column values
					assertEquals(
						String.format(
								"Values did not match for line %s, column %s.\n" + "expected line: %s\n" + "actual line: %s\n",
								lineNum, colNum, expectedLine, actualLine),
						expectedVal,
						actualVal);
				}
			}
		}
	}
	
	//@Test
    public void testAnnotateCommandWithConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
    	System.out.println("Testing: AnnotateCommand With ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/default_output.tsv"));
		
		String configFilePath = "src/test/resources/treat/configtest/all.config";
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput, "-c", configFilePath); //with 'config' option

		if (out.exit != 0) {
			fail(out.stderr);
		}
		
		assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);
		System.out.println("header="+header);
    }
	
    //@Test
    public void testAnnotateCommandWithoutConfigFile() throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException, InvalidDataException {
    	System.out.println("Testing: AnnotateCommand Without ConfigFile...");
    	String goldInput  = FileUtils.readFileToString(new File("src/test/resources/treat/gold.vcf"));
		String output = FileUtils.readFileToString(new File("src/test/resources/treat/configtest/default_output.tsv"));
		
		// execute command with config file option - default
		CommandOutput out = executeScript("bior_annotate", goldInput); //with 'config' option

		if (out.exit != 0) {
			fail(out.stderr);
		}
		
		assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);

        String header = getHeader(out.stdout);
		System.out.println("header="+header);
    }
    
	private List<String> splitLines(String s) throws IOException
	{
		List<String> lines = new ArrayList<String>();
		
		BufferedReader br = new BufferedReader(new StringReader(s));
		String line = br.readLine();
		while (line != null)
		{
			lines.add(line);
			line = br.readLine();
		}
		br.close();
		
		return lines;
	}	
}
