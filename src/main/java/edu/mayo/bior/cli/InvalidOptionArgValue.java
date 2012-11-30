package edu.mayo.bior.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class InvalidOptionArgValue extends ParseException {

	private static final long serialVersionUID = 1L;

	private Option mOpt;
	private String mValue;

	public InvalidOptionArgValue(Option opt, String value) {
		this(opt, value, "");
	}

	public InvalidOptionArgValue(Option opt, String value, String mesg) {
		super(mesg);
		mOpt = opt;
		mValue = value;
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
		msg.append("Invalid value specified for option: --"+mOpt.getLongOpt()+ " " + mValue + "\n");

		if (super.getMessage().length() > 0) {
			msg.append("\n");
			msg.append(super.getMessage());
		}

		return msg.toString();
	}
}
