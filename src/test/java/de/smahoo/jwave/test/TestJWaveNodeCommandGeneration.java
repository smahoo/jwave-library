package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import de.smahoo.jwave.cmd.*;
import org.junit.Before;
import org.junit.Test;

import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.node.JWaveNode;
import de.smahoo.jwave.node.JWaveNodeFactory;
import de.smahoo.jwave.security.JWaveSecurityMessageEncapsulation;
import de.smahoo.jwave.utils.ByteArrayGeneration;



/**
 * Information which will be send to nodes within the z-wave network have the JWaveCommandType CMD_SEND_DATA and will be encapsulated with JWaveNodeCommand.
 * Thus, the complete communication between controller and nodes will be done with JWaveNodeCommand. All node cmds from the controller to the node will be
 * tested in TestJWavenNodeCmd, all responses (from node to controller) in TestJWaveReportGeneration.   
 * @author Mathias Runge
 *
 */
public class TestJWaveNodeCommandGeneration {

	JWaveNodeCommandFactory factory;
	JWaveNodeFactory  nodeFactory;
	
	
	@Before
	public void init(){		
		String sep = System.getProperty("file.separator");
		String filename = System.getProperty("user.dir")+sep+"cnf"+sep+"cmd_classes.xml";
		JWaveCommandClassSpecification spec = null;
		try {
			spec = new JWaveCommandClassSpecification(filename);
		} catch (Exception exc){
			exc.printStackTrace();
			return;
		}
		factory = new JWaveNodeCommandFactory(spec);
		nodeFactory = new JWaveNodeFactory(spec);
		
	}
	
	//// ++++++++++++++++ CMD_CLASS_ALARM ++++++++++++++++
	
	@Test
	public void testnodeCmdSensorAlarm_Get(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SensorAlarm_Get(01);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0b 00 13 2a 03 9c 01 01 00 2a 00 78");
	}
	
	
	//// +++++++++++++++ CMD_CLASS_ASSOCIATION ++++++++++++++++++++++
	
	@Test
	public void testnodeCmdAssociation_Set(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_Association_Set(01, 01);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();	
		
		assertEquals(byteStr,"01 0c 00 13 2a 04 85 01 01 01 00 2a 00 60");
	}
	
	
	@Test
	public void testnodeCmdAssociation_Get(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_Association_Get(01);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0b 00 13 2a 03 85 01 01 00 2a 00 61");
	}
	
	
	
	// TODO:
	// Association_Groupings_get
	// Association_remove
	// Association_Specific_Group_get
	
	// ++++++++++++   CMD_CLASS_SWITCH_BINARY +++++++++++++++++++++++
	
	@Test
	public void testNodeCmdSwitchBinary_Set_on(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchBinary_Set(0);		
		
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0b 00 13 2a 03 25 01 00 00 2a 00 c0");
		
	}
	
	@Test
	public void testNodeCmdSwitchBinary_Set_off(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchBinary_Set(0xFF);		
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0b 00 13 2a 03 25 01 ff 00 2a 00 3f");
		
	}
	
	@Test
	public void testnodeCmdSwitchBinary_Get(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchBinary_Get();		
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();	
		assertEquals(byteStr,"01 0a 00 13 2a 02 25 02 00 2a 00 c3");
	}
	
	
	// ++++++++++++   CMD_CLASS_SWITCH_MULTILEVEL +++++++++++++++++++++++
	
	@Test
	public void testNodeCmdSwitchMultilevel_Set_on(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchMultilevel_Set(0xFF);
		
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0b 00 13 2a 03 26 01 ff 00 2a 00 3c");
		
	}
	
	@Test
	public void testNodeCmdSwitchMultilevel_Set_off(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchMultilevel_Set(0x00);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0b 00 13 2a 03 26 01 00 00 2a 00 c3");
		
	}
	
