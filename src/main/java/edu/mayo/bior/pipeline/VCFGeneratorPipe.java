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
	Map<Integer,String> biorDrillindexes = new HashMap<Integer,String>();
	int totalcolumns;
	
	boolean modifyMetadata = false;
	List<String> biorcolumnsFromMetadata = new ArrayList<String>();
    List<String> colsFromHeader = new ArrayList<String>();
	
	
	@Override
	protected History processNextStart() throws NoSuchElementException {
		
		History history = this.starts.next();
		

	    // Modify Metadata only once
		if (!modifyMetadata){
	    	totalcolumns = History.getMetaData().getColumns().size();
	    	biorcolumnsFromMetadata = getBIORColumnsFromMetadata(History.getMetaData().getOriginalHeader()); 
	    	colsFromHeader = getBIORColumnsFromHeader(history.getMetaData().getOriginalHeader(),biorcolumnsFromMetadata);
	    	biorindexes = getBiorColumnsIndexes(totalcolumns,biorcolumnsFromMetadata,history);
	        //checks if biorcolumns is not null
	    	if (biorcolumnsFromMetadata != null ) {
	    	
	    	if (biorcolumnsFromMetadata.containsAll(colsFromHeader) && biorcolumnsFromMetadata.size() == colsFromHeader.size()){
	        
	        
    	    	History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader(),null,null));
	        
	        } else if (biorcolumnsFromMetadata.containsAll(colsFromHeader) && biorcolumnsFromMetadata.size() > colsFromHeader.size()) {
	        	
	        	
	        	    List<String> biorcolumn = biorcolumnsFromMetadata;
	        	    biorcolumn.removeAll(colsFromHeader);
	        	    History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader(),null,biorcolumn));  
	        
	        } else if (colsFromHeader.containsAll(biorcolumnsFromMetadata) && colsFromHeader.size() > biorcolumnsFromMetadata.size()) {
	        	
	        	
	        	List<String> addDefaultColumn = colsFromHeader;
        	    addDefaultColumn.removeAll(biorcolumnsFromMetadata);
        	    History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader(),addDefaultColumn,null));  
	        
	        } else if (!colsFromHeader.containsAll(biorcolumnsFromMetadata) || !biorcolumnsFromMetadata.containsAll(colsFromHeader)) {
	        	
	        	  
	               List<String> biorcolumn = biorcolumnsFromMetadata;
	        	   biorcolumn.removeAll(colsFromHeader);
	        	   List<String> addDefaultColumn = colsFromHeader;
	        	   addDefaultColumn.removeAll(biorcolumnsFromMetadata);
	        	   History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader(),addDefaultColumn,biorcolumn));      
	        	
	        }
	    } else {
	    	
	    	//No ##BIOR available since bior
	    	History.getMetaData().setOriginalHeader(addColumnheaders((removeColumnHeader(History.getMetaData(),biorindexes )).getOriginalHeader(),colsFromHeader,null)); 
	    
	    }
	        modifyMetadata = true;
	    
	    }
	    
		
		history =  removecolumns(modifyhistory(history,biorindexes),biorindexes);
		
	    return history;
	    	
		
		
	}

	// Returns a Map of BioR column index and name
        /**
         * 
         * @param totalcolumn - total
         * @param biorcolumn - of those columns in the history, this is a list of the BioR columns (determined by 
         * @param history
         * @return 
         */
	private Map<Integer,String> getBiorColumnsIndexes(int totalcolumn,List<String> biorcolumn,History history) {
		Map<Integer,String> biorindex = new HashMap<Integer,String>();
		
		if (totalcolumn > 7 && History.getMetaData().getColumns().get(7).getColumnName().equalsIgnoreCase("INFO")){
	    	 
  	    	for (int i =7; i < totalcolumn;i++) {
  	    	
  	    		String colname =History.getMetaData().getColumns().get(i).getColumnName(); 
  	    		
  	    	         if(colname.contains("bior") || colname.contains("BIOR")) {
  	    		 	
  	    		          biorindex.put(i,colname) ;

  	    		       } else if (biorcolumn.contains(colname)) {
  		        	    	
	        	    	      biorindex.put(i, colname);
	        	    }	
  	    	
  	    		
  	    	    } 
		}
	    return biorindex;
	}
	
	
	//Add ##INFO columns to the Metadata lines
	/* 
	 * @param colmeta (List<String> of current metadata)
	 * @param retain (List of BioRColumns that has no ##BIOR...Constructs default ##INFO)
	 * @param remove (List of BioRColumns that are only mentioned in ##BIOR but not in ColumnHeader(no data available))
	 * @returns List of modified metadata after adding ##INFO Columns
	 */
	private List<String> addColumnheaders(List<String> colmeta,List<String> retain,List<String> remove) {
	   
	
		List<String> infoMeta = new ArrayList<String>();
		List<String> biorList = new ArrayList<String>();
		
		for (String info: colmeta){
           //Places all BIOR Headers in a list
			if (info.startsWith("##BIOR=<ID")) {
            	biorList.add(info);
            }
		
         if (info.startsWith("##BIOR=<ID") && info.contains("drill")){
		 //Splits ##BIOR into list of Key=value pairs 
		 String[] ast = info.split("<")[1].replace(">", "").split(",");
		
        
		 HashMap<String,String> meta = new HashMap<String,String>();
         
		 //Traverses through the key,value pairs and place them in map
		 for (String as:ast){
        	 String[] ast1 = as.split("=");
        	 meta.put(ast1[0].replace("\"", ""), ast1[1].replace("\"", ""));
         }
		
         //Checks if the ID is not in list that has only Metadata and Builds the INFO
         if ( remove == null || !remove.contains(meta.get("ID"))) {
         StringBuilder st = new StringBuilder();
         st.append("##INFO=<ID=");
         st.append(meta.get("ID"));
         st.append(",Number=.,");
         st.append("Type=String,");
         st.append("Description=\"");
         st.append(meta.get("Description"));
         st.append("\",CatalogShortUniqueName=\"");
         st.append(meta.get("CatalogShortUniqueName"));
         st.append("\",CatalogVersion=\"");
         st.append(meta.get("CatalogVersion"));
         st.append("\",CatalogBuild=\"");
         st.append(meta.get("CatalogBuild"));
         st.append("\",CatalogPath=\"");
         st.append(meta.get("CatalogPath"));
         st.append("\">");
         
         
		 infoMeta.add(st.toString().replaceAll("null", ""))	;
         }
         
         }
		}
		
		// Builds default Info Metadata line when ##BIOR is not available
		if ( retain != null && !retain.isEmpty()) {
			for (String addDefault: retain) {
				 StringBuilder st1 = new StringBuilder();
		         st1.append("##INFO=<ID=");
		         st1.append(addDefault);
		         st1.append(",Number=.,");
		         st1.append("Type=String,");
		         st1.append("Description=");
		     //    st1.append("");
		         st1.append(",CatalogShortUniqueName=");
		     //    st1.append("");
		         st1.append(",CatalogVersion=");
		      //   st1.append("");
		         st1.append(",CatalogBuild=");
		    //     st1.append("");
		         st1.append(",CatalogPath=");
		    //     st1.append("");
		         st1.append(">");
		         infoMeta.add(st1.toString().replaceAll("null", ""))	;
			}
		}
		
		
		
		int index = colmeta.size();
		
		//add the info columns before the #CHROM column header
		colmeta.addAll(index-1, infoMeta);
		colmeta.removeAll(biorList);
		return colmeta;
	}

	
	//removes columnMetadata from header
	
	private HistoryMetaData removeColumnHeader(HistoryMetaData metaData,Map<Integer, String> biorindexes2) {
		
		List<Integer> indexes =   new ArrayList<Integer>(biorindexes2.keySet());
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
		
		List<Integer> indexes =   new ArrayList<Integer>(biorindexes2.keySet());
		Collections.sort(indexes);
		int i = 0;
		for (Integer j : indexes) {
			
			modifyhistory.remove(j - i);
			i++;
		}
		
		return modifyhistory;
		
	}


        /**
         * Extracts the List of Bior Columns looking at Metadata (##BIOR lines)
         * @param metadata - the original header lines including ##BIOR lines
         * @return 
         */
	public List<String> getBIORColumnsFromMetadata(List<String> metadata) {
	  List<String> columns = new ArrayList<String>();
	  for (String info: metadata){
          if (info.startsWith("##BIOR=<ID")) {
        	  String[] ast = info.split("<")[1].replace(">", "").split(",")[0].split("=");
 
          	  columns.add(ast[1].replace("\"", ""));
          }
	  }
	  
	  return columns;
	  }
	
	//Extracts the List of BioR Columns looking at column header
        /**
         *   
         * @param originalheader - header passed to historyIn
         * @param biorcolumn     - all the ##bior rows in the header
         * @return the columns from the header that are bior columns.
         */
	private List<String> getBIORColumnsFromHeader(List<String> originalheader,List<String> biorcolumn) {
	 List<String> columns = new ArrayList<String>();
	 int indexsize = originalheader.size();
	 String columnheader = originalheader.get(indexsize-1);
	 if (originalheader.get(indexsize -1).startsWith("#CHROM")) {
	
	   String[] column = columnheader.split("\t");	 
	  List<String> columnlist = Arrays.asList(column);
	  for (String colname: columnlist){
	        
	     //   String colname =History.getMetaData().getColumns().get(j).getColumnName(); 
	        	   
	              if (colname.contains("bior") || colname.contains("BIOR")) {
	        	    	
	            	           columns.add(colname);
	        	    	
	        	    } else if (biorcolumn.contains(colname)) {
	        	    	
	        	    	      columns.add(colname);
	        	    }
	         
	          }
	 
	 }
	 return columns;
		
	}

	
	//Modify the history string(VCF row) by appending the columns into INFO 
	
	
	private History modifyhistory(History history, Map<Integer,String> biorindexes2) {
		
		Set<Integer> indexes =   biorindexes2.keySet();
		
		Iterator<Integer> iterator = indexes.iterator();
		
		while (iterator.hasNext()) {
			int value = iterator.next();
			String val = null;
		    //Checks sure the index of BioR is within the history 
			if (value < history.size()) {
			
				
			val = history.get(value);
				
			   if (val != null && !val.isEmpty() && !val.contentEquals(".") && !val.startsWith("{")) {
		   
			             String newValue= history.get(7).concat(";" + biorindexes2.get(value) +"=" + val);	
		    
		                        if (newValue.startsWith(".;"))	
		       
		    	                    history.set(7,newValue.replaceFirst(".;", "")) ;
		                         else  
		    	 
		    	                      history.set(7, newValue);
			
			                     }
		  	
		               }	     
		
		     }
		 return history;
		
	}

}
