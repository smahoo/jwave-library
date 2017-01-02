package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveConvertException;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportDoorLockOperation extends JWaveReport{

	
	private DoorLockMode doorLockMode;
	private int doorCondition;
	private int timeoutMinutes;
	private int timeoutSeconds;
	
	boolean insideDoorHandlesMode;
	boolean outsiteDoorHandlesMode;
	
	public JWaveReportDoorLockOperation(JWaveNodeCommand cmd) {
		super(cmd);		
	}
	
	public int getTimeoutMinutes(){
		return timeoutMinutes;
	}
	
	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}
	
	public int getDoorCondition() {
		return doorCondition;
	}
	
	public DoorLockMode getDoorLockMode() {
		return doorLockMode;
	}
	
	public boolean isInsideDoorHandlesMode() {
		return insideDoorHandlesMode;
	}

	public boolean isOutsiteDoorHandlesMode() {
		return outsiteDoorHandlesMode;
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
			
		setDoorLockMode(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0)));
	
		try {
			setHandleProperties(JWaveCommandParameterType.toByte(nodeCmd.getParamValue(1)));
		} catch (JWaveConvertException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setDoorCondition(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2)));
		
		setTimeoutMinutes(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(3)));
		setTimeoutSeconds(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(4)));
	}
	
	private void setHandleProperties(byte properties){
		this.insideDoorHandlesMode = ((properties & 0x0F) == 0x0F);
		this.outsiteDoorHandlesMode = ((properties & 0xF0) == 0xF0);
	}
	
	private void setDoorCondition(int condition){
		this.doorCondition = condition;
	}
	
	private void setTimeoutMinutes(int minutes){
		this.timeoutMinutes = minutes;
	}
	
	private void setTimeoutSeconds(int seconds){
		this.timeoutSeconds = seconds;
	}
	
	private void setDoorLockMode(int mode){
		switch(mode){
			case 0x00:	
				doorLockMode = DoorLockMode.DLM_UNSECURED;
				break;
			case 0x01:	
				doorLockMode = DoorLockMode.DLM_UNSECURED_TIMEOUT;
				break;
			case 0x10:	
				doorLockMode = DoorLockMode.DLM_UNSECURED_INSIDE_DOOR_HANDLES;
				break;
			case 0x11:	
				doorLockMode = DoorLockMode.DLM_UNSECURED_INSIDE_DOOR_HANDLES_TIMEOUT;
				break;
			case 0x20:	
				doorLockMode = DoorLockMode.DLM_UNSECURED_OUTSIDE_DOOR_HANDLES;
				break;
			case 0x21:	
				doorLockMode = DoorLockMode.DLM_UNSECURED_OUTSIDE_DOOR_HANDLES_TIMEOUT;
				break;
			case 0xff:	
				doorLockMode = DoorLockMode.DLM_SECURED;
				break;
			
				
		}
	}
	
	public enum DoorLockMode{
		DLM_UNSECURED, 								// Door Unsecured
		DLM_UNSECURED_TIMEOUT,						// Door Unsecured with timeout" flagmask
		DLM_UNSECURED_INSIDE_DOOR_HANDLES,			// Door Unsecured for inside Door Handles
		DLM_UNSECURED_INSIDE_DOOR_HANDLES_TIMEOUT,  // Door Unsecured for inside Door Handles with timeout
		DLM_UNSECURED_OUTSIDE_DOOR_HANDLES,			// Door Unsecured for outside Door Handles
		DLM_UNSECURED_OUTSIDE_DOOR_HANDLES_TIMEOUT, // Door Unsecured for outside Door Handles with timeout
		DLM_SECURED;								// Door Secured
	}

}
