package de.smahoo.jwave.io;

import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveNodeCommandDatagram extends JWaveDatagram{
	
	private JWaveNodeCommand nodeCmd = null;
	
	public JWaveNodeCommandDatagram(JWaveNodeCommand cmd){
		this.nodeCmd = cmd;
	}
	
	public JWaveNodeCommand getNodeCmd(){
		return nodeCmd;
	}
		
}
