package de.smahoo.jwave.event;

import java.util.Date;


public abstract class JWaveEvent {
	protected Date timestamp = null;
	protected JWaveEventType type = null;
	
	
	public JWaveEvent(JWaveEventType type){
		this.type = type;
		timestamp = new Date();
	}
	
	public JWaveEventType getEventType(){
		return type;
	}
	
	public Date getTimestamp(){
		return timestamp;
	}
}
