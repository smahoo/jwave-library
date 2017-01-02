package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.List;

public class JWaveCommandParameter {
	
	protected int key;	
	protected String name;
	protected JWaveCommandParameterType type;
	protected int typeHashCode;
	protected String comment;
	protected JWaveCommand cmd;
	
	protected List<JWaveCommandParameterValue> values = null;
	
	protected JWaveCommandParameter(){
		values = new ArrayList<JWaveCommandParameterValue>();
	}
	
	public JWaveCommand getCommand(){
		return cmd;
	}
	
	public boolean hasValues(){
		if (values != null){
			return !values.isEmpty();
		}
		return false;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public JWaveCommandParameterType getType() {
		return type;
	}
	
	public int getTypeHashCode() {
		return typeHashCode;
	}
	
	public String getComment() {
		return comment;
	}
	
	
	public List<JWaveCommandParameterValue> getValues() {
		return values;
	}
	
}
