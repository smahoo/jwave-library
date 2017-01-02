package de.smahoo.jwave.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveControllerMode;
import de.smahoo.jwave.event.JWaveEvent;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.simulation.JWaveStreamReplyRule;

public class TestJWaveNodeSecurityCommunication extends TestJWaveAbstractConnection{
	
	protected int nodeId;
	protected int battery;
	protected int sleepingInterval;
	protected int setPoint;
	protected String fileSeperator;
	
	
	
	@Before
	public void init() throws Exception{
		super.init();
		fileSeperator = System.getProperty("file.seperator");
	}
	
	@After
	public void cleanUp() throws IOException{
		File f = new File(getTestFilePath());
		if (f.exists()){
			assertTrue(f.delete());
		}
	}
	
	
	@Override
	protected void prepare(){
		nodeId = 2;
		battery = 74;
		sleepingInterval = 240;
		setPoint = 22;
		addDeviceSpecificReplyRules();
		addDeviceSpecificNotificationRules();
	}	
	
	@Override
	protected void evalJWaveEvent(JWaveEvent event){
		if (event.getEventType()==JWaveEventType.CNTRL_EVENT_ADD_NODE_TO_NETWORK_START){
			connection.send("01 1a 00 4a 04 03 "+getByteStr(nodeId)+" 13 04 10 01 5e 86 72 98 5a 85 59 73 25 20 27 32 70 71 75 7a #LRC");
			connection.send("01 07 00 4a 04 05 "+getByteStr(nodeId)+" 00 #LRC");
		}
	}
	
