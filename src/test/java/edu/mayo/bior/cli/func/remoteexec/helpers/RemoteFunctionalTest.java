package edu.mayo.bior.cli.func.remoteexec.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.management.BadAttributeValueExpException;
import javax.swing.JOptionPane;

import org.apache.commons.cli.MissingArgumentException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import edu.mayo.bior.cli.func.BaseFunctionalTest;

public class RemoteFunctionalTest extends BaseFunctionalTest {

	public enum DevServerUserPropKeys { 
		devServerName, devServerUsername, devServerPassword, devServerPath, isFirstSync,
		urlSvnPipes, urlSvnPipeline, urlSvnCatalog, svnUser, svnPass };
	
	public final String DEV_SERVER_PROPS_FILE = System.getProperty("user.home") + "/bior.devserver.properties";
	public final String EXAMPLE_PROPS_FILE    = "src/test/resources/remoteexec/bior.devserver.example.properties";
	public final String BIOR_DEV_SERVER		  = "biordev.mayo.edu";
	
	private static boolean mIsSharedDevServer  = false;
	private static boolean mIsHostVerified	   = false;
	


	@BeforeClass
	public static void beforeAll() throws Exception {
		System.out.println("RemoteFunctionalTest.beforeAll()................");
		mIsSharedDevServer = new RemoteFunctionalTest().isOnBiorDevServer();

		// If we are on the biordev server, then just return as the JUnit tests will be run individually.
		// Else, we need to upload them to the server and execute them
		if( mIsSharedDevServer )
			return;
		
		System.out.println("Running tests remotely...");
		new RemoteFunctionalTest().runTestsRemotely();
	}
	


	@Before
	public void beforeEach() {
		System.out.println("RemoteFunctionalTest.beforeEach()................");
		// Only run individual JUnit tests if on biordev box
		if(! mIsSharedDevServer)
			System.out.println("Not running on the biordev server, so skipping test case for now...");
		// Testcase will only execute if on biordev server,
		Assume.assumeTrue(mIsSharedDevServer);
	}
	
//	@AfterClass
//	public static void afterAll() throws MissingArgumentException, IOException {
//		System.out.println("RemoteFunctionalTest.afterAll() ................");
//	}
//
//	@After
//	public void afterEach() {
//		System.out.println("RemoteFunctionalTest.afterEach ................");
//	}
//
	
	private boolean isOnBiorDevServer() throws MissingArgumentException, IOException {
		// Can we get the calling method and class and store it in a list 
		// Could then check these to execute
		
		if( ! mIsHostVerified ) {
			// Check if this is the dev server
			String localHostname = java.net.InetAddress.getLocalHost().getHostName();
			mIsSharedDevServer = localHostname.equalsIgnoreCase(BIOR_DEV_SERVER);
		}
		
		// Only run the testcase if on local system
		return mIsSharedDevServer;
	}

	/** We are probably on a laptop or user system and thus need to copy all current
	  * files to the biordev box and execute all testcases there 
	 * @throws Exception */
	private void runTestsRemotely() throws Exception  {
		try {
			// Get properties for connecting to dev system (use specific user directory)
			Properties devServerProperties = loadProperties();
			
			warnUserIfFirstSync(devServerProperties);
			
			Ssh ssh = new Ssh();
			Session session = ssh.openSession(devServerProperties);
	
			// scp latest files to server  (only update files that have changed)
			System.out.println("Sync local project to biordev server...");
			copyProjectToServer(session, devServerProperties);
			
			System.out.println("Run the Maven build on biordev server...");
			ArrayList<String> resultLines = runMavenBuild(session);
			System.out.println("Get results from Maven's surefire-reports xml files...");
			ArrayList<RemoteTestResult> testResults = getTestResults(session);
	
			verifyTests(session, testResults);
			
			session.disconnect();
			System.out.println("Done.");
		} catch(BadAttributeValueExpException e1) {
			System.err.println("User chose to cancel sync operation");
			throw e1;
		} catch(Exception e2) {
			System.err.println("Error occurred while running remote test cases!");
			e2.printStackTrace();
			throw e2;
		}
		
	}
	
	

