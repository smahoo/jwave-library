package de.smahoo.jwave.test;

import static org.junit.Assert.*;

import org.junit.Test;

import de.smahoo.jwave.cmd.JWaveCommandType;
import de.smahoo.jwave.io.JWaveDatagram;
import de.smahoo.jwave.io.JWaveDatagramFactory;
import de.smahoo.jwave.utils.ByteArrayGeneration;

/**
 * This class checks the datagram generation. Datagrams are byte array frames for sending information. Each datagram has a JWaveCommandType. 
 * The command type CMD_APPLICATION_HANDLER and CMD_SEND_DATA will be tested in detail in 
 * TestJWaveReportGeneration (received messages from nodes -> CMD_APPLICATION_HANDLER) and 
 * TestJWaveNodeCmdGeneration (messages to nodes -> CMD_SEND_DATA)
 * 
 * @author Mathias Runge (runge@domoone.de)
 *
 */

public class TestJWaveDatagramGeneration {

	//// ++++++++++++++ Generating datagrams from stream ++++++++++++++++++++++++++++
	
	@Test
	public void testLRCCalculation(){
		int[] intArray = ByteArrayGeneration.generateIntArray("01 0a 00 04 00 04 04 86 14 84 02 00");		
		assertEquals(JWaveDatagramFactory.calculateLRC(intArray, 1, 11), 0xe5);		
		
		
		byte[] array = ByteArrayGeneration.generateByteArray("01 0a 00 04 00 04 04 86 14 84 02 00");
		assertEquals(JWaveDatagramFactory.calculateLRC(array, 1, 11),(byte)0xe5);		
	}
	
	/**
	 * Generating command handler datagram from byte array. 
	 */
	@Test
	public void testGenerateDatagram() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateDatagram(ByteArrayGeneration.generateByteArray("01 0a 00 04 00 04 04 86 14 84 02 e5"));		
		assertNotNull(datagram);	
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 0a 00 04 00 04 04 86 14 84 02 e5");		                  
		assertEquals( JWaveCommandType.CMD_APPL_COMMAND_HANDLER,datagram.getCommandType());		
	}	
	
	
	////++++++++++++++ Generating datagrams  ++++++++++++++++++++++++++++
	
	@Test
	public void testGenerateDatagram_HomeId() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateGetHomeIdRequest();		
		assertNotNull(datagram);
		assertEquals( JWaveCommandType.CMD_MEMORY_GET_ID,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 03 00 20 dc");
	}
	
	
	@Test
	public void testGenerateDatagram_GetNodeProtocolInfoRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateGetNodeProtocolInfoRequest(03);		
		assertNotNull(datagram);		
		assertEquals(JWaveCommandType.CMD_JWave_GET_NODE_PROTOCOL_INFO,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 04 00 41 03 b9");
	}
	
	@Test
	public void testGenerateDatagram_GetSerialApiCapabilitiesRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateGetSerialApiCapabilitiesRequest();
		assertNotNull(datagram);				
		assertEquals(JWaveCommandType.CMD_SERIAL_GET_CAPABILITIES,datagram.getCommandType());
		String byteStr = datagram.toHexString();	
		assertEquals(byteStr,"01 03 00 07 fb");
	}
	
	
	@Test
	public void testGenerateDatagram_GetVersionRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateResetControllerCmd();
		assertNotNull(datagram);	
		assertEquals(JWaveCommandType.CMD_JWave_SET_DEFAULT,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 04 01 42 02 ba");
	}
	
	@Test
	public void testGenerateDatagram_SerialInitDataRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateSerialInitDataRequest();
		assertNotNull(datagram);		
		assertEquals(JWaveCommandType.CMD_SERIAL_GET_INIT_DATA,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 03 00 02 fe");
	}
	
	@Test
	public void testGenerateDatagram_AddNodeToNetworkRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateAddNodeToNetworkRequest((byte)0x81, (byte)02);
		assertNotNull(datagram);		
		assertEquals(JWaveCommandType.CMD_JWave_ADD_NODE_TO_NETWORK,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 05 00 4a 81 02 33");
	}
		
	@Test
	public void testGenerateDatagram_RemoveNodeFromNetworkCompletedMessage() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateRemoveNodeFromNetworkCompletedMessage();
		assertNotNull(datagram);		
		assertEquals(JWaveCommandType.CMD_JWave_REMOVE_NODE_FROM_NETWORK,datagram.getCommandType());
		String byteStr = datagram.toHexString();
		assertEquals(byteStr,"01 04 01 4b 05 b4");
	}	
	
	@Test
	public void testGenerateDatagram_RemoveNodeFromNetworkRequest() {
		
		JWaveDatagram datagram = JWaveDatagramFactory.generateRemoveNodeFromNetworkRequest((byte)0x01, (byte)02);
		assertNotNull(datagram);			
		assertEquals(JWaveCommandType.CMD_JWave_REMOVE_NODE_FROM_NETWORK,datagram.getCommandType());
		String byteStr = datagram.toHexString();		
		assertEquals(byteStr,"01 05 00 4b 01 02 b2");
	}
	
	@Test 
	public void testGenerateDatagram_sendNodeInfo(){
		JWaveDatagram datagram = JWaveDatagramFactory.generateSendNodeInfo(0x02);
		assertNotNull(datagram);
		assertEquals(JWaveCommandType.CMD_JWave_SEND_NODE_INFORMATION,datagram.getCommandType());		
		assertEquals(datagram.toHexString(),"01 04 00 12 02 eb");
	}
	
	@Test
	public void testGenerateDatagram_stupidTestToReachFullCoverage(){
		// FIXME: No method known to create static class. All members of JWaveDatagramFactory are static, but there exists
		//        no way to make the class static too. Thus, create of an instance is needed to reach 100% test coverage 
		JWaveDatagramFactory factory = new JWaveDatagramFactory();
		assertNotNull(factory);
	}
	
//	@Test
//	public void testGenerateDatagram_GetVersionRequest() {
//		
//		JWaveDatagram datagram = JWaveDatagramFactory.generateResetControllerCmd();
//		assertNotNull(datagram);		
//		System.out.println(datagram.getCommandType().name());
//		assertEquals(JWaveCommandType.CMD_JWave_GET_NODE_PROTOCOL_INFO,datagram.getCommandType());
//		String byteStr = datagram.toHexString();		
//		System.out.println(byteStr);
//		assertEquals(byteStr,"01 04 00 41 03 b9");
//	}
	
	
}

