package de.smahoo.jwave;


/**
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public class JWaveException extends Exception {

	public JWaveException(String message){
		super(message);
	}
	
	public JWaveException(String message, Throwable exc){
		super(message,exc);
	}
}
