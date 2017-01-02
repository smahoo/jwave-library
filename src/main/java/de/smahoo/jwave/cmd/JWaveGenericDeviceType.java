package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.List;

public class JWaveGenericDeviceType {
	protected List<JWaveSpecificDeviceType> specDevTypes;
	protected int key;
	protected String name = null;
	protected String help = null;
	protected boolean readOnly;
	protected String comment = null;
	 
	
	protected JWaveGenericDeviceType(){
		specDevTypes = new ArrayList<JWaveSpecificDeviceType>();	
	}
	
	public JWaveSpecificDeviceType getSpecificDeviceType(int key){
		for (JWaveSpecificDeviceType st : specDevTypes){
			if (st.getKey() == key){
				return st;
			}
		}
		return null;
	}
	
	public List<JWaveSpecificDeviceType> getSpecificDeviceTypes(){
		return specDevTypes;
	}
	
	public String getName(){
		return name;
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
	
	public int getKey(){
		return key;
	}
	
	@Override
	public String toString(){
		return getName();
	}
}