	protected void addDeviceSpecificReplyRules(){
		
		
		/*
		 * 2015-52-03 10:08:15 | libJWave 0.3.74 | DEBUG | Sending Data: 01 05 00 4a 81 04 35 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:15 | libJWave 0.3.74 | INFO | Setting controller mode timeout to 300000ms
2015-52-03 10:08:15 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:15 | libJWave 0.3.74 | DEBUG |       ... Received 01 07 00 4a 04 01 00 00 b7 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:15 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:17 | libJWave 0.3.74 | DEBUG |       ... Received 01 07 00 4a 04 02 00 00 b4 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:17 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:17 | libJWave 0.3.74 | DEBUG |       ... Received 01 1a 00 4a 04 03 03 13 04 10 01 5e 86 72 98 5a 85 59 73 25 20 27 32 70 71 75 7a 74 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:17 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:17 | libJWave 0.3.74 | DEBUG | setting Command Classes to Z-Wave node with id 3
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received 01 07 00 4a 04 05 03 00 b0 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:18 | libJWave 0.3.74 | INFO | Stopping the timeout timer
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | Sending Data: 01 05 00 4a 05 05 b0 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_SECURITY SECURITY_SCHEME_GET unsecure
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received 01 07 00 4a 05 06 03 00 b2 | CMD_JWave_ADD_NODE_TO_NETWORK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 98 04 00 00 03 00 78 | CMD_JWave_SEND_DATA
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:18 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_MANUFACTURER_SPECIFIC MANUFACTURER_SPECIFIC_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:28 | libJWave 0.3.74 | DEBUG | sending cmd COMMAND_CLASS_VERSION VERSION_COMMAND_CLASS_GET unsecure
2015-52-03 10:08:33 | libJWave 0.3.74 | WARN | NODE 3 | Cancel waiting for response SECURITY_SCHEME_GET
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0a 00 13 03 02 72 04 00 03 00 92 | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 0e 00 04 00 03 08 72 05 01 08 00 01 00 11 90 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0a 00 13 03 02 86 11 00 03 00 73 | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 11 00 04 00 03 0b 86 12 03 03 53 01 0a 01 00 01 0a 24 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 5e 00 03 00 2f | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:33 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 5e 02 38 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_JWavePLUS_INFO v1)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_JWavePLUS_INFO v2)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 86 00 03 00 f7 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 86 02 e0 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_VERSION v1)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_VERSION v2)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 72 00 03 00 03 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 72 02 14 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_MANUFACTURER_SPECIFIC v1)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_MANUFACTURER_SPECIFIC v2)
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 98 00 03 00 e9 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 98 01 fd | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 5a 00 03 00 2b | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:34 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 5a 01 3f | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 85 00 03 00 f4 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 85 02 e3 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_ASSOCIATION v1)
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_ASSOCIATION v2)
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 59 00 03 00 28 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 59 01 3c | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 73 00 03 00 02 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 73 01 16 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:35 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 25 00 03 00 54 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 25 01 40 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 20 00 03 00 51 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 20 01 45 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 27 00 03 00 56 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 27 01 42 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 32 00 03 00 43 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 32 03 55 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_METER v1)
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_METER v3)
2015-52-03 10:08:36 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 70 00 03 00 01 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 70 01 15 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 71 00 03 00 00 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 71 01 14 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 75 00 03 00 04 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 75 02 13 | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_PROTECTION v1)
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_PROTECTION v2)
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | Sending Data: 01 0b 00 13 03 03 86 13 7a 00 03 00 0b | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 04 01 13 01 e8 | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG |       ... Received 01 05 00 13 03 00 ea | CMD_JWave_SEND_DATA
2015-52-03 10:08:37 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:38 | libJWave 0.3.74 | DEBUG |       ... Received 01 0a 00 04 00 03 04 86 14 7a 02 1c | CMD_APPL_COMMAND_HANDLER
2015-52-03 10:08:38 | libJWave 0.3.74 | DEBUG | send ACK
2015-52-03 10:08:38 | libJWave 0.3.74 | DEBUG | Removing oldClass (COMMAND_CLASS_FIRMWARE_UPDATE_MD v1)
2015-52-03 10:08:38 | libJWave 0.3.74 | DEBUG | Adding cmdClass (COMMAND_CLASS_FIRMWARE_UPDATE_MD v2)
		 */
		
		
		// Device Danfoss Thermostat
		// command class version
		
//		JWaveStreamReplyRule rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 72 04 00 ## 00 ##");  
//		rule.addReaction("01 0e 00 04 00 "+getByteStr(nodeId)+" 08 72 05 00 02 00 05 00 03 #LRC");			
//		connection.addStreamRule(rule);
//			
//		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 86 11 00 ## 00 ##");  // VERSION_GET
//		rule.addReaction("01 0d 00 04 00 "+getByteStr(nodeId)+" 07 86 12 06 02 43 02 33 #LRC");			
//		connection.addStreamRule(rule);
//			
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 80 00 ## 00 ##");  // CMD CLASS VERSION_GET - BATTERY
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 80 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - BATTERY version 1
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 46 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 46 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE version 1
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 72 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_MANUFACTURER_SPECIFIC		
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 72 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_MANUFACTURER_SPECIFIC version 1
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 8f 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_MULTI_CMD
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 8f 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_MULTI_CMD version 1
//		connection.addStreamRule(rule);
//			
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 81 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_CLOCK
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 81 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_CLOCK version 1
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 75 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_PROTECTION
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 75 02 #LRC");			  					   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_PROTECTION version 2
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 43 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_THERMOSTAT_SETPOINT
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 43 02 #LRC");			 				       // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_THERMOSTAT_SETPOINT version 2
//		connection.addStreamRule(rule);
//				
//		rule = new JWaveStreamReplyRule("01 0b 00 13 02 03 86 13 84 00 02 00 f5");  // VERSION_GET - COMMAND_CLASS_WAKE_UP
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 84 02 #LRC");			   //  VERSION_REPORT - COMMAND_CLASS_WAKE_UP version 2
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 86 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_VERSION
//		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 86 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_VERSION version 1
//		connection.addStreamRule(rule);
//					
//		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 80 02 00 ## 00 ##");     // BATTERY_GET		
//		rule.addReaction("01 09 00 04 00 "+getByteStr(nodeId)+" 03 80 03 "+getByteStr(battery)+" #LRC");			       // BATTERY_REPORT -
//		
//		connection.addStreamRule(rule);
//		
//		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 84 05 00 ## 00 ##");     // WAKE_UP_INTERVAL_GET
//		rule.addReaction("01 0c 00 04 00 "+getByteStr(nodeId)+" 06 84 06 00 00 f0 01 #LRC");		   // WAKE_UP_INTERVAL_REPORT  
//		connection.addStreamRule(rule);
//				
//		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 43 02 01 00 ## 00 ##");  // THERMOSTAT_SETPOINT_GET
//		rule.addReaction("01 0c 00 04 00 "+getByteStr(nodeId)+" 06 43 03 01 42 08 34 #LRC");		   // THERMOSTAT_SETPOINT_REPORT
//		connection.addStreamRule(rule);
		
				
	}
	
	protected void addDeviceSpecificNotificationRules(){
		
		
	}
	
	
	@Test
	public void test_SecurityInclusionTimeout() throws Exception{		
		assertNotNull(cntrl);		
		cntrl.init(connection);
		JWaveNode node = cntrl.getNode(nodeId);		
		assertNull(node);
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,10000));
		cntrl.setInclusionMode(true,true);
		assertTrue(waitForJWaveEventType(JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_START,10000));
		assertTrue(waitForJWaveEventType(JWaveEventType.CNTRL_EVENT_SECURITY_INCLUDE_FAILED,20000));
			
	}
	
////+++++++++++++++++++++ Helper +++++++++++++++++++++++++
	
	protected String getTestFilePath(){		
		return System.getProperty("user.dir")+fileSeperator+"test_tmp";
	}
	
	protected String getConfigFilePath(){		
		return getTestFilePath()+fileSeperator+"config.xml";
	}
}
