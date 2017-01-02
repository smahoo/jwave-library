package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.cmd.JWaveCommandType;
import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.io.JWaveDatagramTransceiver;
import de.smahoo.jwave.io.JWaveDatagramTransceiverListener;
import de.smahoo.jwave.simulation.JWaveMockedConnection;
import de.smahoo.jwave.simulation.JWaveStreamReplyRule;

public class TestJWaveDatagramTransceiver {

	JWaveDatagramTransceiver transceiver = null;
	JWaveMockedConnection serialMock 		 = null;
	
	List<JWaveDatagram> transmittedDatagrams;
	List<JWaveDatagram> failedDatagrams;
	List<JWaveDatagram> receivedDatagrams;
	
	@Before
	public void init(){
		receivedDatagrams = new ArrayList<JWaveDatagram>();
		failedDatagrams   = new ArrayList<JWaveDatagram>();
		transmittedDatagrams = new ArrayList<JWaveDatagram>();
		serialMock = new JWaveMockedConnection();
		transceiver = new JWaveDatagramTransceiver(serialMock.getInputStream(),serialMock.getOutputStream(),new JWaveDatagramTransceiverListener() {
			
			@Override
			public void onDatagramTransmitted(JWaveDatagram datagram) {
				
				
			}
			
			@Override
			public void onDatagramTransmissionFailed(int flag, JWaveDatagram datagram) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDatagramResponse(JWaveDatagram request, JWaveDatagram response) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onDatagramReceived(JWaveDatagram datagram) {
				receivedDatagrams.add(datagram);
				
			}
		});
	}
	
	
	
	@Test
	public void testDatagramTransceiver() {
		// Switch logging on (type false for no logging)
		//JWaveCntrl.doLogging(true);		
		
		// added reply rules to serial Mock
		JWaveStreamReplyRule rule = new JWaveStreamReplyRule("01 03 00 15 e9");  // CMD_JWave_GET_VERSION
		rule.addReaction("01 10 01 15 5a 2d 57 61 76 65 20 33 2e 34 32 00 01 93");			
		serialMock.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 07 fb");  // CMD_SERIAL_GET_CAPABILITIES
		rule.addReaction("01 2b 01 07 04 02 01 15 00 02 00 03 fe 00 16 80 0c 00 00 00 e3 97 7d 80 07 00 00 80 00 00 00 00 00 00 00 00 00 00 02 00 00 80 07 00 2e");			
		serialMock.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 03 00 20 dc");  // CMD_MEMORY_GET_ID (HomeId)
		rule.addReaction("01 08 01 20 d2 7f ff 78 01 fd");			
		serialMock.addStreamRule(rule);
			
		//send datagrams with transceiver		
		transceiver.send(JWaveDatagramFactory.generateGetSerialApiCapabilitiesRequest());	
		transceiver.send(JWaveDatagramFactory.generateGetVersionRequest());			
		transceiver.send(JWaveDatagramFactory.generateGetHomeIdRequest());		
			
		// check if all expected responses have been received
		try {			
			assertTrue(waitForDatagram("01 10 01 15 5a 2d 57 61 76 65 20 33 2e 34 32 00 01 93",1000));
			assertTrue(waitForDatagram("01 2b 01 07 04 02 01 15 00 02 00 03 fe 00 16 80 0c 00 00 00 e3 97 7d 80 07 00 00 80 00 00 00 00 00 00 00 00 00 00 02 00 00 80 07 00 2e",1000));
			assertTrue(waitForDatagram("01 08 01 20 d2 7f ff 78 01 fd",1000));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
	}

	
	
	
	
	protected boolean waitForDatagram(String byteStr, int milliseconds)  throws InterruptedException{
		long startMillies = new Date().getTime();
		long currentMillies = new Date().getTime();
		while ((currentMillies - startMillies) < milliseconds){
			Thread.sleep(10);
			synchronized(receivedDatagrams){
				for (JWaveDatagram datagram : this.receivedDatagrams){
					if (datagram.toHexString().equalsIgnoreCase(byteStr)){
						return true;
					}
				}
			}
			currentMillies = new Date().getTime();
		}
		return false;
	}
	
	protected void waitForDatagramType(JWaveCommandType type, int milliseconds)  throws InterruptedException{
		long startMillies = new Date().getTime();
		long currentMillies = new Date().getTime();
		while (currentMillies - startMillies < milliseconds){
			Thread.sleep(10);
			synchronized(receivedDatagrams){
				for (JWaveDatagram datagram : this.receivedDatagrams){
					if (datagram.getCommandType() == type){
						return;
					}
				}
			}
			currentMillies = new Date().getTime();
		}
			
	}
}
