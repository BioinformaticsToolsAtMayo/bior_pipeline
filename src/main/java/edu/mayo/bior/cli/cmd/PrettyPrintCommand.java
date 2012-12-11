package edu.mayo.bior.cli.cmd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import edu.mayo.bior.cli.CommandPlugin;

public class PrettyPrintCommand implements CommandPlugin {

	public static char OPTION_ROW = 'r';

	private Gson mGson = new GsonBuilder().setPrettyPrinting().create();
	private JsonParser mJsonParser = new JsonParser();

	public void init(Properties props) throws Exception {
	}

	public void execute(CommandLine line) throws Exception {

		// defaults to 1st row
		int rowNum = 1;

		// check whether user is overriding the default row
		if (line.hasOption(OPTION_ROW)) {
			rowNum = Integer.parseInt(line.getOptionValue(OPTION_ROW));
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			int lineNumber = 0;
			String nextLine = br.readLine();
			while (nextLine != null) {

				// only check data lines, ignore comments
				if (nextLine.startsWith("#") == false) {

					lineNumber++;

					if (lineNumber == rowNum) {
						String json = getJson(nextLine);
						JsonElement je = mJsonParser.parse(json);
						String prettyJsonString = mGson.toJson(je);

						System.out.println(prettyJsonString);
						System.exit(0);
					}
				}

				nextLine = br.readLine();
			}

		} finally {
			br.close();
		}

	}

	/**
	 * Finds the last JSON column. An exception is thrown if no JSON column is
	 * found.
	 * 
	 * @param line
	 * @return
	 * @throws Exception
	 */
	private String getJson(String line) throws Exception {
		String[] cols = line.split("\t");

		for (int i = cols.length - 1; i >= 0; i--) {
			if (cols[i].startsWith("{") && cols[i].endsWith("}")) {
				return cols[i];
			}
		}

		// TODO:
		throw new Exception("JSON column not found.");
	}
}
