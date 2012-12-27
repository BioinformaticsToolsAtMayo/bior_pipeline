package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import org.junit.Test;

public class SameVariantTCase extends BaseFunctionalTest {
	
	 @Test
     public void testNoMatch() throws IOException, InterruptedException {

             String stdin = "src/test/resources/sameVariantCatalog.tsv.gz";

             String jsonVariant = "{\"type\":\"gene\",\"chr\":\"17\",\"strand\":\"+\",\"minBP\":41177258,\"maxBP\":41184058,\"gene\":\"RND2\",\"gene_synonym\":\"ARHN; RHO7; RhoN\",\"note\":\"Rho family GTPase 2; Derived by automated computational analysis using gene prediction method: BestRefseq.\",\"GeneID\":\"8153\",\"HGNC\":\"18315\",\"HPRD\":\"03332\",\"MIM\":\"601555\"}";

             CommandOutput out = executeScript("bior_same_variant.sh", jsonVariant, "-d", stdin);

             assertEquals(out.stderr, 0, out.exit);
             assertEquals("", out.stderr);

             String header = getHeader(out.stdout);

             // pull out just data rows
             String data = out.stdout.replace(header, "");
             String[] cols = data.split("\t");

             // If variant is not found, results in empty json string
             assertEquals("{}", cols[1].trim());
     }

}
