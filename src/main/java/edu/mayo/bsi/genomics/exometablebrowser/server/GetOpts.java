/**
 * @author Gregory Dougherty
 * Copyright Mayo Clinic, 2011
 */
package edu.mayo.bsi.genomics.exometablebrowser.server;

import java.util.*;

/**
 * A utility class to handle getting user options.  A class that uses this must implement {@link Usage}, 
 * in order to inform the user of the command options if there's a problem with parsing the user's 
 * command line.
 * 
 * @author Gregory Dougherty
 *
 * @version Revision: 1.0
 */
public class GetOpts
{
	/**
	 * Enumerated Type to specify the type of argument (if any) any option has
	 * @author Gregory Dougherty
	 */
	public enum ArgType {
		/** Parameter that doesn't take an argument */ kNoArgument, 
		/** Parameter that can take an argument, but doesn't have to */ kOptionalArgument, 
		/** Parameter that requires an argument */ kRequiredArgument, 
		/** 
		 * Parameter that can occur more than once, w/ new argument each time.
		 * If actually get more than one of occurrence of the parameter, the occurrences will be 
		 * separated by '\n' within the result string
		 */ 
		kMultiRequiredArgument
	}
	/**
	 * Enumerated Type to specify the type of option.  You can have multiple kOnlyParam, 
	 * and also have kOptionalParam and kRequiredParam, but all kOnlyParam must come before 
	 * any kRequiredParam, or an error will still be reported if have an Only and not one of the 
	 * otherwise required parameters.<br/>
	 * An ignore param, if present, gives the param the user can use to tell GetOpts to ignore any 
	 * parameters it doesn't recognize (i.e. if the program defines "--ignore" as an Ignore param, 
	 * <b>and</b> the user specifies --ignore, then any unrecognized parameters will be ignored, rather 
	 * than generating an error.
	 * @author Gregory Dougherty
	 */
	public enum OptType {
		/** Parameter that is optional */ kOptionalParam, 
		/** Parameter that is required */ kRequiredParam, 
		/** 
		 * Parameter that can be the only parameter (i.e. one that gets the version string and
		 * then exits 
		 */
		kOnlyParam, 
		/** A Parameter that allows the program to ignore unrecognized parameters */ kIgnoreParam
	}
	
	/** Field shortOption */
	private char	shortOption;
	/** Field longOption */
	private String	longOption;
	/** Field argument */
	private ArgType	argument;
	/** Field required */
	private OptType	required;
	/** Field wasDone */
	private boolean	wasDone;
	/** Field value */
	private String	value;
	/** Field values */
	private List<String>	values;
	
	
	/** List of options for this parsing */
	private static List<GetOpts>	theOpts = new ArrayList<GetOpts> ();
	
