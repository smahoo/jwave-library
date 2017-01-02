package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveReportWakeUpInterval extends JWaveReport{
	
	protected int interval;
	
	public JWaveReportWakeUpInterval(JWaveNodeCommand cmd){
		super(cmd);
	}
	
	protected void evaluate(JWaveNodeCommand nodeCmd){
		try {
			interval = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
	}
	
	public int getInterval(){
		return interval;
	}

}
