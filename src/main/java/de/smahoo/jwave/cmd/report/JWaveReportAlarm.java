package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportAlarm extends JWaveReport{

	public static final int ALARM_TYPE_UNKNOWN 			= 0x00;  // is reserved in ALARM_REPORT specification 
	public static final int ALARM_TYPE_SMOKE 			= 0x01;
	public static final int ALARM_TYPE_CO 				= 0x02;
	public static final int ALARM_TYPE_CO2				= 0x03;
	public static final int ALARM_TYPE_HEAT				= 0x04;
	public static final int ALARM_TYPE_WATER			= 0x05;
	public static final int ALARM_TYPE_ACCESS_COMNTROL 	= 0x06;
	public static final int ALARM_TYPE_BURGLAR			= 0x07;
	public static final int ALARM_TYPE_POWER_MANAGEMENT	= 0x08;
	public static final int ALARM_TYPE_SYSTEM			= 0x09;
	public static final int ALARM_TYPE_EMERGENCY 		= 0x0a;
	public static final int ALARM_TYPE_CLOCK	 		= 0x0b;
	public static final int ALARM_TYPE_FIRST			= 0xFF;
	
	private int 	alarmType;
	private int 	alarmLevel;
	private int 	sourceNodeId;
	private boolean alarmStatus;
	private int 	alarmEvent;
	private int 	alarmEventParameters;
	private int 	eventParameter;
	private int reportVersion;
	
	public JWaveReportAlarm(JWaveNodeCommand cmd) {
		super(cmd);
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		alarmType 	= JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		alarmLevel 	= JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(1));
		if (nodeCmd.getCommandClass().getVersion()==2){
			reportVersion = 2;
			sourceNodeId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2));
			alarmStatus = (0xFF == JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(3)));
			alarmEvent = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(5));
			alarmEventParameters = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(6));
			eventParameter = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(7));
		} else {
			reportVersion = 1;
			sourceNodeId = 0;
			alarmEvent = 0;
			alarmEventParameters = 0;
			eventParameter = 0;
			alarmStatus = true;
		}
		
	}

	public int getAlarmType() {
		return alarmType;
	}

	public int getAlarmLevel() {
		return alarmLevel;
	}

	public int getSourceNodeId() {
		return sourceNodeId;
	}

	public boolean isAlarm() {
		return alarmStatus;
	}

	public int getAlarmEvent() {
		return alarmEvent;
	}

	public int getAlarmEventParameters() {
		return alarmEventParameters;
	}

	public int getEventParameter() {
		return eventParameter;
	}

	public int getReportVersion() {
		return reportVersion;
	}

	
	
}
