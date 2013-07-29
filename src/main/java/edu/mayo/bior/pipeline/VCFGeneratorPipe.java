package edu.mayo.bior.pipeline;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;
import edu.mayo.pipes.history.HistoryMetaData;

public class VCFGeneratorPipe extends AbstractPipe<History,History> {

	private static final Logger sLogger = Logger.getLogger(VCFGeneratorPipe.class);
	Map<Integer,String> biorindexes = new HashMap<Integer,String>();
	
	int totalcolumns;
	
	boolean modifyMetadata = false;
	
	
	
	@Override
	protected History processNextStart() throws NoSuchElementException {
		
		History history = this.starts.next();
		
	
	    // Modify Metadata only once
		if (modifyMetadata == false){
	    	totalcolumns = History.getMetaData().getColumns().size();
	    
	    		//check if INFO column is there and standard index is 7 and get indexes of BIOR Added Columns 
	    	    if (totalcolumns > 7 && History.getMetaData().getColumns().get(7).getColumnName().equalsIgnoreCase("INFO")){
	    	    	
	    	    	for (int i =7; i < totalcolumns;i++) {
	    	    	String colname =History.getMetaData().getColumns().get(i).getColumnName();
	    	    		if (colname.toLowerCase().contains("bior")) {
	    	    		  
	    	    		  biorindexes.put(i,colname) ;
	    	    	 } 
	    	    		
	    	    	} 
	    //remove the columns from ColumnHeader    
	    removeColumnHeader(History.getMetaData(),biorindexes );
	    modifyMetadata = true;
	    }
	   
	    
	    
	    }
		
	    history =  removecolumns(modifyhistory(history,biorindexes),biorindexes);
		history =  addColumnheaders(history);
	    return history;
	    	
		
		
	}

	private History addColumnheaders(History history) {
	   
		List<String> colmeta = history.getMetaData().getOriginalHeader();
	
		
		for (String info: colmeta){

			if (info.startsWith("##BIOR=<ID") && info.contains("drill")){
		 
		 String ast = info.split("<")[1];
		 String ast1 = ast.replace(">", "");
		 String[] ast2 = ast1.split(",");
        
		 HashMap<String,String> meta = new HashMap<String,String>();
         for (String as:ast2){
        	String[] ast3 = as.split("=");
        	meta.put(ast3[0], ast3[1]);
         }
		
         StringBuilder st = new StringBuilder();
         st.append("##INFO=<ID=");
         st.append(meta.get("ID"));
         st.append(",Number=.,");
         st.append("Type=String,");
         st.append("Description=");
         st.append(meta.get("Description"));
         st.append(",CatalogShortUniqueName=");
         st.append(meta.get("CatalogShortUniqueName"));
         st.append(",CatalogVersion=");
         st.append(meta.get("CatalogVersion"));
         st.append(",CatalogBuild=");
         st.append(meta.get("CatalogBuild"));
         st.append(",CatalogPath");
         st.append(meta.get("CatalogPath"));
         st.append("\">");
         
   
		 colmeta.add(st.toString())	;
			}
		}
		
		history.getMetaData().setOriginalHeader(colmeta);
		return history;
	}

	
	//removes columns from header
	
	private HistoryMetaData removeColumnHeader(HistoryMetaData metaData,Map<Integer, String> biorindexes2) {
		List<Integer> indexes =   new ArrayList<Integer>(biorindexes.keySet());
		Collections.sort(indexes);
		int i = 0;
		for (Integer j : indexes) {
			
		 metaData.getColumns().remove(j - i);
			i++;
		}
		
		return metaData;
	}

	
	//removes columns from history after appending them to INFO column
	
	private History removecolumns(History modifyhistory, Map<Integer, String> biorindexes2) {
		
		List<Integer> indexes =   new ArrayList<Integer>(biorindexes.keySet());
		Collections.sort(indexes);
		int i = 0;
		for (Integer j : indexes) {
			
			modifyhistory.remove(j - i);
			i++;
		}
		
		return modifyhistory;
		
	}

	
	
	
	
	//Modify the history string(VCF row) by appending the columns into INFO 
	
	
	private History modifyhistory(History history, Map<Integer,String> biorindexes) {
		
		Set<Integer> indexes =   biorindexes.keySet();
		
		Iterator<Integer> iterator = indexes.iterator();
		
		while (iterator.hasNext()) {
			int value = iterator.next();
			String val = null;
		
			if (value < history.size()) {
				val = history.get(value);
			}
			
			if (val != null && !val.isEmpty() && !val.contentEquals(".") && !val.startsWith("{")) {
			String newValue = history.get(7).concat(";" + biorindexes.get(value) +"=" + val);	
			history.set(7,newValue) ;
			
			}
		  	
			     
		}
		 return history;
		
	}

}
