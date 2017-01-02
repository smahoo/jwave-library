package de.smahoo.jwave.security;



import java.util.Arrays;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommand;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveSecurityMessageEncapsulation extends JWaveNodeCommand {

	private byte[] initializationVector;
	private byte[] encryptedPayload;
	private byte[] mac;
	private int nonceId;
	private JWaveNodeCommand nodeCmd;
	
	public JWaveSecurityMessageEncapsulation(JWaveCommand cmd, byte[] initializationVector, byte[] encryptedPayload, byte nonceId, byte[] mac) {
		this(cmd,null,initializationVector,encryptedPayload,nonceId,mac);
	}
		
	
	public JWaveSecurityMessageEncapsulation(JWaveCommand cmd, JWaveNodeCommand nodeCmd, byte[] initializationVector, byte[] encryptedPayload, byte nonceId, byte[] mac) {
		super(cmd);
		this.nodeCmd = nodeCmd;
		this.initializationVector = Arrays.copyOf(initializationVector,8);		
		this.encryptedPayload = encryptedPayload;
		this.mac = mac;
		this.nonceId = (nonceId & 0xFF);
		printDetails();
		
		
	}

	public void printDetails(){
		JWaveController.log(LogTag.DEBUG,"###########################  MESSAGE ENCAPSULATION ###########################");
		JWaveController.log(LogTag.DEBUG,"");
		if (nodeCmd != null){
			JWaveController.log(LogTag.DEBUG,"             command "+nodeCmd.getCommandClass().getName()+"   "+nodeCmd.getCommand().getName());
		}
		JWaveController.log(LogTag.DEBUG,"InitializationVector "+ByteArrayGeneration.toHexString(initializationVector));
		JWaveController.log(LogTag.DEBUG,"    encryptedPayload "+ByteArrayGeneration.toHexString(encryptedPayload));
		JWaveController.log(LogTag.DEBUG,"                 mac "+ByteArrayGeneration.toHexString(mac));
		JWaveController.log(LogTag.DEBUG,"            nonce Id "+Integer.toHexString(nonceId));
		JWaveController.log(LogTag.DEBUG,"  all (toBytesArray) "+ByteArrayGeneration.toHexString(toByteArray()));
		JWaveController.log(LogTag.DEBUG,"");
		JWaveController.log(LogTag.DEBUG,"-----------------------------------------------------------------------------");
	}
	
	@Override
	public byte[] toByteArray(){
		byte[] bytes = new byte[2+initializationVector.length+encryptedPayload.length+1+mac.length];
		
		
		bytes[0] = (byte)cmd.getCommandClass().getKey();
		bytes[1] = (byte)cmd.getKey();
				
		System.arraycopy(initializationVector, 0, bytes, 2, initializationVector.length);
		System.arraycopy(encryptedPayload, 0, bytes, 2 + initializationVector.length, encryptedPayload.length);
		
		bytes[initializationVector.length + encryptedPayload.length + 2] = (byte)nonceId;
		
		System.arraycopy(mac,0, bytes, initializationVector.length + encryptedPayload.length + 2 + 1, mac.length);
		
		return bytes;
	}
	
	public byte[] getInitializationVector(){
		return initializationVector;
	}
	
	public JWaveNodeCommand getNodeCmd(){
		return nodeCmd;
	}
	
	public byte[] getMAC(){
		return mac;
	}
	
	public byte[] getEncryptedPayload(){
		return encryptedPayload;
	}
	
	public int getNonceId(){
		return nonceId;
	}
}
