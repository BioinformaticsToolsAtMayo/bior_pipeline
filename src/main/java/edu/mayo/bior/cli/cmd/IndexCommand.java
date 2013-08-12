package edu.mayo.bior.cli.cmd;

import java.io.File;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import edu.mayo.cli.CommandPlugin;
import edu.mayo.cli.InvalidOptionArgValueException;
import edu.mayo.pipes.JSON.lookup.lookupUtils.IndexUtils;
import edu.mayo.pipes.util.index.IndexDatabaseCreator;

/** Create an index H2 database from a catalog bgzip file */
public class IndexCommand implements CommandPlugin {
	// Catalog path
	private static final char OPTION_CATALOG = 'd';
	
	// Index database path to write
	private static final char OPTION_INDEX_PATH = 'i';

	// JSON path to extract key (if column is specified, the json in that column is used.
	// If not, then the last column is used)
	private static final char OPTION_KEY = 'p';
	
	public void init(Properties props) throws Exception {
	}

	// Example
	//   bior_index  -d /data/catalogs/NCBIGene/GRCh37_p10/genes.tsv.bgz  -p HGNC  -i /data/catalogs/NCBIGene/GRCh37_p10/index/genes.HGNC.idx.h2.db
	public void execute(CommandLine line, Options opts) throws Exception {
		// Catalog path and key are required
		String bgzipPath = line.getOptionValue(OPTION_CATALOG);
		String key 		 = line.getOptionValue(OPTION_KEY);
                
                File f = new File(bgzipPath);
                if(f.isDirectory()){
                   throw new InvalidOptionArgValueException(
					opts.getOption(OPTION_CATALOG + ""),
					bgzipPath,
					"The following file is a directory (it must be a file): " + bgzipPath
					);
                }

		// Throw error if catalog does not exist
		if( ! new File(bgzipPath).exists() ) {
			throw new InvalidOptionArgValueException(
					opts.getOption(OPTION_CATALOG + ""),
					bgzipPath,
					"Catalog file could not be found: " + bgzipPath
					);
		}
		
		// Index path is optional - use it if supplied, else create it from the bgzip catalog path
		String indexDbPathOut = line.getOptionValue(OPTION_INDEX_PATH);
                
                File f2 = new File(indexDbPathOut);
                if(f2.isDirectory()){
                    throw new InvalidOptionArgValueException(
					opts.getOption(OPTION_CATALOG + ""),
					indexDbPathOut,
					"The following file is a directory (it must be a file): " + indexDbPathOut
					);
                }
                                
                
		if(indexDbPathOut == null) {
			//if(catalogLineCount <= 1000000)
				// TODO: Create a text file index instead
			indexDbPathOut = IndexUtils.getH2DbIndexPath(bgzipPath, key);
		}
                
                if(!f2.getName().startsWith(
                    f.getName().replaceAll(".tsv.bgz", "")
                        )){
                    System.err.println("Warning: Your file and your index do not have the same prefix.  Make sure you want to do this\n" +
                            "File: " + bgzipPath +
                            "\nIndex: " + indexDbPathOut
                            );
                    
                }
		
		IndexUtils.createParentDirectories(indexDbPathOut);
		
		IndexDatabaseCreator indexer = new IndexDatabaseCreator();
		indexer.buildIndexH2(bgzipPath, -1, key, indexDbPathOut);
	}
	
	
}
