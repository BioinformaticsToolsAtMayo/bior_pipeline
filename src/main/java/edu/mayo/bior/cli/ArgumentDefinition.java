package edu.mayo.bior.cli;

/**
 * POJO that captures information used to construct a new command line argument.
 * 
 * @author duffp
 * 
 */
public class ArgumentDefinition {
	/**
	 * Name of the argument.
	 */
	private String name;
	
	/**
	 * Description of the argument.
	 */
	private String description;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
