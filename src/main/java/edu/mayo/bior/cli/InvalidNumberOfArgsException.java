package edu.mayo.bior.cli;

public class InvalidNumberOfArgsException extends Exception {

	private static final long serialVersionUID = 1L;

	private int mExpectedArgCount;
	private String[] mActualArgs;
	
	public InvalidNumberOfArgsException(int expectedArgCount, String[] actualArgs) {
		super();
		mExpectedArgCount = expectedArgCount;
		mActualArgs = actualArgs;
	}
	
	public int getExpectedArgCount() {
		return mExpectedArgCount;
	}
	
	public String[] getActualArgs() {
		return mActualArgs;
	}
}
