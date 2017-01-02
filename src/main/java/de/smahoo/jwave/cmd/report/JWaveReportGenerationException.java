package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveConvertException;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportGenerationException extends JWaveConvertException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6318039183971154721L;
	JWaveNodeCommand nodeCmd = null;
	
	public JWaveReportGenerationException(JWaveNodeCommand nodeCmd){
		this(null,null,nodeCmd);
	}
	
	public JWaveReportGenerationException(String message, JWaveNodeCommand nodeCmd){
		this(message,null,nodeCmd);
	}
	
	public JWaveReportGenerationException(String message, Throwable cause, JWaveNodeCommand nodeCmd){
		super(message,cause);
		this.nodeCmd = nodeCmd;
	}
	
	public JWaveNodeCommand getNodeCmd(){
		return nodeCmd;
	}


	
}
