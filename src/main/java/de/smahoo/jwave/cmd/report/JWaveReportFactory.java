package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportFactory {
	
	public static JWaveReport generateReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		
		if (nodeCmd == null){
			throw new JWaveReportGenerationException("Node command is null",nodeCmd);
		}
		
		switch (nodeCmd.getCommandClassKey()) {
			case 0x20: return generateBasicReport(nodeCmd);
			case 0x25: return generateSwitchBinaryReport(nodeCmd);
			case 0x26: return generateSwitchMultilevelReport(nodeCmd);
			case 0x30: return generateSensorBinaryReport(nodeCmd);
			case 0x31: return generateSensorMultilevelReport(nodeCmd);
			case 0x32: return generateMeterReport(nodeCmd);
			case 0x43: return generateThermostatSetpointReport(nodeCmd);		
			case 0x62: return generateDoorLockReport(nodeCmd);
			case 0x70: return generateConfigurationReport(nodeCmd);
			case 0x71:
				// here it depends which Report should be generated
				// COMMAND_CLASS_ALARM turned into COMMAND_CLASS_NOTIFICATION with version 3
				if (nodeCmd.getCommandClass().getVersion() < 3){
					return generateAlarmReport(nodeCmd);
				} else {
					return generateNotificationReport(nodeCmd);
				}
			
			case 0x72: return generateManufacturerSpecificReport(nodeCmd);
			case 0x80: return generateBatteryReport(nodeCmd);
			case 0x84: return generateWakeUpIntervalReport(nodeCmd);
			case 0x98: return generateSecurityReport(nodeCmd);
			case 0x9C:
				if (nodeCmd.getCommand().getKey() == 0x02) {
					return generateSensorAlarmReport(nodeCmd);
				}
				if (nodeCmd.getCommand().getKey() == 0x04) {
					return generateSensorAlarmSupportedReport(nodeCmd);
				}
				
			
		}
		
		return null;
	}
	
	
	public static JWaveReportBasic generateBasicReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x20){
			throw new JWaveReportGenerationException("Command class is not of type BASIC", nodeCmd);
		}
		
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportBasic(nodeCmd);
	}
	
	public static JWaveReportSwitchBinary generateSwitchBinaryReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x25){
			throw new JWaveReportGenerationException("Command class is not of type SWITCH_BINARY", nodeCmd);
		}
		
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportSwitchBinary(nodeCmd);
	}
	
	public static JWaveReportSwitchMultilevel generateSwitchMultilevelReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x26){
			throw new JWaveReportGenerationException("Command class is not of type SWITCH_MULTILEVEL", nodeCmd);
		}
		
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportSwitchMultilevel(nodeCmd);
	}
	
	public static JWaveReportSensorBinary generateSensorBinaryReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x30){
			throw new JWaveReportGenerationException("Command class is not of type SENSOR_BINARY", nodeCmd);
		}
		
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportSensorBinary(nodeCmd);
	}	
	
	
	public static JWaveReportSensorMultilevel generateSensorMultilevelReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x31){
			throw new JWaveReportGenerationException("Command class is not of type SENSOR_MULTILEVEL", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x05){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportSensorMultilevel(nodeCmd);		
	}
	
	public static JWaveReportThermostatSetpoint generateThermostatSetpointReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x43){
			throw new JWaveReportGenerationException("Command class is not of type THERMOSTAT_SETPOINT", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportThermostatSetpoint(nodeCmd);
	}

	public static JWaveReport generateDoorLockReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		switch (nodeCmd.getCommandKey()){
			case 0x03: return generateDoorLockOperationReport(nodeCmd);
			case 0x06: return generateDoorLockConfigurationReport(nodeCmd);
		}
		throw new JWaveReportGenerationException("unable to generate report. Maybe "+Integer.toHexString(nodeCmd.getCommandKey())+" is no Report?",nodeCmd);
	}
	
	public static JWaveReportDoorLockConfiguration generateDoorLockConfigurationReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandKey() != 0x06){
			throw new JWaveReportGenerationException("command is not of type CONFIGURATION_REPORT",nodeCmd);
		}
		return new JWaveReportDoorLockConfiguration(nodeCmd);
	}
	
	public static JWaveReportDoorLockOperation generateDoorLockOperationReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("command is not of type OPERATION_REPORT",nodeCmd);
		}
		return new JWaveReportDoorLockOperation(nodeCmd);
	}
	
	public static JWaveReportAlarm generateAlarmReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x71){
			throw new JWaveReportGenerationException("Command class is not of type ALARM", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x05){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportAlarm(nodeCmd);
	}

	public static JWaveReportNotification generateNotificationReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x71){
			throw new JWaveReportGenerationException("Command class is not of type NOTIFICATION", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x05){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportNotification(nodeCmd);
	}
	
	public static JWaveReportManufacturerSpecific generateManufacturerSpecificReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x72){
			throw new JWaveReportGenerationException("Command class is not of type MANUFACTURER_SPECIFIC", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x05){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportManufacturerSpecific(nodeCmd);
	}
	
	public static JWaveReportBattery generateBatteryReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x80){
			throw new JWaveReportGenerationException("Command class is not of type BATTERY", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x03){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportBattery(nodeCmd);		
	}
	
	public static JWaveReportWakeUpInterval generateWakeUpIntervalReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x84){
			throw new JWaveReportGenerationException("Command class is not of type WAKEUP_INTERVAL", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x06){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		
		return new JWaveReportWakeUpInterval(nodeCmd);
	}
	
	public static JWaveReport generateSecurityReport(JWaveNodeCommand nodeCmd)throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x98){
			throw new JWaveReportGenerationException("Command class is not of type SECURITY", nodeCmd);
		}
		switch (nodeCmd.getCommandKey()){
			case 0x03: 
				return new JWaveReportSecurityCommandsSupported(nodeCmd);
			case 0x05: // SECURITY_SCHEME_REPORT
				return new JWaveReportSecurityScheme(nodeCmd);		
			case 0x80:
				return new JWaveReportSecurityNonce(nodeCmd);
		}
		throw new JWaveReportGenerationException(nodeCmd.getCommandKey() + " "+nodeCmd.getCommand().getName()+
				"is not supported for Report generation",nodeCmd);
	}
	
	public static JWaveReportConfiguration generateConfigurationReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x70){
			throw new JWaveReportGenerationException("Command class is not of type CONFIGURATION", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x06){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportConfiguration(nodeCmd);
	}
	
	
	public static JWaveReportSensorAlarm generateSensorAlarmReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x9C){
			throw new JWaveReportGenerationException("Command class is not of type SENSOR_ALARM", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x02){
			throw new JWaveReportGenerationException("Command is not of type REPORT", nodeCmd);
		}
		return new JWaveReportSensorAlarm(nodeCmd);
	}
	
	public static JWaveReportAlarmSupported generateSensorAlarmSupportedReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x9C){
			throw new JWaveReportGenerationException("Command class is not of type SENSOR_ALARM", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x04){
			throw new JWaveReportGenerationException("Command is not of type REPORT SUPPORTED", nodeCmd);
		}
		return new JWaveReportAlarmSupported(nodeCmd);
	}
	
	public static JWaveReportMeter generateMeterReport(JWaveNodeCommand nodeCmd) throws JWaveReportGenerationException{
		if (nodeCmd.getCommandClassKey() != 0x32){
			throw new JWaveReportGenerationException("Command class is not of type METER", nodeCmd);
		}
		if (nodeCmd.getCommandKey() != 0x02){
			throw new JWaveReportGenerationException("Command is not of type REPORT",nodeCmd);
		}
		return new JWaveReportMeter(nodeCmd);
	}
}
