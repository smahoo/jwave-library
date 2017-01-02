package de.smahoo.jwave.cmd;

public class JWaveCommandParameterConstant extends JWaveCommandParameterValue {
	//key="0x00" flagname="Reserved" flagmask="0x00" />
	
	
	protected String flagName = null;
	protected int flagMask;
	
	
	
	public String getFlagName(){
		return flagName;
	}
	
	public int getFlagMask(){
		return flagMask;
	}
}
