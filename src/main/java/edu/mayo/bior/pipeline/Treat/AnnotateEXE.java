/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mayo.bior.pipeline.Treat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.PipeFunction;

import edu.mayo.exec.AbnormalExitException;
import edu.mayo.exec.UnixStreamCommand;
import edu.mayo.pipes.history.History;

/** 
 * @author Mike Meiners (m054457)
 * 2013-07-02
 * */
public class AnnotateEXE extends AbstractPipe<History,History>{

	private static final Logger sLogger = Logger.getLogger(AnnotateEXE.class);
	private UnixStreamCommand mAnnotate;
	private boolean mWasEndLineSent = false;
	private boolean mWasEndLineAddedToQ = false;
	// When hasNext() returns false, only check this ONCE, or we will get errors!
	private boolean mIsInputDone = false;
	
	private Queue<History> mInQueue = new LinkedList<History>();
	
	// The number of lines in flight refers to the number of lines that have been sent,
	// but not fully cleared out of the pipeline yet.  It is cleared out by no longer appearing on the output (the next line is seen)
	// Tweak this number to get the maximum throughput for number of lines 
	// in pipeline at one time, without stalling the pipe
	// WARNING:  The pipes may have a buffer based on # of bytes instead of # of lines,
	//           in which case longer lines will fill up the buffer faster!!!!!!
	//           Fanout pipes such as overlap or sameVariant will also increase the number of lines in flight, so beware!!!
	private int mMaxLinesInFlight = 10;
	private List<History> mInFlightQ = new ArrayList<History>();

	// 10 second timeout for writing response to STDOUT
	//private static final long RECEIVE_TIMEOUT = 60;
	private int mCmdTimeout = 10;  // seconds
	
	
	
	//TEMP
	//private List<String> mSent = new ArrayList<String>();
	//private List<String> mRcvd = new ArrayList<String>();
	private int mLinesSent = 0;
	private int mLinesRcvd = 0;
	private String mLastLineSent = null;
	
	private final String TERMINATE_LINE = "1\t1\trsXXXXXXXX\tA\tC\t.\t.\t.";
	private final String TERMINATE_RSID =       "rsXXXXXXXX";  // NOTE: Make sure this matches the rsId within the TERMINATE_LINE
	private int mMaxAlts;
	private HashMap<String,History> mUuidToOriginalInputLineMap = new HashMap<String,History>();
	private String mLastUuidReceived = "";

	
	public AnnotateEXE(String[] annotateCmd, Map<String,String> environmentProps, int maxLinesInFlight, int cmdTimeout, int numJsonColsToAdd, int maxAlts) throws IOException, InterruptedException, BrokenBarrierException, TimeoutException, AbnormalExitException {
		// Need an end line in case there are multiple rows outputted for any input lines.
		// We need to keep sending an end line until all valid output is received, and we start seeing the end line appear in the output.
		// This is because this class uses the compute() method which is a 1-to-1 input to output (requires input to produce output)
		//mEndLine = END_LINE;
		
		mMaxLinesInFlight = maxLinesInFlight;
		mCmdTimeout = cmdTimeout;
		mMaxAlts = maxAlts;
		
		mAnnotate = new UnixStreamCommand(annotateCmd, environmentProps, true, false); 
		mAnnotate.launch();
		
		// All original header lines should be stripped out by HistoryInPipe 
		// Send an initial line so we can catch the header and first dummy line (prime the pump!): 
		mAnnotate.send("#CHROM\tPOS\tID\tREF\tALT\tQUAL\tFILTER\tINFO");
		mAnnotate.send("1\t1\trs0\tG\tA\t0.0\t.\t.");
		//mAnnotate.send("1\t564458\trs77440582\tG\tC\t.\t.\t.");
		
		// Now catch all lines beginning with "##" and "#", then catch the first dummy line 
		// 1) "##BIOR="
		// 2) "##BIOR="
		// .....
		//n-1)"#CHROM	POS	ID ......"
		// n) "1        1   rs0 ....."
		String result = "#";
		while(result.startsWith("#")) {
			result = mAnnotate.receive();
		}
	}
	
	@Override
	protected History processNextStart() throws NoSuchElementException {
		sendLine();
		
		return receiveLine();
	}


	private int numAlts(History vcfLineHistory) {
		return vcfLineHistory.get(4).split(",").length;
	}

