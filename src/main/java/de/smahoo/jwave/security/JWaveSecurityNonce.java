package de.smahoo.jwave.security;

import java.util.Date;

public class JWaveSecurityNonce {
	
	private static final long  DEFAULT_NONCE_ALIVE_TIME = 10000; //10sec
	
	private byte[] nonceInBytes;
	private int nonceId;
	private Date creationTimeStamp;
	private long aliveTime;
	
	
	public JWaveSecurityNonce(byte[] bytes){
		this(bytes,new Date(), DEFAULT_NONCE_ALIVE_TIME);
	}
	
	public JWaveSecurityNonce(byte[] bytes, Date creationTimeStamp, long aliveTime){
		this.nonceInBytes = bytes;	
		this.creationTimeStamp = creationTimeStamp;
		this.aliveTime = aliveTime;
		setNonceId();
	}
	
	public byte[] getBytes(){
		return nonceInBytes;
	}

	@Override
	public boolean equals(Object obj){
		if (!(obj instanceof JWaveSecurityNonce)){
			return false;
		}
		
		JWaveSecurityNonce otherNonce = (JWaveSecurityNonce)obj;
		byte[] otherBytes = otherNonce.getBytes();
		if (otherBytes.length != nonceInBytes.length){
			return false;
		}
		
		for (int i = 0; i<nonceInBytes.length-1; i++){
			if (nonceInBytes[i] != otherBytes[i]){
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isValid(){
		Date now = new Date();
		return  (now.getTime() - creationTimeStamp.getTime())<=aliveTime;
	
	}
	
	public int getNonceId(){
		return nonceId;
	}
	
	private void setNonceId(){
		nonceId = nonceInBytes[0]&0xFF;
	}
	
}
