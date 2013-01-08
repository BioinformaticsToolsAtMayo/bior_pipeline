package edu.mayo.bior.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * Utility that connects an InputStream to an OutputStream.
 * 
 * @author duffp
 *
 */
public class StreamConnector implements Runnable {

	private final Logger sLogger = Logger.getLogger(StreamConnector.class);

	private InputStream mInStream;
	private OutputStream mOutStream;
	private int mBufferSize;

	public StreamConnector(InputStream in, OutputStream out, int bufferSize) {
		mInStream = in;
		mOutStream = out;
		mBufferSize = bufferSize;
	}

	public void run() {

		byte[] buffer = new byte[mBufferSize];

		try {

			for (int n = 0; n != -1; n = mInStream.read(buffer)) {
				mOutStream.write(buffer, 0, n);
				mOutStream.flush();
			}

			mOutStream.close();

		} catch (IOException ioe) {
			sLogger.error(ioe.getMessage(), ioe);
		}
	}

}