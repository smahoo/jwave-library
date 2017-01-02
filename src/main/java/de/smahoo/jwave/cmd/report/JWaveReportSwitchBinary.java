package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportSwitchBinary extends JWaveReport{

	boolean value;
	
	public JWaveReportSwitchBinary(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
	}
	
	
	public boolean getValue(){
		return value;
	}
	
	protected void evaluate(JWaveNodeCommand nodeCmd){
		value = (JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0)) != 0);
	}
	
}
