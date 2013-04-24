package edu.mayo.bior.cli.cmd;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.bior.pipeline.UnixStreamPipeline;
import edu.mayo.cli.CommandPlugin;
import edu.mayo.pipes.history.ColumnMetaData;
import edu.mayo.pipes.history.History;

public class PrettyPrintCommand implements CommandPlugin {

	public static char OPTION_ROW = 'r';

	private UnixStreamPipeline mPipeline = new UnixStreamPipeline();

	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line, Options opts) throws Exception {

		// defaults to 1st row
		int rowNum = 1;

		// check whether user is overriding the default row
		if (line.hasOption(OPTION_ROW)) {
			rowNum = Integer.parseInt(line.getOptionValue(OPTION_ROW));
		}

		PrettyPrintPipe pipe = new PrettyPrintPipe(rowNum);
		mPipeline.execute(pipe);
	}

	/**
	 * Pipe that goes through the History 1-by-1 until it finds the selected
	 * row. The selected row is then printed to STDOUT.
	 * 
	 * @author duffp
	 * 
	 */
	class PrettyPrintPipe extends AbstractPipe<History, History> {

		private Integer selectedRow;
		private Gson mGson = new GsonBuilder().setPrettyPrinting().create();
		private JsonParser mJsonParser = new JsonParser();

		public PrettyPrintPipe(int selectedRow) {
			this.selectedRow = selectedRow;
		}

		@Override
		protected History processNextStart() throws NoSuchElementException {

			int rowNum = 0;
			
			while (this.starts.hasNext()) {
				rowNum++;
				History history = this.starts.next();

				if (selectedRow == rowNum) {
					try {
						
						printDataRow(history);
						
					} catch (Exception e) {
						throw new RuntimeException(e);
					} finally {
						// done, no more data
						throw new NoSuchElementException();						
					}
				}
			}
			System.out.println("Please use a value between 1 and " + rowNum);
			throw new NoSuchElementException();
		}

		/**
		 * Pretty prints this row to STDOUT.
		 * 
		 * @param history
		 * @throws Exception
		 */
		private void printDataRow(History history) throws Exception {

			final String PRETTY_HEADER_COL_NUM = "#";
			final String PRETTY_HEADER_COL_NUM_LINE = "-";
			final String PRETTY_HEADER_COL_NAME = "COLUMN NAME";
			final String PRETTY_HEADER_COL_NAME_LINE = "-----------";
			final String PRETTY_HEADER_COL_VALUE = "COLUMN VALUE";
			final String PRETTY_HEADER_COL_VALUE_LINE = "------------";

			List<ColumnMetaData> metaCols = History.getMetaData().getColumns();

			// width of number column
			int maxColNumWidth = String.valueOf(history.size()).length();
			
			// calculate widest column name
			int maxColNameWidth = 0;
			for (ColumnMetaData metaCol : metaCols) {
				String colName = metaCol.getColumnName();
				if (colName.length() > maxColNameWidth) {
					maxColNameWidth = colName.length();
				}
			}
			if (PRETTY_HEADER_COL_NAME.length() > maxColNameWidth) {
				maxColNameWidth = PRETTY_HEADER_COL_NAME.length();
			}

			// printf style format
			final String format = "%1$-" + maxColNumWidth + "s  %2$-" + maxColNameWidth + "s  %3$s";

			// print out the pretty header
			System.out.println(String.format(format, PRETTY_HEADER_COL_NUM, PRETTY_HEADER_COL_NAME,PRETTY_HEADER_COL_VALUE));
			System.out.println(String.format(format, PRETTY_HEADER_COL_NUM_LINE, PRETTY_HEADER_COL_NAME_LINE, PRETTY_HEADER_COL_VALUE_LINE));

			for (int i = 0; i < history.size(); i++) {
				ColumnMetaData cmd = metaCols.get(i);

				int num = i + 1;
				String colName = cmd.getColumnName();
				String dataCol = history.get(i);

				if (dataCol.startsWith("{") && (dataCol.endsWith("}"))) {
					// JSON

					// use GSON API to get a nicely formatted JSON string
					String json = dataCol;
					JsonElement je = mJsonParser.parse(json);
					String prettyJsonString = mGson.toJson(je);

					// construct reader to go through pretty JSON line by line
					StringReader sRdr = new StringReader(prettyJsonString);
					BufferedReader bRdr = new BufferedReader(sRdr);
					String line = bRdr.readLine();

					// print 1st line with column name
					System.out.println(String.format(format, num, colName, line));

					// print subsequent lines w/ same format, but blank col num and name
					line = bRdr.readLine();
					while (line != null) {
						System.out.println(String.format(format, "", "", line));

						line = bRdr.readLine();
					}

				} else {
					// NON-JSON
					System.out.println(String.format(format, num, colName, dataCol));
				}
			}
		}
	}
}
