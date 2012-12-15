package edu.mayo.bior.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.lang.WordUtils;

public class StringUtils {
	
	/**
	 * Wraps the given string to the specified width.  Lines that start with the
	 * tag <NOWRAP> will not be wrapped.
	 * 
	 * @param str 
	 * 			the string to wrap
	 * @param max_width 
	 * 			the maximum width
	 * @return the wrapped string
	 * @throws IOException
	 */
	public static String wrap(String str, int max_width) throws IOException {
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		
		StringReader sRdr = new StringReader(str);
		BufferedReader bRdr = new BufferedReader(sRdr);
		String line = bRdr.readLine();
		while (line != null) {
			if (line.trim().startsWith("<NOWRAP>")) {
				line = line.substring("<NOWRAP>".length());
			} else {
				line = WordUtils.wrap(line, max_width);				
			}
			pWtr.println(line);
			
			line = bRdr.readLine();
		}		
		
		sWtr.close();		
		String wrappedStr = sWtr.toString();

		// chomp trailing line feed
		if (wrappedStr.length() > 0) {
			wrappedStr = wrappedStr.substring(0, wrappedStr.length() - 1);
		}
		
		return wrappedStr;		
	}
	
	/**
	 * Does a "block" indent on the given string such that each line in the string
	 * is indented over the specified number of indents.
	 *  
	 * @param str
	 * 			the string to be indented
	 * @param numIndents
	 * 			number of indents to use
	 * @return the indented string 		
	 * @throws IOException
	 */
	public static String indent(String str, int numIndents) throws IOException {

		String indent = "";
		for (int i=0; i < numIndents; i++) {
			indent += "\t";
		}
		
		StringWriter sWtr = new StringWriter();
		PrintWriter pWtr = new PrintWriter(sWtr);
		
		StringReader sRdr = new StringReader(str);
		BufferedReader bRdr = new BufferedReader(sRdr);
		String line = bRdr.readLine();
		while (line != null) {
			
			pWtr.print(indent);
			pWtr.println(line);
			
			line = bRdr.readLine();
		}		
		
		sWtr.close();		
		String indentedStr = sWtr.toString();

		// chomp trailing line feed
		if (indentedStr.length() > 0) {
			indentedStr = indentedStr.substring(0, indentedStr.length() - 1);
		}
		
		return indentedStr;
	}

}
