package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportNotification extends JWaveReport {

	private int alarmType;
	private int alarmLevel;
	private int sourceNodeId;
	private NotificationType notificationType;
	private NotificationState notificationState;
	private int event;
	private byte[] eventParameter;
	private int sequenceNumber;
	
	public JWaveReportNotification(JWaveNodeCommand cmd) {
		super(cmd);
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		
		alarmType 	= JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		alarmLevel 	= JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(1));
		
		if (nodeCmd.getCommandClass().getVersion() < 2){
			// do not use nodeCmd.getParamValue
			// command class is COMMAND_CLASS_ALARM version 1, thus it only has 2 parameters
			// all values need to be read manually 
			readValuesManually(nodeCmd.getParamValues());
			return;
		}
			
		sourceNodeId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2));
		notificationState = getNotificationState(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(3)));
		notificationType = getNotificationType(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(4)));
		event = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(5));
		eventParameter = nodeCmd.getParamValue(7);
		sequenceNumber = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(8));
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

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public NotificationState getNotificationState() {
		return notificationState;
	}

	public int getEvent() {
		return event;
	}

	public byte[] getEventParameter() {
		return eventParameter;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}



	public enum NotificationState{
		UNKNOWN,
		ON,
		OFF,
		NO_PENDING_NOTIFICATIONS
	}
	
	public enum NotificationType{
		UNKNOWN,
		RESERVED,
        SMOKE,
        CO,
        CO2,
        HEAT,
        WATER,
        ACCESS_CONTROL,
        HOME_SECURTIY,
        POWER_MANAGEMENT,
        SYSTEM,
        EMERGENCY,
        CLOCK,
        APPLIANCE,
        FIRST
	}
	
	
	// +++++++++++++++++++++++ Helpers +++++++++++++++++++++++++++
	
	private void readValuesManually(byte[] values){
		sourceNodeId = (int)(values[2]&0xFF);
		
		notificationState = getNotificationState((int)(values[3]&0xFF));
		notificationType = getNotificationType((int)(values[4]&0xFF));		
		event = (int)(values[5]&0xFF);
		
		
	}
	
	private NotificationState getNotificationState(int state){
		switch (state){
			case 0x0  : return NotificationState.OFF;
			case 0xfe : return NotificationState.NO_PENDING_NOTIFICATIONS;
			case 0xff : return NotificationState.ON;
			default:
				return NotificationState.UNKNOWN;
		}
	}
	
	private NotificationType getNotificationType(int type){
		switch(type){
			case 0x00: return NotificationType.RESERVED;
			case 0x01: return NotificationType.SMOKE;
			case 0x02: return NotificationType.CO;
			case 0x03: return NotificationType.CO2;
			case 0x04: return NotificationType.HEAT;
			case 0x05: return NotificationType.WATER;
			case 0x06: return NotificationType.ACCESS_CONTROL;
			case 0x07: return NotificationType.HOME_SECURTIY;
			case 0x08: return NotificationType.POWER_MANAGEMENT;
			case 0x09: return NotificationType.SYSTEM;
			case 0x0a: return NotificationType.EMERGENCY;
			case 0x0b: return NotificationType.CLOCK;
			case 0x0c: return NotificationType.APPLIANCE;
			case 0xff: return NotificationType.FIRST;
			default:
				return NotificationType.UNKNOWN;

		}
	}
}
