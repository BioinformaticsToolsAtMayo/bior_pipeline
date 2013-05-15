package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;

/**
 * Functional tests for the BioR TREAT annotation module implementation.
 * 
 * @author duffp
 *
 */
public class TreatITCase extends RemoteFunctionalTest
{

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
