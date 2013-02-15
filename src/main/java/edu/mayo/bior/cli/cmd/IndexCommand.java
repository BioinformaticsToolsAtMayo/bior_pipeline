package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.pipes.util.index.IndexDatabaseCreator;

/** Create an index H2 database from a catalog bgzip file */
public class IndexCommand implements CommandPlugin {

	// Column to find key
	private static final char OPTION_COLUMN_CONTAINING_KEY = 'c';

	// JSON path to extract key (if column is specified, the json in that column is used.
	// If not, then the last column is used)
	private static final char OPTION_JSON_PATH = 'p';
	
	public void init(Properties props) throws Exception {
	}

	// Example (selects key from column 4 with JsonPath "HGNC" and the key is an integer):
	//   bior_index  /data/catalogs/NCBIGene/GRCh37_p10/genes.tsv.bgz  /data/catalogs/NCBIGene/GRCh37_p10/index/genes.HGNC.idx.h2.db  -c 4  -p HGNC  -i true
	public void execute(CommandLine line) throws Exception {
		String bgzipPath 	= line.getArgs()[0];
		String h2DbOutPath 	= line.getArgs()[1];

		// JSON may be null if parameter not specified
		// (NOTE: It is NOT required - if not specified, then the entire column will be used as Id)
		String jsonPath = line.getOptionValue(OPTION_JSON_PATH);
                
		// If no column is specified then use the last column
		int col = -1;
		if (line.hasOption(OPTION_COLUMN_CONTAINING_KEY)) {
			col = Integer.parseInt(line.getOptionValue(OPTION_COLUMN_CONTAINING_KEY));
		}

		File indexOutFile = new File(h2DbOutPath);
		if( ! indexOutFile.getParentFile().exists())
			indexOutFile.getParentFile().mkdirs();
		
		IndexDatabaseCreator indexer = new IndexDatabaseCreator();
		indexer.buildIndexH2(bgzipPath, col, jsonPath, h2DbOutPath);
	}

}
