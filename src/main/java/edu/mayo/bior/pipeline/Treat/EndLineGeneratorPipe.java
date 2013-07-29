package edu.mayo.bior.pipeline.Treat;

import java.util.NoSuchElementException;

import com.tinkerpop.pipes.AbstractPipe;

import edu.mayo.pipes.history.History;

/**
 * Reads in all input, then outputs it exactly line-by-line, until there is no more input.  
 * Then it switches to outputting an end line either once, or repeatedly until:
 * 	- the pipes downstream no longer need anymore data and stop requesting more input, OR
 *  - the downstream pipes encounter the end data and tell this pipe to end
 * @author Michael Meiners (m054457)
 * Date created: Jul 17, 2013
 */
public class EndLineGeneratorPipe  extends AbstractPipe<History,History> {
	private boolean mIsDownstreamPipeStillRequesting = true;
	private boolean mIsRepeatEndLine = false;
	private boolean mWasFirstEndLineSent = false;
	
	private History mEndLineIn;
	
	public EndLineGeneratorPipe(String endLine, boolean isRepeatEndLine) {
		mEndLineIn = new History(endLine);
		mIsRepeatEndLine = isRepeatEndLine;
	}
	
	public void stop() {
		mIsDownstreamPipeStillRequesting = false;
	}
	
	@Override
	protected History processNextStart() throws NoSuchElementException {
		// NOTE: May have to check if first end line has already been sent
		// because if it has, that means all input data has been read and the 
		// input stream may already be closed, so don't force an exception!!!
		if( ! mWasFirstEndLineSent && this.starts.hasNext() ) {
			History historyIn = this.starts.next();
			return historyIn;
		}
		else if( mIsDownstreamPipeStillRequesting ) {
			if( ! mIsRepeatEndLine )
				mIsDownstreamPipeStillRequesting = false;
			mWasFirstEndLineSent = true;
			return mEndLineIn;
		}
		else
			throw new NoSuchElementException("No more data available from input stream, and downstream pipes have requested a stop");
	}

}
