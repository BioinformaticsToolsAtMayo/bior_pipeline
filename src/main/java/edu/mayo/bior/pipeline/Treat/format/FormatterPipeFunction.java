package edu.mayo.bior.pipeline.Treat.format;

import java.util.ArrayList;
import java.util.Arrays;
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
	private boolean mIsFirstRow = true;
	
	private List<JsonColumn> mOrderOfAddedColumns = new ArrayList<JsonColumn>();
	private List<Formatter>  mAllPossibleFormatters = new ArrayList<Formatter>();
	
	// Keep track of the list of columns to add.  This may not be all of the columns
	// returned from the formatters, since the user can specify a subset in the config file.
	// If a subset of columns is specified in the config file, only those columns will be set in this list
	private List<String> mColsFromConfig;
	
	/** NOTE: These should be be in the order expected in the output, NOT in the order of the JSON columns coming in.
	 *  For example: Hapmap may be the first JSON column in the history, but dbSNP columns may occur first in the output.
	 *  NOTE: This should NOT contain any columns, such as bior_vcf_to_json, that will not appear in the output  */
	private List<ColumnFormatter> mColFormatters = new ArrayList<ColumnFormatter>();
	
	private class ColumnFormatter {
		public int colIdx; // Index of the column in the input history - can be out of order - min=0 corresponding to the first column added by bior_annotate 
		public JsonColumn jsonColIn;
		public Formatter formatter;
		public List<String> userSelectedColNames = new ArrayList<String>();
		// Indexes of the formatter's json columns headers that match the userSelectedColNames - range: 0 to formatter.getHeaders().size()
		public List<Integer> idxFormatterJsonHeader = new ArrayList<Integer>();
	}
	
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
		mOrderOfAddedColumns = order;
		mColsFromConfig = colsFromConfig;
		mColFormatters = createColFormatters(mOrderOfAddedColumns);
	}
	

	public List<String> getColumnsAdded() {
		List<String> colsAdded = new ArrayList<String>();
		for(ColumnFormatter colFmt : mColFormatters)
			colsAdded.addAll(colFmt.userSelectedColNames);
		return colsAdded;
	}

	
	/** ONLY return the list of headers the user wanted - note */
	private List<String> getHeadersUserWanted(JsonColumn jsonCol) {
		List<String> headers = new ArrayList<String>();
		Formatter fmt = getFormatterMatchingColumn(jsonCol);
		// Formatter will be null if JsonColumn.IGNORE or JsonColumn.VARIANT, so just return empty list
		if(fmt == null)
			return headers;
		// If there was not config file OR the column was specified in the config file, then add it
		for(String colHeader : fmt.getHeaders()) {
			if(mColsFromConfig == null  ||  mColsFromConfig.contains(colHeader))
				headers.add(colHeader);
		}
		return headers;
	}
	
	/**
	 * Get all of the formatters that match the given columns that were added
	 * @param order
	 * @return
	 */
	private Formatter getFormatterMatchingColumn(JsonColumn jsonCol) {
		for(Formatter fmt : mAllPossibleFormatters) {
			if(jsonCol.equals(fmt.getJSONColumn()))
				return fmt;
		}
		// Not found, so return null
		return null;
	}

	private List<ColumnFormatter> createColFormatters(List<JsonColumn> orderOfColumnsAdded) {
		Map<JsonColumn, Integer> jsonToColIdxMap = createOrderIndex(orderOfColumnsAdded);
		
		// The list of all formatters determines the order that the columns should appear in
		mAllPossibleFormatters = getAllPossibleFormatters();
		
		List<ColumnFormatter> colFormatters = new ArrayList<ColumnFormatter>();
		for(Formatter fmt : mAllPossibleFormatters) {
			Integer colIdx = jsonToColIdxMap.get(fmt.getJSONColumn());
			// If the JsonColumn was not in the list (wasn't added), then just skip it
			if(colIdx == null)
				continue;

			// Get user-selected headers for this JsonColumn
			List<String> userSelectedHeaders = getHeadersUserWanted(fmt.getJSONColumn());
			
			// If there are no headers wanted from this JsonColumn, then skip column
			if(userSelectedHeaders == null || userSelectedHeaders.size() == 0)
				continue;
			
			ColumnFormatter colFmt = new ColumnFormatter();
			colFormatters.add(colFmt);
			colFmt.colIdx = colIdx;
			colFmt.formatter = fmt;
			colFmt.jsonColIn = fmt.getJSONColumn();
			colFmt.userSelectedColNames = userSelectedHeaders;
			
			List<String> fmtHeaders = fmt.getHeaders();
			for(int i = 0; i < fmtHeaders.size(); i++) {
				if( userSelectedHeaders.contains(fmtHeaders.get(i)) )
					colFmt.idxFormatterJsonHeader.add(i);
			}
		}

		return colFormatters;
	}
	
	public History compute(History history) {
		// Make a copy of the history, but only up to the original data (before the columns bior_annotate added)
		// We will add the columns after that
		int preAnnotateSize = history.size() - mOrderOfAddedColumns.size();
		History newHistory = new History(history.subList(0, preAnnotateSize));
		
		if(mIsFirstRow) {
			addHeaders();
			mIsFirstRow = false;
		}
		
		for(ColumnFormatter colFmt : mColFormatters) {
			int colIdx = colFmt.colIdx + preAnnotateSize;

			List<String> fmtValues  = colFmt.formatter.format(history.get(colIdx));
			for(Integer idxHeader : colFmt.idxFormatterJsonHeader) {
				newHistory.add(fmtValues.get(idxHeader));
			}
		}
		return newHistory;
	}

	

	private void addHeaders() {
		for(ColumnFormatter colFmt : mColFormatters) {
			List<String> cols = colFmt.userSelectedColNames;
			for(String col : cols)
				History.getMetaData().getColumns().add(new ColumnMetaData(col));
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
	private Map<JsonColumn, Integer> createOrderIndex(List<JsonColumn> order) {
		Map<JsonColumn, Integer> index = new HashMap<JsonColumn, Integer>();
		for (int i=0; i < order.size(); i++) {
			index.put(order.get(i), i);
		}
		return index;
	}
	

	/** Note: This is used by TreatPipeline to check the user's config file for any errors */
	public static List<String> getAllPossibleColumns() {
		ArrayList<String> allPossibleColumns = new ArrayList<String>();
		for (Formatter f: getAllPossibleFormatters())
			allPossibleColumns.addAll(f.getHeaders());
		return allPossibleColumns;
	}
	
	
	private static List<Formatter> getAllPossibleFormatters() {
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
