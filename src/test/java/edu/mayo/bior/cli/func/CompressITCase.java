package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class CompressITCase extends BaseFunctionalTest
{
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1\tA\n" +
				"dataA\t2\tB\n" +
				"dataA\t3\tC\n" +
				"dataB\t100\tW\n" +
				"dataB\t101\tX\n" +
				"dataC\t333\tZ\n";

		String expected = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1|2|3\tA|B|C\n" +
				"dataB\t100|101\tW|X\n" +
				"dataC\t333\tZ\n";
		
		CommandOutput out = executeScript("bior_compress", stdin, "2,3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		assertEquals(expected, out.stdout);
	}	

	@Test
	public void testSeparator() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1\tA\n" +
				"dataA\t2\tB\n" +
				"dataA\t3\tC\n";
		String expected = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1,2,3\tA,B,C\n";
		
		CommandOutput out = executeScript("bior_compress", stdin,"--separator", ",", "2,3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		assertEquals(expected, out.stdout);
	}
	
	@Test
	public void testEscape() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\n" +
				"dataA\t1,A\n" +
				"dataA\t2,B\n" +
				"dataA\t3,C\n";
		String expected = 
				"#COL1\tCOL2\n" +
				"dataA\t1#A,2#B,3#C\n";
		
		CommandOutput out = executeScript("bior_compress", stdin,"--separator", ",", "--escape", "#", "2");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		assertEquals(expected, out.stdout);
	}
	
	@Test
	public void testReverse() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\n" +
				"dataA\t1\n" +
				"dataA\t2\n" +
				"dataB\t3\n";
		String expected = 
				"#COL1\tCOL2\n" +
				"dataA\t1|2\n" +
				"dataB\t3\n";
		
		// pick field 1 (RIGHT_TO_LEFT), so that's actually the 2nd column as the compress column
		CommandOutput out = executeScript("bior_compress", stdin, "--reverse", "1");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		assertEquals(expected, out.stdout);
	}	
	
}
