package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.UNIX.CatPipe;

public class CompressITCase extends BaseFunctionalTest
{
	
	@Test
	public void testNormalPath() throws IOException, InterruptedException {
        System.out.println("CompressITCase.testNormalPath");
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
				"##BIOR=<ID=\"COL2\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\">\n" +
				"##BIOR=<ID=\"COL3\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\">\n" +
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1|2|3\tA|B|C\n" +
				"dataB\t100|101\tW|X\n" +
				"dataC\t333\tZ\n";
		
		CommandOutput out = executeScript("bior_compress", stdin, "2,3", "--log");

		assertEquals("Exit code was not zero. " + out.stderr, 0, out.exit);
		assertEquals("StdErr was NOT null/empty, so there was a problem.", "", out.stderr);
		assertEquals("Expected did NOT match actual output!\nExpected:\n" + expected + "\n====================\n" + out.stdout, 
				expected, out.stdout);
	}	

	@Test
	public void testSeparator() throws IOException, InterruptedException {
        System.out.println("CompressITCase.testSeparator");
        // have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1\tA\n" +
				"dataA\t2\tB\n" +
				"dataA\t3\tC\n";
		String expected = 
				"##BIOR=<ID=\"COL2\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\",\",EscapedDelimiter=\"\\|\">\n" +
				"##BIOR=<ID=\"COL3\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\",\",EscapedDelimiter=\"\\|\">\n" +
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1,2,3\tA,B,C\n";
		
		CommandOutput out = executeScript("bior_compress", stdin,"--separator", ",", "2,3", "--log");

		assertEquals("Exit code was not zero. " + out.stderr, 0, out.exit);
		assertEquals("StdErr was NOT null/empty, so there was a problem.", "", out.stderr);
		assertEquals("Expected did NOT match actual output!\nExpected:\n" + expected + "\n====================\n" + out.stdout, 
				expected, out.stdout);
	}
	
	@Test
	public void testEscape() throws IOException, InterruptedException {
        System.out.println("CompressITCase.testEscape");
        // have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\n" +
				"dataA\t1,A\n" +
				"dataA\t2,B\n" +
				"dataA\t3,C\n";
		String expected = 
				"##BIOR=<ID=\"COL2\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\",\",EscapedDelimiter=\"#\">\n" +
				"#COL1\tCOL2\n" +
				"dataA\t1#A,2#B,3#C\n";
		
		CommandOutput out = executeScript("bior_compress", stdin,"--separator", ",", "--escape", "#", "2", "--log");

		assertEquals("Exit code was not zero. " + out.stderr, 0, out.exit);
		assertEquals("StdErr was NOT null/empty, so there was a problem.", "", out.stderr);
		assertEquals("Expected did NOT match actual output!\nExpected:\n" + expected + "\n====================\n" + out.stdout, 
				expected, out.stdout);
	}
	
	@Test
	public void testReverse() throws IOException, InterruptedException {
        System.out.println("CompressITCase.testReverse");
        // have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\n" +
				"dataA\t1\n" +
				"dataA\t2\n" +
				"dataB\t3\n";
		String expected = 
				"##BIOR=<ID=\"COL2\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\">\n" +
				"#COL1\tCOL2\n" +
				"dataA\t1|2\n" +
				"dataB\t3\n";
		
		// pick field 1 (RIGHT_TO_LEFT), so that's actually the 2nd column as the compress column
		CommandOutput out = executeScript("bior_compress", stdin, "--reverse", "1", "--log");

		assertEquals("Exit code was not zero. " + out.stderr, 0, out.exit);
		assertEquals("StdErr was NOT null/empty, so there was a problem.", "", out.stderr);
		assertEquals("Expected did NOT match actual output!\nExpected:\n" + expected + "\n====================\n" + out.stdout, 
				expected, out.stdout);
	}	
	
	@Test
	public void testAlign() throws IOException, InterruptedException {
        System.out.println("CompressITCase.testAlign");
        // have JSON for STDIN
		String stdin = 
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1\t.\n" +
				"dataA\t.\t.\n" +
				"dataA\t3\t.\n" +
				"dataB\t100\tW\n" +
				"dataB\t101\tW\n" +
				"dataB\t333\t.\n";

		String expected = 
				"##BIOR=<ID=\"COL2\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\">\n" +
				"##BIOR=<ID=\"COL3\",Operation=\"bior_compress\",DataType=\"String\",Number=\".\",Delimiter=\"|\",EscapedDelimiter=\"\\|\">\n" +
				"#COL1\tCOL2\tCOL3\n" +
				"dataA\t1|.|3\t.|.|.\n" +
				"dataB\t100|101|333\tW|W|.\n";
		
		CommandOutput out = executeScript("bior_compress", stdin, "--align", "2,3", "--log");

		assertEquals("Exit code was not zero. " + out.stderr, 0, out.exit);
		assertEquals("StdErr was NOT null/empty, so there was a problem.", "", out.stderr);
		assertEquals("Expected did NOT match actual output!\nExpected:\n" + expected + "\n====================\n" + out.stdout, 
				expected, out.stdout);
	}
	
    public final String VCF_IN 		= "src/test/resources/treat/gold.vcf";
    public final String DBSNP_CATALOG     	= "src/test/resources/treat/brca1.dbsnp.tsv.gz";
    public final String GENES_CATALOG   	= "src/test/resources/genes.tsv.bgz";

    @Test
    public void testPipedCompressMetadata() throws IOException, InterruptedException {
        System.out.println("Test Piped Compress Metadata");
        String cmd = String.format(
                "cat %s  | bior_vcf_to_tjson  |  bior_overlap -d %s  | bior_overlap -d %s | " +
                " bior_drill -p gene | bior_drill -p gene -c -2 | cut -f 1-8,10 | bior_compress 9 | bior_tjson_to_vcf ",
                VCF_IN,
                GENES_CATALOG,
                GENES_CATALOG
        );
        List<String> expected = new ArrayList<String>();
        Pipeline p = new Pipeline(new CatPipe());
        while(p.hasNext()){String s = (String)p.next(); expected.add(s);}
        /*
        cut -f 1-8,10-12
         */
        System.out.println("Command: " + cmd);
        CommandOutput out = executeScriptWithPipes(cmd);
        String[] lines = out.stdout.split("\n");
        int count = 0;
        for(String line : expected){
            assertEquals(line, lines[count]);
            count++;
        }
    }
}
