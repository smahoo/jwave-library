package de.smahoo.jwave.cmd;

public class JWaveSpecificDeviceType {
	
	protected int key;
	protected String name 	 = null;
	protected String help 	 = null;
	protected String comment = null;
	
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHelp() {
		return help;
	}
	
	public String getComment() {
		return comment;
	}
	
	
	@Override
	public String toString(){
		return getName();
	}	
}