	/** Character that specifies a short option */
	private static final char	kOptChar = '-';
	/** Character that specifies a long option */
	private static final String	kLongOpt = "--";
	/** Character that specifies that there is no short argument */
	public static final char	kNoShortArg = '\0';
	/** Character that specifies how multiple arguments are separated in a return string */
	public static final String	kArgumentSeparator = "\n";
	
	
	/**
	 * @param shortOption	Character of the option.  '\0' if no short option
	 * @param longOption	Name of the option, begins with -- if also a long option version
	 * @param argument		Does it take an argument
	 * @param required		Is this option optional, or required, or can it be the only option
	 */
	private GetOpts (char shortOption, String longOption, ArgType argument, OptType required)
	{
		this.longOption = longOption;
		this.shortOption = shortOption;
		this.argument = argument;
		this.required = required;
		value = null;
		values = null;
	}
	
	
	/**
	 * Figure out if this GetOpts is a match for the argument
	 * @param theArg	Argument we're testing against
	 * @return	true if a match, else false
	 */
	private boolean isOpt (char theArg)
	{
		if (shortOption != theArg)
			return false;
		
		wasDone = true;
		return true;
	}
	
	
	/**
	 * Figure out if this GetOpts is a match for the argument
	 * @param theArg	Argument we're testing against
	 * @return	true if a match, else false
	 */
	private boolean isOpt (String theArg)
	{
		if (!longOption.equalsIgnoreCase (theArg))
			return false;
		
		wasDone = true;
		return true;
	}
	
	
	/**
	 * Report to the caller what happened with this option
	 * @return	null if wasn't used, "" if used but no argument.  Otherwise returns
	 * its argument
	 */
	private String report ()
	{
		if (!wasDone)
			return null;
		
		if (values != null)
		{
			StringBuilder	result = new StringBuilder ();
			int				numValues = values.size ();
			
			result.append (values.get (0));
			for (int i = 1; i < numValues; ++i)
			{
				result.append (kArgumentSeparator);
				result.append (values.get (i));
			}
			
			return result.toString ();
		}
		
		if (value != null)
			return value;
		
		return "";
	}
	
	
	/**
	 * Set the value.  If can take multiple values, and this is the second or greater time the 
	 * option has had it's value set, add the value to the list of values for the option
	 * 
	 * @param value the value to set
	 */
	public final void setValue (String value)
	{
		if ((this.value == null) && (values == null))
			this.value = value;
		else if (argument == ArgType.kMultiRequiredArgument)
		{
			if (values == null)
			{
				values = new ArrayList<String> ();
				values.add (this.value);
				this.value = null;
			}
			
			values.add (value);
		}
	}
	
	
	/**
	 * Figure out if the GetOpts is satisfied
	 * 
	 * @return If the option wasn't in the command line, returns true if it wasn't
	 * required, else false.
	 * If the option was in the command line, return true if it had an argument, or
	 * if no argument was required.  (It can't have an argument added if it doesn't
	 * take one, so don't need to check for that.)
	 */
	private final boolean isGood ()
	{
		if (!wasDone)
			return required != OptType.kRequiredParam;
		
		if (value != null)
			return true;
		
		if (argument == ArgType.kMultiRequiredArgument)
			return (values != null);
		
		return (argument != ArgType.kRequiredArgument);
	}
	
	
	/**
	 * Figure out if the GetOpts is satisfied
	 * 
	 * @return If the option was in the command line, returns true if it is a kOnlyParam, and 
	 * it has any required arguments.
	 */
	private final boolean isOnly ()
	{
		if (!wasDone || (required != OptType.kOnlyParam))
			return false;
		
		if (value != null)
			return true;
		
		if (argument == ArgType.kMultiRequiredArgument)
			return (values != null);
		
		return (argument != ArgType.kRequiredArgument);
	}
	
	
	/**
	 * Figure out if the GetOpts has an ignore parameter, and it was used
	 * 
	 * @return If the user to told us to ignore extra parameters, return true, else return false
	 */
	private final boolean isIgnore ()
	{
		return (wasDone && (required != OptType.kIgnoreParam));
	}
	
	
	/**
	 * Figure out if the GetOpts has an ignore parameter.
	 * 
	 * @return If it's possible for the user to tell us to ignore extra parameters, return true, else 
	 * return false
	 */
	private static final boolean hasIgnore ()
	{
		if (theOpts == null)
			return false;
		
		for (GetOpts anOpt : theOpts)
		{
			if (anOpt.required == OptType.kIgnoreParam)
				return true;
		}
		
		return false;
	}
	
	
	/**
	 * @param shortOption	Character of the option.  '\0' if no short option
	 * @param longOption	Name of the option begins with -- if also a long option version
	 * @param argument		Does it take an argument
	 * @param required		Is this option optional, or required
	 */
	public static void addOption (char shortOption, String longOption, ArgType argument, 
								  OptType required)
	{
		theOpts.add (new GetOpts (shortOption, longOption, argument, required));
	}
	
	
	/**
	 * Parse a command line, given the already added GetOpts descriptions
	 * 
	 * @param args		The elements of the command line
	 * @param target	Usage implementer to call if there's a problem
	 * @return	Array of strings, or null if there was an error.  The first n strings will be for the 
	 * possible options, in the order they were specified.  Null means the option wasn't in the 
	 * command line, "" means it was, and did not have an argument, a non-empty string holds the 
	 * argument that was with the option.
	 */
	public static String[] parseArgs (String[] args, Usage target)
	{
		String[]		results = null;
		GetOpts			curOption = null;
		List<String>	unused = new ArrayList<String> ();
		boolean			canIgnore = hasIgnore ();
		boolean			mustIgnore = false;
		boolean			hasIgnore = false;
		
		for (String theArg : args)
		{
			if (theArg.startsWith (kLongOpt))
			{
				curOption = handleLongOption (theArg, curOption);
				if (curOption == null)
				{
					if (!canIgnore)
					{
						reportBadOption (args, target);
						return null;
					}
					unused.add (theArg);
					mustIgnore = true;
				}
			}
			else if (!theArg.isEmpty () && (theArg.charAt (0) == kOptChar))
			{
				int	numUnused = unused.size ();
				curOption = handleSingleCharOptions (theArg, curOption, unused);
				if ((curOption == null) && (numUnused == unused.size ()))
				{
					if (!canIgnore)
					{
						reportBadOption (args, target);
						return null;
					}
					unused.add (theArg);
					mustIgnore = true;
				}
			}
			else
			{
				if ((curOption != null) && (curOption.argument != ArgType.kNoArgument))
				{
					curOption.setValue (theArg);
					curOption = null;
				}
				else	// Allow user to place options anywhere in the command line
					unused.add (theArg);
			}
		}
		
		// Now verify that all required options were included
		for (GetOpts theOpt : theOpts)
		{
			if (theOpt.isOnly ())
				break;	// Don't need to check anyone else
			
			if (theOpt.isIgnore ())
				hasIgnore = true;
			
			if (!theOpt.isGood ())
			{
				if (!canIgnore)
				{
					reportBadOption (args, target);
					return null;
				}
				mustIgnore = true;
			}
		}
		
		if (mustIgnore && !hasIgnore)
		{
			reportBadOption (args, target);
			return null;
		}
		
		int	pos = 0;
		results = new String[theOpts.size () + unused.size ()];
		for (GetOpts theOpt : theOpts)
		{
			results[pos] = theOpt.report ();
			++pos;
		}
		
		for (String theArg : unused)
		{
			results[pos] = theArg;
			++pos;
		}
		
		return results;
	}
	
	
	/**
	 * Handle situation where passed an argument that begins w/ a dash.  Will be either an 
	 * command to use stdin / stdout ('-'), or one or more single character commands together
	 * 
	 * @param theArg	The argument to parse
	 * @param curOption	The current option.  Matters if it's '-'
	 * @param unused	List to add to if it's '-' and don't have a current option that takes args
	 * @return		The last option given in the string, if any, else null.  If null, then either 
	 * something was added to unused, or it's an error
	 */
	private static GetOpts handleSingleCharOptions (String theArg, GetOpts curOption, 
													List<String> unused)
	{
		int	optsLen = theArg.length ();
		
		if (optsLen == 1)	// Just a '-'
		{
			if ((curOption != null) && (curOption.argument != ArgType.kNoArgument))
				curOption.setValue (theArg);
			else
				unused.add (theArg);
			
			return curOption;
		}
		
		for (int i = 1; i < optsLen; ++i)
		{
			if ((curOption != null) && (!curOption.isGood ()))
				return null;
			
			curOption = null;
			char	theChar = theArg.charAt (i);
			for (GetOpts theOpt : theOpts)
			{
				if (theOpt.isOpt (theChar))
				{
					curOption = theOpt;
					break;
				}
			}
			
			if (curOption == null)	// Couldn't find the option
				return null;
		}
		
		return curOption;
	}
	
	
	/**
	 * Handle situation where passed an argument that begins w/ two dashes.
	 * 
	 * @param theArg	The argument to parse
	 * @param curOption	The current option.  Have to verify it's complete
	 * @return		The last option given in the string, if any, else null.  If null, then either 
	 * something was added to unused, or it's an error
	 */
	private static GetOpts handleLongOption (String theArg, GetOpts curOption)
	{
		if ((curOption != null) && (!curOption.isGood ()))
			return null;
		
		curOption = null;
		for (GetOpts theOpt : theOpts)
		{
			if (theOpt.isOpt (theArg))
			{
				curOption = theOpt;
				break;
			}
		}
		
		return curOption;
	}
	
	
	/**
	 * Routine to tell the user about bad arguments on the command line
	 * 
	 * @param args		The command line
	 * @param target	The app the user was trying to run
	 */
	private static void reportBadOption (String[] args, Usage target)
	{
		if (args.length > 0)	// If not, it's not an error, they just wanted the usage info
		{
			System.err.print ("Error running " + target.name ());
			for (String theArg : args)
				System.err.print (" " + theArg);
			System.err.println ();
		}
		
		System.err.println (target.usage ());
	}
}
