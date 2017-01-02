package de.smahoo.jwave.event;

import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.node.JWaveNode;

public class JWaveNodeDataEvent extends JWaveNodeEvent {

	public JWaveNodeCommand cmd = null;
	
	public JWaveNodeDataEvent(JWaveEventType type, JWaveNode node, JWaveNodeCommand cmd){
		super(type,node);
		this.cmd = cmd;
	}
	
	public JWaveNodeCommand getNodeCmd(){
		return cmd;
	}
	
}
