package edu.mayo.bior.cli.cmd;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: m102417
 * Date: 7/25/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class CreateCatalogCommandTest {

    public static String json = "{\"_type\":\"gene\",\"_landmark\":\"1\",\"_strand\":\"+\",\"_minBP\":10954,\"_maxBP\":11507,\"gene\":\"LOC100506145\",\"note\":\"Derived by automated computational analysis using gene prediction method: GNOMON. Supporting evidence includes similarity to: 1 Protein\",\"pseudo\":\"\",\"GeneID\":\"100506145\"}";


    @Test
    public void testExtractLandmark(){
        CreateCatalogCommand ccc = new CreateCatalogCommand();
        CreateCatalogCommand.TJSON2Catalog tjson2Catalog = ccc.newTJSON2Catalog(-1);
        assertEquals("1", tjson2Catalog.extractLandmark(json));
    }

    @Test
    public void testExtractMinBP(){
        CreateCatalogCommand ccc = new CreateCatalogCommand();
        CreateCatalogCommand.TJSON2Catalog tjson2Catalog = ccc.newTJSON2Catalog(-1);
        assertEquals("10954", tjson2Catalog.extractMinBP(json));
    }

    @Test
    public void testExtractMaxBP(){
        CreateCatalogCommand ccc = new CreateCatalogCommand();
        CreateCatalogCommand.TJSON2Catalog tjson2Catalog = ccc.newTJSON2Catalog(-1);
        assertEquals("11507", tjson2Catalog.extractMaxBP(json));
    }

}
