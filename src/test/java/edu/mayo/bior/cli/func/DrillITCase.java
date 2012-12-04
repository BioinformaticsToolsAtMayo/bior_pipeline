package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DrillITCase extends BaseFunctionalTest {

	private final String json = 
			"{" +
					"\"key1\":\"string_value1\"," +
					"\"key2\": true," +
					"\"key3\": 1" +
			"}";	
	
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = json;
		
		CommandOutput out = executeScript("bior_drill.sh", stdin, "-p", "key1", "-p", "key2", "-p", "key3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String[] cols = out.stdout.split("\t");
		
		assertEquals(3, cols.length);

        assertEquals("string_value1",	cols[0].trim());
        assertEquals("true",			cols[1].trim());
        assertEquals("1",				cols[2].trim());
	}	

	@Test
	public void testKeepJson() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = json;
		
		CommandOutput out = executeScript("bior_drill.sh", stdin, "-k", "-p", "key2");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		// JSON should be added as last column (4th)
		String[] cols = out.stdout.split("\t");
		
		assertEquals(2, cols.length);

        assertEquals("true",			cols[0].trim());
        assertEquals(json,				cols[1].trim());
	}	
}
