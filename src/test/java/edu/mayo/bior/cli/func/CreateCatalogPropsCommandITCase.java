package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.cli.Option;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.bior.cli.cmd.CreateCatalogPropsCommand;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.util.PropertiesFileUtil;

public class CreateCatalogPropsCommandITCase extends BaseFunctionalTest {
	
	private String DIR = "src/test/resources/metadata/createCatalogProps/";
	private String GENE_CTLG_PREFIX 	= DIR + "genes";
	private String DEEPJSON_CTLG_PREFIX = DIR + "testJsonDepth";
	private String LONGDOTS_CTLG_PREFIX = DIR + "ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37";
	
	private enum Prop {
		columns,
		datasource
	};

	@Test
	public void testCommand() throws IOException, InterruptedException {
		String catalog = GENE_CTLG_PREFIX + ".tsv.bgz";
	    CommandOutput out = executeScript("bior_create_catalog_props", catalog, "-d", catalog);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);
        
        // Compare datasource props file
        assertPropertiesSame(GENE_CTLG_PREFIX, Prop.datasource);
        
        // Compare columns props file
        assertPropertiesSame(GENE_CTLG_PREFIX, Prop.columns);
	}

	@Test
	public void testNoCommand() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = GENE_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		Option opt = new Option("d", "catalog path");
		creator.execNoCmd(catalog, opt);
        
        // Compare datasource props file
        assertPropertiesSame(GENE_CTLG_PREFIX, Prop.datasource);
        
        // Compare columns props file
        assertPropertiesSame(GENE_CTLG_PREFIX, Prop.columns);
	}

	@Test
	public void testNoCommandLongNameWithDots() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = LONGDOTS_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		Option opt = new Option("d", "catalog path");
		creator.execNoCmd(catalog, opt);
        
        // Compare datasource props file
        assertPropertiesSame(LONGDOTS_CTLG_PREFIX, Prop.datasource);
        
        // Compare columns props file
        assertPropertiesSame(LONGDOTS_CTLG_PREFIX, Prop.columns);
	}

	@Test
	public void testNoCmd_DeepJson() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = DEEPJSON_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		Option opt = new Option("d", "catalog path");
		creator.execNoCmd(catalog, opt);
        
        // Compare datasource props file
        assertPropertiesSame(DEEPJSON_CTLG_PREFIX, Prop.datasource);
        
        // Compare columns props file
        assertPropertiesSame(DEEPJSON_CTLG_PREFIX, Prop.columns);
	}


	@Before
	public void beforeEachTest() {
		removeTempPropFiles();
	}

	@After
	public void afterEachTest() {
		removeTempPropFiles();
	}
	
	private void removeTempPropFiles() {
		for(String prefix : Arrays.asList(GENE_CTLG_PREFIX, DEEPJSON_CTLG_PREFIX, LONGDOTS_CTLG_PREFIX) ) {
			new File(prefix + ".datasource.properties").delete();
			new File(prefix + ".columns.properties").delete();
		}
	}
	
	private void assertPropertiesSame(String propertiesPathPrefix, Prop propExt) throws IOException {
        Properties propsActual   = new PropertiesFileUtil(propertiesPathPrefix + "." + propExt.toString() + ".properties").getProperties();
        Properties propsExpected = new PropertiesFileUtil(propertiesPathPrefix + "." + propExt.toString() + ".properties.expected").getProperties();
        assertPropertiesSame(propsExpected, propsActual);
	}
	
	private void assertPropertiesSame(Properties expected, Properties actual) {
		String[] keysExpected = expected.keySet().toArray(new String[0]);
		String[] keysActual   = actual.keySet().toArray(new String[0]);
		assertEquals("Size of properties are not the same", keysExpected.length, keysActual.length);
		
		// Validate that all keys are the same
		for(int i=0; i < keysActual.length; i++) {
			assertEquals("Key[" + i + "] not same (expected=" + keysExpected[i] + ", actual=" + keysActual[i] + ")",
				keysExpected[i], keysActual[i]);
		}
		
		// Validate all values are the same
		for(int i=0; i < keysActual.length; i++) {
			String key = keysActual[i];
			assertEquals("Values[" + i + "] for key [" + key + "] don't match (expected=" 
				+ expected.getProperty(key) + ", actual=" + actual.getProperty(key) + ")",
				expected.getProperty(key), actual.getProperty(key)); 
		}
	}
}
