package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;

public class CreateCatalogPropsCommand {

	// Catalog path
	private static final char OPTION_CATALOG = 'd';
	
	private enum datasourcePropsAttibs {
										CatalogShortUniqueName,
										CatalogDescription,
										Patch,
										CatalogSource,
										CatalogVersion,
										CatalogBuild;
										};
	
	public void init(Properties props) throws Exception {
	}
	
	public void execute(CommandLine line, Options opts) throws InvalidOptionArgValueException, InvalidDataException {
		// Catalog path and key are required		
		String bgzipPath = line.getOptionValue(OPTION_CATALOG);
		
		// Throw error if catalog does not exist
		
		File catalogFile = new File(bgzipPath);
		
		if( catalogFile.exists() ) {
			String catalogFilename = catalogFile.getName(); 
			System.out.println("Cat:"+catalogFilename);
		} else {
			throw new InvalidOptionArgValueException(
					opts.getOption(OPTION_CATALOG + ""),
					bgzipPath,
					"Catalog file could not be found: " + bgzipPath
					);
		}
				
		//Create an empty-file/template "catalog.datasource.properties" with attributes in enum
		//TODO Generate file name, from catalog
		
		
	}
		
}