	private void warnUserIfFirstSync(Properties devServerProperties) throws BadAttributeValueExpException, IOException {
		boolean isFirstSync = ! "false".equalsIgnoreCase(devServerProperties.getProperty(DevServerUserPropKeys.isFirstSync.toString()));
		if( ! isFirstSync )
			return;

		String remoteDir = devServerProperties.getProperty(DevServerUserPropKeys.devServerPath.toString());
		int choice = JOptionPane.showConfirmDialog(null, 
				"WARNING!  This is the first time you have sync'd up with the biordev server!\n"
				+ "If you continue, the remote directory " + remoteDir + "\n"
				+ "will be wiped and all files and directories sync'd to your local project directory.\n"
				+ "Are you sure you want to continue?!", 
				"Warning", 
				JOptionPane.YES_NO_OPTION);
		if( choice == JOptionPane.NO_OPTION )
			throw new BadAttributeValueExpException("User chose to cancel sync operation");
		
		// Update the properties so it will NOT be the firstSync
		devServerProperties.setProperty(DevServerUserPropKeys.isFirstSync.toString(), "false");
		saveProperties(devServerProperties);
	}



	private void verifyTests(Session session, ArrayList<RemoteTestResult> testResults) throws JSchException, IOException {
		System.out.println("\n================================================================================");
		System.out.println("Verify test cases...");
		System.out.println("================================================================================");

		// Verify that at least one test ran
		int numTestsRun = 0;
		int numErrors = 0;
		int numSkipped = 0;
		for(RemoteTestResult testResult : testResults) {
			numTestsRun += testResult.numTestsRun;
			numErrors += testResult.numErrors + testResult.numFailures;
			numSkipped += testResult.numSkipped;
		}
		Assert.assertTrue(numTestsRun > 0);
		
		// Verify that no tests had errors
		if( numErrors > 0 ) {
			printErrors(getTestErrors(session));
			System.out.println("\n============================================================================");
			System.out.println("Doh!  Some tests failed! ************************************************");
			System.out.println("============================================================================");
			Assert.fail();
		}

		// Warn user if some tests skipped
		if( numSkipped > 0 ) {
			System.out.println("WARNING!!!  Some tests were skipped!  Num skipped: " + numSkipped);
		}
		
		System.out.println("\n============================================================================");
		System.out.println("Congratulations - all tests passed!");
		System.out.println("============================================================================");
	}
		
	private void printErrors(ArrayList<String> testErrors) {
		for(String s : testErrors) {
			System.out.println(s);
		}
	}


	private void copyProjectToServer(Session session, Properties devServerProperties) throws JSchException, IOException, ParseException, SftpException  {
		RSync rsync = new RSync();
		rsync.syncSftp(session, devServerProperties);
		
		
		//File projectDir = new File(".").getCanonicalFile();
		//jazsync jaz = new jazsync();
		//jazsync.main(null);
		// TODO:.....
	}


