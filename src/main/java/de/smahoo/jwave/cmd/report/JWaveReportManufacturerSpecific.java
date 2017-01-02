package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportManufacturerSpecific extends JWaveReport{

	protected int manufacturerId;
	protected int productTypeId;
	protected int productId;
	
	public JWaveReportManufacturerSpecific(JWaveNodeCommand nodeCmd){
		super(nodeCmd);
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		manufacturerId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		productTypeId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(1));
		productId = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2));
	}
	
	public int getManufacturerId(){
		return manufacturerId;
	}
	
	public int getProductTypeId(){
		return productTypeId;
	}
	
	public int getProductId(){
		return productId;
	}
	
}
