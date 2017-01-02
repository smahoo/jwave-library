package de.smahoo.jwave.cmd;

public class JWaveCommandParameterBitfield extends JWaveCommandParameterValue {
//bitfield key="0x00" fieldname="Reports to follow" fieldmask="0x0F" shifter="0" />
	
	protected String fieldname = null;
	protected int fieldmask;
	protected int shifter;
	
	public String getFieldName(){
		return fieldname;
	}
	
	public int getFieldMask(){
		return fieldmask;
	}
	
	public int getShifter(){
		return shifter;
	}
}
