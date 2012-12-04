package edu.mayo.bior.cli.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;

public abstract class BaseFunctionalTest {

	// stores the $BIOR_LITE_HOME value
	private static String sHomePath;
	
	// array of UNIX environment variables
	private static String[] sEnvVars;

	@BeforeClass
	public static void setup() throws FileNotFoundException {
		
		sHomePath = getHomeFolder().getAbsolutePath();

		// setup UNIX environment variables
		sEnvVars = new String[1];
		sEnvVars[0] = "BIOR_LITE_HOME=" + sHomePath;
	}

	/**
	 * Locates the unzipped distribution folder under target built by  maven.
	 * 
	 * @return
	 * @throws FileNotFoundException thrown if the folder is not found
	 */
	private static File getHomeFolder() throws FileNotFoundException {
		// figure out $BIOR_LITE_HOME value
		File targetFolder = new File("target");
		for (File f: targetFolder.listFiles()) {
			if (f.isDirectory() && (f.getName().startsWith("bior_pipeline"))) {
				return f;
			}
		}		

		throw new FileNotFoundException("Unable to locate target/bior_pipeline-<version> distribution folder");
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

		// setup gobblers to grab the STDOUT and STDERR
		StreamGobbler stderrGobbler = new StreamGobbler(p.getErrorStream());
		StreamGobbler stdoutGobbler = new StreamGobbler(p.getInputStream());
		stderrGobbler.start();
		stdoutGobbler.start();

		// feed STDIN into process if necessary
		if (stdin != null) {
			p.getOutputStream().write(stdin.getBytes());
			p.getOutputStream().close();
		}

		// block until process ends
		int exitCode = p.waitFor();

		CommandOutput out = new CommandOutput();
		out.stderr = stderrGobbler.getStreamContent();
		out.stdout = stdoutGobbler.getStreamContent();
		out.exit = exitCode;
		return out;
	}

	/**
	 * Grabbed from
	 * http://www.javaworld.com/jw-12-2000/jw-1229-traps.html?page=4 with slight
	 * mods.
	 */
	class StreamGobbler extends Thread {
		private InputStream mInputStream;
		private StringWriter mStrWtr = new StringWriter();

		StreamGobbler(InputStream is) {
			this.mInputStream = is;
		}

		public void run() {
			PrintWriter pWtr = new PrintWriter(mStrWtr);

			try {
				InputStreamReader isr = new InputStreamReader(mInputStream);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					pWtr.println(line);
				}
				pWtr.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public String getStreamContent() {
			return mStrWtr.toString();
		}
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
}
