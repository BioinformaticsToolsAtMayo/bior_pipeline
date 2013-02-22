package edu.mayo.bior.cli;

import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

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
	 * @param opts commons-cli Options object that can be used to get ahold of the Option object itself
	 *             via the Option.getOption() method.
	 * @throws Exception
	 */
	public void execute(CommandLine line, Options opts) throws Exception;
}
