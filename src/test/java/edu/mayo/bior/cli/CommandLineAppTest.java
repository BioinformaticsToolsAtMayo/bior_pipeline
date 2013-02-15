package edu.mayo.bior.cli;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.mayo.bior.cli.ArgumentDefinition;
import edu.mayo.bior.cli.CommandLineApp;
import edu.mayo.bior.cli.CommandPlugin;
import edu.mayo.bior.cli.InvalidNumberOfArgsException;


public class CommandLineAppTest {

	private static final String[] NO_JVM_ARGS = new String[] {};
	private static final Options NO_OPTS = new Options();
	private static final List<ArgumentDefinition> NO_ARG_DEFS = Collections.emptyList();

	private static final String MOCK_SCRIPT_NAME = "MOCK_SCRIPT.sh";
	
	private CommandLineApp mApp = new CommandLineApp();
	private CommandPlugin mMockPlugin;
	
	@Before
	public void setUp() throws Exception {
		
		mMockPlugin = new CommandPlugin() {

			public void init(Properties props) throws Exception {
			}

			public void execute(CommandLine line) throws Exception {
			}
			
		};
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void executeWithNoArguments() throws Exception {		
		mApp.execute(mMockPlugin, NO_JVM_ARGS, NO_OPTS, NO_ARG_DEFS, MOCK_SCRIPT_NAME);
	}	
	
	@Test
	public void executeWithArguments() throws Exception {

		String[] jvmArgs = new String[] {
			"arg1_value",
			"arg2_value"
		};
		
		ArgumentDefinition arg1 = new ArgumentDefinition();
		arg1.setName("arg1");
		arg1.setDescription("The arg1 description.");

		ArgumentDefinition arg2 = new ArgumentDefinition();
		arg2.setName("arg2");
		arg2.setDescription("The arg2 description.");

		List<ArgumentDefinition> argDefs = new ArrayList<ArgumentDefinition>();
		argDefs.add(arg1);
		argDefs.add(arg2);
		
		mApp.execute(mMockPlugin, jvmArgs, NO_OPTS, argDefs, MOCK_SCRIPT_NAME);
	}
	
	@Test
	public void executeWithWrongNumOfArguments() throws Exception {
		ArgumentDefinition arg1 = new ArgumentDefinition();
		arg1.setName("arg1");
		arg1.setDescription("The arg1 description.");

		List<ArgumentDefinition> argDefs = new ArrayList<ArgumentDefinition>();
		argDefs.add(arg1);
		
		try {
			mApp.execute(mMockPlugin, NO_JVM_ARGS, NO_OPTS, argDefs, MOCK_SCRIPT_NAME);
			fail("Expected " + InvalidNumberOfArgsException.class.getName());
		} catch (InvalidNumberOfArgsException e) {
			// expected behavior
		}			
	}
}
