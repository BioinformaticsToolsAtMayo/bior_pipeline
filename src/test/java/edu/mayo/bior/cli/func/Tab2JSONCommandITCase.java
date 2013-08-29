package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class Tab2JSONCommandITCase extends BaseFunctionalTest
{
	@Test
	public void test() throws IOException, InterruptedException
	{
        System.out.println("Tab2JSONCommandITCase.test");
        String config =
				"1	COL1	STRING	COLUMN	.	. " + "\n" +
				"2	COL2	NUMBER	COLUMN	.	. " + "\n" +
				"3	COL3	BOOLEAN	COLUMN	.	. " + "\n";
		
		File configFile = File.createTempFile("config", ".txt");
		configFile.deleteOnExit();
		FileUtils.write(configFile, config);
		
		String stdin = 
				"#COL1	COL2	COL3" + "\n" +
				"AAAA	1111	true";

		CommandOutput out = executeScript("bior_tab_to_tjson", stdin, "--config", configFile.getAbsolutePath());

		assertEquals("STDERR:"+out.stderr+"\n"+"STDOUT:"+out.stdout, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
		assertEquals(
				"##BIOR=<ID=\"bior.ToTJson\",Operation=\"bior_tab_to_tjson\",DataType=\"JSON\",ShortUniqueName=\"ToTJson\">" + "\n" +
 				"#COL1	COL2	COL3	bior.ToTJson" + "\n", 
				header);
		
		// pull out just data rows
		String data = out.stdout.replace(header, "");
		
		// JSON should be added as last column (9th)
		String[] cols = data.split("\t");
		assertEquals(4, cols.length);
		
		String json = cols[cols.length - 1];
		System.out.println(json);
		
        assertEquals(new String("AAAA"),	JsonPath.compile("COL1").read(json));
        assertEquals(new Integer("1111"),	JsonPath.compile("COL2").read(json));
        assertEquals(new Boolean(true),		JsonPath.compile("COL3").read(json));
	}
}
