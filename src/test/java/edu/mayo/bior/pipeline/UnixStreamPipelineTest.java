package edu.mayo.bior.pipeline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;

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
	public void testExecute() throws IOException {
		
		final String history = "A\tB\tC";
		
		// create IN/OUT streams to be used by UnixStreamPipeline
		InputStream inStream = new ByteArrayInputStream(history.getBytes());		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		
		// override STDIN/STDOUT defaults for java
		System.setIn(inStream);
		System.setOut(new PrintStream(outStream));
		
		// create pipe that will modify history by appending a suffix to each item
		Pipe<List<String>, List<String>> pipe = new AppendSuffixPipe("_MODIFIED");			
		
		// run UnixStreamPipeline
		mPipeline.execute(pipe);
		
		// flush stream and grab the modified history
		outStream.flush();
		String historyModified = outStream.toString().trim();
		
		assertEquals("A_MODIFIED\tB_MODIFIED\tC_MODIFIED", historyModified);
		
	}

	/**
	 * Simple pipe that appends the given SUFFIX to the end of each item in the history.
	 */
	class AppendSuffixPipe extends AbstractPipe<List<String>, List<String>> {

		private String mSuffix;
		
		public AppendSuffixPipe(String suffix) {
			this.mSuffix = suffix;
		}
		
		@Override
		protected List<String> processNextStart() throws NoSuchElementException {
	        if(this.starts.hasNext()){

	        	List<String> history = this.starts.next();
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
}
