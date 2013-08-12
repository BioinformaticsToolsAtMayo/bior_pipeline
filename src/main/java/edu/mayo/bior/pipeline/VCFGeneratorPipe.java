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
	        List<String> columns = getBIORColumnsFromMetadata();
	        if (columns.size() > 0)
	    		//check if INFO column is there and standard index is 7 and get indexes of BIOR Added Columns 
	    	    if (totalcolumns > 7 && History.getMetaData().getColumns().get(7).getColumnName().equalsIgnoreCase("INFO")){
	    	    	
	    	    	for (int i =7; i < totalcolumns;i++) {
	    	    	String colname =History.getMetaData().getColumns().get(i).getColumnName();
	    	    //		if (colname.toLowerCase().contains("bior")) {
	    	    		if (columns.contains(colname)) {  
	    	    		  biorindexes.put(i,colname) ;
	    	    	 } 
	    	    		
	    	    	} 
	    //remove the columns from ColumnHeader and Modify metadata    
	    addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader());
	    modifyMetadata = true;
	    }
	   
	    
	    
	    }
		
	    history =  removecolumns(modifyhistory(history,biorindexes),biorindexes);
		//history =  addColumnheaders(history);
	    return history;
	    	
		
		
	}

	private void addColumnheaders(List<String> colmeta) {
	   
	//	List<String> colmeta = history.getMetaData().getOriginalHeader();
	
		List<String> infoMeta = new ArrayList<String>();
		List<String> biorList = new ArrayList<String>();
		for (String info: colmeta){
            if (info.startsWith("##BIOR=<ID")) {
            	biorList.add(info);
            }
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
         st.append(",CatalogPath=");
         st.append(meta.get("CatalogPath"));
         st.append(">");
         
         
		 infoMeta.add(st.toString().replaceAll("null", ""))	;
			}
		}
		int inde = colmeta.size();
		
		if (colmeta.addAll(inde-1, infoMeta) && colmeta.removeAll(biorList)) {
		History.getMetaData().setOriginalHeader(colmeta);
		}
		
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

	
	private List<String> getBIORColumnsFromMetadata() {
	  List<String> metadata =	History.getMetaData().getOriginalHeader();
	  List<String> columns = new ArrayList<String>();
	  for (String info: metadata){
          if (info.startsWith("##BIOR=<ID")) {
        	  String[] ast = info.split("<")[1].replace(">", "").split(",");
 
     		  String[] ast3 = ast[0].split("=");
          	  columns.add(ast3[1]);
          }
	  }
	  
	  return columns;
	  }
	
	
	//Modify the history string(VCF row) by appending the columns into INFO 
	
	
	private History modifyhistory(History history, Map<Integer,String> biorindexes2) {
		
		Set<Integer> indexes =   biorindexes.keySet();
		
		Iterator<Integer> iterator = indexes.iterator();
		
		while (iterator.hasNext()) {
			int value = iterator.next();
			String val = null;
		
			if (value < history.size()) {
				val = history.get(value);
			}
			
			if (val != null && !val.isEmpty() && !val.contentEquals(".") && !val.startsWith("{")) {
		   
			String newValue= history.get(7).concat(";" + biorindexes.get(value) +"=" + val);	
		    
		   if (newValue.startsWith(".;"))	
		        history.set(7,newValue.replaceFirst(".;", "")) ;
		    else  
		    	 history.set(7, newValue);
			
			}
		  	
			     
		}
		 return history;
		
	}

}