	/**
	 If there is more data to get, then get and send each line
	 Else, send the terminate line
	 Else, if the terminate line has already been sent, then send nothing
	 MoreData?	Qsize>0?	InFlight>=Max	TermSent?	WhatToDo....
	 ---------	----------	----------		----------	---------------------------------------------------------
	 Yes		No			No				No			Add input to Q, remove 0th from Q and send, increment inFlight
	 Yes		Yes			No				No			Add input to Q, remove 0th from Q and send, increment inFlight
	 Yes		Yes			Yes				No			Add input to Q, do NOT send any lines, do NOT increment inFlight  (receive should remove one from inFlight)
	 Yes		Yes			No 				No			Add input to Q, remove 0th from Q and send, increment inFlight
	 No			Yes			No				No			No input to add to Q, remove 0th from Q and send, increment inFlight
	 No			No			No				No			No input to add to Q, no line from Q to send, send terminate line
	 No			No			No				Yes			No input to add to Q, no line from Q to send, send nothing (wait for receive to get terminate line)
	*/
	private void sendLine() {
		try {
			// this.starts.hasNext() should only be called once, 
			// because when there is no more data, it throws RuntimeExceptions 
			// ("ERROR: java.io.IOException: Stream closed"???) on 2nd call !!
			boolean isMoreData = mIsInputDone  ?  false  :  this.starts.hasNext();
			if( ! isMoreData )
				mIsInputDone = true;
			

			// If there is more data, then add it to inputQ
			if( isMoreData ) {
				mInQueue.add(this.starts.next());
			} 

			// If there is no more data but the end line has not been sent yet, then put it in Q
			if( ! isMoreData  &&  ! mWasEndLineAddedToQ ) {
				mInQueue.add(new History(TERMINATE_LINE));
				mWasEndLineAddedToQ = true;
			}
			
			// If the in-flight Q is full
			// OR, If there is no more data AND the terminate line has already been sent,
			// then just return (don't send a line)
			if( mInFlightQ.size() >= mMaxLinesInFlight  ||  (! isMoreData  &&  mWasEndLineSent) ) {
				return;
			}
			
			
			mLinesSent++;
			
			History originalLine = mInQueue.remove();
			History lineToSend = stripTo5Cols(originalLine);
				
			checkAlts(lineToSend);
				
			// Add original line mapped to its UUID to the HashMap
			String uuid = lineToSend.get(7);
			mUuidToOriginalInputLineMap.put(uuid, originalLine);

			
			// Now send the line
			sLogger.info("----------------------");
			String lineStr = lineToSend.getMergedData("\t");
			if( lineStr.contains(TERMINATE_RSID) )
				mWasEndLineSent = true;
			sLogger.info("AnnotateEXE sending line (data line #" + mLinesSent + "): \n" + firstXchars(lineStr, 100));
			mInFlightQ.add(lineToSend);
			sLogger.info("------");
			sLogger.info("mInQueue size   = " + mInQueue.size());
			sLogger.info("mInFlightQ size = " + mInFlightQ.size());
			sLogger.info("UUID map size   = " + mUuidToOriginalInputLineMap.size());
			sLogger.info("Lines sent      = " + mLinesSent);
			sLogger.info("Lines received  = " + mLinesRcvd);
			//sLogger.info("mSent size      = " + mSent.size());
			//sLogger.info("mRcvd size      = " + mRcvd.size());
			mAnnotate.send(lineStr);
			
			// TEMP:
			//mLastLineSent = lineStr;
			//sLogger.info("Line sent.");
			//mSent.add(lineStr);

			//printInVsOut();
			
		} catch(Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			terminate();
			throw new NoSuchElementException("ERROR: " + e.getMessage());
		}
	}
	

	private void checkAlts(History lineToSend) {
		// Print # of alts if > mMaxAlts, and truncate them to mMaxAlts
		int numAlts = numAlts(lineToSend);
		if( numAlts > mMaxAlts ) {
			String msg = "Warning: Data Line #" + mLinesSent + " has > " + mMaxAlts + " alts : " + numAlts
					+ "\n  The number of alts will be truncated to " + mMaxAlts;
			System.err.println(msg);
			System.err.println("  Line: " + firstXchars(lineToSend.getMergedData("\t"), 100));
			sLogger.warn(msg);
			sLogger.warn(lineToSend);
			
			// Truncate the alts to mMaxAlts
			String[] alts = lineToSend.get(4).split(",");
			String altsTruncatedStr = StringUtils.join(Arrays.copyOf(alts, mMaxAlts), ",");
			lineToSend.set(4, altsTruncatedStr);
		}
	}

