package edu.mayo.bior.pipeline.createCatalogProps;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.lang.NotImplementedException;

import com.google.gson.JsonParser;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.ColumnMetaData.Type;

/** Allow us to grab info from either a VCF-formatted datasource, or try to guess the types from a catalog crawler.
 *  These should eventually be converted to ColumnMetaData objects before writing the objects to a columns properties file
 * @author Michael Meiners (m054457)
 * Date created: Aug 16, 2013
 */
public class ColumnTypeCounter {
	/** Approved delimiters that may be used to separate numeric values */
	public static final String[] DELIMITERS = { ",", ";", ":" };

	
	private ColumnMetaData mColMeta = new ColumnMetaData();

	//-----------------------------------------------
	// The following values will be used if trying to guess at the types, counts, etc
	//-----------------------------------------------
	/** The number of times each type occurred in the catalog.  dots are disregarded.
	 *  This will be used to determine the type of object.  */
	private int mCountJson = 0;
	private int mCountJsonArray = 0;
	private int mCountString = 0;
	private int mCountFloat = 0;
	private int mCountInteger = 0;
	private int mCountBoolean = 0;
	
	/** If the value was delimited by one of the approved delimiters, then check the size of the array
	 *  and track how many values occur so we know the number (if consistent) */
	private HashSet<Integer> mNumValuesInDelimited = new HashSet<Integer>();
	
	private int mNumLinesDelimited = 0;
	
	// Use this for testing only as it will take up a lot of memory in production
	private ArrayList<String> examples = new ArrayList<String>();
		
	//=============================================================================
	
	public ColumnTypeCounter(String colName, String description) {
		mColMeta.columnName = colName;
		mColMeta.description = description;
	}
	
	/** Given a value, try to figure out what type it is and increment that type of object */
	public void addValue(String value) {
		value = removeQuotes(value);
		
		// Just ignore it if it is a dot ('.')
		if(".".equals(value))
			return;
		
		boolean isMultiValue = isMultiValue(value);
		Type type = getType(value, isMultiValue);
		incrementType(type);
		
		int count = getCount(value, isMultiValue);
		this.mNumValuesInDelimited.add(count);
		
		if( isMultiValue )
			mNumLinesDelimited++; 
	}
	

	
	/** This should only be called after ALL values have been added and we want to get the final results */
	public ColumnMetaData getColumnMetaData() {
		mColMeta.type  = getTypeFromAllValues();
		mColMeta.count = getCountFromAllValues(mColMeta.type);
		
		return mColMeta;
	}

	//===========================================

	/** If quoted, remove the quotes */
	private String removeQuotes(String val) {
		if( val != null && val.length() > 1  && val.startsWith("\"")  &&  val.endsWith("\"") )
			return val.substring(1, val.length()-1);
		else
			return val;
	}

	private Type getType(String value, boolean isMultiValue) {
		Type type = Type.String;
		if( isBool(value) )
			type = Type.Boolean;
		else if( isInt(value) ) 
			type = Type.Integer;
		else if( isFloat(value) ) 
			type = Type.Float;
		else if( isJson(value) ) 
			type = Type.JSON;
		else if( isJsonArray(value) )
			type = Type.JSONArray;
		else if( isMultiValue ) {
			String delim = getDelimChar(value);
			type = getType(value.split(delim)[0], false);
		} else { // Else just a String that happens to contain a delimiter character
			type = Type.String;
		}
		return type;
	}

	private int getNumDelimiterStyles(String value) {
		int numDelims = 0;
		for( String d : DELIMITERS ) {
			if( value.contains(d) ) {
				numDelims++;
			}
		}
		return numDelims;
	}

	/** Splits the string by the delimiter, then trims, 
	 *  then checks if each split string contains a space - if any do, then return true 
	 *  (this is to rule out descriptions that contain commas and spaces between words)
	 * @param value
	 * @return
	 */
	private boolean isSpacesWithinSplits(String value) {
		String delim = getDelimChar(value);
		String[] strs = value.split(delim);
		for(String s : strs) {
			if( s.trim().contains(" ") )
				return true;
		}
		return false;
	}

