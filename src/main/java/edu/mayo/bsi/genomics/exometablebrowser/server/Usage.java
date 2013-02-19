/**
 * Mach Builder
 *
 * @author Gregory Dougherty
 * Copyright Mayo Clinic, 2010
 *
 */
package edu.mayo.bsi.genomics.exometablebrowser.server;

/**
 * Interface for classes that have command line arguments.  Companion to {@link GetOpts}
 * 
 * @author Gregory Dougherty
 *
 * @version Revision: 1.1
 */
public interface Usage
{
	/**
	 * Routine that gives a command line app's usage string, to be printed out
	 * whenever the user's command line options are not valid
	 * 
	 * @return	String describing how to call the application
	 */
	public String usage ();
	
	/**
	 * Routine that gives a command line app's name, to be printed out as needed
	 * 
	 * @return	String holding the application's name
	 */
	public String name ();
}
