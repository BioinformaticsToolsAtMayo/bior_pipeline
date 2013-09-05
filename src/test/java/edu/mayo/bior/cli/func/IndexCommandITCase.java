package edu.mayo.bior.cli.func;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Properties;

//import org.h2.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.pipes.JSON.lookup.lookupUtils.IndexUtils;
import edu.mayo.pipes.util.index.IndexDatabaseCreator;

//import edu.mayo.pipes.util.index.IndexDatabaseCreator;

//==========================================================================================
// NOTE: These testcases will require these steps to be performed before running:
// 1) From the command line, run (inserting the path to the bior_pipeline dir):
//		export BIOR_LITE_HOME=/home/bior/bior_lite/bior_pipeline/target/bior_pipeline-0.0.2-SNAPSHOT
// 2) From the bior_pipeline project, run command:
//		To update the code:
//			svn up
//		To build the target directory and skip the tests initially:
//			mvn clean package install -DskipTests
// 3) Also, if the pipes or pipeline code changes, then the mvn command must be run again to push the code into the target directory
//==========================================================================================
public class IndexCommandITCase extends BaseFunctionalTest {
	final String CATALOG   = "src/test/resources/sameVariantCatalog.tsv.bgz";
	final String INDEX_OUT = "src/test/resources/index/sameVariantCatalog.ID.idx.h2.db";
	final String INDEX_OUT_CHROM  = "src/test/resources/index/sameVariantCatalog.CHROM.idx.h2.db";
	final String INDEX_OUT_NESTED = "src/test/resources/index/sameVariantCatalog.INFO.different_bc_ref_notmatch.idx.h2.db";
	final String INDEX_USER_OUT = System.getProperty("user.home") + "/tempIndex/myIndexFile.Hgnc.idx.h2.db";

	final String TEMP_OUTPUT = "src/test/resources/temp.txt";
	
	final String JSON_PATH = "ID";  //rsIds

	@Before
	public void beforeTests() {
		deleteH2Db();
	}
	
	@After
	public void afterTests() {
		deleteH2Db();
	}
	
	public void deleteH2Db() {
		// Delete the default index files
		File idxOut = new File(INDEX_OUT);
		idxOut.delete();
		// Delete the indexes on extra paths
		new File(INDEX_OUT_CHROM).delete();
		new File(INDEX_OUT_NESTED).delete();
		
		
		// Delete user index and directory
		File idxOutFile = new File(INDEX_USER_OUT);
		idxOutFile.delete();
		idxOutFile.getParentFile().delete();
		
		// REmove the temp out file
		new File(TEMP_OUTPUT).delete();
	}
	
