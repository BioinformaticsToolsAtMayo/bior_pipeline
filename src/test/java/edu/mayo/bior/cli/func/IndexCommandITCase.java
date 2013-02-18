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
	final String INDEX_OUT = "src/test/resources/tempOutput/sameVariantCatalog.ID.idx.h2.db";
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
		new File(INDEX_OUT).delete();
	}
	
	@Test
	public void help() throws IOException, InterruptedException {
		CommandOutput out = executeScript("bior_index", null);
		String expected = "Error executing bior_index\n\n"
				+ "Usage: bior_index [--column <arg>] [--pathJson <JSON path>] [--log] [--help] [--isKeyAnInt <Is Key an integer>] CATALOG_BGZIP_FILE INDEX_FILE_OUT\n\n"
				+ "Invalid number of argument values specified.\n\n"
				+ "Arguments that are required:\n"
				+ "	CATALOG_BGZIP_FILE\n"
				+ "	INDEX_FILE_OUT\n\n"
				+ "Arguments specified by user:\n\n"
				+ "Execute the command with -h or --help to find out more information\n\n";
		assertEquals(1, out.exit);
		assertEquals(expected, out.stderr);
		assertFalse(new File(INDEX_OUT).exists());
		
		String helpTextOut = loadTxtFile("src/test/resources/IndexCommand.expectedOutputHelp.txt");
		out = executeScript("bior_index", null, "--help");
		assertNoErrors(out);
		assertEquals(helpTextOut, out.stdout);
		assertFalse(new File(INDEX_OUT).exists());
		//System.out.println(out.stdout);

		out = executeScript("bior_index", null, "-h");
		assertNoErrors(out);
		assertEquals(helpTextOut, out.stdout);
		assertFalse(new File(INDEX_OUT).exists());
	}
	
	@Test
	public void columnAndJson() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, CATALOG, INDEX_OUT, "-c", "4", "-p", JSON_PATH);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}

	@Test
	public void keyIsInt() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, CATALOG, INDEX_OUT, "-c", "4", "-p", "CHROM");
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}

	/** Json path not found in ANY rows - should return "1" for exit code when it realizes the index is empty */
	public void badJsonPath() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, CATALOG, INDEX_OUT, "-c", "4", "-p", "SomeBadJsonPath");
		assertEquals(1, out.exit);
		assertTrue(out.stderr.contains("java.lang.IllegalArgumentException: There were no keys indexed!  Check your inputs and try again."));
	}
	
	@Test
	public void keyIsString() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, CATALOG, INDEX_OUT, "-c", "4", "-p", JSON_PATH);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}

	@Test
	// NOTE: This will choose the last column by default which is the JSON column, 
	//       and since no JSON path is specified, it will use the whole JSON string as key 
	public void noColumnNorJsonFlags() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		//System.out.println("=============================");
		CommandOutput out = executeScript("bior_index", null, CATALOG, INDEX_OUT);
		//IndexDatabaseCreator.printDatabase(new File(INDEX_OUT), false);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}
	
	// Use minBP column
	@Test
	public void columnFlag_noJsonFlag() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		//System.out.println("=============================");
		CommandOutput out = executeScript("bior_index", null, "-c", "2", CATALOG, INDEX_OUT);
		//IndexDatabaseCreator.printDatabase(new File(INDEX_OUT), false);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}
	
	@Test
	/** NOTE: This will be the default way of calling the index builder */
	public void jsonFlag_noColumnFlag() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		//System.out.println("=============================");
		CommandOutput out = executeScript("bior_index", null, "-p", JSON_PATH, CATALOG, INDEX_OUT);
		//IndexDatabaseCreator.printDatabase(new File(INDEX_OUT), false);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}
	
	@Test
	public void flagsBeforeArgs() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-c", "4", "-p", JSON_PATH, CATALOG, INDEX_OUT);
		assertNoErrors(out);
		assertDbRows(38, INDEX_OUT);
	}
	 
	@Test
	public void jsonPathNested() throws IOException, InterruptedException, SQLException, ClassNotFoundException {
		CommandOutput out = executeScript("bior_index", null, "-c", "4", "-p", "INFO.different_bc_ref_notmatch", CATALOG, INDEX_OUT);
		assertNoErrors(out);
		assertDbRows(1, INDEX_OUT);
	}	
	//========================================================================
	
	private void assertNoErrors(CommandOutput out) {
		// Assert no errors and nothing in stderr
		assertEquals(out.stderr, 0, out.exit);
		assertEquals("", out.stderr);
	}
	
	private void assertDbRows(int rowsExpected, String h2DbPath) throws SQLException, ClassNotFoundException, IOException {
		File indexFile = new File(INDEX_OUT);
		assertTrue(indexFile.exists());
//		assertEquals(rowsExpected, IndexDatabaseCreator.countDatabaseRows(indexFile));
	}
	
	private Properties loadProperties(String propertiesPath) throws IOException {
		Properties props = new Properties();
		FileInputStream fin = new FileInputStream(propertiesPath);
		props.load(fin);
		fin.close();
		return props;
	}
	
	private String loadTxtFile(String txtPath) throws IOException {
		FileInputStream fin = new FileInputStream(txtPath);
		byte[] buf = new byte[64*1024];
		int len = -1;
		StringBuilder str = new StringBuilder();
		while( (len = fin.read(buf)) != -1 ) {
			str.append(new String(buf), 0, len);
		}
		fin.close();
		return str.toString();
	}

}
