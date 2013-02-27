package edu.mayo.bior.cli;

/**
 * Thrown when the input data to the command is invalid.
 * 
 * @author duffp
 *
 */
public class InvalidDataException extends Exception {

	public InvalidDataException(String message) {
		super(message);
	}
}
