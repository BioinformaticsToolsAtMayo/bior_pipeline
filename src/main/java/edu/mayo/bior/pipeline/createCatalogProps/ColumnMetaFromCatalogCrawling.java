package edu.mayo.bior.pipeline.createCatalogProps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.UNIX.CatGZPipe;
import edu.mayo.pipes.history.ColumnMetaData;

/** Crawls an existing catalog, grabbing all column names, and attempts to guess the type and number from all data in the file
 * @author Michael Meiners (m054457)
 * Date created: Aug 16, 2013
 */
public class ColumnMetaFromCatalogCrawling {

	public static void main(String[] args) {
		try {
			String a = "1.0";
			String[] strs = a.split(";");
			List<ColumnMetaData> colMeta = new ColumnMetaFromCatalogCrawling().getColumnMetadata("src/test/resources/example_dbSNP_catalog.tsv.bgz");
			System.out.println("Done.");
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	/** Process each line in the catalog bgzip file, noting the JSON path, field type, and count (will need to split by delimiters)
	 * @param catalogBgzFilePath
	 * @return
	 * @throws IOException 
	 */
	public List<ColumnMetaData> getColumnMetadata(String catalogBgzipFilePath) throws IOException {
		HashMap<String,ColumnTypeCounter> colMap = new HashMap<String, ColumnTypeCounter>();

		Pipeline pipe = new Pipeline(new CatGZPipe("gzip"));
		pipe.setStarts(Arrays.asList(new File(catalogBgzipFilePath).getCanonicalPath()));
		
		// Read each line from the catalog
		System.out.println("Reading lines from catalog:  .=10k  o=100k  O=1M  X=10M");
		int numLines = 0;
		while(pipe.hasNext()) {
			String row = (String)(pipe.next());
			
			// If this is a blank line or a header line, then skip
			if( row == null || row.length() == 0 || row.startsWith("#") )
				continue;
			
			// Split by tab - JSON will be the last column
			String[] cols = row.split("\t");
			addJsonKeysToMap(cols[3], null, colMap);
			
			// Print an indicator of # of lines read
			numLines++;
			if( numLines % 10000000 == 0 )
				System.out.print("X");
			else if( numLines % 1000000 == 0 )
				System.out.print("O");
			else if( numLines % 100000 == 0 )
				System.out.print("o");
			else if( numLines % 10000 == 0 )
				System.out.print(".");
		}
		System.out.println();

		// Resolve the type of each item and the count for # of occurrences
		List<ColumnMetaData> colMetaList = new ArrayList<ColumnMetaData>();
		for(String key : colMap.keySet()) {
			ColumnTypeCounter colType = colMap.get(key);
			ColumnMetaData colMeta = colType.getColumnMetaData();
			colMetaList.add(colMeta);
		}
		
		return colMetaList;
	}
	
	/** Given a JSON string, pull out all keys.  Ex: {"CHR":1,"POS":111,"INFO":{"SAO":1.0,"SSR":2.0}}<br>
	 *  and values, and add them to the HashMap that maps columnName to ColumnTypeCounter object
	 * @param jsonStr The full JSON string to search
	 * @param parentJsonPath - The parent of the current JSON key (since nested, for "INFO.SSR.KEY1" for KEY1 it would be "INFO.SSR"
	 * @param colMap - The mapping from columnName to ColumnTypeCounter that contains the tally info for that column.  This HashMap will be modified as the method recursively calls itself.
	 * NOTE: modifies the HashMap 
	 */
	private void  addJsonKeysToMap(String jsonStr, String parentJsonPath, HashMap<String,ColumnTypeCounter> colMap) {
	    JsonObject root = new JsonParser().parse(jsonStr).getAsJsonObject();
	    
	    for (Map.Entry<String,JsonElement> entry : root.entrySet()) {
	    	// Key may be several layers deep (such as INFO.OBJ.SOMEKEY), so build the full JSON path
	    	String key = (parentJsonPath == null || parentJsonPath.length() == 0  ?  ""  :  parentJsonPath + ".") + entry.getKey();
            JsonElement value = entry.getValue();
	    	
            // If it is a complex object, then break it down further
            if( value instanceof JsonObject )
            	addJsonKeysToMap(value.getAsJsonObject().toString(), key, colMap);
            else // should be a primitive or an array
            	addKeyValue(key, value.toString(), colMap);
	    }
	}


	/** Add a key and value to the HashMap that maps a columnName to a ColumnTypeCounter object,
	 *  incrementing the appropriate counters depending on what the value is 
	 * @param key - The JSON key
	 * @param value - The JSON value (a primitive)
	 * @param colMap - The mapping between columnName and ColumnTypeCounter
	 */
	private void addKeyValue(String key, String value,	HashMap<String, ColumnTypeCounter> colMap) {
		// Get the ColumnTypeCounter matching the key.  If none, create one and add to hashmap
		ColumnTypeCounter colTypeCount = colMap.get(key);
		if( colTypeCount == null ) {
			colTypeCount = new ColumnTypeCounter(key, "");
			colMap.put(key, colTypeCount);
		}
		
		colTypeCount.addValue(value);
	}
	
}
