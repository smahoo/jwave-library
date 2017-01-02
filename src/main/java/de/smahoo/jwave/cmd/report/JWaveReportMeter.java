package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveReportMeter extends JWaveReport{

	
	public static final int RATE_TYPE_UNKNOWN  = 0;
	public static final int RATE_TYPE_IMPORT   = 1;
	public static final int RATE_TYPE_EXPORT   = 2;
	public static final int RATE_TYPE_IMPORT_EXPORT = 3;
	
	public static final int METER_TYPE_UNKNOWN  = 0;
	public static final int METER_TYPE_ELECTRIC = 1;
	public static final int METER_TYPE_GAS		= 2;
	public static final int METER_TYPE_WATER	= 3;
	
	// electricity	
	public static final int SCALE_KWH 	= 0;
	public static final int SCALE_KVAH 	= 1; // do not ask why, 1W = 1V x 1A = 1VA -> should be the same as SCALE_KWH
	public static final int SCALE_W	  	= 2;
	public static final int SCALE_V		= 3;
	public static final int SCALE_A		= 4;
	public static final int SCALE_POWERFACTOR = 5; // have no idea what that could be
	
	// gas and water
	public static final int SCALE_CUBIC_METERS = 0;
	public static final int SCALE_FEET		   = 1;
	// common
	public static final int SCALE_PULSE_COUNT = 3;

	
	private int rateType;
	private int meterType; 
	
	private int size;
	private int scale;
	private int precission;
	
	
	private int value;
	
	public JWaveReportMeter(JWaveNodeCommand cmd) {
		super(cmd);
	}

	public int getRateType(){
		return rateType;
	}
	
	public int getMeterType(){
		return meterType;
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
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		meterType = METER_TYPE_ELECTRIC;
		rateType = RATE_TYPE_UNKNOWN;		
		
		try {
			byte p2 = JWaveCommandParameterType.toByte(nodeCmd.getParamValue(1));
			size = getSize(p2);	
			precission = getPrecission(p2);
			if (precission > 0){
				precission = (int)Math.pow(10,precission);
			} else {
				precission = 1;
			}
			scale = getScale(p2);
			nodeCmd.setParamSize(2,size);
			value = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2));
			if (nodeCmd.getParamValues().length > 2 + size){
				// it's not METER_REPORT_V1			
				byte p1 = JWaveCommandParameterType.toByte(nodeCmd.getParamValue(0));
				meterType = getMeterType(p1);
				rateType = getRateType(p1);				
			}		
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
		}
		
	}
	

	protected int getRateType(byte value){
		byte n = (byte)(value >>> 5);		
		return n & 0x03; // 
	}
	
	protected int getMeterType(byte value){
		return value & 0x1F;
	}
	
	protected int getPrecission(byte value){
		byte n =  (byte)(value >>> 5);
		return n & 0x07;
	}
	
	protected int getScale(byte value){
		byte n = (byte)(value >>> 3);		
		return n & 0x03; // 
	}
	
	protected int getSize(byte value){
		return value & 0x07;		
		
	}
	
	
	
}
