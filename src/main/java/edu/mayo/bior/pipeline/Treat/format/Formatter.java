package edu.mayo.bior.pipeline.Treat.format;

import java.util.List;

import edu.mayo.bior.pipeline.Treat.JsonColumn;

/**
 * Formats raw annotation from a catalog's JSON into one or more custom-tailored
 * output columns.
 * 
 * @author duffp
 *
 */
public interface Formatter {

	/**
	 * Gets the catalog JSON column that will be processed.
	 * 
	 * @return A {@link JsonColumn} that corresponds to to a catalog JSON column.
	 */
	public JsonColumn getJSONColumn();
	
	/**
	 * Gets the header names of the formatted columns that this Formatter will produce.
	 * The order of the List will be preserved in the final output.  The order of the
	 * headers must align with the formatted values produced by {@link Formatter#format(String)}.
	 * 
	 * @return A list of column headers.
	 */
	public List<String> getHeaders();
	
	/**
	 * Formats the given catalog JSON annotation data into one or more output values.
	 * 
	 * @param json The catalog JSON data to be formatted.
	 * 
	 * @return A list of formatted column values. The order of the values must align
	 * with the headers from {@link Formatter#getHeaders()}.
	 */
	public List<String> format(String json);
}
