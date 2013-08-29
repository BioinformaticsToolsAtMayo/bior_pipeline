package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;

public class BED2JSONCommandITCase extends BaseFunctionalTest
{
	@Test
	public void test() throws IOException, InterruptedException
	{
        System.out.println("BED2JSONCommandITCasetest");
        String stdin = "chr22	1000	5000	cloneA	960	+	1000	5000	0	2	567,488,	0,3512" + "\n";
		
		CommandOutput out = executeScript("bior_bed_to_tjson", stdin);

		assertEquals("STDERR:"+out.stderr+"\n"+"STDOUT:"+out.stdout, 0, out.exit);
		assertEquals("", out.stderr);

		String header = getHeader(out.stdout);
		assertEquals(
				"##BIOR=<ID=\"bior.ToTJson\",Operation=\"bior_bed_to_tjson\",DataType=\"JSON\",ShortUniqueName=\"ToTJson\">" + "\n" +
 				"#chrom	chromStart	chromEnd	name	score	strand	thickStart	thickEnd	itemRgb	blockCount	blockSizes	blockStarts	bior.ToTJson" + "\n", 
				header);
		
		// pull out just data rows
		String data = out.stdout.replace(header, "");
		
		// JSON should be added as last column (9th)
		String[] cols = data.split("\t");
		assertEquals(13, cols.length);
		
		String json = cols[cols.length - 1];
		System.out.println(json);
		
        assertEquals("chr22",	JsonPath.compile("chrom").read(json));
        assertEquals("1000",	JsonPath.compile("chromStart").read(json));
        assertEquals("5000",	JsonPath.compile("chromEnd").read(json));
        assertEquals("cloneA",	JsonPath.compile("name").read(json));
        assertEquals("960",		JsonPath.compile("score").read(json));
        assertEquals("+",		JsonPath.compile("strand").read(json));
        assertEquals("1000",	JsonPath.compile("thickStart").read(json));
        assertEquals("5000",	JsonPath.compile("thickEnd").read(json));
        assertEquals("0",		JsonPath.compile("itemRgb").read(json));
        assertEquals("2",		JsonPath.compile("blockCount").read(json));
        assertEquals("567,488,",	JsonPath.compile("blockSizes").read(json));
        assertEquals("0,3512",	JsonPath.compile("blockStarts").read(json));
	}
}
