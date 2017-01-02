package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.List;

public class JWaveCommand {

	protected int key;
    protected String name 		= null;
    protected String help		= null;
    protected String comment 	= null;
    protected List<JWaveCommandParameter> params = null;
    protected JWaveCommandClass cmdClass = null;
    
    protected JWaveCommand(){
    	params = new ArrayList<JWaveCommandParameter>();
    }
    
    public List<JWaveCommandParameter> getParamList(){
    	return params;
    }
    
    public JWaveCommandParameter getParam(int key){
    	for (JWaveCommandParameter p : params){
    		if (p.getKey() == key){
    			return p;
    		}
    	}
    	return null;
    }
    
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
	
	public JWaveCommandClass getCommandClass(){
		return cmdClass;
	}
	
	protected List<JWaveCommandParameter> getParams(){
		return params;
	}
    
}
