package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveReportBattery  extends JWaveReport{

	public static final int DEFAULT_BATTERY_STATE_WHEN_WARNING = 0;
	
	protected int battery;
	protected boolean batteryWarning;
	
	public JWaveReportBattery(JWaveNodeCommand cmd){
		super(cmd);
	}
	
	public int getBattery(){
		return battery;
	}

	public boolean isBatteryLowWarning(){
		return batteryWarning;
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd){
		try {
			battery = JWaveCommandParameterType.toByte(nodeCmd.getParamValue(0)) & 0xFF;
			if (battery == 0xFF){
				batteryWarning = true;
				battery = DEFAULT_BATTERY_STATE_WHEN_WARNING;
			} else {
				batteryWarning = false;
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
	}
	

	
}
