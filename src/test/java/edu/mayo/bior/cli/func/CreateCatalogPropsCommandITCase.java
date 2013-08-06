package edu.mayo.bior.cli.func;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
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
	
	private File mDsPropsFile  = new File("src/test/resources/genes.datasource.properties");
    private File mColPropsFile = new File("src/test/resources/genes.columns.properties");

	private File mTestJsonDepthDsPropsFile  = new File("src/test/resources/metadata/testJsonDepth.datasource.properties");
    private File mTestJsonDepthColPropsFile = new File("src/test/resources/metadata/testJsonDepth.columns.properties");

	@Test
	public void testCommand() throws IOException, InterruptedException {
		String stdin = "src/test/resources/genes.tsv.bgz";

	    CommandOutput out = executeScript("bior_create_catalog_props", stdin, "-d", stdin);
        assertEquals(out.stderr, 0, out.exit);
        assertEquals("", out.stderr);
        
        //the above command should create this file
        Properties dsPropsActual   = new PropertiesFileUtil(mDsPropsFile.getCanonicalPath()).getProperties();
        Properties dsPropsExpected = new PropertiesFileUtil("src/test/resources/genes.datasource.properties.expected").getProperties();
        assertPropertiesSame(dsPropsExpected, dsPropsActual);
        
        //the above command should create this file
        Properties colPropsActual   = new PropertiesFileUtil(mColPropsFile.getCanonicalPath()).getProperties();
        Properties colPropsExpected = new PropertiesFileUtil("src/test/resources/genes.columns.properties.expected").getProperties();
        assertPropertiesSame(colPropsExpected, colPropsActual);
	}

	@Test
	public void testNoCommand() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException {
		String catalog = "src/test/resources/genes.tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		Option opt = new Option("d", "catalog path");
		creator.execNoCmd(catalog, opt);
        
        //the above command should create this file
        Properties dsPropsActual   = new PropertiesFileUtil(mDsPropsFile.getCanonicalPath()).getProperties();
        Properties dsPropsExpected = new PropertiesFileUtil("src/test/resources/genes.datasource.properties.expected").getProperties();
        assertPropertiesSame(dsPropsExpected, dsPropsActual);
        
        //the above command should create this file
        Properties colPropsActual   = new PropertiesFileUtil(mColPropsFile.getCanonicalPath()).getProperties();
        Properties colPropsExpected = new PropertiesFileUtil("src/test/resources/genes.columns.properties.expected").getProperties();
        assertPropertiesSame(colPropsExpected, colPropsActual);
	}

	@Test
	public void testNoCmd_DeepJson() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException {
		String catalog = "src/test/resources/metadata/testJsonDepth.tsv.bgz";

		CreateCatalogPropsCommand creator = new CreateCatalogPropsCommand();
		Option opt = new Option("d", "catalog path");
		creator.execNoCmd(catalog, opt);
        
        //the above command should create this file
        Properties dsPropsActual   = new PropertiesFileUtil(mTestJsonDepthDsPropsFile.getCanonicalPath()).getProperties();
        Properties dsPropsExpected = new PropertiesFileUtil("src/test/resources/metadata/testJsonDepth.datasource.properties.expected").getProperties();
        assertPropertiesSame(dsPropsExpected, dsPropsActual);
        
        //the above command should create this file
        Properties colPropsActual   = new PropertiesFileUtil(mTestJsonDepthColPropsFile.getCanonicalPath()).getProperties();
        Properties colPropsExpected = new PropertiesFileUtil("src/test/resources/metadata/testJsonDepth.columns.properties.expected").getProperties();
        assertPropertiesSame(colPropsExpected, colPropsActual);
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
        mDsPropsFile.delete();
        mColPropsFile.delete();
        
        mTestJsonDepthDsPropsFile.delete();
        mTestJsonDepthColPropsFile.delete();
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
