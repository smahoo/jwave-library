package de.smahoo.jwave.io;

import de.smahoo.jwave.cmd.JWaveCommandType;

/**
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public class JWaveDatagram {
	
	public static final int TRANSMISSION_FAILED_NACK = 1;
	public static final int TRANSMISSION_FAILED_CAN = 2;
	
	protected static final int BYTE_REQUEST 	= 2;
	protected static final int BYTE_LENGTH  	= 1;
	protected static final int BYTE_COMMANDTYPE = 3;	
	
	protected byte[] payload;		// contains only the relevant data for further processing
	protected byte[] bytes;			// complete bytes including checksum	
	protected JWaveDatagramStatus status;
	
	
	protected JWaveDatagram response = null;
	
	public boolean isRequest(){
		return (bytes[BYTE_REQUEST] == 0);
	}
	
	protected void setStatus(JWaveDatagramStatus status){
		this.status = status;
	}
	
	public JWaveDatagramStatus getStatus(){
		return status;
	}
	
	protected void setBytes(byte[] bytes){
		this.bytes = bytes.clone();	
		setPayload();
	}
	
	private void setPayload(){
		payload = new byte[bytes[BYTE_LENGTH]-3];
		for (int i = 0; i< bytes[BYTE_LENGTH]-3;i++){
			payload[i] = bytes[BYTE_LENGTH + 3 + i];
		}
	}
	
	public byte[] toByteArray(){
		return bytes;
	}
	
	public byte[] getPayload(){
		return payload;
	}
	
	public JWaveCommandType getCommandType(){
		return JWaveCommandType.getCommandType(bytes[BYTE_COMMANDTYPE]);
	}

		
	public int getDataLength(){
		return payload.length;
	}
	
		
	public String toHexString(){
		return toHexString(bytes);
	}	
	
	
	public boolean hasResponse(){
		return response != null;
	}
	
	public void setResponse(JWaveDatagram datagram){
		this.response = datagram;		
	}
	
	public static String toHexString(byte[] bytes){
		StringBuffer res =new StringBuffer();
		String tmp;
		boolean firstDigit = true;
		if (bytes != null){
			for (int i= 0; i<bytes.length; i++){
				tmp = Integer.toHexString((bytes[i]&0xFF));
				if (tmp.length() <2){
					tmp = "0"+tmp;
				}
				if (firstDigit){
					res.append(tmp);
					firstDigit = false;
				} else {
					res.append(" "+tmp);
				}
			}
		}
		
		return res.toString();
	}
	
	
	
}
