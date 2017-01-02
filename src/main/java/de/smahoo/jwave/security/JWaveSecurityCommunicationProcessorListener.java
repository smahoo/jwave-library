package de.smahoo.jwave.security;

import java.util.Collection;

import de.smahoo.jwave.cmd.JWaveCommandClass;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.event.JWaveErrorEvent;


public interface JWaveSecurityCommunicationProcessorListener {
	public void onSendCommand(JWaveNodeCommand cmd);
	public void onCommandReceived(JWaveNodeCommand cmd);
	public void onSecureInclusionFinished(Collection<JWaveCommandClass> supportedCmdClasses);
	public void onSecureInclusionError(String cause);
	public void onJWaveError(JWaveErrorEvent evnt);
}
