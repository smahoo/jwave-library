package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportSensorBinary extends JWaveReport {

	boolean value;
	
	public JWaveReportSensorBinary(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
	}
	
	
	public boolean getValue(){
		return value;
	}
	
	protected void evaluate(JWaveNodeCommand nodeCmd){
		int v = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		value = v != 0;
	}
	
}
