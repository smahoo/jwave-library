package de.smahoo.jwave.security;

import de.smahoo.jwave.JWaveException;

public class JWaveSecurityException extends JWaveException{

	public JWaveSecurityException(String message, Throwable exc) {
		super(message, exc);
	}

	public JWaveSecurityException(String message) {
		this(message,null);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5021856974908543620L;

}
