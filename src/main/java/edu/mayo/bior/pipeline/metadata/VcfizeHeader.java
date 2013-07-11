package edu.mayo.bior.pipeline.metadata;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.bior.util.BiorProperties;
import edu.mayo.bior.util.PropertiesFileUtil;
import edu.mayo.pipes.bioinformatics.VCF2VariantPipe;
import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;

public class VcfizeHeader extends AbstractPipe<History,History> {
	
	private static final Logger sLogger = Logger.getLogger(VcfizeHeader.class);
	
	/*
 		From VCF 4.0 format specification:
 	
		INFO fields should be described as follows (all keys are required):
		##INFO=<ID=ID,Number=number,Type=type,Description=description>
		Possible Types for INFO fields are: Integer, Float, Flag, Character, and String.
		A regular expression is used to extract 4 pieces of information:
		1. ID 		(regex grouping #1)
		2. Number	(regex grouping #2)
		3. Type		(regex grouping #3)
        4. Description  (regex grouping #4)
	*/
	private final String mRegexStr = ".+" + "ID=([^,]+)" + ".+"+ "Number=([^,]+)" + ".+" + "Type=(Integer|Float|Flag|Character|String)" + ".+" + "Description=([^,]+)" + ".+";
	private static final int REGEX_GRP_ID   = 1;
	private static final int REGEX_GRP_NUM  = 2;
	private static final int REGEX_GRP_TYPE = 3;
	private static final int REGEX_DESCRIPTION = 4;
	private Pattern mRegexPattern = Pattern.compile(mRegexStr);

	public void VcfizeHeader() {		
	}
	
	@Override
    protected History processNextStart() throws NoSuchElementException, InvalidPipeInputException {
        History history = this.starts.next();
        
        /**
		 *  TODO
		 *    
		 *  PROCESS
		 *  1. Get the entire header form the input file
		 *  2. Get the "Column header row" form the above header
		 *  3. From the above column header row, get all columns that has a prefix "bior_"
		 *  4. For each of the above column, build the INFO line as:
		 *  	4.1 ##INFO=<ID=datasource.column_name, Number=., Type=String, Description="description...">
		 *  	4.2 hard code Number to '.' and Type to 'String'
		 *  	4.3 pull ID and DESCRIPTION from the catalogs' "column.properties" file. 
		 *  	4.4 Read the header line to look for he metadata info for each of the newly added column. This metadata should include the path to the catalog.
		 *  5. Append each of the INFO lines to the header of the output VCF file.
		 *     
		 */
        
        // Step 1
        List<String> headerLines = History.getMetaData().getOriginalHeader();    
        //this.processHeader(headerLines);
        //System.out.println("HL=\n"+Arrays.asList(headerLines));
        
        // Step 2
        ColumnMetaData cmd = new ColumnMetaData(getClass().getSimpleName());
        //System.out.println("Column header row:"+History.getMetaData().getColumnHeaderRow("\t"));
        
        String colHeaderRow = history.getMetaData().getColumnHeaderRow("\t");
        System.out.println("Column header row:"+colHeaderRow);
                
        // Step 3: identify extra columns
        List<String> cols = Arrays.asList(colHeaderRow.split("\t"));
        
        //System.out.println(cols.size());
        
        String datasourceName = "";
        
        for(String column_name:cols) {
        	//System.out.println(column_name);
        	if (column_name.contains("bior")) {
        		
        		String[] colNameSplit = column_name.split("\\.");
        		
        		if (colNameSplit.length >= 2) {
        			datasourceName = colNameSplit[1];
        			System.out.println("ColumnName="+column_name+"; DatasourceName="+datasourceName);
        			 
        			//for this datasourcename, find the catalog.datasource.properties file location from the catalogs.properties file
        			
        			
        		} else {
        			System.out.println("no match..");
        		}
        	    
        		
        	} else {
        		//System.out.println("not in..");
        	}
        }
        
        // Step 4:
        
        return history;        
    }   
	
	private void processHeader(List<String> headerLines){
        for (String row: headerLines) {
        	Matcher m = mRegexPattern.matcher(row);
        	if (m.find()) {
        	}
        }
	}
}
