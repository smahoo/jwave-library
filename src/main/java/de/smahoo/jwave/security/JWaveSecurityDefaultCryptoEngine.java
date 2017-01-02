package de.smahoo.jwave.security;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveSecurityDefaultCryptoEngine implements JWaveSecurityCryptoEngine{

	private final static int SIZE_CRYPTOBLOCK = 16;
	private final static int SIZE_INITIALIZATION_VECTOR = SIZE_CRYPTOBLOCK;
	
	private static final int SIZE_NETWORK_KEY = 16;
	private static final byte[] AUTHENTICATION_VECTOR 	= new byte[SIZE_NETWORK_KEY];
    private static final byte[] ENCRYPTION_VECTOR 		= new byte[SIZE_NETWORK_KEY];
	
    
    static {
        Arrays.fill(AUTHENTICATION_VECTOR, (byte)0x55);
        Arrays.fill(ENCRYPTION_VECTOR, (byte)0xAA);
    }
    
	
	
	public byte[] encrypt(byte[] payload, byte[] encryptionKey, byte[] initializationVector) throws JWaveSecurityException{
		return aes_ofb(payload,encryptionKey,initializationVector);
	 }
	
	
	
	public byte[] decrypt(byte[] encryptedMessage, JWaveSecurityNetworkKey networkKey, byte[] iv) throws JWaveSecurityException{		
		return aes_ofb(encryptedMessage,networkKey.getEncryptionKey(),iv);
	}
	
		
	public byte[] generateMAC(byte nodeId, byte[] initializationVector, JWaveSecurityNetworkKey networkKey, byte securityHeader, final byte[] encryptedPayload) throws JWaveSecurityException{
		
		byte[] authenticationData = new byte[encryptedPayload.length + 4];
        authenticationData[0] = securityHeader;        
        authenticationData[1] = 1;
        authenticationData[2] = nodeId;
        authenticationData[3] = (byte)encryptedPayload.length;
      
        System.arraycopy(encryptedPayload, 0, authenticationData, 4, encryptedPayload.length);
        
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(networkKey.getAuthenticationKey(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
          
            byte[] mac = cipher.doFinal(initializationVector);
           // JWaveController.log(LogTag.DEBUG,"mac 1 "+ByteArrayGeneration.toHexString(mac));
            for (int i = 0; i < authenticationData.length; i += SIZE_CRYPTOBLOCK) {
           	// JWaveController.log(LogTag.DEBUG,"mac before xor "+ByteArrayGeneration.toHexString(mac));
               for (int j = 0; j < mac.length; j++) {
               	
           	   if (i+j < authenticationData.length){
           		  
           		   mac[j] ^= authenticationData[i+j];
        
           	   } else {
           		   mac[j] ^= 0;
           	   }
               }           
        
               mac = cipher.doFinal(mac);             
        
            }

            return Arrays.copyOf(mac, 8);

        } catch (Exception exc){
       	 throw new JWaveSecurityException("unable to generate mac",exc);
        }
	 }
	
	
	public byte[] generateInitializationVector(JWaveSecurityNonce sendersNonce, JWaveSecurityNonce receiversNonce) {
		 
		 byte[] iv = new byte[SIZE_INITIALIZATION_VECTOR];
		 
		 System.arraycopy(sendersNonce.getBytes(), 0, iv, 0,sendersNonce.getBytes().length);
		 System.arraycopy(receiversNonce.getBytes(),0,iv, sendersNonce.getBytes().length,sendersNonce.getBytes().length);
	
		 return iv;
	 }
	
	
	@Override
	public JWaveSecurityNetworkKey generateNetworkKey(byte[] networkKey) throws JWaveSecurityException {
		byte[] encryptionKey;
		byte[] authenticationKey;
			
			
			try {	
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
				SecretKeySpec keySpec = new SecretKeySpec(networkKey, "AES");
				cipher.init(Cipher.ENCRYPT_MODE, keySpec);

				authenticationKey = cipher.doFinal(AUTHENTICATION_VECTOR);
				encryptionKey = cipher.doFinal(ENCRYPTION_VECTOR);
				return new JWaveSecurityNetworkKey(networkKey, encryptionKey, authenticationKey);
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR,"unable to generate encryption and authentication key for given network key",exc);
				throw new JWaveSecurityException("Unable to generate JWaveSecurityNetworkKey with given bytes ("+ByteArrayGeneration.toHexString(networkKey)+")",exc);
			}
		
	}
	

	private byte[] aes_ofb(byte[] payload, byte[] encryptionKey, byte[] iv) throws JWaveSecurityException{
		try {
	 		
	 		Cipher cipher = Cipher.getInstance("AES/ECB/noPadding");
	 		SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
	 		//AlgorithmParameterSpec ivSpec = new IvParameterSpec(initializationVector);
	 		cipher.init(Cipher.ENCRYPT_MODE, keySpec);

	 		 int additionalBytes = SIZE_CRYPTOBLOCK - (payload.length % SIZE_CRYPTOBLOCK);
	 		 byte[] plaintext = new byte[payload.length+additionalBytes];
		    
	 		 
	 		 System.arraycopy(payload,0,plaintext,0,payload.length);
		        if (additionalBytes > 0){	        		        	
		        	Arrays.fill(plaintext,payload.length, plaintext.length-1,(byte)0x00);	        	
		        }
		        
		       // JWaveController.log(LogTag.DEBUG, "plaintext        "+ByteArrayGeneration.toHexString(plaintext));
		        
		        byte[] ciphertext = Arrays.copyOf(plaintext,plaintext.length);
		        
		        byte[] aesResult = Arrays.copyOf(iv,iv.length);
		        
		        for (int i = 0; i < plaintext.length; i += SIZE_CRYPTOBLOCK) {
		        	
		        		//aesResult = cipher.doFinal(aesResult);
		        		aesResult = cipher.update(aesResult);
		        	
		        	for (int j = 0; j < SIZE_CRYPTOBLOCK; j++){
		        		ciphertext[i+j] ^= aesResult[j];
		        		
		        	}
		        }
	 		


	 		byte[] ret = Arrays.copyOf(ciphertext,payload.length);
	 		return ret;
	 	} catch (Exception exc){
	 		throw new JWaveSecurityException("unable to encrypt payload "+exc.getMessage(),exc);
	 	}
	}



	
	
	
	
}
