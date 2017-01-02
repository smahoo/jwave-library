package de.smahoo.jwave.io;


public interface JWaveDatagramTransceiverListener {
	public void onDatagramReceived(JWaveDatagram datagram);
	public void onDatagramTransmitted(JWaveDatagram datagram);
	public void onDatagramTransmissionFailed(int flag, JWaveDatagram datagram);
	public void onDatagramResponse(JWaveDatagram request, JWaveDatagram response);
}
