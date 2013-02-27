package edu.mayo.bior.cli;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import edu.mayo.bior.util.StringUtils;

/**
 * Command line interface java application built upon Apache commons-cli.
 * CLI specific command behavior is delegated to a plugin-style architecture
 * using implementations of the CommandPlugin interface.
 * 
 * @author duffp
 *
 */
public class CommandLineApp {

	private static Logger sLogger = Logger.getLogger(CommandLineApp.class);

	private static final int MAX_WIDTH = 94;
	
	private static final char OPTION_HELP = 'h';
	
	private static final char OPTION_ENABLELOG = 'l';

	private CommandLineParser mParser = new PosixParser();
	
	// by default, log is off
	private static boolean sIsLogEnabled = false;
	
	private static Gson sGson = new Gson();	

	/**
	 * Main entry point for application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java " + CommandLineApp.class.getName()
					+ " <plugin classname> <script name> <arg1> <arg2> <arg3>...");
			System.exit(1);
		}
		String pluginClassname = args[0];

		String scriptName = args[1];
		
		String[] cmdArgs = Arrays.copyOfRange(args, 2, args.length);
		        
		Options opts = null;
		List<ArgumentDefinition> argDefs = null;
		try {
			Properties props = new Properties();
			props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("CommandLineApp.properties"));
			
			CommandPlugin plugin = loadPlugin(pluginClassname);
			plugin.init(props);

			opts = loadOptions(plugin);
			opts.addOption(getHelpOption());	
			opts.addOption(getLogfileOption());
			
			argDefs = loadArgumentDefinitions(plugin);
			
			CommandLineApp app = new CommandLineApp();
			app.execute(plugin, cmdArgs, opts, argDefs, scriptName);

		} catch (Throwable t) {
			try {
				processError(scriptName, cmdArgs, opts, argDefs, t);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			System.exit(1);
		}
	}

	/**
	 * Loads zero or more ArgumentDefinition objects from the CommandPlugin's
	 * corresponding ResourceBundle.
	 * 
	 * @param plugin
	 * @return
	 */
	private static List<ArgumentDefinition> loadArgumentDefinitions(
			CommandPlugin plugin) {
		
		Map<String, String> props = getProperties(plugin, "arg.");
		
		List<ArgumentDefinition> argDefs = new ArrayList<ArgumentDefinition>();
		
		SortedSet<String> keys = new TreeSet<String>(props.keySet());
		for (String key: keys) {
			String json = props.get(key);
			
			// transform JSON into POJO
			if( ! isValidJson(json) )
				throw new JsonSyntaxException("Argument in command help text is not valid json: " + key);
			ArgumentDefinition def = sGson.fromJson(json, ArgumentDefinition.class);				
			
			argDefs.add(def);
		}		
		
		return argDefs;
	}

	/**
	 * Loads zero or more Option objects from the CommandPlugin's corresponding 
	 * ResourceBundle.
	 * 
	 * @param plugin
	 * @return
	 */
	private static Options loadOptions(CommandPlugin plugin) {

		Map<String, String> props = getProperties(plugin, "flag.");
		
		Options opts = new Options();
		
		for (String key: props.keySet()) {
			String json = props.get(key);
			
			// transform JSON into POJO
			if( ! isValidJson(json) )
				throw new JsonSyntaxException("Flag option in command help text is not valid json: " + key);
			OptionDefinition def = sGson.fromJson(json, OptionDefinition.class);				
			
			opts.addOption(def.toOption());
		}
		
		return opts;
	}
	
	private static boolean isValidJson(String json) {
		boolean isValidJson = false;
		try {
			JsonElement jelem = new JsonParser().parse(json);
			isValidJson = true;
		}catch(Exception e) {}
		return isValidJson;
	}

	/**
	 * Loads zero or more properties from the CommandPlugin's corresponding ResourceBundle.
	 * Properties are restricted to those that have a name that starts with the given prefix.
	 * 
	 * @param plugin
	 * @param prefix
	 * @return
	 */
	private static Map<String, String> getProperties(CommandPlugin plugin, String prefix) {
		ResourceBundle bundle = getBundle(plugin);
		
		Map<String, String> props = new HashMap<String, String>();
		
		for (String key: bundle.keySet()) {
			if (key.startsWith(prefix)) {
				String val = bundle.getString(key);
				props.put(key, val);
			}
		}
		return props;
	}
	
	/**
	 * Gets ResourceBundle that corresponds to the given CommandPlugin.
	 * 
	 * @param plugin
	 * @return
	 */
	private static ResourceBundle getBundle(CommandPlugin plugin) {
		// bundle name is the Plugin's class name (w/o package)
		return ResourceBundle.getBundle(plugin.getClass().getSimpleName(), Locale.getDefault());		
	}
	
