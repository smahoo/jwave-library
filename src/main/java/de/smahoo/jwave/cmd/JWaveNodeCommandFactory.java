package de.smahoo.jwave.cmd;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveException;
import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.security.JWaveSecurityException;
import de.smahoo.jwave.security.JWaveSecurityMessageEncapsulation;
import de.smahoo.jwave.security.JWaveSecurityMessageEncapsulationGet;
import de.smahoo.jwave.security.JWaveSecurityNonce;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveNodeCommandFactory {

	protected JWaveCommandClassSpecification defs;
	
	public JWaveNodeCommandFactory(JWaveCommandClassSpecification defs) {
		this.defs = defs;
	}
	
	
	public JWaveCommandClassSpecification getCmdClassSpecification(){
		return defs;
	}
	// +++++++++++++++++++++++++++  Generating node commands from given datagram ++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	
	public JWaveNodeCommand generateNodeCmd(JWaveDatagram datagram) throws JWaveException{
		if (datagram.getCommandType() == JWaveCommandType.CMD_JWave_SEND_DATA){
			// try to generate NodeCmd from datagram of type send data
			return generateNodeCmdFromSendDataDatagram(datagram);
		}
		if (datagram.getCommandType() != JWaveCommandType.CMD_APPL_COMMAND_HANDLER){
			throw new JWaveConvertException("Unable to convert datagram to node cmd. Datagram is of type "+datagram.getCommandType().name());
		}
		byte[] payload = datagram.getPayload();
		
		if (payload[2] > 0){			
			byte[] cmdData = new byte[payload[2]];
			for (int i = 0; i< payload[2]; i++){
				cmdData[i] = payload[i+3];
			}
			
			try {
				return generateNodeCmd(cmdData);
			
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			}
			
		}
		return null;
	}
	
	protected JWaveNodeCommand generateNodeCmdFromSendDataDatagram(JWaveDatagram datagram) throws JWaveException {
		
		byte[] payload = datagram.getPayload(); 
		
		if (payload[1] > 0){			
			byte[] cmdData = new byte[payload[1]];
			for (int i = 0; i< payload[1]; i++){
				cmdData[i] = payload[i+2];
			}
			
			try {
				return generateNodeCmd(cmdData);
			
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			}
			
		}
		return null;
	}
	
	public JWaveMultiNodeCommand generateMultiNodeCmd(Collection<JWaveNodeCommand> nodeCmds){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_MULTI_CMD", "MULTI_CMD_ENCAP");
		return new JWaveMultiNodeCommand(cmd,nodeCmds);
		
	}
	
	
//	protected JWaveSecurityMessageEncapsulation generateSecurityMessageEncapsulationCmd(JWaveNodeCommand nodeCmd, JWaveSecurityNonce receiversNonce, JWaveSecurityNonce sendersNonce, JWaveSecurityNetworkKey networkKey){
//		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","SECURITY_MESSAGE_ENCAPSULATION");
//		
//		JWaveSecurityMessageEncapsulation secMsg = new JWaveSecurityMessageEncapsulation(cmd, nodeCmd, receiversNonce, sendersNonce, networkKey);
//		return secMsg;
//	}
	
	
	protected JWaveMultiNodeCommand generateMultiNodeCmd(byte[] cmdData) throws JWaveException{
		
		JWaveCommandClass cmdClass = defs.getCommandClass((byte)cmdData[0]&0xFF);
		JWaveCommand cmd = cmdClass.getCommand((byte)cmdData[1]&0xFF);
		
		JWaveMultiNodeCommand multiNodeCmd = new JWaveMultiNodeCommand(cmd);
		int numberOfCommands = cmdData[2];
		JWaveController.log(LogTag.DEBUG,"MultiNodeCmd extraction in progress. "+numberOfCommands+" sub commands to extract:");
			multiNodeCmd.setParamValue(0, numberOfCommands);		
		int currCmdCount = 0;
		int index = 3;
		while ((index < cmdData.length) && (currCmdCount < numberOfCommands)){		
			byte[] subCmdData = new byte[cmdData[index]];
			for (int i = 0; i< cmdData[index]; i++){
				subCmdData[i] = cmdData[index+1+i];
			}
			JWaveNodeCommand nodeCmd = generateNodeCmd(subCmdData);
			JWaveController.log(LogTag.DEBUG,"   - extracted new NodeCmd from MultiCmd - "+nodeCmd.getCommandClass().getName()+" | "+nodeCmd.getCommand().getName());
			multiNodeCmd.addJWaveNodeCmd(nodeCmd);
			index = index+ cmdData[index]+1;
			currCmdCount++;
		}		
		
		return multiNodeCmd;
	}
	
	
	protected JWaveNodeCommand generateSecurityMessageEncapsulation(byte[] cmdData) throws JWaveException{
		JWaveCommandClass cmdClass = defs.getCommandClass((byte)cmdData[0]&0xFF);
		JWaveCommand cmd = cmdClass.getCommand((byte)cmdData[1]&0xFF);
		// 16 55 10 f9 7c 22 f4 86         62 57  0c     57        3d 88 1c 06 41 aa 42 27
		// iv								msg	         nonceid   mac
		
		if (cmdData.length <= (8+1+8) ){
			throw new JWaveSecurityException("received byte array is not a valid message of COMMAND_CLASS_SECURITY. Length of array ("+cmdData.length+") is too short for decryption");
		}
		
		byte[] initializationVector = Arrays.copyOfRange(cmdData, 2, 10);
		byte[] mac = Arrays.copyOfRange(cmdData, cmdData.length-8, cmdData.length);
		byte[] encryptedPayload = Arrays.copyOfRange(cmdData, initializationVector.length+2,cmdData.length-8-1);
		byte receiversNonceId = cmdData[initializationVector.length+1+encryptedPayload.length+1];
		
				
		
		JWaveSecurityMessageEncapsulation secMsg = new JWaveSecurityMessageEncapsulation(cmd, initializationVector, encryptedPayload, receiversNonceId, mac);
		//secMsg.printDetails();
		return secMsg;
	}
	
	protected JWaveNodeCommand generateSecurityMessageEncapsulationGet(byte[] cmdData) throws JWaveException{
		JWaveCommandClass cmdClass = defs.getCommandClass((byte)cmdData[0]&0xFF);
		JWaveCommand cmd = cmdClass.getCommand((byte)cmdData[1]&0xFF);
		// 16 55 10 f9 7c 22 f4 86         62 57  0c     57        3d 88 1c 06 41 aa 42 27
		// iv								msg	         nonceid   mac
		
		if (cmdData.length <= (8+1+8) ){
			throw new JWaveSecurityException("received byte array is not a valid message of COMMAND_CLASS_SECURITY. Length of array ("+cmdData.length+") is too short for decryption");
		}
		
		byte[] initializationVector = Arrays.copyOfRange(cmdData, 2, 10);
		byte[] mac = Arrays.copyOfRange(cmdData, cmdData.length-8, cmdData.length);
		byte[] encryptedPayload = Arrays.copyOfRange(cmdData, initializationVector.length+2,cmdData.length-8-1);
		byte receiversNonceId = cmdData[initializationVector.length+1+encryptedPayload.length+1];
		
				
		
		JWaveSecurityMessageEncapsulationGet secMsg = new JWaveSecurityMessageEncapsulationGet(cmd, initializationVector, encryptedPayload, receiversNonceId, mac);
		//secMsg.printDetails();
		return secMsg;
	}
	
	public JWaveNodeCommand generateNodeCmd(byte[] cmdData) throws JWaveException{
		
		int cmdClassId = (byte)cmdData[0]&0xFF;
		int cmdId   = (byte)cmdData[1]&0xFF;
		
		
		JWaveCommandClass cmdClass = defs.getCommandClass(cmdClassId);
		
		
		if (cmdClass == null){
			throw new JWaveException("Unknown command class (0x"+Integer.toHexString(cmdClassId)+"). Unable to generate NodeCmd");
		}
		
		JWaveCommand cmd = cmdClass.getCommand(cmdId);
		
		if (cmd == null){
			throw new JWaveException("Unknown command (0x"+Integer.toHexString(cmdId)+"). Unable to generate NodeCmd");
		}
		
		if (cmdClass.getKey() == 0x8f){
			return generateMultiNodeCmd(cmdData);
		}
		if (cmdClass.getKey() == 0x98){
			if (cmd.getKey() == 0x81){
				return generateSecurityMessageEncapsulation(cmdData);
			}
			if (cmd.getKey() == 0xC1){
				return generateSecurityMessageEncapsulationGet(cmdData);
			}
		}
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		
		
		byte[] values = new byte[cmdData.length-2]; 
		for(int i = 0; i< values.length; i++){
			values[i] = cmdData[i+2];
		}		
		try {
			nodeCmd.setParamValues(values);
		} catch (Exception exc){
			throw new JWaveException("Unable to generate nodeCmd with given byte array. ("+ByteArrayGeneration.toHexString(cmdData)+")", exc);
		}
		
		
		return nodeCmd;
	}
	
	
	
	
	// +++++++++++++++++++++++++++++++++++++++++ Generating specific node commands +++++++++++++++++++++++++++++++++
	
	
	public JWaveNodeCommand generateCmd_SecurityNonceGet(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY", "SECURITY_NONCE_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_SecurityNonceReport(JWaveSecurityNonce nonce) throws JWaveConvertException{
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","SECURITY_NONCE_REPORT");
		
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		nodeCmd.setParamValues(nonce.getBytes());
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SecuritySchemeGet(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","SECURITY_SCHEME_GET");
		JWaveNodeCommand nodeCmd =  new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0,0);
		} catch (JWaveConvertException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_DoorLock_get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_DOOR_LOCK", "DOOR_LOCK_OPERATION_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_DoorLock_set(boolean lock){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_DOOR_LOCK", "DOOR_LOCK_OPERATION_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		try {
			if (lock){
				nodeCmd.setParamValue(0,0xff);
			} else {
				nodeCmd.setParamValue(0,0x00);
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SecurityKeySet(byte[] networkKey){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","NETWORK_KEY_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		try {
			nodeCmd.setParamValue(0,networkKey);
		} catch (JWaveConvertException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SecuritySchemeInherit(byte scheme){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","SECURITY_SCHEME_INHERIT");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0, scheme & 0xFF);
		} catch (JWaveConvertException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SecuritySupportedGet(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SECURITY","SECURITY_COMMANDS_SUPPORTED_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Clock_Set(Date date){
		JWaveCommand setClockCmd = defs.getCommand("COMMAND_CLASS_CLOCK",1,"CLOCK_SET");
		if (setClockCmd == null){
			// FIXME: handle that shit!
			return null;
		}
		
		JWaveNodeCommand nodeCmdClock = new JWaveNodeCommand(setClockCmd);
					
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);		
		
		int param0; 
		
		int hours = calendar.get(Calendar.HOUR_OF_DAY); 	// gets hour in 24h format		
		int  day = calendar.get(Calendar.DAY_OF_WEEK) -1; 	// SUNDAY = 1, MONDAY = 2, ...
															// Need to have the day in a format // Monday = 1, ... , Sunday = 7
		if (day == 0){
			day = 7;
		}		
		param0 =  (day << 5) + hours;
		
		int param1 = calendar.get(Calendar.MINUTE);
				
		try {
			nodeCmdClock.setParamValue(0, param0 );
			nodeCmdClock.setParamValue(1, param1);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
		return nodeCmdClock;
	}
	
	
	public JWaveNodeCommand generateCmd_Clock_Get(){
		JWaveCommand setClockCmd = defs.getCommand("COMMAND_CLASS_CLOCK",1,"CLOCK_GET");
		if (setClockCmd == null){
			// FIXME: handle that shit!
			return null;
		}
		
		JWaveNodeCommand nodeCmdClock = new JWaveNodeCommand(setClockCmd);
		
		return nodeCmdClock;
	}
	
	
	
	
	public JWaveNodeCommand generateCmd_SwitchBinary_Get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SWITCH_BINARY","SWITCH_BINARY_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_SwitchBinary_Set(int value){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SWITCH_BINARY","SWITCH_BINARY_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0, value);
		} catch (Exception exc) {
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: handle that shit!
		}
		return nodeCmd;
	}
	
	
	public JWaveNodeCommand generateCmd_SwitchMultilevel_Get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SWITCH_MULTILEVEL","SWITCH_MULTILEVEL_GET");
		
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		return nodeCmd;
	}
	
	
	public JWaveNodeCommand generateCmd_SwitchMultilevel_Set(int value){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SWITCH_MULTILEVEL","SWITCH_MULTILEVEL_SET");
		
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0, value);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: handle that shit!
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_WakeUpInterval_NoMoreInformation(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_WAKE_UP","WAKE_UP_NO_MORE_INFORMATION");
		return new JWaveNodeCommand(cmd);
	}
	
	
	public JWaveNodeCommand generateCmd_SensorBinary_Get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SENSOR_BINARY","SENSOR_BINARY_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	
	
	public JWaveNodeCommand generateCmd_WakUpInterval_Get(){
		return generateCmd_WakUpInterval_Get(1);
	}
	
	public JWaveNodeCommand generateCmd_ColorControl_State_Get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_COLOR_CONTROL",1,"STATE_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_ColorControl_State_Set(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_COLOR_CONTROL",1,"STATE_SET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_WakUpInterval_Get(int version){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_WAKE_UP",version,"WAKE_UP_INTERVAL_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_WakeUpInterval_Set(long seconds, int nodeId){
		
		JWaveCommandClass cmdClass = defs.getCommandClass(0x84); // COMMAND_CLASS_WAKE_UP_INTERVAL
		JWaveCommand cmd = cmdClass.getCommand(0x04); // WAKE_UP_INTERVAL_SET
		
		
		
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0x00,(int)seconds);
			nodeCmd.setParamValue(0x01,nodeId);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
		return nodeCmd;
	}
	
	
	public JWaveNodeCommand generateCmd_Battery_Get(int version){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_BATTERY",version,"BATTERY_GET");
		return new JWaveNodeCommand(cmd);
	}
	
	public JWaveNodeCommand generateCmd_Battery_Get(){
		return generateCmd_Battery_Get(1);		 
	}
	
	public JWaveNodeCommand generateCmd_SensorMultilevel_Get(int expectedResponses){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SENSOR_MULTILEVEL", "SENSOR_MULTILEVEL_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		if (expectedResponses > 1){
			nodeCmd.setExpectedResponses(expectedResponses);
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SensorMultilevel_Get(){
		return generateCmd_SensorMultilevel_Get(1);
	}
	
	public JWaveNodeCommand generateCmd_SensorMultilevel_Get_V5(int sensortype){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SENSOR_MULTILEVEL", 5,"SENSOR_MULTILEVEL_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		try{
			nodeCmd.setParamValue(0,(byte)sensortype);
			nodeCmd.setParamValue(1,(byte)0);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME
		}
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Thermostat_Setpoint_Get(int type){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_THERMOSTAT_SETPOINT", "THERMOSTAT_SETPOINT_GET");
		JWaveNodeCommand nodeCmd =  new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0,type);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: handle that!
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Association_Get(int group){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_ASSOCIATION", "ASSOCIATION_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0,group);			
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}		
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Association_Set(int group, int nodeId){
		//int[] ids = new int[1];
		//ids[0] = nodeId;
		//return generateCmd_Association_Set(group, ids);
		
		
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_ASSOCIATION", "ASSOCIATION_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0,group);
			nodeCmd.setParamValue(1,nodeId);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
			
		
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Thermostat_Setpoint_Set(int type, double value){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_THERMOSTAT_SETPOINT", "THERMOSTAT_SETPOINT_SET");
		JWaveNodeCommand nodeCmd =  new JWaveNodeCommand(cmd);
		
		int val = (int)Math.round(value * 100);
		
		
		
		byte[] bytes = new byte[2];
		
		 
		 
		bytes[0] = (byte)(val >>> 8);
		bytes[1] = (byte)(val);
		
		try {
			nodeCmd.setParamValue(0,1);
			nodeCmd.setParamValue(1, 0x42);
			nodeCmd.setParamValue(2, bytes);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: handle that!
		}		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Time_Parameters_Set(Date date){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_TIME_PARAMETERS","TIME_PARAMETERS_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		try {
			nodeCmd.setParamValue(0, cal.get(Calendar.YEAR));
			nodeCmd.setParamValue(1, cal.get(Calendar.MONTH));
			nodeCmd.setParamValue(2, cal.get(Calendar.DAY_OF_MONTH));
			nodeCmd.setParamValue(3, cal.get(Calendar.HOUR_OF_DAY));
			nodeCmd.setParamValue(4, cal.get(Calendar.MINUTE));
			nodeCmd.setParamValue(5, cal.get(Calendar.SECOND));
		} catch (Exception exc){
			
		}
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Configuration_Get(int paramId){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_CONFIGURATION", "CONFIGURATION_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		try {
			nodeCmd.setParamValue(0,(byte)paramId);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: Error Handling
		}
		
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_Configuration_Set(int paramId, JWaveCommandParameterType type, int value){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_CONFIGURATION", "CONFIGURATION_SET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		try {
			nodeCmd.setParamValue(0,(byte)paramId);
			nodeCmd.setParamValue(1, JWaveCommandParameterType.getSize(type));
			nodeCmd.setParamValue(2, JWaveCommandParameterType.toByteArray(type, value));
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: Error Handling
		}
		
		return nodeCmd;
	}
	
	
	public JWaveNodeCommand generateCmd_Meter_Get(){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_METER",1, "METER_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		return nodeCmd;
	}
	
	/**
	 * 
	 * @param scale 0 = kWh, 1 = kVAh, 2 = W, 3 = V, 4 = A, 5 = Power Factor
	 * @return
	 */
	public JWaveNodeCommand generateCmd_Meter_Get_V2(byte scale){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_METER",2, "METER_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		byte value = (byte)(scale << 3);
		try {
			nodeCmd.setParamValue(0,value);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: Error Handling
		}
		return nodeCmd;
	}
	
	/**
	 * 
	 * @param scale 0 = kWh, 1 = kVAh, 2 = W, 3 = V, 4 = A, 5 = Power Factor
	 * @return
	 */
	public JWaveNodeCommand generateCmd_Meter_Get_V4(byte scale){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_METER",2, "METER_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		byte struct = 0;
		try {
			nodeCmd.setParamValue(0,struct);
			nodeCmd.setParamValue(1,scale);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: Error Handling
		}
		return nodeCmd;
	}
	
	public JWaveNodeCommand generateCmd_SensorAlarm_Get(int sensorType){
		JWaveCommand cmd = defs.getCommand("COMMAND_CLASS_SENSOR_ALARM", "SENSOR_ALARM_GET");
		JWaveNodeCommand nodeCmd = new JWaveNodeCommand(cmd);
		
		try {
			nodeCmd.setParamValue(0,sensorType);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
		
		return nodeCmd;
	}
	
}
