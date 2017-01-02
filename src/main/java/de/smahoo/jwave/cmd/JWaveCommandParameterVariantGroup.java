package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.List;

public class JWaveCommandParameterVariantGroup extends JWaveCommandParameter {
	
	protected List<JWaveCommandParameter> params;
	
	public JWaveCommandParameterVariantGroup(){
		super();
		params = new ArrayList<JWaveCommandParameter>();
	}
	
	
	public void addCmdParam(JWaveCommandParameter param){
		params.add(param);
	}
	
	public List<JWaveCommandParameter> getParams(){
		return params;
	}
	
	public JWaveCommandParameterType getType() {
		return JWaveCommandParameterType.VARIANT_GROUP;
	}
	
}
