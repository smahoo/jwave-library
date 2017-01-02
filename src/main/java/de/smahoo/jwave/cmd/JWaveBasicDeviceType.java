package de.smahoo.jwave.cmd;

public class JWaveBasicDeviceType {
	//  <bas_dev read_only="False" name="BASIC_TYPE_CONTROLLER" key="0x01" help="Controller" comment="Node is a portable controller " />
	
	protected String name = null;
	protected int key;
	protected String help = null;
	protected String comment = null;
	protected boolean readOnly;
	
	public String getName(){
		return name;
	}
	
	public int getKey(){
		return key;
	}
	
	public String getHelp(){
		return help;
	}
	
	public String getComment(){
		return comment;
	}
	
	public boolean isReadOnly(){
		return readOnly;
	}
	
	@Override
	public String toString(){
		return getName();
	}
}
