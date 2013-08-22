package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.bior.cli.cmd.CreateCatalogPropsCommand;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.util.PropertiesFileUtil;

public class CreateCatalogPropsCommandITCase extends BaseFunctionalTest {
	
	private String DIR = "src/test/resources/metadata/createCatalogProps/";
	private String GENE_CTLG_PREFIX 	= DIR + "genes";
	private String DEEPJSON_CTLG_PREFIX = DIR + "testJsonDepth";
	private String LONGDOTS_CTLG_PREFIX = DIR + "ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37";
	private String TARGET_PREFIX 		= DIR + "testTargetFolder/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37";
	
	private enum Prop {
		columns,
		datasource
	};

	@Test
	public void testCmd() throws IOException, InterruptedException {
		String catalog = GENE_CTLG_PREFIX + ".tsv.bgz";
	    CommandOutput out = executeScript("bior_create_catalog_props", catalog, "-d", catalog);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);
        
        // Compare datasource and columns props file
        assertPropertiesSame(GENE_CTLG_PREFIX);
	}

	@Test
	public void testCmd_vcfAndTargetDir() throws IOException, InterruptedException {
		String catalog = LONGDOTS_CTLG_PREFIX + ".tsv.bgz";
		String vcf = "src/test/resources/metadata/createCatalogProps/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37.vcf";
		String targetDir = "src/test/resources/metadata/createCatalogProps/testTargetFolder/";
	    CommandOutput out = executeScript("bior_create_catalog_props", catalog, "-d", catalog, "-v", vcf, "-t", targetDir);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);
        
        // Compare datasource and columns props file
        assertPropertiesSame(LONGDOTS_CTLG_PREFIX, 
        	new File(targetDir).getCanonicalPath(), 
        	new File(targetDir).getCanonicalPath() );
	}
	
	@Test
	public void testNoCmd() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = GENE_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		creator.execNoCmd(catalog, null, null, false);
        
        // Compare datasource and columns props file
        assertPropertiesSame(GENE_CTLG_PREFIX);
	}

	@Test
	public void testNoCmd_LongNameWithDots() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = LONGDOTS_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		creator.execNoCmd(catalog, null, null, false);
        
        // Compare datasource and columns props file
        assertPropertiesSame(LONGDOTS_CTLG_PREFIX);
	}

	@Test
	/** Test multiple JSON levels as well as a catalog that has a header line */
	public void testNoCmd_DeepJson() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = DEEPJSON_CTLG_PREFIX + ".tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		creator.execNoCmd(catalog, null, null, false);
        
        // Compare datasource and columns props files
        assertPropertiesSame(DEEPJSON_CTLG_PREFIX);
	}

	@Test
	public void testNoCmd_vcfAndTargetDir() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException, URISyntaxException {
		String catalog = LONGDOTS_CTLG_PREFIX + ".tsv.bgz";
		String vcf = "src/test/resources/metadata/createCatalogProps/ALL.wgs.phase1_release_v3.20101123.snps_indels_sv.sites_GRCh37.vcf";
		String targetDir = "src/test/resources/metadata/createCatalogProps/testTargetFolder/";
		
		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		creator.execNoCmd(catalog, vcf, targetDir, true);
        
        // Compare datasource and columns props file
        assertPropertiesSame(LONGDOTS_CTLG_PREFIX, 
        	new File(targetDir).getCanonicalPath(), 
        	new File(targetDir).getCanonicalPath() );
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
		for(String prefix : Arrays.asList(GENE_CTLG_PREFIX, DEEPJSON_CTLG_PREFIX, LONGDOTS_CTLG_PREFIX, TARGET_PREFIX) ) {
			new File(prefix + ".datasource.properties").delete();
			new File(prefix + ".columns.tsv").delete();
		}
	}

	private void assertPropertiesSame(String propertiesPathPrefix) throws IOException {
		File dirWithProps = new File(propertiesPathPrefix).getParentFile().getCanonicalFile();
		assertPropertiesSame(propertiesPathPrefix, dirWithProps.getCanonicalPath(), dirWithProps.getCanonicalPath());
	}
	
	private void assertPropertiesSame(String propertiesPathPrefix, String expectedDir, String actualTargetDir) throws IOException {
		// Compare DataSource properties
		String namePrefix = new File(propertiesPathPrefix).getName();
		String parentDirActual   = new File(actualTargetDir).getCanonicalPath();
		String parentDirExpected = new File(expectedDir).getCanonicalPath();
        Properties propsActual   = new PropertiesFileUtil(parentDirActual   + File.separator + namePrefix + ".datasource.properties").getProperties();
        Properties propsExpected = new PropertiesFileUtil(parentDirExpected + File.separator + namePrefix + ".datasource.properties.expected").getProperties();
        assertPropertiesSame(propsExpected, propsActual);
        
        // Compare column properties
        HashMap<String,ColumnMetaData> colMetaMapActual   = ColumnMetaData.parseColumnProperties(parentDirActual   + File.separator + namePrefix + ".columns.tsv");
        HashMap<String,ColumnMetaData> colMetaMapExpected = ColumnMetaData.parseColumnProperties(parentDirExpected + File.separator + namePrefix + ".columns.tsv.expected");
        assertColsSame(colMetaMapExpected, colMetaMapActual);
	}
	
	private void assertColsSame(
			HashMap<String, ColumnMetaData> colMetaMapExpected,
			HashMap<String, ColumnMetaData> colMetaMapActual)
	{
		assertEquals("Size of columns properties files are different", colMetaMapExpected.size(), colMetaMapActual.size());
		
		for(String key : colMetaMapExpected.keySet()) {
			assertEquals(colMetaMapExpected.get(key).toString(), colMetaMapActual.get(key).toString());
		}
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
