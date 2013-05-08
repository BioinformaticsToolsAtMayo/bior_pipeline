package edu.mayo.bior.cli.func.remoteexec;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import edu.mayo.bior.cli.func.CommandOutput;
import edu.mayo.bior.cli.func.remoteexec.helpers.RemoteFunctionalTest;
import edu.mayo.pipes.util.test.FileCompareUtils;
import edu.mayo.pipes.util.test.PipeTestUtils;

/**
 * @author Michael Meiners (m054457)
 * Date created: Apr 26, 2013
 */
public class ManyCmdsITCase extends RemoteFunctionalTest {

	@Test
	public void testManyCmds() throws IOException, InterruptedException { 
		System.out.println("-------------------------------------------------------------------------");
		System.out.println("ManyCmdsITCase.testManyCmds(): Test many commands piped into one another");
		
		// GOAL:
		// 1) Take a small list of variants as input
		// 2) Use bior_vcf_to_json to convert to json
		// 3) Do bior_same_variant against dbSnp (small dbSnp with BRCA1 variants)
		// 4) Do bior_overlap to find genes associated with those variants
		// Commands:
		// 		bior_vcf_to_json
		//		bior_same_variant
		//		bior_overlap
		//		bior_drill
		//		bior_lookup
		//		bior_vep
		//		bior_snpeff
		// NOT used:
		//		bior_index
		//		bior_squish
		//      bior_pretty_print
		final String VCF_IN 		= "src/test/resources/tools/manycmds/manycmds.input.vcf";
		final String DBSNP_CATALOG 	= "src/test/resources/treat/brca1.dbsnp.tsv.gz";
		final String GENES_CATALOG 	= "src/test/resources/genes.tsv.bgz";
		String cmd = String.format(
				"cat %s  | bior_vcf_to_json  | bior_same_variant -d %s  | bior_overlap -d %s  | " +
				"bior_drill -p HGNC  | bior_lookup -d %s -p HGNC  | bior_vep  | bior_snpeff",
				VCF_IN,
				DBSNP_CATALOG,
				GENES_CATALOG,
				GENES_CATALOG
			);
		System.out.println("Command: " + cmd);
		CommandOutput out = executeScriptWithPipes(cmd);
		
		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
		
		String actualStr = out.stdout;
		List<String> actual = Arrays.asList(actualStr.split("\n"));
		List<String> expected = FileCompareUtils.loadFile("src/test/resources/tools/manycmds/manycmds.out.vcf");
	
		VEPITCase.printComparison(null, expected, actual);

		PipeTestUtils.assertListsEqual(expected, actual);
	}
}