	/** Get the number of occurrences of a value in the string if it is delimited.  1 if not delimited */
	private int getCount(String value, boolean isMultiValue) {
		int count = 1;
		if( isMultiValue )
			count = value.split(getDelimChar(value)).length;
		return count;
	}

	
	/** Quick method to tell if a value is make up of a delimited list */ 
	private boolean isMultiValue(String value) {
		return getNumDelimiterStyles(value) == 1  && ! isSpacesWithinSplits(value) && isDelimitedAllSameType(value);
	}
	
	private boolean isDelimited(String value) {
		for( String d : DELIMITERS ) {
			if( value.contains(d) ) {
				return true;
			}
		}
		return false;
	}
	
	private String getDelimChar(String value) {
		for( String d : ColumnTypeCounter.DELIMITERS ) {
			if( value.contains(d) ) {
				return d;
			}
		}
		return null;
	}

	/** Make sure value contains a delimiter character before calling this */
	private boolean isDelimitedAllSameType(String value) {
		String delim = getDelimChar(value);
		boolean isMultiVal = delim != null;
		String[] vals = value.split(delim);
		// Get the type, but since the value is already split, it should not be multiValue
		Type type0 = getType(vals[0], false);
		for(int i=1; i < vals.length; i++) {
			Type typeX = getType(vals[i], false);
			if( ! typeX.equals(type0) )
				return false;
		}
		return true;
	}

	private void incrementType(Type type) {
		if( type.equals(Type.Boolean) )
			this.mCountBoolean++;
		else if( type.equals(Type.Float) )
			this.mCountFloat++;
		else if( type.equals(Type.Integer) )
			this.mCountInteger++;
		else if( type.equals(Type.JSON) )
			this.mCountJson++;
		else if( type.equals(Type.JSONArray) )
			this.mCountJsonArray++;
		else if( type.equals(Type.String) )
			this.mCountString++;
	}

	
	//===========================================
	// Methods performed when all lines have been processed
	//===========================================
	
	/** After ALL values are added, get the correct type of object based on the counts */
	private ColumnMetaData.Type getTypeFromAllValues() {
		int countAll = getAllCount();
		
		Type type = Type.String;
		// If ALL values are boolean, then it is a boolean
		if( mCountBoolean == countAll )
			type = Type.Boolean;
		// Else if ALL values are int, then it is an int
		else if( mCountInteger == countAll )
			type = Type.Integer;
		// Else if all values are either int or float, then it is a float
		else if( (mCountFloat + mCountInteger) == countAll )
			type = Type.Float;
		else if( mCountJson == countAll )
			type = Type.JSON;
		else if( mCountJsonArray == countAll)
			type = Type.JSONArray;
		else
			type = Type.String;
		
		return type;
	}
	
	/** After ALL values are added, get the correct count/number for the object based on the counts<br>
	 *  0 = boolean
	 *  1 = only one occurrence
	 *  2..n = multiple occurrences, but always the same
	 *  . = unknown or varies from row to row */
	private String getCountFromAllValues(Type type) {
		String count = ".";
		
		if( type.equals(Type.Boolean) )
			count = "0";
		else if( type.equals(Type.JSONArray) )
			count = ".";
		else if( type.equals(Type.JSON) )
			count = "1";
		else if( mNumValuesInDelimited.size() == 1 )
			count = mNumValuesInDelimited.iterator().next() + "";
		else
			count = ".";
		
		return count;
	}

	private int getAllCount() {
		return mCountBoolean + mCountFloat + mCountInteger + mCountJson + mCountJsonArray + mCountString; 
	}
	//===========================================

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
	
	// Note:  Just calling Boolean.parseBoolean() or Boolean.valueOf() or Boolean.getBoolean() will 
	//        not be enough to validate if it is a boolean or not, because even "12345" will return false.
	private boolean isBool(String val) {
		if( val == null || val.trim().length() == 0 )
			return false;
		return "true".equalsIgnoreCase(val)  ||  "false".equalsIgnoreCase(val);
	}

	private boolean isJson(String val) {
		try {
			return new JsonParser().parse(val).isJsonObject();
		}catch(Exception e) {
			return false;
		}
	}

	private boolean isJsonArray(String val) {
		try {
			return new JsonParser().parse(val).isJsonArray();
		}catch(Exception e) {
			return false;
		}
	}

	
}
