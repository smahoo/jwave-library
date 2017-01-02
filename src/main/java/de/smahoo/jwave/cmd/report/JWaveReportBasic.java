package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportBasic extends JWaveReport{

	int value;
	
	public JWaveReportBasic(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
	}
	
	
	public int getValue(){
		return value;
	}
	
	protected void evaluate(JWaveNodeCommand nodeCmd){
		value = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
	}
	
}
