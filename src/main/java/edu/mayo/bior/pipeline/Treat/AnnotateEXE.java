/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.tinkerpop.pipes.PipeFunction;

import edu.mayo.exec.AbnormalExitException;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.history.History;

/** 
 * @author Mike Meiners (m054457)
 * 2013-07-02
 * */
public class AnnotateEXE implements PipeFunction<History,History>{

	private static final Logger sLogger = Logger.getLogger(AnnotateEXE.class);
	private UnixStreamCommand mAnnotate;
	private String mEndLine;
	private boolean mWasEndLineSent = false;
	private boolean mIsEndLineInQ = false;
	
	// 10 second timeout for writing response to STDOUT
	private static final long RECEIVE_TIMEOUT = 10;
	
	private Queue<History> mInQueue = new LinkedList<History>();
	
	// The number of lines in flight refers to the number of lines that have been sent,
	// but not fully cleared out of the pipeline yet.  It is cleared out by no longer appearing on the output (the next line is seen)
	// Tweak this number to get the maximum throughput for number of lines 
	// in pipeline at one time, without stalling the pipe
	// WARNING:  The pipes may have a buffer based on # of bytes instead of # of lines,
	//           in which case longer lines will fill up the buffer faster!!!!!!
	//           Fanout pipes such as overlap or sameVariant will also increase the number of lines in flight, so beware!!!
	private int mMaxLinesInFlight = 20;
	private List<History> mInFlightQ = new ArrayList<History>();

	public AnnotateEXE(String[] annotateCmd, Map<String,String> environmentProps, final String END_LINE, int maxLinesInFlight) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		// Need an end line in case there are multiple rows outputted for any input lines.
		// We need to keep sending an end line until all valid output is received, and we start seeing the end line appear in the output.
		// This is because this class uses the compute() method which is a 1-to-1 input to output (requires input to produce output)
		mEndLine = END_LINE;
		
		mMaxLinesInFlight = maxLinesInFlight;
		
		mAnnotate = new UnixStreamCommand(annotateCmd, environmentProps, true, false); 
		mAnnotate.launch();
		
		// All original header lines should be stripped out by HistoryInPipe 
		// Send an initial line so we can catch the header: 
		mAnnotate.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		mAnnotate.send("1\t1\trs0\tG\tA\t0.0\t.\t.");
		//mAnnotate.send("1\t564458\trs77440582\tG\tC\t.\t.\t.");
		String s1 = mAnnotate.receive(); // Header "#CHROM	POS	ID ......"
		String s2 = mAnnotate.receive(); // Dummy line: "chr1....."
		String s3 = "";
	}

	public History compute(History vcfLineHistory)
	{
		try	{
			sendLine(vcfLineHistory);
			
			return receiveLine(vcfLineHistory);
		}
		catch( RuntimeException runtimeExc)	{
			sLogger.debug("No more data.  Ending...");
			terminate();
			// Rethrow any runtime exceptions
			throw runtimeExc;
		} catch (Exception ex) {
			terminate();
			sLogger.error(ex);
		}

		// If we make it here, then throw a NoSuchElementException
		// However, since this is not a normal pipe, it may not reach this point
		throw new NoSuchElementException();
	}

	private void sendLine(History vcfLineHistory) throws AbnormalExitException,	InterruptedException {
		// Don't send any more lines after the end line has been sent
		if( ! mWasEndLineSent ) {
			// Add all lines (but only one end line)
			if( ! mIsEndLineInQ ) {
				mInQueue.add(vcfLineHistory);
				String vcfLine = vcfLineHistory.getMergedData("\t");
				mIsEndLineInQ = vcfLine.contains(mEndLine);
				sLogger.info("AnnotateEXE - added line to queue (# in input queue: " + mInQueue.size()
						+ ", # in flight: " + mInFlightQ.size() + "): " + vcfLine );
			}

			// Only allow so many lines to be in flight at the same time (to avoid overflowing command buffers)
			if( mInFlightQ.size() < mMaxLinesInFlight ) {
				History toSend = mInQueue.remove();
				
				String strToSend = toSend.getMergedData("\t");
				sLogger.info("AnnotateEXE sending line: (# in queue: " + mInQueue.size()
						+ ", # in flight: " + mInFlightQ.size() + "): " + strToSend);
				mAnnotate.send(strToSend);
				mInFlightQ.add(toSend);
			
				if( strToSend.contains(mEndLine) ) {
					sLogger.debug("Hit the end line, so not sending any more lines to annotate command");
					mWasEndLineSent = true;
				}
			}
		}
	}
	
	private History receiveLine(History vcfLineHistory)	throws AbnormalExitException, IOException, InterruptedException, BrokenBarrierException, ExecutionException {
		try {
			String result = mAnnotate.receive(RECEIVE_TIMEOUT, TimeUnit.SECONDS);
			History h = new History(result);
			// If the first 5 cols are NOT the same as the first item in the mInFlightQ, 
			// then remove that first element in the mInFlightQ
			// (This means we've fully finished that line and can send another if at the max in-flight limit)
			if( ! isFirst5ColsSame(mInFlightQ.get(0), h) )
				mInFlightQ.remove(0);
			sLogger.info("AnnotateEXE received line (# in flight: " + mInFlightQ.size() + "): " + result);

			if( result.contains(mEndLine) )
				throw new NoSuchElementException("AnnotateEXE: Received the end line - no more valid data being processed by bior_annotate, so end.");

			return h;
		} catch (TimeoutException te)	{
			sLogger.warn(String.format("Timeout of %s seconds reached when receiving VCF line.  Last line sent was: %s",
					RECEIVE_TIMEOUT,
					(mInFlightQ.size() > 0  ?  mInFlightQ.get(mInFlightQ.size()-1)  :  "(not known???)") ));
			vcfLineHistory.add("\t{\"ERROR\":\"Annotate timed out\"}");
			return vcfLineHistory;
		}
	}

	
	private boolean isFirst5ColsSame(History h1, History h2) {
		if( h1 == null && h2 == null )
			return true;
		else if( h1 == null || h2 == null )
			return false;
		return h1.size() >= 5 && h2.size() >= 5
				&& h1.get(0).equals(h2.get(0))
				&& h1.get(1).equals(h2.get(1))
				&& h1.get(2).equals(h2.get(2))
				&& h1.get(3).equals(h2.get(3))
				&& h1.get(4).equals(h2.get(4));
	}

	public void terminate() {
		try {
			this.mAnnotate.terminate();
		} catch(Exception e) { 
			sLogger.error("Error terminating AnnotateEXE pipe" + e);
		}
	}

}
