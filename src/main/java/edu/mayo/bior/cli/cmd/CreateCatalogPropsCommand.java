package edu.mayo.bior.cli.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;

public class CreateCatalogPropsCommand implements CommandPlugin {

	// Catalog path
	private static final char OPTION_CATALOG = 'd';
	
	private enum DatasourcePropsAttibs {
		CatalogShortUniqueName("CatalogShortUniqueName="," "),
		CatalogDescription("CatalogDescription="," "),
		Patch("Path="," "),
		CatalogSource("CatalogSource="," "),
		CatalogVersion("CatalogVersion="," "),
		CatalogBuild("CatalogBuild="," ");
		
		private String key;
	    private String description;
	    
	    private DatasourcePropsAttibs(String key, String desc){
	    	this.key = key;
	    	this.description = desc;
	    }
		
	    @Override
	    public String toString() {
	        final StringBuilder sb = new StringBuilder();
	        sb.append(key);
	        sb.append(description);
	        //sb.append("/n");
	        return sb.toString();
	    }
	};
	
	public void init(Properties props) throws Exception {
	}
	
	public void execute(CommandLine line, Options opts) throws InvalidOptionArgValueException, InvalidDataException {		
	}
	
	public void execute() throws InvalidOptionArgValueException, InvalidDataException, IOException {

		// Catalog path and key are required		
		//String bgzipPath = line.getOptionValue(OPTION_CATALOG);
		String bgzipPath = "src/test/resources/genes.tsv.bgz";
		
		String msg;
		
		//Create datasource.props file
		if (createDatasourceProps(bgzipPath)) {
			msg = "Successfully created datasource properties file for catalog - " + bgzipPath;
		} else {
			msg = "FAILED!! creating datasource properties file for catalog - " + bgzipPath;
		}
		
		//Create the columns.props file
		
	}

	/**
	 * 
	 * @param userCatalogPath Catalog file name and path as give by the user in the command 
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	private boolean createDatasourceProps(String userCatalogPath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		File catalogFile = new File(userCatalogPath);
		
		if( catalogFile.exists() ) {
			String catalogFullFilename = catalogFile.getName();
			
			String catalogFilename = catalogFullFilename.substring(0, catalogFullFilename.indexOf("."));
			String catalogFilePath = catalogFile.getParent();
						
			System.out.println("Cat:"+catalogFilename+"::"+catalogFilePath);
			
			//Check to see if "catalogFilePath" is WRITABLE
			File dir = new File(catalogFilePath);
			if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
				throw new IOException("Source file directory doesn't exist or is not writable: " + catalogFilePath);
		    }
			
			//Create the props file
			File datasourcePropsFile = new File(catalogFilePath + catalogFile.separator + catalogFilename+".datasource"+".properties");
			datasourcePropsFile.createNewFile();
			System.out.println(datasourcePropsFile.exists());
			
			//write all values from the enum to the file
			List<DatasourcePropsAttibs> dsAttribs = Arrays.asList(DatasourcePropsAttibs.values());
			StringBuilder sb = new StringBuilder();
			
			sb.append("# Datasource properties file for Catalog - ");
			sb.append(catalogFilename);
			sb.append(". Please fill in the description to the below keys.");
			
			for(int i=0;i<dsAttribs.size();i++) {
				sb.append(dsAttribs.get(i));
				sb.append("\n");
			}
			
			//write all keys in enum to the file
			FileWriter fw = new FileWriter(datasourcePropsFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();
			
			return true;
		} else {			
			throw new InvalidOptionArgValueException(
					//opts.getOption(OPTION_CATALOG + ""),
					null,
					userCatalogPath,
					"Catalog file could not be found: " + userCatalogPath
					);			
		}	
		
	}
	
	/**
	 * 
	 * @param userCatalogPath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	private void createColumnProps(String userCatalogPath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		File catalogFile = new File(userCatalogPath);

	}
}
