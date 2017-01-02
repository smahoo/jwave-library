package de.smahoo.jwave.simulation;


public class JWaveAbstractStreamRule {
	String conditionStr = null;
	
	public JWaveAbstractStreamRule(String conditionStr){
		this.conditionStr = conditionStr;		
	}
	
	public String getConditionStr(){
		return conditionStr;
	}
	
	
}
