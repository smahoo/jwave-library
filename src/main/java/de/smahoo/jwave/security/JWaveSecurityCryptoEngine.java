package de.smahoo.jwave.security;

public interface JWaveSecurityCryptoEngine {
	
	public byte[] encrypt(byte[] payload, byte[] encryptionKey, byte[] initializationVector) throws JWaveSecurityException;
	public byte[] decrypt(byte[] encryptedMessage, JWaveSecurityNetworkKey networkKey, byte[] initializationVector) throws JWaveSecurityException;
	public byte[] generateMAC(byte nodeId, byte[] initializationVector, JWaveSecurityNetworkKey networkKey, byte securityHeader, final byte[] encryptedPayload) throws JWaveSecurityException;
	public byte[] generateInitializationVector(JWaveSecurityNonce sendersNonce, JWaveSecurityNonce receiversNonce)  throws JWaveSecurityException ;
	public JWaveSecurityNetworkKey generateNetworkKey(byte[] networkKey)  throws JWaveSecurityException ;
}
