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

	private Map<JsonColumn, Integer> mIndex;

	private List<Formatter> mFormatters;
	
	private boolean mIsFirst = true;
	
	/**
	 * Constructor 
	 * 
	 * @param order
	 * 		The order of JSON columns added <b>after</b> the original user's columns.
	 * @param formatters
	 * 		One or more {@link Formatter} implementations to apply to the JSON columns.
	 */
	public FormatterPipeFunction(List<JsonColumn> order, List<Formatter> formatters) {
		mIndex = index(order);
		mFormatters = formatters;
	}
	
	public History compute(History history)
	{
		// remove JSON columns from History + metadata
		List<String> jsonCols = removeJSON(mIndex.size(), history, mIsFirst);
		
		for (Formatter f: mFormatters)
		{
			// get designated JSON column that will be processed
			JsonColumn col = f.getJSONColumn();
			
			if (mIndex.containsKey(col))
			{
				// 1st time through, add column metadata
				if (mIsFirst)
				{
					for (String header: f.getHeaders())
					{
						History.getMetaData().getColumns().add(new ColumnMetaData(header));
					}
				}				
				
				// get JSON for formatter
				String json = jsonCols.get(mIndex.get(col));
				
				// process JSON to get formatted values and add to History
				for (String value: f.format(json))
				{
					history.add(value);
				}				
			}
		}
		
		// toggle flag off
		if (mIsFirst)
			mIsFirst = false;
		
		return history;
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
	private List<String> removeJSON(int numJsonCols, History h, boolean removeMetadata)
	{
		List<String> list = new ArrayList<String>();

		int firstJsonColIdx = h.size() - numJsonCols;
		
		// go in reverse-order from end of History for "safe" removes
		for (int colIdx=h.size() - 1; colIdx >= firstJsonColIdx; colIdx--)
		{
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
}
