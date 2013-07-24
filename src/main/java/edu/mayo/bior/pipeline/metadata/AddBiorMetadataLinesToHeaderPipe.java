package edu.mayo.bior.pipeline.metadata;

import java.io.IOException;
import java.util.ArrayList;
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

public class AddBiorMetadataLinesToHeaderPipe extends AbstractPipe<History,History> {
	
	private static final Logger sLogger = Logger.getLogger(AddBiorMetadataLinesToHeaderPipe.class);
	
	private boolean isHeaderProcessed = false;
	
	public AddBiorMetadataLinesToHeaderPipe() {		
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
		 *  4. For each of the above column, build the BIOR line as:
		 *  	4.1 ##BIOR=<ID=bior.column_name,Operation="command that was run",DataType=JSON/String >
		 *  		##BIOR=<ID=bior.VCF2VariantPipe,Operation="bior_vcf_to_variant",DataType="JSON">
		 *			##BIOR=<ID=bior.dbSNP137,Operation="bior_same_variant",DataType="JSON",CatalogShortUniqueName="dbSNP137",CatalogSource="dbSNP",CatalogVersion="137",CatalogBuild="GRCh37.p10",CatalogPath="/data5/bsi/catalogs/bior/v1/dbSNP/137/00-All-GRCh37.tsv.bgz">
		 *			##BIOR=<ID=bior.dbSNP137.INFO.SSR,Operation="bior_drill",DataType="String",Key="INFO.SSR",Description="Variant suspect reason code (0 - unspecified, 1 - paralog, 2 - byEST, 3 - Para_EST, 4 - oldAlign, 5 - other)",CatalogShortUniqueName="dbSNP137",CatalogSource="dbSNP",CatalogVersion="137",CatalogBuild="GRCh37.p10",CatalogPath="/data5/bsi/catalogs/bior/v1/dbSNP/137/00-All-GRCh37.tsv.bgz">
		 *  	4.2 pull ... from catalogs "datasource.properties" file
		 *  	4.3 pull ID and DESCRIPTION from the catalogs' "column.properties" file. 
		 *  	4.4 
		 *  5. Append each of the BIOR lines to the header of the output VCF file.
		 *     
		 */
        
        if (isHeaderProcessed == false) { //process this only once per file
	        // Stevarp 1
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
	        
	        String datasourceName = null;
	        PropertiesFileUtil propsUtil = null;
	        String catalogShortUniqueName = null;
	        String catalogSource=null;
	        String catalogVersion=null;
	        String catalogBuild=null;
	        String buildHeaderLine=null;
	        List<String> attributes = null;               
	        
	        for(String column_name:cols) {
	        	//System.out.println(column_name);
	        	if (column_name.contains("bior")) {
	        		
	        		String[] colNameSplit = column_name.split("\\.");
	        		
	        		if (colNameSplit.length >= 2) {
	        			attributes = new ArrayList<String>();
	        			
	        			datasourceName = colNameSplit[1];
	        			//System.out.println("ColumnName="+column_name+"; DatasourceName="+datasourceName);
	        			
	        			attributes.add("ID=\""+column_name+"\"");
	        			
	        			//for this datasourcename, find the catalog.datasource.properties file location from the catalogs.properties file
	        			String catalogFile = "src/test/resources/metadata/00-All_GRch37.datasource.properties";
	        			try {
	        				propsUtil = new PropertiesFileUtil(catalogFile);
						
	            			catalogShortUniqueName = propsUtil.get("CatalogShortUniqueName"); 
	            	        catalogSource = propsUtil.get("CatalogSource");
	            	        catalogVersion = propsUtil.get("CatalogVersion");            	 
	            	        catalogBuild = propsUtil.get("CatalogBuild");            	        
	            	        
	            	        attributes.add("CatalogShortUniqueName=\""+catalogShortUniqueName+"\"");
	            	        attributes.add("CatalogSource=\""+catalogSource+"\"");
	            	        attributes.add("CatalogVersion=\""+catalogVersion+"\"");
	            	        attributes.add("CatalogBuild=\""+catalogBuild+"\"");            	        
	            	        
	            	        //S
	            	        
	        			} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	        			
	        			System.out.println("Att="+buildHeaderLine(attributes));
	        			
	        		} else {
	        			System.out.println("no match..");
	        		}
	        	    
	        		
	        	} else {
	        		//System.out.println("not in..");
	        	}
	        }
	        
	        isHeaderProcessed = true;
        } 
        
        // Step 4:
        
        return history;        
    } 
	
	/**
	 * Builds lines like 
	 * 	##BIOR=<ID=bior.column_name,Operation="command that was run",DataType=JSON/String >
	 *  ##BIOR=<ID=bior.VCF2VariantPipe,Operation="bior_vcf_to_variant",DataType="JSON">
	 *	##BIOR=<ID=bior.dbSNP137,Operation="bior_same_variant",DataType="JSON",CatalogShortUniqueName="dbSNP137",CatalogSource="dbSNP",CatalogVersion="137",CatalogBuild="GRCh37.p10",CatalogPath="/data5/bsi/catalogs/bior/v1/dbSNP/137/00-All-GRCh37.tsv.bgz">
	 *	##BIOR=<ID=bior.dbSNP137.INFO.SSR,Operation="bior_drill",DataType="String",Key="INFO.SSR",Description="Variant suspect reason code (0 - unspecified, 1 - paralog, 2 - byEST, 3 - Para_EST, 4 - oldAlign, 5 - other)",CatalogShortUniqueName="dbSNP137",CatalogSource="dbSNP",CatalogVersion="137",CatalogBuild="GRCh37.p10",CatalogPath="/data5/bsi/catalogs/bior/v1/dbSNP/137/00-All-GRCh37.tsv.bgz">
	 * @param attributes
	 * @return
	 */
	private String buildHeaderLine(List<String> attributes) {
		StringBuilder sb = new StringBuilder();
		sb.append("##BIOR=<");
		
		String delim="";
		for(String attrib : attributes) {
			sb.append(delim).append(attrib);
			delim = ",";
		}
		sb.append(">");
		
		return sb.toString();
	}
		
}
