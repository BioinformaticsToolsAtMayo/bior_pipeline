package edu.mayo.bior.cli;

import org.apache.commons.cli.Option;


/**
 * POJO that captures information used to construct a new commons-cli Option.
 * 
 * @see <a href="http://commons.apache.org/cli/properties.html">Option Properties</a>
 * 
 * @author duffp
 * 
 */
public class OptionDefinition {
	/**
	 * the identification string of the Option
	 */
	private String opt;

	/**
	 * an alias and more descriptive identification string
	 */
	private String longOpt;
	
	/**
	 * a description of the function of the option
	 */
	private String description;
	
	/**
	 * a flag to say whether the option must appear on the command line
	 */
	private Boolean required;
	
	/**
	 * number of arguments
	 */
	private Integer numArgs;
	
	/**
	 * a flag to say whether the option's argument is optional
	 */
	private Boolean optionalArg;
	
	/**
	 * the name of the argument value for the usage statement 
	 */
	private String argName;
	
	/**
	 * 	the character value used to split the argument string, that is used in conjunction with multipleArgs e.g. if the separator is ',' and the argument string is 'a,b,c' then there are three argument values, 'a', 'b' and 'c'.
	 */
	private Character valueSeparator;

	public String getOpt() {
		return opt;
	}

	public void setOpt(String opt) {
		this.opt = opt;
	}

	public String getLongOpt() {
		return longOpt;
	}

	public void setLongOpt(String longOpt) {
		this.longOpt = longOpt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Integer getNumArgs() {
		return numArgs;
	}

	public void setNumArgs(int numArgs) {
		this.numArgs = numArgs;
	}

	public Boolean isOptionalArg() {
		return optionalArg;
	}

	public void setOptionalArg(boolean optionalArg) {
		this.optionalArg = optionalArg;
	}

	public String getArgName() {
		return argName;
	}

	public void setArgName(String argName) {
		this.argName = argName;
	}

	public Character getValueSeparator() {
		return valueSeparator;
	}

	public void setValueSeparator(Character valueSeparator) {
		this.valueSeparator = valueSeparator;
	}
	
	public Option toOption() {
		Option theOpt = new Option(getOpt(), getDescription());

		if (getNumArgs() != null) {
			theOpt.setArgs(getNumArgs());
		}
		
		if (getArgName() != null) {
			theOpt.setArgName(getArgName());
		}
		
		if (getLongOpt() != null) {
			theOpt.setLongOpt(getLongOpt());
		}
		
		if (isOptionalArg() != null) {
			theOpt.setOptionalArg(isOptionalArg());
		}
		
		if (isRequired() != null) {
			theOpt.setRequired(isRequired());
		}

		if (getValueSeparator() != null) {
			theOpt.setValueSeparator(getValueSeparator());
		}
		
		return theOpt;

	}
}
