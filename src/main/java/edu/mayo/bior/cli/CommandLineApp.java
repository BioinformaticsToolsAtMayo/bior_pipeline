package edu.mayo.bior.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

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

	private static Logger sLogger; 

	private static final char OPTION_HELP = 'h';
	
	private static final char OPTION_ENABLELOG = 'l';

	private CommandLineParser mParser = new PosixParser();
	
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
		
		//Generate log file only if user specifies explicitly
        if ( ArrayUtils.contains(cmdArgs, "-l") || ArrayUtils.contains(cmdArgs, "--log")) {
                sLogger = Logger.getLogger(CommandLineApp.class);
        }
        
		Options opts = null;
		try {
			Properties props = new Properties();
			props.load(ClassLoader.getSystemClassLoader().getResourceAsStream("CommandLineApp.properties"));
			
			CommandPlugin plugin = loadPlugin(pluginClassname);
			plugin.init(props);

			opts = loadOptions(plugin);
			opts.addOption(getHelpOption());	
			opts.addOption(getLogfileOption());
			
			CommandLineApp app = new CommandLineApp();
			app.execute(plugin, cmdArgs, opts, scriptName);

		} catch (Exception e) {
			processException(scriptName, opts, e);
			System.exit(1);
		}
	}

	/**
	 * Loads zero or more Option objects from the CommandPlugin's corresponding 
	 * ResourceBundle.
	 * 
	 * @param plugin
	 * @return
	 */
	private static Options loadOptions(CommandPlugin plugin) {

		ResourceBundle bundle = getBundle(plugin);
		
		Options opts = new Options();
		
		for (String key: bundle.keySet()) {
			if (key.startsWith("flag.")) {
				String json = bundle.getString(key);
				
				// transform JSON into POJO
				OptionDefinition def = sGson.fromJson(json, OptionDefinition.class);
				
				
				opts.addOption(def.toOption());
			}
		}
		
		return opts;
	}
	
	/**
	 * Gets the CommandPlugin's description dynamically from the CommandPlugin's ResourceBundle.
	 * 
	 * @param plugin
	 * @return
	 */
	private static String getDescription(CommandPlugin plugin) {
		ResourceBundle bundle = getBundle(plugin);
		return bundle.getString("description");
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
	 * @param opts
	 * @param e
	 */
	private static void processException(String scriptName, Options opts, Exception e) {
		
		if (sLogger != null) {
			sLogger.error("Error executing script " + scriptName);
		}
		
		System.err.println("Error executing script " + getShortScriptName(scriptName));
		System.err.println();

		if (e instanceof MissingOptionException) {
			System.err.println("The command requires the following options:");
			MissingOptionException moe = (MissingOptionException) e;
			for (String optName: (List<String>) moe.getMissingOptions()) {
				Option opt = opts.getOption(optName);
				
				StringBuilder msg = new StringBuilder();
				msg.append("\tOption:\t");
				msg.append("-"+opt.getOpt());
				msg.append(", ");
				msg.append("--"+opt.getLongOpt());
				msg.append("\n");
				
				msg.append("\tArgs:\t");
				if (opt.hasArg()) {
					msg.append("<"+ opt.getArgName()+">");
				} else {
					msg.append("none");
				}
				msg.append("\n");
								
				msg.append("\tDesc:\t");
				msg.append(opt.getDescription());
				System.err.println(msg.toString());				
				System.err.println();				
			}
			System.err.println("Execute the command with -h or --help to find out more information");
		} else if (e instanceof ParseException ) {
			System.err.println(e.getMessage());
			System.err.println();				
			System.err.println("Execute the command with -h or --help to find out more information");
		} else {
			if (sLogger != null) {
				sLogger.error(e.getMessage(), e);
			}
			e.printStackTrace(System.err);
		}
	}
	
	/**
	 * Executes the command
	 * 
	 * @param plugin
	 * @throws Exception 
	 */
	public void execute(CommandPlugin plugin, String[] args, Options opts, String scriptName)
			throws Exception {

		for (String arg: args) {
			if (arg.equals("-h") || arg.equals("--help")) {
				printHelp(plugin, opts, scriptName);
				return;
			}
		}
		
		CommandLine line = mParser.parse(opts, args);
		plugin.execute(line);
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
	 * @param scriptName
	 * @throws IOException 
	 */
	private void printHelp(CommandPlugin plugin, Options opts, String scriptName) throws IOException {
		
		ResourceBundle bundle = getBundle(plugin);
		
		final int MAX_WIDTH = 94;
		
		String shortScriptName = getShortScriptName(scriptName);
		
		String shortDesc = StringUtils.indent(shortScriptName + " -- " + StringUtils.wrap(bundle.getString("short.description"), MAX_WIDTH), 1);
		String longDesc = StringUtils.indent(StringUtils.wrap(bundle.getString("long.description"), MAX_WIDTH), 1);
		String usage = StringUtils.indent(getUsage(shortScriptName, opts), 1);
		
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
		if (examples.size() > 0) {
			for (String example: examples) {
				System.out.println("EXAMPLE");
				System.out.println(example);
				System.out.println();
			}
		}
	}
	
	private String getUsage(String scriptName, Options opts) {
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
		
		return sb.toString();
	}

	
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
		
		if (sLogger != null) {
			sLogger.info("Loading plugin: " + classname);
		}
		Class c = Class.forName(classname);
		CommandPlugin plugin = (CommandPlugin) c.newInstance();
		return plugin;
	}
}
