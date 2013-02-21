package edu.mayo.bior.cli.func;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

//import org.h2.util.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	}
	
	@Test
	public void help() throws IOException, InterruptedException {
		CommandOutput out = executeScript("bior_index", null);
		String helpTextOut = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutputHelp.txt"));
		// bior_index --help
		out = executeScript("bior_index", null, "--help");
		assertNoErrors(out);
		assertEquals(out.stdout, helpTextOut, out.stdout);
		assertFalse(new File(INDEX_OUT).exists());

		// bior_index -h
		out = executeScript("bior_index", null, "-h");
		assertNoErrors(out);
		assertEquals(out.stdout, helpTextOut, out.stdout);
		assertFalse(new File(INDEX_OUT).exists());
	}
	
	@Test
	public void badCmd_noCatalogOrJsonFlag() throws IOException, InterruptedException {
		// bior_index
		CommandOutput out = executeScript("bior_index", null);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingOptions.txt"));
		assertEquals(1, out.exit);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badCmd_noCatalog() throws IOException, InterruptedException {
		// bior_index -k ID
		CommandOutput out = executeScript("bior_index", null, "-k", JSON_PATH);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingArgs.txt"));
		assertEquals(1, out.exit);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badCmd_noJsonFlag() throws IOException, InterruptedException {
		// bior_index -d src/test/resources/sameVariantCatalog.tsv.bgz
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.missingKeyOption.txt"));
		assertEquals(1, out.exit);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}

	@Test
	public void badOption() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		// bior_index -p ID -k ID -d src/test/resources/sameVariantCatalog.tsv.bgz
		CommandOutput out = executeScript("bior_index", null, "-p", JSON_PATH, "-k", JSON_PATH, "-d", CATALOG);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.badOption.txt"));
		assertEquals(1, out.exit);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}	

	
	@Test
	public void tooManyArgs() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		// bior_index -k ID -d src/test/resources/sameVariantCatalog.tsv.bgz  src/test/resources/index/sameVariantCatalog.INFO.different_bc_ref_notmatch.idx.h2.db
		CommandOutput out = executeScript("bior_index", null, "-k", JSON_PATH, "-d", CATALOG, INDEX_OUT_NESTED);
		String expected = loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.tooManyArgs.txt"));
		assertEquals(1, out.exit);
		assertEquals(out.stderr, expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
	}	

	/** Json path not found in ANY rows - should return "1" for exit code when it realizes the index is empty */
	public void jsonPathNotFound() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		// bior_index -d src/test/resources/sameVariantCatalog.tsv.bgz  -k SomeBadJsonPath 
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", "SomeBadJsonPath");
		assertEquals(1, out.exit);
		assertTrue(out.stderr, out.stderr.contains("java.lang.IllegalArgumentException: There were no keys indexed!  Check your inputs and try again."));
	}
	
	
	@Test
	public void jsonPath() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", JSON_PATH);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}

	@Test
	public void indexParm() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", JSON_PATH, "-x", INDEX_OUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}

	@Test
	public void userIndexNotInDefaultDir() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", JSON_PATH, "-x", INDEX_USER_OUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_USER_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_USER_OUT)));
	}

	@Test
	public void keyIsInt() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", "CHROM");
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT_CHROM);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.CHROM.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT_CHROM)));
	}

	@Test
	public void keyIsString() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-d", CATALOG, "-k", JSON_PATH);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
		assertEquals(loadFile(new File("src/test/resources/index/IndexCommand.expectedOutput.ID.txt")),
			     IndexDatabaseCreator.getTableAsString(new File(INDEX_OUT)));
	}


	@Test
	public void jsonPathNested_matchOne() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-k", "INFO.different_bc_ref_notmatch", "-d", CATALOG);
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
