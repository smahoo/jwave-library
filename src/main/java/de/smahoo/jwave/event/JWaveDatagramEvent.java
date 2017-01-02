package de.smahoo.jwave.event;

import de.smahoo.jwave.io.JWaveDatagram;

public class JWaveDatagramEvent extends JWaveEvent{
	private JWaveDatagram datagram = null;
	
	public JWaveDatagramEvent(JWaveEventType type, JWaveDatagram datagram){
		super(type);
		this.datagram = datagram;
	}
	
	public JWaveDatagram getDatagram(){
		return datagram;
	}
	
}
