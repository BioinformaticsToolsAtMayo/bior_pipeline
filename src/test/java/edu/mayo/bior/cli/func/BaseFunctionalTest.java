package edu.mayo.bior.cli.func;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

import edu.mayo.bior.util.StreamConnector;

public abstract class BaseFunctionalTest {

	private static final String ENV_VAR_BIOR_LITE_HOME = "BIOR_LITE_HOME";
	
	// stores the $BIOR_LITE_HOME value
	private static String sHomePath;
	
	// array of UNIX environment variables
	private static String[] sEnvVars;

	@BeforeClass
	public static void setup() throws FileNotFoundException {
		
		sHomePath = getHomeFolder().getAbsolutePath();
		
		// setup UNIX environment variables
		sEnvVars = new String[1];
		sEnvVars[0] = ENV_VAR_BIOR_LITE_HOME + "=" + sHomePath;
	}

	/**
	 * Locates the unzipped distribution folder under target built by  maven.
	 * 
	 * @return
	 * @throws FileNotFoundException thrown if the folder is not found
	 */
	private static File getHomeFolder() throws FileNotFoundException {
		
		File homeFolder = null;
		
		// figure out $BIOR_LITE_HOME value
		String envValue = System.getenv(ENV_VAR_BIOR_LITE_HOME);
		if ((envValue != null) && (envValue.trim().length() > 0)) {
			// use UNIX environment variable if available
			homeFolder = new File(envValue);
			System.out.println("WARNING: found $" + ENV_VAR_BIOR_LITE_HOME + " in your environment.  Running functional test against: " + homeFolder.getAbsolutePath());
		} else {
			// auto-detect inside maven target folder
			File targetFolder = new File("target");
			for (File f: targetFolder.listFiles()) {
				if (f.isDirectory() && (f.getName().startsWith("bior_pipeline"))) {
					homeFolder = f;
				}
			}
		}
		
		if (homeFolder == null) {
			throw new FileNotFoundException("Unable to locate target/bior_pipeline-<version> distribution folder");
		}
		
		return homeFolder;
	}
	
	/**
	 * Executes the specified shell script.
	 * 
	 * @param scriptName
	 *            Name of the shell script
	 * @param stdin
	 *            Content typed to STDIN. Set to NULL if not used
	 * @param scriptArgs
	 *            Zero or more arguments sent to the script
	 * @return CommandOutput bean.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected CommandOutput executeScript(String scriptName, String stdin,
			String... scriptArgs) throws IOException, InterruptedException {

		List<String> cmdList = new ArrayList<String>();

		// add command that will be invoked
		cmdList.add(sHomePath + "/bin/" + scriptName);

		// add command args
		for (String arg : scriptArgs) {
			cmdList.add(arg);
		}
		String[] cmdArray = cmdList.toArray(new String[0]);

		Process p = Runtime.getRuntime().exec(cmdArray, sEnvVars);

		// connect STDERR from script process and store in local memory
		// STDERR [script process] ---> local byte array
		ByteArrayOutputStream stderrData = new ByteArrayOutputStream();
		StreamConnector stderrConnector = new StreamConnector(p.getErrorStream(), stderrData, 1024);
		new Thread(stderrConnector).start();

		// connect STDOUT from script process and store in local memory
		// STDOUT [script process] ---> local byte array
		ByteArrayOutputStream stdoutData = new ByteArrayOutputStream();		
		StreamConnector stdoutConnector = new StreamConnector(p.getInputStream(), stdoutData, 1024);
		new Thread(stdoutConnector).start();

		// feed STDIN into process if necessary
		if (stdin != null) {
			p.getOutputStream().write(stdin.getBytes());
			p.getOutputStream().close();
		}

		// block until process ends
		int exitCode = p.waitFor();

		CommandOutput out = new CommandOutput();
		out.stderr = stderrData.toString("UTF-8");
		out.stdout = stdoutData.toString("UTF-8");
		out.exit = exitCode;
		return out;
	}

	/**
	 * Loads contents of a file into a String object.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	protected String loadFile(File f) throws IOException {
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		BufferedReader br = new BufferedReader(new FileReader(f));

		String line = br.readLine();
		while (line != null) {
			pWtr.println(line);
			line = br.readLine();
		}
		pWtr.close();

		return sWtr.toString();
	}

	/**
	 * Extracts any rows that begin with a '#' character
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	protected String getHeader(String s) throws IOException {
		
		StringReader sRdr = new StringReader(s);
		BufferedReader bRdr = new BufferedReader(sRdr);
		
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		
		String line = bRdr.readLine();
		while (line != null) {
			if (line.startsWith("#")) {
				pWtr.println(line);
			}
			line = bRdr.readLine();
		}
		
		pWtr.close();
		return sWtr.toString();
	}
}
