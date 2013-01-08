package edu.mayo.bior.cli.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.log4j.Logger;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.util.StreamConnector;

/**
 * Command that executes a script.  Since the script will run in a process
 * independent of "this" JVM process, this class does the logic for handling 
 * the STDIN, STDOUT, and STDERR streams between the 2 independent processes.
 *    
 * @author duffp
 *
 */
public abstract class GenericScriptCommand implements CommandPlugin {
	
	private static final Logger sLogger = Logger.getLogger(GenericScriptCommand.class);

	// TODO: does this need to be configurable?
	private static final int BUFFER_SIZE = 1024;

	public void init(Properties props) throws Exception {
	}

	/**
	 * Name of the script to execute.  This should be the absolute path to the script if the script is not on the JVM process' PATH.
	 * 
	 * @param line
	 * @return
	 */
	public abstract String getScriptName(CommandLine line);
	
	/**
	 * Zero or more arguments passed to the script.
	 * 
	 * @param line
	 * @return
	 */
	public abstract String[] getScriptArgs(CommandLine line);
	
	/**
	 * Shell environment variables setup for the script execution.
	 * 
	 * @param line
	 * @return
	 */
	public abstract Map<String, String> getEnvVars(CommandLine line);
	
	public void execute(CommandLine line) throws Exception {

		String script 			= getScriptName(line);
		String[] scriptArgs 	= getScriptArgs(line);
		Map<String, String> env	= getEnvVars(line);
		
		execute(script, scriptArgs, env);
	}

	/**
	 * Executes the script.
	 * 
	 * @param scriptName name of the script
	 * @param scriptArgs array of zero or more script arguments
	 * @param env Map of name/value pairs that represent environment variables
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Exception
	 */
	private void execute(String scriptName, String[] scriptArgs, Map<String, String> env)
			throws IOException, InterruptedException, Exception {

		// translate command and command args into a string array
		List<String> cmdList = new ArrayList<String>();
		cmdList.add(scriptName);
		for (String arg : scriptArgs) {
			cmdList.add(arg);
		}
		String[] cmdArray = cmdList.toArray(new String[0]);

		// translate env vars into a string array
		List<String> envList = new ArrayList<String>();
		for (String name: env.keySet()) {
			String value = env.get(name);
			envList.add(name + "=" + value);
		}
		String[] envArray = envList.toArray(new String[0]);
		
		// setup script process
		Process p = Runtime.getRuntime().exec(cmdArray, envArray);
		
		// connect STDERR from script process and store in local memory
		// STDERR [script process] ---> local byte array
		ByteArrayOutputStream stderrOutputStream = new ByteArrayOutputStream();
		StreamConnector stderrConnector = new StreamConnector(p.getErrorStream(), stderrOutputStream, BUFFER_SIZE);
		new Thread(stderrConnector).start();
		
		// connect STDIN from this JVM process to STDIN for script process
		// STDIN [this jvm] ---> STDIN [script process]
		StreamConnector stdinConnector = new StreamConnector(System.in, p.getOutputStream(), BUFFER_SIZE);
		new Thread(stdinConnector).start();

		// connect STDOUT for script process to STDOUT for this JVM process
		// STDOUT [script process] ---> STDOUT [this jvm]
		StreamConnector stdoutConnector = new StreamConnector(p.getInputStream(), System.out, BUFFER_SIZE);
		new Thread(stdoutConnector).start();

		// block until process ends
		int exitCode = p.waitFor();
		
		// check if process exited abnormally
		String stderr = stderrOutputStream.toString("UTF-8");
		if (stderr.length() > 0) {
			sLogger.error(stderr);
		}
		if (exitCode != 0) {
			throw new Exception(stderr);
		}
	}
	
}