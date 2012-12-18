package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class DrillITCase extends BaseFunctionalTest {

	private final String jsonColumn = 
			"#JSON_COLUMN\n" + 
			"{" +
					"\"key1\":\"string_value1\"," +
					"\"key2\": true," +
					"\"key3\": 1" +
			"}";	
	
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
		
		// have JSON for STDIN
		String stdin = jsonColumn;
		
		CommandOutput out = executeScript("bior_drill.sh", stdin, "-p", "key1", "-p", "key2", "-p", "key3");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
		assertEquals("#key1\tkey2\tkey3\n", header);

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
		
		// have JSON for STDIN
		String stdin = jsonColumn;
		
		CommandOutput out = executeScript("bior_drill.sh", stdin, "-k", "-p", "key2");

		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
		assertEquals("#key2\tJSON_COLUMN\n", header);

		// pull out just data rows
		String data = out.stdout.replace(header, "");		
		
		// pull out just json
		String expectedJson = jsonColumn.replace(getHeader(jsonColumn), "");
		
		// JSON should be added as last column (4th)
		String[] cols = data.split("\t");
		
		assertEquals(2, cols.length);

        assertEquals("true",			cols[0].trim());
        assertEquals(expectedJson,		cols[1].trim());
	}	
}
