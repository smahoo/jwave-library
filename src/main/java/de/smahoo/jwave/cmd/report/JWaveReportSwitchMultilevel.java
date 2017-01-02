package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportSwitchMultilevel extends JWaveReport {

	int level;
	
	public JWaveReportSwitchMultilevel(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		level = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
	}
		
	public int getLevel(){
		return level;
	}
	
	public boolean isOn(){
		return level > 0;
	}	
	
}
