package edu.mayo.bior.pipeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;

import edu.mayo.cli.InvalidDataException;
import edu.mayo.pipes.exceptions.InvalidPipeInputException;
import edu.mayo.pipes.history.History;

public class UnixStreamPipelineTest {

	private UnixStreamPipeline mPipeline;

	@Before
	public void setUp() throws Exception {
		mPipeline = new UnixStreamPipeline();
	}

	@Test
	/**
	 * Creates a MOCK history, runs the UnixStreamPipeline that modifies the 
	 * history, and then checks that the modified history is correct. 
	 * 
	 * @throws IOException
	 */
	public void testExecute() throws IOException, InvalidDataException {
		final String history = 
				"##Directive\n" + 
				"#COL1\tCOL2\tCOL3\n" + 
				"A\tB\tC";
		
		// create pipe that will modify history by appending a suffix to each item
		Pipe<History, History> pipe = new AppendSuffixPipe("_MODIFIED");			
		
		String historyModified = run(history, pipe);
		
		String[] lines = historyModified.split("\n");				
		assertEquals(3, lines.length);		
		assertEquals("##Directive",		 					lines[0].trim());
		assertEquals("#COL1\tCOL2\tCOL3", 					lines[1].trim());
		assertEquals("A_MODIFIED\tB_MODIFIED\tC_MODIFIED", 	lines[2].trim());                		
	}

	/**
	 * Helper method that runs the UnixStreamPipeline with the given history and logic pipe.
	 * @param history
	 * @param pipe
	 * @return
	 * @throws IOException
	 * @throws InvalidDataException
	 */
	private String run(String history, Pipe pipe) throws IOException, InvalidDataException {
		
		// create IN/OUT streams to be used by UnixStreamPipeline
		InputStream inStream = new ByteArrayInputStream(history.getBytes());		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		// save default streams for STDIN and STDOUT
		final InputStream DEFAULT_STDIN = System.in;
		final PrintStream DEFAULT_STDOUT = System.out;
		
		// override STDIN/STDOUT defaults for java
		System.setIn(inStream);		
		System.setOut(new PrintStream(outStream, true));
		
		// run UnixStreamPipeline
		mPipeline.execute(pipe);
		
		// flush stream and grab the modified history
		outStream.flush();
		String historyModified = outStream.toString().trim();
		outStream.close();

		// reset streams back to defaults
		System.setIn(DEFAULT_STDIN);
		System.setOut(DEFAULT_STDOUT);		
		
		return historyModified;
	}

	/** Test invalid lines - this should only show a warning, but not fail the pipeline (since pipeline runs to completion. */
	@Test
	public void testInvalidInput() throws IOException {

		final String history = 
				"##Directive\n" + 
				"#COL1\tCOL2\tCOL3\n" + 
				"A\tB\tC\n" +
				"1\t2\t3";
		
		Pipe<History, History> pipe = new InvalidInputPipe();

		final String expected = 
				"##Directive\n" + 
				"#COL1\tCOL2\tCOL3\n" + 
				"1\t2\t3";

		try {
			// run UnixStreamPipeline
			String out = run(history, pipe);
			System.out.println("\nOutput:\n" + out);
			assertEquals(expected, out);
			System.out.println("Test passed.  InvalidDataException was caught and handled correctly as just a warning, not an error.");
		} catch (InvalidDataException e) {
			fail("FAILED: testInvalidInput - one or more invalid lines should not fail the pipeline.");
		}
	}
	
	/** Test an unrecoverable error - this will happen on the first line and fail the pipeline, 
	 *  preventing subsequent lines from being read. 
	 * @throws InvalidDataException */
	@Test
	public void testDeadlyError() throws IOException, InvalidDataException {

		final String history = 
				"##Directive\n" + 
				"#COL1\tCOL2\tCOL3\n" + 
				"A\tB\tC" +
				"1\t2\t3";
		
		Pipe<History, History> pipe = new DeadlyErrorPipe();

		try {
			// run UnixStreamPipeline
			String out = run(history, pipe);			
			System.out.println("output = " + out);
			fail("FAILED: testDeadlyError() - a NullPointerException should kill the pipeline, but did not.");
		} catch (NullPointerException e) {
			System.out.println("PASSED: testDeadlyError() passed.  NullPointerException was expected..");
		}
	}

	
	/**
	 * Simple pipe that appends the given SUFFIX to the end of each item in the history.
	 */
	class AppendSuffixPipe extends AbstractPipe<History, History> {
                
		private String mSuffix;
		
		public AppendSuffixPipe(String suffix) {
			this.mSuffix = suffix;
		}
		
		@Override
		protected History processNextStart() throws NoSuchElementException {
	        if(this.starts.hasNext()){

	        	History history = this.starts.next();
	            for (int i=0; i < history.size(); i++) {
	            	String value = history.get(i) + mSuffix;
	            	history.set(i, value);
	            }
	            
	            return history;	            
	        } else {
	        	throw new NoSuchElementException();
	        }
		}

	}
	
	/**
	 * Pipe that immediately throws an InvalidPipeInputException for 1st history, but allows subsequent lines to be processed
	 */
	class InvalidInputPipe extends AbstractPipe<History, History> {

		private boolean mIsFirst = true;
		
		@Override
		protected History processNextStart() throws NoSuchElementException, InvalidPipeInputException {
			History h = this.starts.next();
			if (mIsFirst) {				
				mIsFirst = false;
				throw new InvalidPipeInputException("message", this);
			}
			return h;
		}
	}

	/** Pipe that immediately throws a NullPointerException which will end the pipeline, and not allow further processing */
	class DeadlyErrorPipe extends AbstractPipe<History, History> {

		private boolean mIsFirst = true;
		
		@Override
		protected History processNextStart() throws NoSuchElementException, InvalidPipeInputException {
			History h = this.starts.next();
			if (mIsFirst) {				
				mIsFirst = false;
				throw new NullPointerException("This should be an unrecoverable error, and end the pipeline");
			}
			return h;
		}
	}

}
