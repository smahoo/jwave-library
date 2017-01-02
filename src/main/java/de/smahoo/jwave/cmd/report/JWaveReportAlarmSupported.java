package de.smahoo.jwave.cmd.report;

import java.util.ArrayList;
import java.util.Collection;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportAlarmSupported extends JWaveReport{
	
	protected Collection<Integer> supportedTypes;
	
	
	public JWaveReportAlarmSupported(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
		
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		// 01 0a 00 04 00 07 04 9c 04 01 01 6a
		if (supportedTypes == null) {
			supportedTypes = new ArrayList<Integer>();
		}
		int numberOfBits = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		int types = 0;
		int valueSize = nodeCmd.getParamValue(1).length;
		if ( valueSize <= 4) {
			types = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(1));
		} else {
			// WHAT? 
			return;
		}
		
		for(int bit = 0; bit < numberOfBits; bit++) {		
			if ((types & 1) != 0){
				supportedTypes.add(bit+1);
			}
	        types >>= 1;		
		}		
	}
	
	public Collection<Integer> getSupportedTypes(){
		return supportedTypes;
	}
	
	public boolean supportsType(int type){
		return supportedTypes.contains(type);
	}
	
}
