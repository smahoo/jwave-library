package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportSensorAlarm extends JWaveReport{

	int sourceNodeId;// =-1;
	int sensorType;  // = -1;
	boolean alarm; //= false;
	int seconds;// = -1;
	
	public JWaveReportSensorAlarm(JWaveNodeCommand cmd) {
		super(cmd);	
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		sourceNodeId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		sensorType  = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(1));
		alarm = (JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2)) != 0);
		seconds = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(3));
	}

	public int getSourceNodeId() {
		return sourceNodeId;
	}

	public int getSensorType() {
		return sensorType;
	}

	public boolean isAlarm() {
		return alarm;
	}

	public int getSeconds() {
		return seconds;
	}

	
}