	@Test
	public void help() throws IOException, InterruptedException {
        System.out.println("IndexCommandITCase.help");
        CommandOutput out = executeScript("bior_index_catalog", null);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.help.txt"));
		// bior_index --help
		out = executeScript("bior_index_catalog", null, "--help");
		assertNoErrors(out);
		assertEquals(out.stdout, expected, out.stdout);
		assertFalse(new File(INDEX_OUT).exists());

		// bior_index_catalog -h
		out = executeScript("bior_index_catalog", null, "-h");
		assertNoErrors(out);
		assertEquals(out.stdout, expected, out.stdout);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badCmd_noCatalogOrJsonFlag() throws IOException, InterruptedException {
        System.out.println("IndexCommandITCase.badCmd_noCatalogOrJsonFlag");
        // bior_index_catalog
		CommandOutput out = executeScript("bior_index_catalog", null);
		assertEquals(1, out.exit);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingOptions.txt"));
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badCmd_noCatalog() throws IOException, InterruptedException {
        System.out.println("IndexCommandITCase.badCmd_noCatalog");
        // bior_index_catalog -k ID
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH);
		assertEquals(1, out.exit);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingCatalog.txt"));
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badCmd_noJsonFlag() throws IOException, InterruptedException {
        System.out.println("IndexCommandITCase.badCmd_noJsonFlag");
        // bior_index_catalog -d src/test/resources/sameVariantCatalog.tsv.bgz
		CommandOutput out = executeScript("bior_index_catalog", null, "-d", CATALOG);
		assertEquals(1, out.exit);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingKeyOption.txt"));
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badOption() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.badOption");
        // bior_index_catalog -p ID -k ID -d src/test/resources/sameVariantCatalog.tsv.bgz
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-k", JSON_PATH, "-d", CATALOG);
		assertEquals(1, out.exit);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.badOption.txt"));
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}


	@Test
	public void tooManyArgs() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.tooManyArgs");
        // bior_index_catalog -p ID -d src/test/resources/sameVariantCatalog.tsv.bgz  someUnnecessaryArg
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-d", CATALOG, "someUnnecessaryArg");
		assertEquals(1, out.exit);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.tooManyArgs.txt"));
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}	

	/** Json path not found in ANY rows - should return "1" for exit code when it realizes the index is empty */
	public void jsonPathNotFound() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.jsonPathNotFound");
        // bior_index_catalog  -p SomeBadJsonPath  -d src/test/resources/sameVariantCatalog.tsv.bgz
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", "SomeBadJsonPath", "-d", CATALOG);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertEquals(1, out.exit);
		assertTrue(out.stderr, out.stderr.contains("java.lang.IllegalArgumentException: There were no keys indexed!  Check your inputs and try again."));
	}
	
	
	@Test
	public void jsonPath() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.jsonPath");
        // bior_index_catalog    -p ID  -d src/test/resources/sameVariantCatalog.tsv.bgz
		System.out.println("bior_index_catalog " +" -p " + JSON_PATH +" -d " + CATALOG);
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-d", CATALOG);
		
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}

	@Test
	public void indexParm() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.indexParam");
        CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-d", CATALOG, "-i", INDEX_OUT);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}

	@Test
	public void longOptionNames() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.longOptionNames");
        // bior_index_catalog --path ID  --database src/test/resources/sameVariantCatalog.tsv.bgz  --index  src/test/resources/index/sameVariantCatalog.ID.idx.h2.db
		CommandOutput out = executeScript("bior_index_catalog", null, "--path", JSON_PATH, "--database", CATALOG, "--index", INDEX_OUT);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}

	@Test
	public void userIndexNotInDefaultDir() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.userIndexNotInDefaultDir");
        // bior_index_catalog -p ID  -d src/test/resources/sameVariantCatalog.tsv.bgz  -i  src/test/resources/index/sameVariantCatalog.ID.idx.h2.db
		CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-d", CATALOG, "-i", INDEX_USER_OUT);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
	//	assertNoErrors(out);
		assertDbRows(38, INDEX_USER_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_USER_OUT)));
	}

	@Test
	public void keyIsInt() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.keyIsInt");
        CommandOutput out = executeScript("bior_index_catalog", null, "-p", "CHROM",  "-d", CATALOG);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT_CHROM);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.CHROM.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT_CHROM)));
	}

	@Test
	public void keyIsString() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.keyIsString");
        CommandOutput out = executeScript("bior_index_catalog", null, "-p", JSON_PATH, "-d", CATALOG);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}


	@Test
	public void jsonPathNested_matchOne() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        System.out.println("IndexCommandITCase.jsonPathNested_matchOne");
        CommandOutput out = executeScript("bior_index_catalog", null, "-p", "INFO.different_bc_ref_notmatch", "-d", CATALOG);
		IndexUtils.writeToFile(out.stdout, TEMP_OUTPUT);
		assertNoErrors(out);
		assertDbRows(1, INDEX_OUT_NESTED);
		assertEquals("	KEY	FILEPOS\n1)	true	5403\n", 
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT_NESTED)));
	}
	//========================================================================
	
	private void assertNoErrors(CommandOutput out) {
		// Assert no errors and nothing in stderr
		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
	}
	
	private void assertDbRows(int rowsExpected, String h2DbPath) throws SQLException, ClassNotFoundException, IOException {
		File indexFile = new File(h2DbPath);
		assertTrue(indexFile.exists());
		assertEquals(rowsExpected, IndexDatabaseCreator.countDatabaseRows(indexFile));
	}
	
}
