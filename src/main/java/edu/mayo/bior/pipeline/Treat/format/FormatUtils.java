package edu.mayo.bior.pipeline.Treat.format;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;


public class FormatUtils {

	/**
	 * Performs a drill using the specified JSON path.
	 * 
	 * @param path JSON search path to apply.
	 * @param json JSON document to be searched.
	 * @return The extracted path result.  Otherwise, a dot "."
	 */
	public static String drill(JsonPath path, String json)
	{
		try
		{
			return path.read(json).toString(); 
		}
		catch (InvalidPathException ipe)
		{
			return ".";
		}		
	}	
}
