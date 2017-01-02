package de.smahoo.jwave.io;

import de.smahoo.jwave.cmd.JWaveCommandType;
import de.smahoo.jwave.cmd.JWaveNodeCommand;
import de.smahoo.jwave.node.JWaveNode;

public class JWaveDatagramFactory {
	
	
	
	
	public static JWaveDatagram generateDatagram(byte[] buffer, int start, int len){	
		byte[] bytes = new byte[len];
		for (int i=0; i<len; i++){
			bytes[i] = buffer[start+i];
		}
		return generateDatagram(bytes);
	}
	
	public static JWaveDatagram generateDatagram(byte[] bytes, boolean checkCRC){
		if (checkCRC){
			
			// FIXME: check CRC first and throw Exception
		}
		JWaveDatagram datagram = new JWaveDatagram();
		datagram.setBytes(bytes);		
		
		datagram.status = JWaveDatagramStatus.STATUS_GENERATED;
		return datagram;
	}
	
	public static JWaveDatagram generateDatagram(byte[] bytes){
		return generateDatagram(bytes,false);
	}
	
	public static JWaveDatagram generateDatagram(byte type, byte cmd){
		return generateDatagram(type,cmd,null);
	}
	
	public static JWaveDatagram generateDatagram(byte type, byte cmd, byte payload){
		byte[] tmp = new byte[1];
		tmp[0] = payload;
		return generateDatagram(type,cmd,tmp);
	}
	
	public static JWaveDatagram generateDatagram(byte type, byte cmd, byte[] payload){
		JWaveDatagram datagram = new JWaveDatagram();
		return generateDatagram(datagram,type,cmd,payload);
		
	}
	
	protected static JWaveNodeCommandDatagram generateDatagram(JWaveNodeCommand nodeCmd, byte type, byte cmd, byte[] payload){
		JWaveNodeCommandDatagram datagram = new JWaveNodeCommandDatagram(nodeCmd);
		return (JWaveNodeCommandDatagram) generateDatagram(datagram,type,cmd,payload);
	}
	
	protected static JWaveDatagram generateDatagram(JWaveDatagram datagram, byte type, byte cmd, byte[] payload){
		int payloadLen;
		if (payload == null){
			payloadLen = 0;
		} else {
			payloadLen = payload.length;
		}
		
		byte[] bytes = new byte[payloadLen+5];
		bytes[0] = 0x01;
		bytes[1] = (byte)(payloadLen+3);
		bytes[2] = type;
		bytes[3] = cmd;
		
		if (payload != null){
			for (int i = 0; i<payload.length; i++){
				bytes[4+i] = payload[i];
			}
		}
		
		bytes[bytes[1]+1]=calculateLRC(type, cmd, bytes[1], payload);
		datagram.setBytes(bytes);
		return datagram;
	}
		
	public static JWaveDatagram generateGetSerialApiCapabilitiesRequest(){
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_SERIAL_GET_CAPABILITIES));
	}
	
	public static JWaveDatagram generateResetControllerCmd(){
		byte[] bytes = new byte[1];
		bytes[0] = 0x02;
		return generateDatagram((byte)0x01, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_SET_DEFAULT),bytes);
	}
	
	public static JWaveDatagram generateRemoveNodeFromNetworkCompletedMessage(){
		byte[] bytes = new byte[1];
		bytes[0] = 0x05;
		return generateDatagram((byte)0x01, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_REMOVE_NODE_FROM_NETWORK),bytes);
	}
	
	public static JWaveDatagram generateRemoveNodeFromNetworkRequest(byte mode, byte funcId){
		byte[] bytes = new byte[2];
		bytes[0] = mode;
		bytes[1] = funcId;
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_REMOVE_NODE_FROM_NETWORK),bytes);
	}
	
	
	
	public static JWaveDatagram generateAddNodeToNetworkRequest(byte mode, byte funcId){
		byte[] bytes = new byte[2];
		bytes[0] = mode;
		bytes[1] = funcId;
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_ADD_NODE_TO_NETWORK),bytes);
	}	
	
	public static JWaveDatagram generateSerialInitDataRequest(){
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_SERIAL_GET_INIT_DATA));
	}
	
	public static JWaveDatagram generateGetVersionRequest(){	
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_GET_VERSION));
	}
	
	public static JWaveDatagram generateGetHomeIdRequest(){		
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_MEMORY_GET_ID));
	}
	
	public static JWaveDatagram generateGetNodeProtocolInfoRequest(int nodeId){		
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_GET_NODE_PROTOCOL_INFO),(byte)nodeId);
	}
	
	public static JWaveDatagram generateSendNodeInfo(int destination){		
		return generateDatagram((byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_SEND_NODE_INFORMATION),(byte)destination);
	}
	
	public static JWaveDatagram generateSendDataDatagram(JWaveNode node, JWaveNodeCommand cmd, byte funcId){
		
		byte[] cmdBytes = cmd.toByteArray();
		
		byte[] data = new byte[cmdBytes.length+5];
		data[0] = (byte)node.getNodeId();
		data[1] = (byte)cmdBytes.length;
		for (int i=0; i< cmdBytes.length; i++){
			data[2+i] = cmdBytes[i];
		}
		
		data[data.length - 2] = funcId; 
		data[data.length - 1] = 00;		// txOptions
		return generateDatagram(cmd,(byte)0x00, JWaveCommandType.getByte(JWaveCommandType.CMD_JWave_SEND_DATA), data);
		
	}
	
	public static int calculateLRC(int[] array,int start,int end){
		byte calcChksum = (byte)0xFF;
		for (int i = start; i < end; i++){
            calcChksum ^= array[i];      
        }
        return calcChksum & 0xFF;
	}
	
	public static byte calculateLRC(byte[] array,int start,int end){
		byte calcChksum = (byte)0xFF;
		for (int i = start; i < end; i++){
            calcChksum ^= array[i];      
        }
        return calcChksum;
	}
	
	private static byte calculateLRC(byte type, byte cmd, byte length, byte[] payload) {
		byte calcChksum = (byte)0xFF;
		
        calcChksum ^= (byte)(length); 
        calcChksum ^= (byte)type;     
        calcChksum ^= (byte)cmd;      
        if (payload != null) {
        for (int i = 0; i < payload.length; i++)
            calcChksum ^= payload[i];      // Data
        }
        return calcChksum;
	    
	}
}