	@Test
	public void testNodeCmdSwitchMultilevel_Set_50(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchMultilevel_Set(0x32);		
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0b 00 13 2a 03 26 01 32 00 2a 00 f1");
		
	}
	
	@Test
	public void testnodeCmdSwitchMultilevel_Get(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_SwitchMultilevel_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0a 00 13 2a 02 26 02 00 2a 00 c0");		
	}
	
	/// ++++++++++  CMC_CLASS_BATTERY +++++++++++++++++++++++++++
	
	@Test
	public void testNodeCmdBattery_Get(){
		assertNotNull(factory);
		JWaveNodeCommand nodeCmd = factory.generateCmd_Battery_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0a 00 13 2a 02 80 02 00 2a 00 66");	
	}
	
	/// +++++++++ CMD_CLASS_CLOCK
	
	@Test
	public void testNodeCmdClock_Set(){
		assertNotNull(factory);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = df.parse("2014-08-08 12:00:00");
		} catch (Exception exc){
			exc.printStackTrace();
		}		
		assertNotNull(date);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_Clock_Set(date);
		JWaveDatagram datagram = generateDatagram(nodeCmd);		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 0c 00 13 2a 04 81 04 ac 00 00 2a 00 cd");	
	}
	
	@Test
	public void testNodeCmdClock_Get(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_Clock_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		
		assertEquals(byteStr,"01 0a 00 13 2a 02 81 05 00 2a 00 60");	
	}
	
	////++++++++++++++++++++ CMD_CLASS_COLORCONTROL ++++++++++++++++ 
	
	@Test
	public void testNodeCmdColorControl_State_Get(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_ColorControl_State_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0a 00 13 2a 02 33 03 00 2a 00 d4");	
	}
	
	@Test
	public void testNodeCmdColorControl_State_Set(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_ColorControl_State_Set();
		// There exists no abstracted way of transmitting color 
		// -> following example is related to how Fibaro is transferring the color information
		int red = 100;
		int blue = 20;
		int green = 255;
		try {
			nodeCmd.setParamValue(0, 3);
			byte[] bytes = new byte[6];
			bytes[0] = 2; // capability id red
			bytes[1] = (byte)(255 * red/100);
			bytes[2] = 3; // capability id green
			bytes[3] = (byte)(255 * green/100);
			bytes[4] = 4; // capability id blue
			bytes[5] = (byte)(255 * blue/100);
			nodeCmd.setParamValue(1, bytes);			
		} catch (Exception exc){
			// FIXME
			exc.printStackTrace();
		}		
		
		JWaveDatagram datagram = generateDatagram(nodeCmd);		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 11 00 13 2a 09 33 05 03 02 ff 03 8a 04 33 00 2a 00 82");	
	}
	
	// TODO:
	// colorControl_Capability_Get	
	// colorcontrol_Start_Capability_level_change
	// colorcontrol_stop_state_change
	
	//// ++++++++++++++++++++ CMD_CLASS_CONFIGURATION ++++++++++++++++ 
	
	
	@Test
	public void testNodeCmdConfiguration_Set(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_Configuration_Set(02, JWaveCommandParameterType.BYTE, 13);
		JWaveDatagram datagram = generateDatagram(nodeCmd);		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();				
	
		assertEquals(byteStr,"01 0d 00 13 2a 05 70 04 02 01 0d 00 2a 00 9e");	
	}
	
	@Test
	public void testNodeCmdConfiguration_Get(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_Configuration_Get(02);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();	
		
		assertEquals(byteStr,"01 0b 00 13 2a 03 70 05 02 00 2a 00 93");	
	}
	
	//// ++++++++++++++++ CMD_CLASS_WAKEUP ++++++++++++++++ 
	
	@Test
	public void testNodeCmdWakeupInterval_Get(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_WakUpInterval_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();	
		assertEquals(byteStr,"01 0a 00 13 2a 02 84 05 00 2a 00 65");	
	}
	
	@Test
	public void testNodeCmdWakeupInterval_Set(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_WakeUpInterval_Set(240, 05);
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();	
		assertEquals(byteStr,"01 0e 00 13 2a 06 84 04 00 00 f0 05 00 2a 00 91");	
	}
	
	@Test
	public void testNodeCmdWakeupInterval_NoMoreInformation(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_WakeUpInterval_NoMoreInformation();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		
		assertEquals(byteStr,"01 0a 00 13 2a 02 84 08 00 2a 00 68");	
	}
	
	// +++++++++ CMD_CLASS_THERMOSTAT_SETPOINT +++++++++++++++++++
	
	@Test
	public void testNodeCmdThermostatSetpoint_get(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_Thermostat_Setpoint_Get(1); 
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0b 00 13 2a 03 43 02 01 00 2a 00 a4");	
	}
	
	@Test
	public void testNodeCmdThermostatSetpoint_set(){
		assertNotNull(factory);
	
		JWaveNodeCommand nodeCmd = factory.generateCmd_Thermostat_Setpoint_Set(01, 21.5); 
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();		
		
		assertEquals(byteStr,"01 0e 00 13 2a 06 43 01 01 42 08 66 00 2a 00 8b");	
	}
	
	// ++++++++ CMD_CLASS_SENSOR_MULTILEVEL ++++++++++++++++++++++++
	
	@Test
	public void testNodeCmdSensorMultilevel_Get(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_SensorMultilevel_Get();
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
	
		assertEquals(byteStr,"01 0a 00 13 2a 02 31 04 00 2a 00 d1");
	}
	
	@Test
	public void testNodeCmdSensorMultilevel_Get_V1(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_SensorMultilevel_Get(2); // expected 2 responses
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
	
		assertEquals(byteStr,"01 0a 00 13 2a 02 31 04 00 2a 00 d1");
		assertEquals(nodeCmd.getExpectedResponses(),2);
	}
	
	@Test
	public void testNodeCmdSensorMultilevel_Get_V5(){
		assertNotNull(factory);
		
		JWaveNodeCommand nodeCmd = factory.generateCmd_SensorMultilevel_Get_V5(0x03); // 0x03 = Power Consumption
		JWaveDatagram datagram = generateDatagram(nodeCmd);
		
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		
		assertEquals(byteStr,"01 0c 00 13 2a 04 31 04 03 00 00 2a 00 d2");
	}
	
	@Test
	public void testMultiNodeCommand_Receive(){
		assertNotNull(factory);
		
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 22 00 04 00 03 1c 8f 01 06 03 80 03 64 06 43 03 01 42 08 98 04 46 08 00 7f 02 81 05 02 46 04 02 84 07 4d"));
		assertNotNull(datagram);
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		
		assertTrue(nodeCmd instanceof JWaveMultiNodeCommand);
		JWaveMultiNodeCommand multiCmd = (JWaveMultiNodeCommand)nodeCmd;
		assertEquals(multiCmd.getNodeCmdList().size(),6);		
				
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x43,(byte)3));
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x46,(byte)8));
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x46,(byte)4));
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x81,(byte)5));
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x80,(byte)3));
		assertTrue(multiCmdContainsCommand(multiCmd,(byte)0x84,(byte)7));
		
	}
	

	@Test
	public void testMultiNodeCommand_Receive_2(){
		assertNotNull(factory);
		
	
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(ByteArrayGeneration.generateByteArray("8f 01 04 03 80 03 64 09 71 05 00 00 00 ff 06 16 00 05 31 05 03 01 61 06 31 05 01 0a 00 53"));
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assertNotNull(nodeCmd);
		
		assertTrue(nodeCmd instanceof JWaveMultiNodeCommand);
		JWaveMultiNodeCommand multiCmd = (JWaveMultiNodeCommand)nodeCmd;
		assertEquals(multiCmd.getNodeCmdList().size(),4);		
				
		
		
	}
	
	
	@Test
	public void testMultiNodeCommand_Send(){
		assertNotNull(factory);
		Collection<JWaveNodeCommand> nodeCmds = new ArrayList<JWaveNodeCommand>();
		nodeCmds.add(factory.generateCmd_WakeUpInterval_Set(240, 05));
		nodeCmds.add(factory.generateCmd_Association_Set(1, 1));
		nodeCmds.add(factory.generateCmd_SensorBinary_Get());
		JWaveMultiNodeCommand multiCmd = factory.generateMultiNodeCmd(nodeCmds);
		assertEquals(multiCmd.getNodeCmdList().size(), 3);
		JWaveDatagram datagram = generateDatagram(multiCmd);
		assertNotNull(datagram);
		String byteStr = datagram.toHexString();
		assertEquals(byteStr, "01 1a 00 13 2a 12 8f 01 03 06 84 04 00 00 f0 05 04 85 01 01 01 02 30 02 00 2a 00 aa");
	}
	
	@Test
	public void testNodeCmdGenerationFromSendDataDatagram(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0e 00 13 03 06 43 01 01 42 08 fc 00 03 00 11"));
		assertNotNull(datagram);
		
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		nodeCmd.setParamSize(2, 2);
		double temperature = JWaveCommandParameterType.toInteger(nodeCmd.getParamValue(2)) / 100.0;
		assertEquals(temperature,23.0,0.0);
	}
	
	
	//++++++++++++++++ CMD_CLASS_SECURITY ++++++++++++++++
	@Test
	public void testNodeCmdGenerationFromSendDataDatagram_Security(){
		assertNotNull(factory);
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 1c 00 04 00 04 16 98 81 16 55 10 f9 7c 22 f4 86 62 57 0c 57 3d 88 1c 06 41 aa 42 27 25"));
		assertNotNull(datagram);
	
		JWaveNodeCommand nodeCmd = null;
		try {
			nodeCmd = factory.generateNodeCmd(datagram);
		} catch (Exception exc){
			exc.printStackTrace();
		}
		
		assert nodeCmd != null;
		assert nodeCmd instanceof JWaveSecurityMessageEncapsulation;
		
		JWaveSecurityMessageEncapsulation secMsg = (JWaveSecurityMessageEncapsulation)nodeCmd;
		assert "16 55 10 f9 7c 22 f4 86".equalsIgnoreCase(ByteArrayGeneration.toHexString(secMsg.getInitializationVector()));
		//assert "3d 88 1c 06 41 aa 42 27".equalsIgnoreCase(ByteArrayGeneration.toHexString(secMsg.getInitializationVector()));
	//	assert "62 57 0c".equalsIgnoreCase(ByteArrayGeneration.toHexString(secMsg.getInitializationVector()));
		assert (secMsg.getNonceId() & 0xFF) == 0x57;
		
	}
	
	// ++++++++++++  Tests for JWaveCommandParam
	
	
	

	
	//// HELPERS
	
	protected JWaveDatagram generateDatagram(JWaveNodeCommand nodeCmd){
		JWaveNode tmpNode = nodeFactory.createNode(0x2a);  // do not change this id!
														   // 5th position on byte stream is node id, every test will fail	
	
		return JWaveDatagramFactory.generateSendDataDatagram(tmpNode,nodeCmd,(byte)(0x2a&0xFF));		
		
	}
	
	protected boolean multiCmdContainsCommand(JWaveMultiNodeCommand multiCmd, byte cmdClass, byte cmdKey){
		
		if (multiCmd == null) return false;
		if (multiCmd.getNodeCmdList() == null) return false;
		if (multiCmd.getNodeCmdList().isEmpty()) return false;
		
		for (JWaveNodeCommand nodeCmd : multiCmd.getNodeCmdList()){
			if (nodeCmd.getCommandClassKey() == (cmdClass&0xff)){
				if (nodeCmd.getCommandKey() == cmdKey)
				return true;
			}
		}
		
		return false;
	}
	
	
	
}
