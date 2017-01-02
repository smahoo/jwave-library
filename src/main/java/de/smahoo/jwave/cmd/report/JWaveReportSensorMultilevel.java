package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveReportSensorMultilevel extends JWaveReport{

	public static final int SCALE_CELSIUS    = 1;
	public static final int SCALE_FAHRENHEIT = 0;
	
	public static final int TYPE_TEMPERATURE = 0x01; 		// v1
	public static final int TYPE_GENERAL_PURPOSE = 0x02; 	// v1
	public static final int TYPE_LUMINANCE = 0x03; 			// v1
	public static final int TYPE_POWER = 0x04; 				// v2
	public static final int TYPE_RELATIVE_HUMIDITY = 0x05;  // v2
	/*
	0x06 Velocity (version 2) 
    0x07 Direction (version 2) 
    0x08 Atmospheric pressure (version 2) 
    0x09 Barometric pressure (version 2) 
    0x0a Solar radiation (version 2) 
    0x0b Dew point (version 2) 
    0x0c Rain rate (version 2) 
    0x0d Tide level (version 2) 
    0x0e Weight (version 3)
    0x0f Voltage (version 3)
    0x10 Current (version 3)
    0x11 CO2-level (version 3)
    0x12 Air flow (version 3)
    0x13 Tank capacity (version 3)
    0x14 Distance (version 3)
    0x15 Angle Position (version 4)
    0x16 Rotation (v5)
    0x17 Water temperature (v5)
    0x18 Soil temperature (v5)
    0x19 Seismic intensity (v5)
    0x1a Seismic magnitude (v5)
    0x1b Ultraviolet (v5)
    0x1c Electrical resistivity (v5)
    0x1d Electrical conductivity (v5)
    0x1e Loudness (v5)
    0x1f Moisture (v5)
    0x20 Frequency (v6)
    0x21 Time (v6)
    0x22 Target Temperature (v6)
*/

	
	int sensorType;
	int value;
	int scale;
	int precission;		
	
	public JWaveReportSensorMultilevel(JWaveNodeCommand cmd){
		super(cmd);
	}
	
	public int getSensorType(){
		return sensorType;
	}
	
	public int getValue(){
		return value;
	}
	
	public int getScale(){
		return scale;
	}
	
	public int getPrecission(){
		return precission;
	}
	
	@Override
	protected void evaluate(JWaveNodeCommand cmd){
		try {
			sensorType = JWaveCommandParameterType.toByte(cmd.getParamValue(0));
			byte p1 = JWaveCommandParameterType.toByte(cmd.getParamValue(1));
			int size = getSize(p1);			
			cmd.setParamSize(2, size);
			scale = getScale(p1);
		
			precission = (int)Math.pow(10,getPrecission(p1));
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
