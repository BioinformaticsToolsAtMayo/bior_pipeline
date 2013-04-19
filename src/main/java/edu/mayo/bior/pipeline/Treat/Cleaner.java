/**
 * bior_pipeline
 *
 * <p>@author Gregory Dougherty</p>
 * Copyright Mayo Clinic, 2011
 *
 */
package edu.mayo.bior.pipeline.Treat;

import edu.mayo.pipes.history.History;

/**
 * Interface for classes that add raw data to a pipeline, and can also clean up that raw data into 
 * something more consistent
 * <p>@author Gregory Dougherty</p>
 *
 */
public interface Cleaner
{
	/**
	 * The one method.  Takes a History that has had raw data added to it, parses that raw data, removes 
	 * it, and adds the parsed data instead
	 * 
	 * @param history	The history to parse
	 * @return	The parsed history
	 */
	public History doClean (History history);
}