	/**
	 Receive a line
	 If the terminate line has been received, then throw a NoSuchElementException
	*/
	private History receiveLine() {
		try {
			sLogger.info("----------------------");
			sLogger.info("Receiving next line...");
			String result = mAnnotate.receive(mCmdTimeout, TimeUnit.SECONDS);
			
			// TEMP:
			//mRcvd.add(result);
			
			sLogger.info("Line received: (line #" + ++mLinesRcvd + "): \n" + firstXchars(result,100));
			if( result.contains(TERMINATE_RSID) ) {
				terminate();
				throw new NoSuchElementException("AnnotateEXE: Received the end line - no more valid data being processed by bior_annotate, so end.");
			}
			History histOut = mergeResults(result);
			return histOut;
		} catch( NoSuchElementException nse ) {
			// Expected exception at end of pipe - log message
			sLogger.info(nse.getMessage());
			throw nse;
		} catch (TimeoutException te)	{
			String msg = String.format("Timeout of %s seconds reached when receiving VCF line.  Last line sent was (#%s): ",
					mCmdTimeout, mLinesSent);
			sLogger.error(msg + mLastLineSent);
			System.err.println(msg + firstXchars(mLastLineSent, 100));
			terminate();
			throw new NoSuchElementException(te.getMessage());
		} catch(Exception e) {
			System.err.println("ERROR: " + e.getMessage());
			terminate();
			throw new NoSuchElementException("ERROR: " + e.getMessage());
		}
	}
	

	/** Strip history to just first 5 columns, dots in 6th and 7th, and UUID in 8th/INFO */
	private History stripTo5Cols(History h) {
		UUID uuid = UUID.randomUUID();
		History hist = new History();
		for(int i=0; i < 5; i++)
			hist.add(h.get(i));
		hist.add(".");
		hist.add(".");
		hist.add(uuid.toString());
		return hist;
	}
	
	/** Merge the results with the original line, appending all BioR added columns to end */
	private History mergeResults(String resultLine) {
		History histResult = new History(resultLine);
		
		// Lookup original in hashmap
		String uuid = histResult.get(7);
		// NOTE: Make sure to clone the History object in the map, otherwise multiple
		//       lines with the same UUID will continue to expand this same object.
		History histOut = (History)(mUuidToOriginalInputLineMap.get(uuid).clone());
		
		// If the uuid is different from the last one, then remove the last one
		// to clean up any entries that are no longer needed.
		// Plus, remove the 0th element from the in-flight queue
		if( ! mLastUuidReceived.equals("")  &&  ! uuid.equals(mLastUuidReceived) ) {
			mUuidToOriginalInputLineMap.remove(mLastUuidReceived);
			mInFlightQ.remove(0);
		}
		
		// Now, add all columns after 8th col in results to the original history
		for(int i=8; i < histResult.size(); i++) {
			histOut.add(histResult.get(i));
		}
		
		// Set the last uuid to this one
		mLastUuidReceived = uuid;
		
		return histOut;
	}
	
	private String firstXchars(String str, int charLimit) {
		if(str == null || str.length() < charLimit )
			return str;
		return str.substring(0,charLimit) + "........";
	}

	private boolean isFirst8ColsSame(History h1, History h2) {
		if( h1 == null && h2 == null )
			return true;
		else if( h1 == null || h2 == null )
			return false;
		return h1.size() >= 8 && h2.size() >= 8
				&& h1.get(0).equals(h2.get(0))
				&& h1.get(1).equals(h2.get(1))
				&& h1.get(2).equals(h2.get(2))
				&& h1.get(3).equals(h2.get(3))
				&& h1.get(4).equals(h2.get(4))
				&& h1.get(5).equals(h2.get(5))
				&& h1.get(6).equals(h2.get(6))
				&& h1.get(7).equals(h2.get(7));
	}

	public void terminate() {
		try {
			this.mAnnotate.terminate();
		} catch(Exception e) { 
			sLogger.error("Error terminating AnnotateEXE pipe" + e);
		}
	}

	/*
	private void printInVsOut() {
		int sentSize = 0; 
		StringBuilder msg = new StringBuilder("\n");
		int rcvdIdx = 0;
		for(String sent : mSent) {
			String uuid = sent.split("\t")[7];
			int uuidsRcvd = 0;
			int size = sent.length();
			while(rcvdIdx < mRcvd.size()) {
				String uuidRcvd = mRcvd.get(rcvdIdx).split("\t")[7];
				if( uuidRcvd.equals(uuid) ) {
					uuidsRcvd++;
					rcvdIdx++;
					size = 0;
				} else 
					break;
			}
			sentSize += size;
			msg.append(uuid + "\t(" + uuidsRcvd + ")\n");
		}
		msg.append("\nBuffer size sent: " + sentSize);
		sLogger.info(msg);
	}
	*/

}
