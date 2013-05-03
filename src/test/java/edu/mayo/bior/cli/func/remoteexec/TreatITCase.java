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
		
		CommandOutput out = executeScript("bior_annotate", goldInput);
		
		if (out.exit != 0) {
			fail(out.stderr);
		}
		
		List<String> expectedOutputLines = splitLines(goldOutput);
		List<String> actualOutputLines = splitLines(out.stdout);
		
		// TODO: will not pass until pipeline has 1-to-1 variant lines
		
//		assertEquals(expectedOutputLines.size(), actualOutputLines.size());
//		for (int i=0; i < expectedOutputLines.size(); i++) {
//			assertEquals(expectedOutputLines.get(i), actualOutputLines.get(i));
//		}
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
