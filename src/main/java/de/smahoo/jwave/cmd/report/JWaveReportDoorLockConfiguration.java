package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportDoorLockConfiguration extends JWaveReport {

	private OperationType operationType;
	private int timeoutMinutes;
	private int timeoutSeconds;
	
	
	boolean insideDoorHandlesMode;
	boolean outsiteDoorHandlesMode;
	
	public JWaveReportDoorLockConfiguration(JWaveNodeCommand cmd) {
		super(cmd);		
	}

	
	public OperationType getOperationType() {
		return operationType;
	}
	
	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}
	
	public int getTimeoutMinutes() {
		return timeoutMinutes;
	}
	
	public boolean isInsideDoorHandlesMode() {
		return insideDoorHandlesMode;
	}
	
	public boolean isOutsiteDoorHandlesMode() {
		return outsiteDoorHandlesMode;
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		setOperationType(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0)));
		try {
			setHandleProperties(JWaveCommandParameterType.toByte(nodeCmd.getParamValue(1)));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		setTimeoutMinutes(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2)));
		setTimeoutSeconds(JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(3)));
	}

	private void setOperationType(int type){
		switch(type){
			case 0x01 : 
				operationType = OperationType.OT_CONSTANT; 
				break;
			case 0x02 : 
				operationType = OperationType.OT_TIMED;
				break;
		}
	}
	
	private void setTimeoutMinutes(int minutes){
		this.timeoutMinutes = minutes;
	}
	
	private void setTimeoutSeconds(int seconds){
		this.timeoutSeconds = seconds;
	}
	
	private void setHandleProperties(byte properties){
		this.insideDoorHandlesMode = ((properties & 0x0F) == 0x0F);
		this.outsiteDoorHandlesMode = ((properties & 0xF0) == 0xF0);
	}
	
	public enum OperationType{
		OT_CONSTANT, 
		OT_TIMED
	}
	
}
