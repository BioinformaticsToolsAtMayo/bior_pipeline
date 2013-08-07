package edu.mayo.bior.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import edu.mayo.bior.cli.cmd.VEPCommand;

public class ClasspathUtil {
	/**
	 * Locates resource at the specified classpath location.
	 * 
	 * @param classpathLocation
	 * 		Classpath based path to the resource.
	 * @return
	 * 		File that represents the resource.
	 * 
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 */
	public static File loadResource(String classpathLocation) throws FileNotFoundException, URISyntaxException
	{
		// locate resources via classpath
		URL url = VEPCommand.class.getResource(classpathLocation);
		if (url == null)
		{
			throw new FileNotFoundException(String.format("Failed to locate resource at classpath location %s ", classpathLocation));
		}
		
		return new File(url.toURI());
	}

}
