package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommandParameterType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.security.JWaveSecurityScheme;

public class JWaveReportSecurityScheme extends JWaveReport {

	private JWaveSecurityScheme scheme; 
	
	public JWaveReportSecurityScheme(JWaveNodeCommand cmd) {
		super(cmd);		
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		int supportedScheme = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(0));
		switch (supportedScheme){
			case 0: 
				scheme = JWaveSecurityScheme.SECURITY_SCHEME_NETWORK_KEY;
			default:
				
		}
		
	}
	
	public JWaveSecurityScheme getSecurityScheme(){
		return scheme;
	}


}
