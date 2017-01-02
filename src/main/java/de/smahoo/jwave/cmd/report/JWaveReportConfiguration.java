package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportConfiguration extends JWaveReport {

	
	private int paramId;
	private byte[] valueBytes;
	private int valueSize;
	private int value;
	
	public JWaveReportConfiguration(JWaveNodeCommand cmd) {
		super(cmd);
		
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		/*
		 *  <param key="0x00" name="Parameter Number" type="BYTE" typehashcode="0x01" comment="">
        <valueattrib key="0x00" hasdefines="False" showhex="True" />
      </param>
      <param key="0x01" name="Level" type="STRUCT_BYTE" typehashcode="0x07" comment="">
        <bitfield key="0x00" fieldname="Size" fieldmask="0x07" shifter="0" />
        <bitfield key="0x01" fieldname="Reserved" fieldmask="0xF8" shifter="3" />
      </param>
      <param key="0x02" name="Configuration Value" type="VARIANT" typehashcode="0x0C" comment="">
        <variant paramoffs="1" showhex="True" signed="True" sizemask="0x07" sizeoffs="0" />
      </param>
		 */
		
		try {
			paramId = JWaveCommandParameterType.toByte(nodeCmd.getParamValue(0));
			valueSize = getSize(JWaveCommandParameterType.toByte(nodeCmd.getParamValue(1)));
			nodeCmd.setParamSize(2, valueSize);
			switch (valueSize){
			case 1:				
			case 2:
			case 3:
			case 4:
				value = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2));
				default:
					valueBytes = nodeCmd.getParamValue(2);
			}
		} catch (Exception exc){
			
		}
	}

	protected int getSize(byte value){	
		return value;
	}
	
	public int getParamId(){
		return paramId;
	}
	
	public int getValueSize(){
		return valueSize;
	}
	
	public byte[] getValueBytes(){
		return valueBytes;
	}
	
	public int getValue(){
		return value;
	}
	
}
