package de.smahoo.jwave.io;

public interface JWaveDatagramEventListener {
	public void onDatagramReceived(JWaveDatagram datagram);
	public void onDatagramTransmitted(JWaveDatagram datagram);
}
