package de.smahoo.jwave.security;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.utils.ByteArrayGeneration;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveSecurityMessageEncapsulationBuilder {
	
	
	byte[] firstMessage = null;
	byte[] secondMessage = null;
	
	public JWaveSecurityMessageEncapsulationBuilder(){
		
	}
	public JWaveSecurityMessageEncapsulationBuilder(byte[] firstData){
		addEncryptedData(firstData);
	}
	
	public void addEncryptedData(byte[] data){
		if (firstMessage == null){
			firstMessage = data;
			JWaveController.log(LogTag.DEBUG,"first message  "+ByteArrayGeneration.toHexString(data));
			return;
		}
		if (secondMessage == null){
			secondMessage = data;
			JWaveController.log(LogTag.DEBUG,"second message "+ByteArrayGeneration.toHexString(data));
			return;
		}
		
		
	}
	
	public boolean isComplete(){
		if (firstMessage == null){
			return false;
		}  
		
		if (secondMessage == null){
			return firstMessage[0] == 0x00;
		}
		return secondMessage != null;
		
	}
	
	public byte[] getCompleteCmdData(){
		byte[] cmdBytes = null;
		if (!isComplete()){
			return null;
		}
		if (secondMessage == null){
		
			cmdBytes = new byte[firstMessage.length-1];
			System.arraycopy(firstMessage, 1, cmdBytes, 0,cmdBytes.length);
		} else {
			cmdBytes = new byte[firstMessage.length-1 + secondMessage.length-1];
			System.arraycopy(firstMessage, 1, cmdBytes, 0,firstMessage.length-1);
			System.arraycopy(secondMessage, 1, cmdBytes, firstMessage.length-1, secondMessage.length -1);
			JWaveController.log(LogTag.DEBUG,"complete cmd array "+ByteArrayGeneration.toHexString(cmdBytes));
		}
    	return cmdBytes;
	}
	
}
