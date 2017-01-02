package de.smahoo.jwave.simulation;

public class JWaveStreamNotificationRule extends JWaveAbstractStreamRule{
	
	protected JWaveStreamNotificationListener listener = null;
	
	public JWaveStreamNotificationRule(String conditionStr, JWaveStreamNotificationListener listener){
		super(conditionStr);
		this.listener = listener;
	}
	
	protected void informListener(String message){
		if (listener != null){
			listener.onStreamTransmitted(message);
		}
	}
	
}
