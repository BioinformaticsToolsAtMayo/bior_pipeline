package edu.mayo.bior.pipeline;
/*
 * Split File
 * 
 * Master's Thesis by Greg Dougherty
 * Created: Mar 18, 2008
 * 
 * Copyright 2007 by Greg Dougherty
 * License to be determined.
 */



import java.util.ArrayList;

/**
 * @author Greg Dougherty
 *
 */
public class SplitFile
{
	
	/**
	 * Constant to say want String.split to find every possible split in a String.
	 */
	public static final int		kReturnAll = -1;
	private static final int	kNotFound = -1;
	
	
	/**
	 * Return an array of strings split on the string split.  Unlike the String
	 * class version of this function, split is not treated as a regular expression
	 * @param target	string to split up
	 * @param split		string with which to do the splitting
	 * @param maxCol	maximum number of columns to return. -1 to return all
	 * @return	an empty array if target is length 0, otherwise an array with 
	 * n + 1 strings, where n is the number of occurrences of split.
	 * Will only return n strings if the final occurrence of split is at the
	 * end of target.
	 */
	public static final String[] mySplit (String target, String split, int maxCol)
	{
		ArrayList<String>	items = new ArrayList<String> ();	
		
		int	nextPos, curPos = 0;
		int	len = split.length ();
		int	targetLen = target.length ();
		int	numSplits = 0;
		if (maxCol <= 0)
			maxCol = targetLen;
		
		while ((numSplits < maxCol) && ((nextPos = target.indexOf (split, curPos)) > kNotFound))
		{
			items.add (target.substring (curPos, nextPos));
			curPos = nextPos + len;
			++numSplits;
		}
		
		// If have a string at the end, add it
		if (curPos < targetLen)
			items.add (target.substring (curPos, targetLen));
		
		String[]	results = new String[items.size ()];
		return items.toArray (results);
	}
}
