package edu.mayo.bior.cli;

/**
 * Thrown if invalid characters that are not part of the ASCII character set are encountered.
 * 
 * @author duffp
 *
 */
public class InvalidCharactersException extends Exception {

	public InvalidCharactersException(String message) {
		super(message);
	}
	
}
