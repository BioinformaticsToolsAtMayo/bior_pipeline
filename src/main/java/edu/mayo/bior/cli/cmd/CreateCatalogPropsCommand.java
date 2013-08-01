package edu.mayo.bior.cli.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.UNIX.CatGZPipe;

public class CreateCatalogPropsCommand implements CommandPlugin {

	// Catalog path
	private static final char OPTION_CATALOG = 'd';
	
	private enum DatasourcePropsAttibs {
		CatalogShortUniqueName,
		CatalogDescription,
		Patch,
		CatalogSource,
		CatalogVersion,
		CatalogBuild;
		
	    @Override
	    public String toString() {
	    	String s = super.toString();
	    	return s + "=";
	    }
	};
	
	public void init(Properties props) throws Exception {
	}
	
	public void execute(CommandLine line, Options opts) throws InvalidOptionArgValueException, InvalidDataException, IOException {		
		// Catalog path and key are required		
		//String bgzipPath = line.getOptionValue(OPTION_CATALOG);
		String bgzipPath = "src/test/resources/genes.tsv.bgz";
		
		String msg;
		
		File catalogFile = new File(bgzipPath);
		
		if( catalogFile.exists() ) {
			String catalogFullFilename = catalogFile.getName();
			String catalogFilename = catalogFullFilename.substring(0, catalogFullFilename.indexOf("."));
			String catalogFilePath = catalogFile.getParent();
			
			// Create the datasource.props file
			createDatasourcePropsFile(catalogFile, catalogFilename, catalogFilePath);
			
			// Create the columns.props file
			createColumnPropsFile(catalogFile, catalogFilename, catalogFilePath);
			
		} else {
			throw new InvalidOptionArgValueException(
					//opts.getOption(OPTION_CATALOG + ""),
					null,
					bgzipPath,
					"Catalog file could not be found: " + bgzipPath
					);
		}
	}

	/**
	 * 
	 * @param catalogFile
	 * @param catalogFilename
	 * @param catalogFilePath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	private void createDatasourcePropsFile(File catalogFile, String catalogFilename, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		//Check to see if "catalogFilePath" is WRITABLE
		File dir = new File(catalogFilePath);
		if (!dir.exists() || !dir.isDirectory() || !dir.canWrite()) {
			throw new IOException("Source file directory doesn't exist or is not writable: " + catalogFilePath);
	    }
		
		//Create the props file
		File datasourcePropsFile = new File(catalogFilePath + catalogFile.separator + catalogFilename+".datasource"+".properties");
		datasourcePropsFile.createNewFile();
		//System.out.println(datasourcePropsFile.exists());
		
		//write all values from the enum to the file
		List<DatasourcePropsAttibs> dsAttribs1 = Arrays.asList(DatasourcePropsAttibs.values());
		List<String> dsAttribs = new ArrayList<String>();
		for (DatasourcePropsAttibs val : dsAttribs1) {
			dsAttribs.add(val.toString());
		}

		String content = buildContent(catalogFilename, "Datasource", dsAttribs);
		
		this.writeToFile(datasourcePropsFile, content);

		//return true;
	}
	

	/**
	 * 
	 * @param catalogFile
	 * @param catalogFilename
	 * @param catalogFilePath
	 * @throws InvalidOptionArgValueException
	 * @throws InvalidDataException
	 * @throws IOException
	 */
	private void createColumnPropsFile(File catalogFile, String catalogFilename, String catalogFilePath) throws InvalidOptionArgValueException, InvalidDataException, IOException {
		
		Pipeline pipe = new Pipeline(new CatGZPipe("gzip"));
		pipe.setStarts(Arrays.asList(catalogFile.getPath()));
		
		String jsonLine = null;
		while(pipe.hasNext()) {
			String nextVal = (String)pipe.next();
			if (!nextVal.startsWith("#")) {
				String[] aVal = nextVal.split("\t");
				jsonLine = aVal[3];
				//System.out.println(jsonLine);
				
				break;
			}			
		}
		
		List<String> jKeys = new ArrayList<String>();
	    JsonObject root = new JsonParser().parse(jsonLine).getAsJsonObject();

	    for (Map.Entry<String,JsonElement> entry : root.entrySet()) {
	    	//TODO handle arrays within the json
	    	jKeys.add(entry.getKey()+"=");
	    }    

	    //Create the props file
	    File columnPropsFile = new File(catalogFilePath + catalogFile.separator + catalogFilename+".columns"+".properties");
	    columnPropsFile.createNewFile();

		String content = buildContent(catalogFilename, "Column", jKeys);
		
		this.writeToFile(columnPropsFile, content);			
		
		//return true;		
	}
	
	
	/**
	 * Write content to a file
	 * @param source
	 * @param content
	 * @throws IOException
	 */
	private void writeToFile(File source, String content) throws IOException {		
		//write all keys in enum to the file
		FileWriter fw = new FileWriter(source.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(content);
		bw.close();
	}
	
	/**
	 * 
	 * @param catalogFilename
	 * @param type - Datasource or Columns
	 * @param listValues
	 * @return
	 */
	private String buildContent(String catalogFilename, String type, List<String> listValues) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("# " + type + " properties file for Catalog - ");
		sb.append(catalogFilename);
		sb.append(". Please fill in the description to the below keys.\n");
		
		for(int i=0;i<listValues.size();i++) {
			sb.append(listValues.get(i));
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