	/** Run the "mvn clean install" command on the biordev server and return result lines
	 * @throws IOException 
	 * @throws JSchException */
	public ArrayList<String> runMavenBuild(Session session) throws JSchException, IOException  {
		Properties props = Ssh.getTempProperties();
		String projectDir = props.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPath.toString());
		String cmd = "cd " + projectDir + "; mvn clean install";
		ArrayList<String> output = new Ssh().runRemoteCommand(session, cmd, true);
		return output;
	}
	
	/** Get the results from running "mvn clean install" on biordev 
	 * @throws IOException 
	 * @throws JSchException */
	public ArrayList<RemoteTestResult> getTestResults(Session session) throws JSchException, IOException {
		Properties tempProps = Ssh.getTempProperties();
		String xmlDir = tempProps.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPath.toString()) + "/target/surefire-reports";
		String cmd = "grep -R \"<testsuite\" " + xmlDir + "/*.xml";
		ArrayList<String> output = new Ssh().runRemoteCommand(session, cmd, false);
		ArrayList<RemoteTestResult> testResults = new ArrayList<RemoteTestResult>();
		for(String line : output) {
			RemoteTestResult test = new RemoteTestResult();
			test.path = line.substring(0, line.indexOf(":<"));
			test.numFailures = Integer.parseInt(getMidStr(line, "failures=\"", "\""));
			test.runTime    =Double.parseDouble(getMidStr(line, "time=\"",     "\""));
			test.numErrors 	 = Integer.parseInt(getMidStr(line, "errors=\"",   "\""));
			test.numSkipped  = Integer.parseInt(getMidStr(line, "skipped=\"",  "\""));
			test.numTestsRun = Integer.parseInt(getMidStr(line, "tests=\"",    "\""));
			test.testSuiteName=getMidStr(line, "name=\"", "\"");
			testResults.add(test);
		}
		return testResults;
	}
	
	public ArrayList<String> getTestErrors(Session session) throws JSchException, IOException {
		Properties tempProps = Ssh.getTempProperties();
		String xmlDir = tempProps.getProperty(RemoteFunctionalTest.DevServerUserPropKeys.devServerPath.toString()) + "/target/surefire-reports";
		String cmd = "grep -B 1 -A 5 \"<failure\" " + xmlDir + "/*.xml";
		ArrayList<String> output = new Ssh().runRemoteCommand(session, cmd, false);
		return output;
	}
	
	private String getMidStr(String fullStr, String prefix, String post) {
		int idx = fullStr.indexOf(prefix) + prefix.length();
		return fullStr.substring(idx,  fullStr.indexOf(post, idx));
	}
	
	private Properties loadProperties() throws MissingArgumentException, IOException {
		// Check if properties file exists, if not then print a warning message about how to create it, but continue to run tests
		if( ! new File(DEV_SERVER_PROPS_FILE).exists() ) {
			System.err.println("ERROR:   Properties file does not exist: " + DEV_SERVER_PROPS_FILE);
			System.err.println("         You can copy the file");
			System.err.println("           " +  EXAMPLE_PROPS_FILE);
			System.err.println("         to");
			System.err.println("           " + DEV_SERVER_PROPS_FILE);
			System.err.println("         change the properties to fit your user on the biordev server, and try again.");
			//Files.copy(new File(), new File(DEV_SERVER_PROPS_FILE));
			throw new FileNotFoundException("Missing properties file: " + DEV_SERVER_PROPS_FILE);
		}
		
		FileInputStream fin = new FileInputStream(new File(DEV_SERVER_PROPS_FILE)); 
		Properties devServerProps = new Properties();
		devServerProps.load(fin);
		fin.close();
		
		// Throw error if doesn't have all keys or values
		ArrayList<String> missingKeys = getMissingKeys(devServerProps);
		if( missingKeys.size() > 0 )
			throw new MissingArgumentException("Keys or values missing in dev server properties: " + missingKeys.toString());

		return devServerProps;
	}
	
	private void saveProperties(Properties devServerProperties) throws IOException {
		FileOutputStream fout = new FileOutputStream(DEV_SERVER_PROPS_FILE);
		devServerProperties.store(fout, "Connection and SVN info for running remote tests on biordev development server");
		fout.close();
	}




	
	/** Find any missing keys in the properties file that the user should add */
	private ArrayList<String> getMissingKeys(Properties props) throws MissingArgumentException {
		ArrayList<String> missingKeys = new ArrayList<String>();
		for(DevServerUserPropKeys key : DevServerUserPropKeys.values()) {
			if( ! props.containsKey(key.toString()) ||  props.getProperty(key.toString()) == null )
				missingKeys.add(key.toString());
		}
		return missingKeys;
	}

	/** Try to get the method that is calling this parent class' method */
	private String getCallingMethod() {
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		for(int i=0; i < stacks.length; i++)
			System.out.println(stacks[i]);
		return stacks.toString();
	}


}
