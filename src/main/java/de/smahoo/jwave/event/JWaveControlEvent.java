package de.smahoo.jwave.event;

import de.smahoo.jwave.JWaveController;

public class JWaveControlEvent extends JWaveEvent {
	

	private JWaveController cntrl;
	
	
	
	public JWaveControlEvent(JWaveEventType type, JWaveController cntrl){
		super(type);
		
		this.cntrl = cntrl;
	}	
		
	public JWaveController getCntrl(){
		return this.cntrl;
	}
	
}
