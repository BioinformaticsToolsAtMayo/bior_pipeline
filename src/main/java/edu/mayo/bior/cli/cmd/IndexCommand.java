package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.util.index.IndexDatabaseCreator;

/** Create an index H2 database from a catalog bgzip file */
public class IndexCommand implements CommandPlugin {
	// Catalog path
	private static final char OPTION_CATALOG = 'd';
	
	// Index database path to write
	private static final char OPTION_INDEX_PATH = 'x';

	// JSON path to extract key (if column is specified, the json in that column is used.
	// If not, then the last column is used)
	private static final char OPTION_JSON_PATH = 'k';
	
	public void init(Properties props) throws Exception {
	}

	// Example
	//   bior_index  /data/catalogs/NCBIGene/GRCh37_p10/genes.tsv.bgz  -p HGNC  -x /data/catalogs/NCBIGene/GRCh37_p10/index/genes.HGNC.idx.h2.db
	public void execute(CommandLine line, Options opts) throws Exception {
		// Catalog path is required
		String bgzipPath 	= line.getOptionValue(OPTION_CATALOG);
		
		// JsonPath is required
		String jsonPath = line.getOptionValue(OPTION_JSON_PATH);

		// Index path is optional - use it if supplied, else create it from the bgzip catalog path
		String indexDbPathOut = line.getOptionValue(OPTION_INDEX_PATH);
		if(indexDbPathOut == null)
			indexDbPathOut = buildIndexPath(bgzipPath, jsonPath);

		File indexOutFile = new File(indexDbPathOut);
		if( ! indexOutFile.getParentFile().exists())
			indexOutFile.getParentFile().mkdirs();
		
		IndexDatabaseCreator indexer = new IndexDatabaseCreator();
		indexer.buildIndexH2(bgzipPath, -1, jsonPath, indexDbPathOut);
	}

	private String buildIndexPath(String bgzipPath, String jsonPath) throws IOException {
		File bgzipFile = new File(bgzipPath);
		
		// Get the catalog prefix up to the first dot
		String bgzipPrefix = bgzipFile.getName();
		int idxFirstDot = bgzipPrefix.indexOf(".");
		if(idxFirstDot != -1)
			bgzipPrefix = bgzipPrefix.substring(0, idxFirstDot);
		
		File bgzipParentDir = bgzipFile.getParentFile();
		String fullIndexPath = bgzipParentDir.getCanonicalPath() + "/index/" + bgzipPrefix + "." + jsonPath + ".idx.h2.db";
		return fullIndexPath;
	}

}
