package edu.mayo.bior.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;

public class InvalidOptionArgValueException extends ParseException {

	private static final long serialVersionUID = 1L;

	private Option mOpt;
	private String mValue;
	private boolean mIsPrintOptionName = true;

	public InvalidOptionArgValueException(Option opt, String value) {
		this(opt, value, "", true);
	}

	public InvalidOptionArgValueException(Option opt, String value, String mesg) {
		this(opt, value, mesg, true);
	}

	/** Can choose whether to print the option name.  
	 *  This is helpful when the user did NOT specify the option (since it is optional)
	 *  BUT the default was not valid either.
	 * @param opt
	 * @param value
	 * @param mesg
	 * @param isPrintOptionName
	 */
	public InvalidOptionArgValueException(Option opt, String value, String mesg, boolean isPrintOptionName) {
		super(mesg);
		mOpt = opt;
		mValue = value;
		mIsPrintOptionName = isPrintOptionName;
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
		
		if(mIsPrintOptionName)
			msg.append("Invalid value specified for option: --"+mOpt.getLongOpt()+ " " + mValue + "\n");

		if (super.getMessage().length() > 0) {
			msg.append("\n");
			msg.append(super.getMessage());
		}

		return msg.toString();
	}
}
