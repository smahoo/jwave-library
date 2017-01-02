package de.smahoo.jwave.node;

import de.smahoo.jwave.JWaveException;

public class JWaveNodeConfigurationException extends JWaveException{
	
	
	protected JWaveNode node = null;

	public JWaveNodeConfigurationException(String message, Throwable throwable, JWaveNode node){
		super(message, throwable);
		this.node = node;
	}
	
	public JWaveNodeConfigurationException(String message, JWaveNode node) {
		super(message);
		
		this.node = node;
	}
	
	public JWaveNode getNode(){
		return node;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
}
