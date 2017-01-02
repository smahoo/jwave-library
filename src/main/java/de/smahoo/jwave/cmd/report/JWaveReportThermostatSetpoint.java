package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveReportThermostatSetpoint extends JWaveReport{

	public final static int SETPOINT_TYPE_HEATING  = 1;
	public final static int SETPOINT_TYPE_COOLING  = 2;
	public final static int SETPOINT_TYPE_FURNANCE = 7;
	public final static int SETPOINT_TYPE_DRY_AIR  = 8;
	public final static int SETPOINT_TYPE_MOIST_AIR = 9;
	public final static int SETPOINT_TYPE_AUTO_CHANGEOVER = 10;
	
	
	protected int setpointType;
	protected int value;
	protected int scale;
	protected int precision;		
	
	public JWaveReportThermostatSetpoint(JWaveNodeCommand cmd){
		super(cmd);
	}
	public int getSetpointType(){
		return setpointType;
	}
	
	public int getValue(){
		return value;
	}
	
	public int getScale(){
		return scale;
	}
	
	public int getPrecision(){
		return precision;
	}
	
	/**
	 * always givs the temperature in Celsius
	 * @return setpoint temperature in celsius
	 */
	public double getTemperature(){
		double temperature;
		if (getPrecision() > 0){
			temperature = (double)getValue()/(double)getPrecision();

		} else {
			temperature = (double)getValue();
		}
		if (getScale() == 1){ // Temperature is given in Fahrenheit
			temperature = (temperature - 32.0) * 5.0/9.0; // transform temperature to celsius
		}
		// precision might have been lost during calculation
		temperature = (double)Math.round(temperature*getPrecision())/(double)getPrecision(); 
		return temperature;
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand cmd){
		try {
			setpointType = JWaveCommandParameterType.toByte(cmd.getParamValue(0));
			byte p1 = JWaveCommandParameterType.toByte(cmd.getParamValue(1));
			int size = getSize(p1);			
			cmd.setParamSize(2, size);
			scale = getScale(p1);
			precision = getPrecission(p1);
			if (precision > 0){
				precision = (int)Math.pow(10,precision);
			}
			switch (size){
			case 1: 
				value = JWaveCommandParameterType.toByte(cmd.getParamValue(2));
				break;
			case 2:
				value = JWaveCommandParameterType.toInteger(cmd.getParamValue(2));
			
			}
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
	}
	
	protected int getPrecission(byte value){
		return (byte)(value >> 5);
	}
	
	protected int getScale(byte value){
		byte n = (byte)(value << 3);		
		n = (byte)(n >> 6);		
		return n;
	}
	
	protected int getSize(byte value){
		byte n = (byte)(value << 5);		
		n = (byte)(n >> 5);		
		return n;
	}
	
	
}
