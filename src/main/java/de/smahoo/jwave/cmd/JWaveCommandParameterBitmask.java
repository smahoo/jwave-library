package de.smahoo.jwave.cmd;

public class JWaveCommandParameterBitmask extends JWaveCommandParameterValue {
	//  <bitmask key="0x00" paramoffs="255" lenmask="0x00" lenoffs="0" />
	
	protected int paramoffs;
	protected int lenmask;
	protected int lenoffs;
	
	
	public int getParamOffs(){
		return paramoffs;
	}
	
	public int getLenMask(){
		return lenmask;
	}
	
	public int getLenOffs(){
		return lenoffs;
	}
}
