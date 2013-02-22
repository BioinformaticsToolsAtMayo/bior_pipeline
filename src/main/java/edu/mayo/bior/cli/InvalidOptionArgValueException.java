package edu.mayo.bior.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class InvalidOptionArgValueException extends ParseException {

	private static final long serialVersionUID = 1L;

	private Option mOpt;
	private String mValue;
	private String mLongOpt;

	public InvalidOptionArgValueException(Option opt, String value) {
		this(opt, value, "");
	}

	public InvalidOptionArgValueException(Option opt, String value, String mesg) {
		super(mesg);
		mOpt = opt;
		mLongOpt = mOpt.getLongOpt();
		mValue = value;
	}
	
	public InvalidOptionArgValueException(String longOpt, String value, String mesg) {
		super(mesg);
		mValue = value;
		mLongOpt = longOpt;
	}

	public Option getOption() {
		return mOpt;
	}

	public String getInvalidValue() {
		return mValue;
	}

	@Override
	public String getMessage() {
		StringBuilder msg = new StringBuilder();
		//msg.append("Invalid value specified for option: --"+mOpt.getLongOpt()+ " " + mValue + "\n");
		msg.append("Invalid value specified for option: --"+mLongOpt+ " " + mValue + "\n");

		if (super.getMessage().length() > 0) {
			msg.append("\n");
			msg.append(super.getMessage());
		}

		return msg.toString();
	}
}
