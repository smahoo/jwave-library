package de.smahoo.jwave.event;

public class JWaveErrorEvent extends JWaveEvent{

	private String message = null;
	private Throwable throwable = null;
	
	public JWaveErrorEvent(JWaveEventType type, String message, Throwable throwable){
		super(type);
		this.message = message;
		this.throwable = throwable;
	}
	
	public boolean hasMessage(){
		return message != null;
	}
	
	public String getMessage(){
		return message;
	}
	
	
	public boolean hasThrowable(){
		return throwable != null;
	}
	
	public Throwable getThrowable(){
		return throwable;
	}
	
	
}
