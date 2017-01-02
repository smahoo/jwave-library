package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.security.JWaveSecurityNonce;

public class JWaveReportSecurityNonce extends JWaveReport{

	JWaveSecurityNonce nonce;
	
	
	public JWaveReportSecurityNonce(JWaveNodeCommand cmd) {
		super(cmd);		
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		nonce = new JWaveSecurityNonce(nodeCmd.getParamValues());		
	}

	public JWaveSecurityNonce getNonce(){
		return nonce;
	}
	
}