	/**
	 * Helper method to nicely print error messages to STDERR to the user.
	 * 
	 * @param scriptName
	 * @param cmdArgs
	 * @param opts
	 * @param argDefs
	 * @param t
	 * @throws IOException 
	 */
	private static void processError(String scriptName, String[] cmdArgs, Options opts, List<ArgumentDefinition> argDefs, Throwable t) throws IOException {
		if( t instanceof JsonSyntaxException ) {
			System.err.println("Error in command properties file.");
			t.printStackTrace(System.err);
			return;
		}
		
		String shortScriptName = getShortScriptName(scriptName);
		String usage = getUsage(shortScriptName, opts, argDefs);
				
		System.err.println("Error executing " + shortScriptName);
		System.err.println();

		if (t instanceof MissingOptionException) {
			System.err.println("Usage: " + usage);
			System.err.println();
			System.err.println("The command requires the following options:");
			System.err.println();
			MissingOptionException moe = (MissingOptionException) t;
			for (String optName: (List<String>) moe.getMissingOptions()) {
				Option opt = opts.getOption(optName);
				
				String optStr = "-" + opt.getOpt() +", --" + opt.getLongOpt();  
				if (opt.hasArg()) {
					optStr += " <" + opt.getArgName() + ">";
				}
				System.err.println(StringUtils.indent(optStr, 1));
				System.err.println(StringUtils.indent(StringUtils.wrap(opt.getDescription(), MAX_WIDTH), 2));
				System.err.println();				
			}
			System.err.println("Execute the command with -h or --help to find out more information");
			System.err.println();
		} else if (t instanceof ParseException ) {
			System.err.println(t.getMessage());
			System.err.println();				
			System.err.println("Execute the command with -h or --help to find out more information");
			System.err.println();
		} else if (t instanceof InvalidNumberOfArgsException) {
			InvalidNumberOfArgsException ex = (InvalidNumberOfArgsException) t;
			System.err.println("Usage: " + usage);
			System.err.println();
			System.err.println("Invalid number of argument values specified.");
			System.err.println();
			System.err.println("Arguments that are required:");
			for (ArgumentDefinition argDef: argDefs) {
				System.err.println("\t" + argDef.getName());				
			}
			System.err.println();
			System.err.println("Arguments specified by user:");
			for (String actualArg: ex.getActualArgs()) {
				System.err.println("\t" + actualArg);				
			}
			System.err.println();
			System.err.println("Execute the command with -h or --help to find out more information");
			System.err.println();		
		} 
		else if (t instanceof InvalidDataException ) {
			// alert user about invalid data and direct them to the log file
			System.err.println(t.getMessage());
			System.err.println();
			String logMessage;
			if (sIsLogEnabled) 
				logMessage = String.format(
					"Please view the log file for details.",
					shortScriptName
				);
			else
				logMessage = String.format(
					"Please re-execute the command %s with the -l or --log option to dump the log file with details.",
					shortScriptName
				);
			System.err.println(logMessage);
			System.err.println();
			
		} else {
			// uncaught Exception or unchecked RuntimeException

			// dump UNIX environment to log
			StringBuilder debug = new StringBuilder(); 
			debug.append("\n");
			debug.append("ENVIRONMENT VARIABLES:\n");
			for(String name: System.getenv().keySet()) {
				String value = System.getenv().get(name);
				debug.append(String.format("\t%s=%s\n", name, value));
			}
			debug.append("\n");

			// dump command with args to log
			debug.append("COMMAND:\n");
			debug.append(String.format("\tScript=%s\n", scriptName));
			for (String cmdArg: cmdArgs) {
				debug.append(String.format("\tArg=%s\n", cmdArg));			
			}			
			
			sLogger.error(debug.toString());
			
			
			// dump exception to log
			sLogger.error(t.getMessage(), t);

			System.err.println("Internal system error.");
			System.err.println();
			System.err.println("Please send the following to the development team:");
			System.err.println(String.format("\t1. Log file by running %s with -l or --log", shortScriptName));
			System.err.println("\t2. Entire command executed at the UNIX command prompt ");
			System.err.println();			
		}
	}
	
	/**
	 * Executes the command
	 * 
	 * @param plugin
	 * @param jvmArgs Completely raw argument strings passed to the JVM.
	 * @param opts
	 * @param argDefs
	 * @param scriptName
	 * 
	 * @throws Exception 
	 */
	public void execute(CommandPlugin plugin, String[] jvmArgs, Options opts, List<ArgumentDefinition> argDefs, String scriptName)
			throws Exception {

		for (String arg: jvmArgs) {
			if (arg.equals("-h") || arg.equals("--help")) {
				printHelp(plugin, opts, argDefs, scriptName);
				return;
			}
			//Generate log file only if user specifies explicitly
			else if (arg.equals("-l") || arg.equals("--log")) {
	        	turnLogOn();
	        }			
		}
		
		CommandLine line = mParser.parse(opts, jvmArgs);

		// validate whether the correct number of arguments was given
		int expectedArgCnt = argDefs.size();
		int actualArgCnt = line.getArgs().length;
		if (actualArgCnt != expectedArgCnt) {
			throw new InvalidNumberOfArgsException(expectedArgCnt, line.getArgs());
		}

		plugin.execute(line, opts);
	}

	private static Option getHelpOption() {
		return OptionBuilder.withLongOpt("help")
				.withDescription("print this message")
				.create(OPTION_HELP);
	}

