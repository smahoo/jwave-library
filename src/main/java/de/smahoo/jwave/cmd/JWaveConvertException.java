package de.smahoo.jwave.cmd;

import de.smahoo.jwave.JWaveException;

public class JWaveConvertException extends JWaveException {
	
	public JWaveConvertException(String message,Throwable exc ){
		super(message, exc);
	}
	
	public JWaveConvertException(String message){
		super(message);	
	}
	
}
