package de.smahoo.jwave.security;

import de.smahoo.jwave.cmd.JWaveCommand;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveSecurityMessageEncapsulationGet extends JWaveSecurityMessageEncapsulation {

	public JWaveSecurityMessageEncapsulationGet(JWaveCommand cmd, JWaveNodeCommand nodeCmd, byte[] initializationVector, byte[] encryptedPayload, byte nonceId, byte[] mac) {
		super(cmd, nodeCmd, initializationVector, encryptedPayload, nonceId, mac);
		// TODO Auto-generated constructor stub
	}

	public JWaveSecurityMessageEncapsulationGet(JWaveCommand cmd, byte[] initializationVector, byte[] encryptedPayload, byte nonceId, byte[] mac) {
		super(cmd, initializationVector, encryptedPayload, nonceId, mac);
		// TODO Auto-generated constructor stub
	}


	
	
}
