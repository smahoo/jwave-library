package de.smahoo.jwave.event;

import de.smahoo.jwave.node.JWaveNode;

public class JWaveNodeEvent extends JWaveEvent{

	protected JWaveNode node = null;
	
	public JWaveNodeEvent(JWaveEventType type, JWaveNode node){
		super(type);		
		this.node = node;		
	}
	
	public JWaveNode getNode(){
		return node;
	}
	
}
