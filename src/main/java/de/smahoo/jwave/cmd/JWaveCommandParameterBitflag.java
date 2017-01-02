package de.smahoo.jwave.cmd;

public class JWaveCommandParameterBitflag extends JWaveCommandParameterValue {
	//<bitflag key="0x02" flagname="First" flagmask="0x80" />
	protected String flagname = null;
	protected int flagmask;
	
	
	public String getFlagName(){
		return flagname;
	}
	
	public int getFlagMask(){
		return flagmask;
	}
	
}
