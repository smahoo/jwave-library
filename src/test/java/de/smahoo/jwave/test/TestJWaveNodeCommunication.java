package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.smahoo.jwave.JWaveControllerMode;
import de.smahoo.jwave.event.JWaveEvent;
import de.smahoo.jwave.event.JWaveEventType;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.node.JWaveNodeCommunicator;
import de.smahoo.jwave.simulation.JWaveStreamReplyRule;
import de.smahoo.jwave.utils.xml.XmlUtils;

public class TestJWaveNodeCommunication extends TestJWaveAbstractConnection{

	
	protected int nodeId;
	protected int battery;
	protected int sleepingInterval;
	protected int setPoint;
	protected String fileSeperator;
	
	
	@Before
	public void init() throws Exception{
		super.init();
		fileSeperator = System.getProperty("file.separator");
	}
	
	@After
	public void cleanUp() throws IOException{
		File f = new File(getConfigFilePath());
		if (f.exists()){
			assertTrue(f.delete());
		}
		f = new File(getTestFilePath());
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
			connection.send("01 17 00 4a 03 03 "+getByteStr(nodeId)+" 10 04 08 04 80 46 81 72 8f 75 43 86 84 ef 46 81 8f #LRC");
			connection.send("01 07 00 4a 03 05 "+getByteStr(nodeId)+" 00 #LRC");
		}
	}
	
	
	protected void addDeviceSpecificReplyRules(){
			
		// Device Danfoss Thermostat
		// command class version
		
		JWaveStreamReplyRule rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 72 04 00 ## 00 ##");  
		rule.addReaction("01 0e 00 04 00 "+getByteStr(nodeId)+" 08 72 05 00 02 00 05 00 03 #LRC");			
		connection.addStreamRule(rule);
			
		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 86 11 00 ## 00 ##");  // VERSION_GET
		rule.addReaction("01 0d 00 04 00 "+getByteStr(nodeId)+" 07 86 12 06 02 43 02 33 #LRC");			
		connection.addStreamRule(rule);
			
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 80 00 ## 00 ##");  // CMD CLASS VERSION_GET - BATTERY
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 80 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - BATTERY version 1
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 46 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 46 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_CLIMATE_CONTROL_SCHEDULE version 1
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 72 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_MANUFACTURER_SPECIFIC		
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 72 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_MANUFACTURER_SPECIFIC version 1
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 8f 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_MULTI_CMD
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 8f 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_MULTI_CMD version 1
		connection.addStreamRule(rule);
			
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 81 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_CLOCK
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 81 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_CLOCK version 1
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 75 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_PROTECTION
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 75 02 #LRC");			  					   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_PROTECTION version 2
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 43 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_THERMOSTAT_SETPOINT
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 43 02 #LRC");			 				       // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_THERMOSTAT_SETPOINT version 2
		connection.addStreamRule(rule);
				
		rule = new JWaveStreamReplyRule("01 0b 00 13 02 03 86 13 84 00 02 00 f5");  // VERSION_GET - COMMAND_CLASS_WAKE_UP
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 84 02 #LRC");			   //  VERSION_REPORT - COMMAND_CLASS_WAKE_UP version 2
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 86 13 86 00 ## 00 ##");  // CMD CLASS VERSION_GET - COMMAND_CLASS_VERSION
		rule.addReaction("01 0a 00 04 00 "+getByteStr(nodeId)+" 04 86 14 86 01 #LRC");			   // CMD_CLASS_VERSION_REPORT - COMMAND_CLASS_VERSION version 1
		connection.addStreamRule(rule);
					
		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 80 02 00 ## 00 ##");     // BATTERY_GET		
		rule.addReaction("01 09 00 04 00 "+getByteStr(nodeId)+" 03 80 03 "+getByteStr(battery)+" #LRC");			       // BATTERY_REPORT -
		
		connection.addStreamRule(rule);
		
		rule = new JWaveStreamReplyRule("01 0a 00 13 "+getByteStr(nodeId)+" 02 84 05 00 ## 00 ##");     // WAKE_UP_INTERVAL_GET
		rule.addReaction("01 0c 00 04 00 "+getByteStr(nodeId)+" 06 84 06 00 00 f0 01 #LRC");		   // WAKE_UP_INTERVAL_REPORT  
		connection.addStreamRule(rule);
				
		rule = new JWaveStreamReplyRule("01 0b 00 13 "+getByteStr(nodeId)+" 03 43 02 01 00 ## 00 ##");  // THERMOSTAT_SETPOINT_GET
		rule.addReaction("01 0c 00 04 00 "+getByteStr(nodeId)+" 06 43 03 01 42 08 34 #LRC");		   // THERMOSTAT_SETPOINT_REPORT
		connection.addStreamRule(rule);
		
				
	}
	
	protected void addDeviceSpecificNotificationRules(){
		
		
	}
	
	protected void handleMessageForDevice(String message){
		
	}
	
	@Test
	public void testAddingNode() throws Exception{
		assertNotNull(cntrl);		
		cntrl.init(connection);
		JWaveNode node = cntrl.getNode(nodeId);		
		assertNull(node);
		assertTrue(waitForControllerMode(JWaveControllerMode.CNTRL_MODE_NORMAL,10000));
		cntrl.setInclusionMode(true,true);
		assertTrue(waitForJWaveEventType(JWaveEventType.NODE_EVENT_INTERVIEW_FINISHED,10000));
		node = cntrl.getNode(nodeId);
		assertNotNull(node);
		assertEquals(node.getNodeId(),nodeId);		
		
		
	}
	
	@Test
	public void testNodeCommunicator() throws Exception{
		testAddingNode();
		JWaveNode node = cntrl.getNode(nodeId);
		
		
		new JWaveNodeCommunicator(node,cntrl.getTransceiver(),cntrl.getCommandClassSpecifications(),null);
		
		assertTrue(node.isListening());
		assertFalse(node.isSleeping());
		assertFalse(node.isRouting());
		assertTrue(node.isNodeCmdBufferEmpty());
		
	}
	
	@Test
	public void testPersistency_save() throws Exception {
		testAddingNode();
		String confFilePath = getConfigFilePath();
		Document doc = XmlUtils.createDocument();
		Element elem = cntrl.getConfiguration(doc);
		assertNotNull(elem);
		cntrl.saveConfiguration(confFilePath);		
				
		cntrl.loadConfiguration(confFilePath);
	}
	
	
	//// +++++++++++++++++++++ Helper +++++++++++++++++++++++++
	
	protected String getTestFilePath(){		
		return System.getProperty("user.dir")+fileSeperator+"test_tmp";
	}
	
	protected String getConfigFilePath(){		
		return getTestFilePath()+fileSeperator+"config.xml";
	}
	
}
