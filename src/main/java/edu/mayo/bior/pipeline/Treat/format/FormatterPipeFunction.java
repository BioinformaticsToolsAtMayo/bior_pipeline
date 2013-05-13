package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tinkerpop.pipes.PipeFunction;

import edu.mayo.bior.pipeline.Treat.JsonColumn;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;

/**
 * Takes RAW JSON annotation data from the History and formats it into the
 * expected TREAT output format.
 * 
 * @author duffp
 *
 */
public class FormatterPipeFunction implements PipeFunction<History, History>
{

	private Map<JsonColumn, Integer> mJsonIndex;

	
	private boolean mIsFirst = true;
	
	// Keep track of the list of columns to add.  This may not be all of the columns
	// returned from the formatters, since the user can specify a subset in the config file.
	// If a subset of columns is specified in the config file, only those columns will be set in this list
	private List<String> mColsFromConfig;
	
	// Keep track of the columns that were actually added, and the order in which they were added.
	// NOTE: This will be empty until after the first row is processed
	private List<String> mColsAdded = new ArrayList<String>();
	
	
	/**
	 * Constructor 
	 * 
	 * @param order
	 * 		The order of JSON columns added <b>after</b> the original user's columns.
	 * @param formatters
	 * 		One or more {@link Formatter} implementations to apply to the JSON columns.
	 * @param colsFromConfig
	 * 		The list of columns the user wants to see in the output (as specified in the config file)
	 * 		If this is null, then there was no config file specified, so ALL columns will be outputted. 
	 */
	public FormatterPipeFunction(List<JsonColumn> order, List<String> colsFromConfig) {
		mJsonIndex = index(order);
		mColsFromConfig = colsFromConfig;
	}
	
	/** Get the column headers that were added to the output.  	 */
	public List<String> getColumnsAdded() {
		List<String> allCols = getAllPossibleColumns();
		if(mColsFromConfig == null)
			return allCols;
		
		List<String> colsAdded = new ArrayList<String>();
		for(String col : allCols) {
			if(mColsFromConfig.contains(col))
				colsAdded.add(col);
		}
		return mColsAdded;
	}
	
	public History compute(History history)
	{
		// remove JSON columns from History + metadata
		List<String> jsonCols = removeJSON(mJsonIndex.size(), history, mIsFirst);
		
		for(Formatter fmt: getAllPossibleFormatters())	{
			// get the JSON column that data will be pulled from
			JsonColumn col = fmt.getJSONColumn();
			
			// If the list of JSON columns contains the one we want to pull from...
			// (if it does NOT, then the user may have selected to not output from that source)
			if( mJsonIndex.containsKey(col) ) {
				// 1st time through, add column metadata
				if (mIsFirst) {
					addHeaders(fmt);
					mIsFirst = false;
				}
				
				// get JSON for formatter
				String json = jsonCols.get(mJsonIndex.get(col));
				
				// process JSON to get formatted values and add to History
				List<String> headers = fmt.getHeaders();
				List<String> values  = fmt.format(json);
				for(int i=0; i < headers.size(); i++) {
					if( mColsFromConfig == null || mColsFromConfig.contains(headers.get(i)) )
						history.add(values.get(i));
				}
			}
		}
		
		return history;
	}
	
	private void addHeaders(Formatter formatter) {
		for (String header: formatter.getHeaders())	{
			// Add the header if the config file was not specified (which means user wants all output columns)
			// or if the user had specifically wanted that column as output
			if(mColsFromConfig == null  ||  mColsFromConfig.contains(header)) {
				mColsAdded.add(header);
				History.getMetaData().getColumns().add(new ColumnMetaData(header));
			}
		}
	}

	/**
	 * Given the order of JSON columns, put an index by their position in the List
	 * where:
	 * 
	 * <ul>
	 * <li>key is the {@link JsonColumn}</li>
	 * <li>value is the position in the {@link java.util.List}</li>
	 * </ul>
	 * 
	 * @param order
	 * 		The order of JSON columns added <b>after</b> the original user's columns.
	 */
	private Map<JsonColumn, Integer> index(List<JsonColumn> order) {
		Map<JsonColumn, Integer> index = new HashMap<JsonColumn, Integer>();
		
		for (int i=0; i < order.size(); i++) {
			index.put(order.get(i), i);
		}
		
		return index;
	}
	
	/**
	 * Removes all JSON columns from the history and its column metadata.  The 
	 * JSON data is passed back in the same order as it was in the History.
	 * @param numJsonCols
	 * 		The total number of JSON columns. 
	 * @param h
	 * 		The History.
	 * @param removeMetadata
	 * 		Flag that determines whether the Column metadata will be removed as well.  This should only be done once.
	 * @return
	 * 		List of the JSON column data in the <b>same</b> order they were originally added.
	 */
	private List<String> removeJSON(int numJsonCols, History h, boolean removeMetadata) {
		List<String> list = new ArrayList<String>();

		int firstJsonColIdx = h.size() - numJsonCols;
		
		// go in reverse-order from end of History for "safe" removes
		for (int colIdx=h.size() - 1; colIdx >= firstJsonColIdx; colIdx--) {
			String value = h.get(colIdx).trim();

			list.add(value);
			
			if (removeMetadata)
				// remove metadata
				History.getMetaData().getColumns().remove(colIdx);
			
			// remove data
			h.remove(colIdx);
		}
		
		Collections.reverse(list);
		
		return list;
	}
	
	
	public static List<String> getAllPossibleColumns() {
		ArrayList<String> allPossibleColumns = new ArrayList<String>();
		for (Formatter f: getAllPossibleFormatters())
			allPossibleColumns.addAll(f.getHeaders());
		return allPossibleColumns;
	}
	
	public static List<Formatter> getAllPossibleFormatters() {
		List<Formatter> allFormatters = new ArrayList<Formatter>();

		allFormatters.add(new DbsnpFormatter());
		allFormatters.add(new DbsnpClinvarFormatter());
		allFormatters.add(new CosmicFormatter());
		allFormatters.add(new ThousandGenomesFormatter());
		allFormatters.add(new BgiFormatter());
		allFormatters.add(new EspFormatter());
		allFormatters.add(new HapmapFormatter());
		allFormatters.add(new NcbiGeneFormatter());
		allFormatters.add(new HgncFormatter());
		allFormatters.add(new OmimFormatter());
		allFormatters.add(new MirBaseFormatter());
		allFormatters.add(new UcscBlacklistedFormatter());
		allFormatters.add(new UcscConservationFormatter());
		allFormatters.add(new UcscRegulationFormatter());
		allFormatters.add(new UcscTfbsFormatter());
		allFormatters.add(new UcscTssFormatter());
		allFormatters.add(new UcscEnhancerFormatter());
		allFormatters.add(new UcscUniqueFormatter());
		allFormatters.add(new UcscRepeatFormatter());
		allFormatters.add(new VEPFormatter());
		allFormatters.add(new VEPHgncFormatter());
		allFormatters.add(new SNPEffFormatter());
		
		return allFormatters;
	}

}
