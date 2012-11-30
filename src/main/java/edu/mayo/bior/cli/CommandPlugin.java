package edu.mayo.bior.cli;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

/**
 * Represents a single command in the command-line-interface.
 * 
 * @author duffp
 *
 */
public interface CommandPlugin {

	/**
	 * Initializes the command.
	 * 
	 * @param props Global properties loaded from CommandLineApp.properties
	 * 
	 * @throws Exception
	 */
	public void init(Properties props) throws Exception;
	
	/**
	 * Executes the command.
	 * @param line commons-cli CommandLine object used to extract Option data.
	 * @param in input stream
	 * @param out output stream
	 * @throws Exception
	 */
	public void execute(CommandLine line, InputStream in, OutputStream out) throws Exception;
}
