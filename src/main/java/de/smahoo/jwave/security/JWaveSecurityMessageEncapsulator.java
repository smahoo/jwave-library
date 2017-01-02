package de.smahoo.jwave.security;



import java.util.Arrays;

import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveSecurityMessageEncapsulator {
	
	private final static int MAX_PAYLOAD_SIZE = 28;
	
	private JWaveNodeCommand nodeCmd;
	private JWaveSecurityNetworkKey networkKey;
	
	
	public JWaveSecurityMessageEncapsulator(JWaveNodeCommand nodeCmd , JWaveSecurityNetworkKey networkKey){
		this.nodeCmd = nodeCmd;
		this.networkKey = networkKey;
	}
   
	public JWaveNodeCommand getNodeCmd(){
		return nodeCmd;
	}
	
	public JWaveSecurityNetworkKey getNetworkKey(){
		return networkKey;
	}
	
	
	
	public boolean isSecondFrameNeeded(){
		return (nodeCmd.toByteArray().length) > MAX_PAYLOAD_SIZE;
	}
	
	public byte[] getFirstPayloadPart(){
		
		int numBytes = Math.min(MAX_PAYLOAD_SIZE, nodeCmd.toByteArray().length);
		byte[] bytes = new byte[ numBytes+1]; // plus 1 because second Frame information need be added afterwards
		if (isSecondFrameNeeded()){
			// TODO: handle the shit!
			
		} else {
			bytes[0] = 0;
		}
		
		System.arraycopy(nodeCmd.toByteArray(),0,bytes,1,numBytes);  
		
		return bytes;
	}
	
	public byte[] getSecondPayloadPart(){
		if (isSecondFrameNeeded()){
			return Arrays.copyOfRange(nodeCmd.toByteArray(), MAX_PAYLOAD_SIZE, nodeCmd.toByteArray().length - MAX_PAYLOAD_SIZE);
		} 
		throw new IllegalStateException("Message of command "+nodeCmd.getCommand().getName()+" does not need two frames for sending.");
	}
	
}
