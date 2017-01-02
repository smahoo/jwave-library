package de.smahoo.jwave.security;

public class JWaveSecurityNetworkKey {
	
	

	private byte[] key;
	private byte[] encryptionKey;
	private byte[] authenticationKey;
	
//	public JWaveSecurityNetworkKey(byte[] key){
//		this(key,null,null);
//	}
//	

	public JWaveSecurityNetworkKey(byte[] key, byte[] encryptionKey, byte[] authenticationKey){
		this.key = key;
		this.encryptionKey = encryptionKey;
		this.authenticationKey = authenticationKey;
	}
	
	public byte[] getKey(){
		return key;
	}
	
	public byte[] getAuthenticationKey(){
		return authenticationKey;
	}
	
	public byte[] getEncryptionKey(){
		return encryptionKey;
	}
	
	public boolean hasEncryptionKey(){
		return (encryptionKey != null);
	}
	
	public boolean hasAuthenticationKey(){
		return (authenticationKey != null);
	}
	
	

	
	
}
