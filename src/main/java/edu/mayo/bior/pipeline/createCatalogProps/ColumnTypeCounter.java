package edu.mayo.bior.pipeline.createCatalogProps;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.ColumnMetaData.Type;

/** Allow us to grab info from either a VCF-formatted datasource, or try to guess the types from a catalog crawler.
 *  These are converted to ColumnMetaData objects before writing the objects to a columns properties file
 * @author Michael Meiners (m054457)
 * Date created: Aug 16, 2013
 */
public class ColumnTypeCounter {
	/** Approved delimiters that may be used to separate values */
	public static final String[] DELIMITERS = { ",", ";" }; //, ":" };

	
	private ColumnMetaData mColMeta = new ColumnMetaData();

	// Since JsonArrays will be resolved to whatever their primitives are, we want to know if 
	// the JsonArrays were encountered so we can resolve the count to "."
	private int mCountJsonArray = 0;
	
	// Use this for testing only as it will take up a lot of memory in production
	private ArrayList<String> mExamples = new ArrayList<String>();
		
	//=============================================================================
	
	public ColumnTypeCounter(String colName, String description) {
		mColMeta.columnName = colName;
		mColMeta.description = description;
	}
	
	/** Given a value, try to figure out what type it is and increment that type of object */
	public void addValue(String value) {
		// Just ignore it if it is a dot ('.')
		if(".".equals(value))
			return;
		
		// TEMP: For debugging:
		//if( ! mExamples.contains(value) )
		//	mExamples.add(value);

		Type type = getType(value);
		
		// If type is null, then it was probably an empty JSONArray, so ignore it since we can't determine type
		if( type == null )
			return;
		else
			mColMeta.type = type;
	}
	
	
	/** This should only be called after ALL values have been added and we want to get the final results */
	public ColumnMetaData getColumnMetaData() {
		// If the type is still null, then it must have been a JsonArray that was always empty
		if(mColMeta.type == null)
			mColMeta.type = Type.String;
		
		// Establish count
		if( mColMeta.type.equals(Type.Boolean) ) 
			mColMeta.count = "0";
		else if( mCountJsonArray > 0 ) 
			mColMeta.count = ".";
		else
			mColMeta.count = "1";
			
		// TEMP: to view examples
		//System.out.println("Key: " + mColMeta.columnName + ",  Type: " + mColMeta.type + ", Count: " + mColMeta.count + ",   Examples: " + mExamples);
		
		return mColMeta;
	}

	private Type getType(String value) {
		Type type = null;
		JsonElement jsonElem = new JsonParser().parse(value);
		if( jsonElem.isJsonObject() )
			type = Type.JSON;
		else if( jsonElem.isJsonArray() ) {
			type = getTypeFromJsonArray((JsonArray)jsonElem);
			mCountJsonArray++;
		}else if( jsonElem.isJsonPrimitive() )
			type = getTypeFromJsonPrimitive((JsonPrimitive)jsonElem);
		else
			type = Type.String;
		return type;
	}
	
	private Type getTypeFromJsonArray(JsonArray jsonArray) {
		// If empty list, then return null since we can't determine type
		if( jsonArray == null || jsonArray.size() == 0 )
			return null;
		JsonElement elem = jsonArray.get(0);
		Type type = null;
		if( elem.isJsonObject() )
			type = Type.JSON;
		else if( elem.isJsonPrimitive() ) {
			type = getTypeFromJsonPrimitive((JsonPrimitive)elem);
		} else
			type = Type.String;
		return type;
	}
	
	private Type getTypeFromJsonPrimitive(JsonPrimitive prim) {
		Type type = null;
		if( prim.isBoolean() )
			type = Type.Boolean;
		else if( prim.isNumber() ) {
			if( isInt(prim.toString()) )
				type = Type.Integer;
			else if( isFloat(prim.toString()) )
				type = Type.Float;
		} else
			type = Type.String;
		return type;
	}

	private boolean isFloat(String val) {
		try {
			Float.parseFloat(val);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	private boolean isInt(String val) {
		try {
			Integer.parseInt(val);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
}
