package edu.mayo.bior.pipeline.createCatalogProps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.util.Pipeline;

import edu.mayo.pipes.PrintPipe;
import edu.mayo.pipes.UNIX.CatGZPipe;
import edu.mayo.pipes.UNIX.CatPipe;
import edu.mayo.pipes.history.ColumnMetaData;

/** Attempts to generate columns properties file from the original vcf 
 *  file that the catalog came from instead of from the catalog itself
 * @author Michael Meiners (m054457)
 * Date created: Aug 16, 2013
 */
public class ColumnMetaFromVcf {

	public static void main(String[] args) {
		new ColumnMetaFromVcf().getColumnMetadata("src/test/resources/tools/vep/cancer.vcf");
	}
	
	public List<ColumnMetaData> getColumnMetadata(String vcfFilePath) {
		List<String> headerLines = getHeaderLines(vcfFilePath);
		return toColumnMetadata(headerLines);
	}

	private List<ColumnMetaData> toColumnMetadata(List<String> headerLines) {
		List<ColumnMetaData> colMetaList = new ArrayList<ColumnMetaData>();

		for(String line : headerLines) {
			// Just process INFO lines for now
			if( ! line.startsWith("##INFO") )
				continue;
			
			// About vcf format ##INFO lines:  
			// http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
			// Possible Types for INFO fields are: Integer, Float, Flag, Character, and String.
			// Number can be: 1,2,..., A (one value per alt allele), G  (one value for each possible genotype (more relevant to the FORMAT tags))
			ColumnMetaData colMeta = new ColumnMetaData();
			// Make sure to prepend "INFO." onto the front of each of the IDs, 
			// otherwise the columnname will not match those that crawl in the catalog
			colMeta.columnName = "INFO." + between(line, "##INFO=<ID=", ",");
			String type = between(line, "Type=", ",");
			if( type.equals("Integer") )
				colMeta.type = ColumnMetaData.Type.Integer;
			else if( type.equals("Float") )
				colMeta.type = ColumnMetaData.Type.Float;
			else if( type.equals("Flag") )
				colMeta.type = ColumnMetaData.Type.Boolean;
			else if( type.equals("Character") || type.equals("String") )
				colMeta.type = ColumnMetaData.Type.String;
			colMeta.count = between(line, "Number=", ",");
			// NOTE: This assumes that the Description is the last thing on the line - may need to change this!
			colMeta.description = between(line, "Description=\"", "\">");
			
			colMetaList.add(colMeta);
		}
		return colMetaList;
	}
	
	/** Find the string between two substrings. 
	 *  Ex: full = "abcdefg", pre = "ab", post = "ef", then result = "cd"	 */
	private String between(String full, String pre, String post) {
		if( full == null || full.length() == 0 || post == null || post.length() == 0 )
			return "";
		
		int start = full.indexOf(pre);
		int stop  = full.indexOf(post, (start + pre.length()));
		if( start == -1 || stop == -1 )
			return "";
		else 
			return full.substring(start + pre.length(), stop);
	}


	/** Get all metadata header lines from a vcf-formatted file.
	 * Example header line: 
	 *   ##INFO=<ID=PV4,Number=4,Type=Float,Description="P-values for strand bias, baseQ bias, mapQ bias and tail distance bias">
	 * @param vcfFilePath - the vcf file path (can be compressed vcf: vcf.gz or vcf.bgz)
	 * @return - list of all ## or # lines in header
	 */
	private List<String> getHeaderLines(String vcfFilePath) {
		boolean isGzipFile = vcfFilePath.endsWith(".gz") || vcfFilePath.endsWith(".bgz");
		Pipeline pipeline = new Pipeline(
				isGzipFile ? new CatGZPipe("gzip") : new CatPipe(),
				new HeaderOnlyPipe()
				//new PrintPipe()
			);
		List<String> headerLines = new ArrayList<String>();
		pipeline.setStarts(Arrays.asList(vcfFilePath));
		while(pipeline.hasNext()) {
			headerLines.add((String)(pipeline.next()));
		}
		return headerLines;
	}
	
	private class HeaderOnlyPipe extends AbstractPipe<String,String> {
		@Override
		protected String processNextStart() throws NoSuchElementException {
			String line = this.starts.next();
			if( line.startsWith("#") )
				return line;
			else
				throw new NoSuchElementException("Done with header lines");
		}
	}
}