	private static Option getLogfileOption() {
        return OptionBuilder.withLongOpt("log")
        		.withDescription("Use this option to generate the log file. By default, log file is not generated.")
                .create(OPTION_ENABLELOG);
	}

	/**
	 * Prints help to STDOUT.
	 * 
	 * @param plugin
	 * @param opts
	 * @param argDefs
	 * @param scriptName
	 * @throws IOException 
	 */
	private void printHelp(CommandPlugin plugin, Options opts, List<ArgumentDefinition> argDefs, String scriptName) throws IOException {
		
		ResourceBundle bundle = getBundle(plugin);
				
		String shortScriptName = getShortScriptName(scriptName);
		
		String shortDesc = StringUtils.indent(shortScriptName + " -- " + StringUtils.wrap(bundle.getString("short.description"), MAX_WIDTH), 1);
		String longDesc = StringUtils.indent(StringUtils.wrap(bundle.getString("long.description"), MAX_WIDTH), 1);
		String usage = StringUtils.indent(getUsage(shortScriptName, opts, argDefs), 1);
		
		List<String> examples = new ArrayList<String>();
		for (String key: bundle.keySet()) {
			if (key.startsWith("example.")) {
				examples.add(StringUtils.indent(StringUtils.wrap(bundle.getString(key), MAX_WIDTH), 1));
			}
		}
		
		System.out.println();
		System.out.println("NAME");
		System.out.println(shortDesc);
		System.out.println();
		System.out.println("SYNOPSIS");
		System.out.println(usage);
		System.out.println();
		System.out.println("DESCRIPTION");
		System.out.println(longDesc);
		System.out.println();
		if (argDefs.size() > 0) {
			System.out.println(StringUtils.indent("The arguments are as follows:", 1));
			System.out.println();
			for (ArgumentDefinition argDef: argDefs) {
				System.out.println(StringUtils.indent(argDef.getName(), 1));
				System.out.println(StringUtils.indent(StringUtils.wrap(argDef.getDescription(), MAX_WIDTH), 2));
				System.out.println();				
			}
		}		
		if (opts.getOptions().size() > 0) {
			System.out.println(StringUtils.indent("The options are as follows:", 1));
			System.out.println();
			for (Object optObj: opts.getOptions()) {
				Option opt = (Option) optObj;
				String optStr = "-" + opt.getOpt() +", --" + opt.getLongOpt();  
				if (opt.hasArg()) {
					optStr += " <" + opt.getArgName() + ">";
				}
				System.out.println(StringUtils.indent(optStr, 1));
				System.out.println(StringUtils.indent(StringUtils.wrap(opt.getDescription(), MAX_WIDTH), 2));
				System.out.println();
			}
		}
		if (examples.size() > 0) {
			for (String example: examples) {
				System.out.println("EXAMPLE");
				System.out.println(example);
				System.out.println();
			}
		}
	}
	
	/**
	 * Dynamically builds the USAGE text based on the option and argument metadata given.
	 * 
	 * @param scriptName
	 * @param opts
	 * @param argDefs
	 * @return
	 */
	private static String getUsage(String scriptName, Options opts, List<ArgumentDefinition> argDefs) {
		StringBuilder sb = new StringBuilder();
		sb.append(scriptName);
				
		Iterator<Option> optItr = opts.getOptions().iterator();
		while (optItr.hasNext()) {
			sb.append(" ");
			
			Option opt = optItr.next();
			
			if (opt.isRequired() == false) {
				sb.append("[");
			}
			
			sb.append("--"+opt.getLongOpt());
			if (opt.hasArg()) {
				sb.append(" ");
				sb.append("<"+opt.getArgName()+">");
			}
			
			if (opt.isRequired() == false) {
				sb.append("]");
			}
		}
		
		for(ArgumentDefinition argDef: argDefs) {
			sb.append(" ");
			
			sb.append(argDef.getName());
		}		
		
		return sb.toString();
	}

	/**
	 * Gets the name of the script minus any path information.
	 * 
	 * @param scriptName
	 * @return
	 */
	private static String getShortScriptName(String scriptName) {
		int lastSeparatorPos = scriptName.lastIndexOf('/');
		if (lastSeparatorPos != -1) {
			return scriptName.substring(lastSeparatorPos + 1);
		} else {
			return scriptName;
		}
	}

	/**
	 * Dynamically load the plugin class and instantiate a new instance.
	 * 
	 * @param classname
	 * @return
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private static CommandPlugin loadPlugin(String classname)
			throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		
		//if (sLogger != null) {
		sLogger.info("Loading plugin: " + classname);
		//}
		Class c = Class.forName(classname);
		CommandPlugin plugin = (CommandPlugin) c.newInstance();
		return plugin;
	}
	
	/**
	 * Point LOG4J framework at different properties file that has logging
	 * enabled.
	 */
	private void turnLogOn() {
		sIsLogEnabled = true;
		URL configURL = Loader.getResource("log4j.properties.enabled");
		PropertyConfigurator.configure(configURL);
	}

	private void turnLogOff() {
		sIsLogEnabled =false;
		URL configURL = Loader.getResource("log4j.properties");
		PropertyConfigurator.configure(configURL);
	}
}
