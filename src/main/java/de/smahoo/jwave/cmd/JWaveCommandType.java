package de.smahoo.jwave.cmd;

public enum JWaveCommandType {
	
	CMD_NONE,	
	CMD_UNKNOWN,
	
	CMD_APPL_COMMAND_HANDLER,
	CMD_APPL_COMMAND_HANDLER_BRIDGE,	
	CMD_APPL_CONTROLLER_UPDATE,
	CMD_APPL_SLAVE_COMMAND_HANDLER,
	
	CMD_CLOCK_COMPARE,
    CMD_CLOCK_GET,
    CMD_CLOCK_SET,    
		
    CMD_MEMORY_GET_BUFFER,
    CMD_MEMORY_GET_BYTE,
    CMD_MEMORY_GET_ID,    
    CMD_MEMORY_PUT_BUFFER,
    CMD_MEMORY_PUT_BYTE,    
    
    CMD_RTC_TIMER_CALL,    
    CMD_RTC_TIMER_CREATE,
    CMD_RTC_TIMER_DELETE,
    CMD_RTC_TIMER_READ,   	
    
	CMD_SERIAL_APPL_NODE_INFORMATION,
	CMD_SERIAL_GET_CAPABILITIES,
	CMD_SERIAL_GET_INIT_DATA,
	CMD_SERIAL_SET_TIMEOUTS,	
	CMD_SERIAL_SLAVE_NODE_INFO,
	CMD_SERIAL_SOFT_RESET,
	CMD_SERIAL_TEST,
    
	CMD_TIMER_CALL,
	CMD_TIMER_CANCEL,	
    CMD_TIMER_RESTART,
    CMD_TIMER_START,        
    
	CMD_JWave_GET_CONTROLLER_CAPABILITIES,
	CMD_JWave_SET_RF_RECEIVEMODE,
	CMD_JWave_SET_SLEEPMODE,
	CMD_JWave_SEND_NODE_INFORMATION,
	CMD_JWave_SEND_DATA,
	CMD_JWave_SEND_DATA_BRIDGE,
	CMD_JWave_SEND_DATA_MULTI,
	CMD_JWave_SEND_DATA_MULTI_BRIDGE,
	CMD_JWave_GET_VERSION,
	CMD_JWave_SEND_DATA_ABORT,
	CMD_JWave_SET_RF_POWER_LEVEL,
	CMD_JWave_SEND_DATA_META,
	CMD_JWave_SEND_DATA_META_BRIDGE,
    CMD_JWave_SEND_DATA_MR,
    CMD_JWave_SEND_DATA_META_MR,
    CMD_JWave_SET_ROUTING_INFO,    
    CMD_JWave_SEND_TEST_FRAME,
    CMD_JWave_GET_NODE_PROTOCOL_INFO,
    CMD_JWave_SET_DEFAULT,
    CMD_JWave_REPLICATION_COMMAND_COMPLETE,
    CMD_JWave_REPLICATION_SEND_DATA,
    CMD_JWave_ASSIGN_RETURN_ROUTE,
    CMD_JWave_DELETE_RETURN_ROUTE,
    CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE,
    CMD_JWave_ADD_NODE_TO_NETWORK,
    CMD_JWave_REMOVE_NODE_FROM_NETWORK,
    CMD_JWave_CREATE_NEW_PRIMARY,
    CMD_JWave_CONTROLLER_CHANGE,
    CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE_MR,
    CMD_JWave_ASSIGN_RETURN_ROUTE_MR,
    CMD_JWave_SET_LEARN_MODE,
    CMD_JWave_ASSIGN_SUC_RETURN_ROUTE,
    CMD_JWave_ENABLE_SUC,
    CMD_JWave_REQUEST_NETWORK_UPDATE,
    CMD_JWave_SET_SUC_NODE_ID,
    CMD_JWave_DELETE_SUC_RETURN_ROUTE,
    CMD_JWave_GET_SUC_NODE_ID,
    CMD_JWave_SEND_SUC_ID,
    CMD_JWave_ASSIGN_SUC_RETURN_ROUTE_MR,
    CMD_JWave_REDISCOVERY_NEEDED,
    CMD_JWave_REQUEST_NODE_INFO,
    CMD_JWave_REMOVE_FAILED_NODE_ID,
    CMD_JWave_IS_FAILED_NODE,
    CMD_JWave_REPLACE_FAILED_NODE,
    CMD_JWave_SEND_DATA_ROUTE_DEMO,
    CMD_JWave_SEND_SLAVE_NODE_INFO,
    CMD_JWave_SEND_SLAVE_DATA,
    CMD_JWave_SET_SLAVE_LEARN_MODE,
    CMD_JWave_GET_VIRTUAL_NODES,
    CMD_JWave_IS_VIRTUAL_NODE,
    CMD_JWave_RESERVED_SSD,
    CMD_JWave_GET_RANDOM,
    CMD_JWave_SET_PROMISCUOUS_MODE,
    
	CMD_GET_ROUTING_TABLE_LINE,
    CMD_GET_TX_COUNTER,
    CMD_RESET_TX_COUNTER,
    CMD_STORE_NODE_INFO,
    CMD_STORE_HOME_ID,
    CMD_LOCK_ROUTE_RESPONSE;
    	    
	
	public static byte getByte(JWaveCommandType cmdType){
		switch(cmdType){
			case CMD_NONE: 									return 0x00;		
			case CMD_APPL_COMMAND_HANDLER: 					return 0x04;
			case CMD_APPL_CONTROLLER_UPDATE: 				return 0x49;
			case CMD_APPL_SLAVE_COMMAND_HANDLER: 			return (byte)0xa1;
			case CMD_APPL_COMMAND_HANDLER_BRIDGE: 			return (byte)0xa8;	

			case CMD_CLOCK_SET: 							return 0x30;
			case CMD_CLOCK_GET:								return 0x31;
			case CMD_CLOCK_COMPARE: 						return 0x32;
			
			case CMD_MEMORY_GET_ID: 						return 0x20;
			case CMD_MEMORY_GET_BYTE: 						return 0x21;
			case CMD_MEMORY_PUT_BYTE: 						return 0x22;
			case CMD_MEMORY_GET_BUFFER: 					return 0x23;
			case CMD_MEMORY_PUT_BUFFER: 					return 0x24;
	        	
			case CMD_RTC_TIMER_CREATE: 						return 0x33;
			case CMD_RTC_TIMER_READ: 						return 0x34;
			case CMD_RTC_TIMER_DELETE: 						return 0x35;
			case CMD_RTC_TIMER_CALL: 						return 0x36;
	    	
			case CMD_SERIAL_GET_INIT_DATA: 					return 0x02;
			case CMD_SERIAL_APPL_NODE_INFORMATION:		 	return 0x03;
			case CMD_SERIAL_SET_TIMEOUTS: 					return 0x06;
			case CMD_SERIAL_GET_CAPABILITIES: 				return 0x07;
			case CMD_SERIAL_SOFT_RESET: 					return 0x08;
			case CMD_SERIAL_TEST: 							return (byte)0x95;
			case CMD_SERIAL_SLAVE_NODE_INFO: 				return (byte)0xa0;
	        
			case CMD_TIMER_START: 							return 0x70;
			case CMD_TIMER_RESTART: 						return 0x71;
			case CMD_TIMER_CANCEL: 							return 0x72;
			case CMD_TIMER_CALL: 							return 0x73;    
	    
			case CMD_JWave_GET_CONTROLLER_CAPABILITIES: 	return 0x05;
			case CMD_JWave_SET_RF_RECEIVEMODE: 				return 0x10;
			case CMD_JWave_SET_SLEEPMODE: 					return 0x11;
			case CMD_JWave_SEND_NODE_INFORMATION: 			return 0x12;
			case CMD_JWave_SEND_DATA: 						return 0x13;
			case CMD_JWave_SEND_DATA_BRIDGE: 				return (byte)0xa9;
			case CMD_JWave_SEND_DATA_MULTI: 				return 0x14;
			case CMD_JWave_SEND_DATA_MULTI_BRIDGE:			return (byte)0xab;
			case CMD_JWave_GET_VERSION: 					return 0x15;
			case CMD_JWave_SEND_DATA_ABORT:					return 0x16;
			case CMD_JWave_SET_RF_POWER_LEVEL: 				return 0x17;
			case CMD_JWave_SEND_DATA_META: 					return 0x18;
			case CMD_JWave_SEND_DATA_META_BRIDGE: 			return (byte)0xaa;
			case CMD_JWave_SEND_DATA_MR: 					return 0x19;
			case CMD_JWave_SEND_DATA_META_MR: 				return 0x1a;
			case CMD_JWave_SET_ROUTING_INFO: 				return 0x1b;    
			case CMD_JWave_SEND_TEST_FRAME: 				return (byte)0xbe;
			case CMD_JWave_GET_NODE_PROTOCOL_INFO: 			return 0x41;
			case CMD_JWave_SET_DEFAULT: 					return 0x42;
			case CMD_JWave_REPLICATION_COMMAND_COMPLETE: 	return 0x44;
			case CMD_JWave_REPLICATION_SEND_DATA:	 		return 0x45;
			case CMD_JWave_ASSIGN_RETURN_ROUTE: 			return 0x46;
			case CMD_JWave_DELETE_RETURN_ROUTE: 			return 0x47;
			case CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE: 	return 0x48;
			case CMD_JWave_ADD_NODE_TO_NETWORK: 			return 0x4a;
			case CMD_JWave_REMOVE_NODE_FROM_NETWORK:		return 0x4b;
			case CMD_JWave_CREATE_NEW_PRIMARY: 				return 0x4c;
			case CMD_JWave_CONTROLLER_CHANGE: 				return 0x4d;
			case CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE_MR: return 0x4e;
			case CMD_JWave_ASSIGN_RETURN_ROUTE_MR: 			return 0x4f;
			case CMD_JWave_SET_LEARN_MODE: 					return 0x50;
			case CMD_JWave_ASSIGN_SUC_RETURN_ROUTE: 		return 0x51;
			case CMD_JWave_ENABLE_SUC: 						return 0x52;
			case CMD_JWave_REQUEST_NETWORK_UPDATE: 			return 0x53;
			case CMD_JWave_SET_SUC_NODE_ID: 				return 0x54;
			case CMD_JWave_DELETE_SUC_RETURN_ROUTE:			return 0x55;
			case CMD_JWave_GET_SUC_NODE_ID: 				return 0x56;
			case CMD_JWave_SEND_SUC_ID:						return 0x57;
			case CMD_JWave_ASSIGN_SUC_RETURN_ROUTE_MR: 		return 0x58;
			case CMD_JWave_REDISCOVERY_NEEDED:				return 0x59;
			case CMD_JWave_REQUEST_NODE_INFO: 				return 0x60;
			case CMD_JWave_REMOVE_FAILED_NODE_ID:			return 0x61;
			case CMD_JWave_IS_FAILED_NODE: 					return 0x62;
			case CMD_JWave_REPLACE_FAILED_NODE: 			return 0x63;
			case CMD_JWave_SEND_DATA_ROUTE_DEMO: 			return (byte)0x91;
			case CMD_JWave_SEND_SLAVE_NODE_INFO: 			return (byte)0xa2;
			case CMD_JWave_SEND_SLAVE_DATA: 				return (byte)0xa3;
			case CMD_JWave_SET_SLAVE_LEARN_MODE: 			return (byte)0xa4;
			case CMD_JWave_GET_VIRTUAL_NODES: 				return (byte)0xa5;
			case CMD_JWave_IS_VIRTUAL_NODE: 				return (byte)0xa6;
			case CMD_JWave_RESERVED_SSD: 					return (byte)0xa7;
			case CMD_JWave_GET_RANDOM: 						return (byte)0x1c;
			case CMD_JWave_SET_PROMISCUOUS_MODE: 			return (byte)0xd0;
	    
			case CMD_GET_ROUTING_TABLE_LINE: 				return (byte)0x80;
			case CMD_GET_TX_COUNTER: 						return (byte)0x81;
			case CMD_RESET_TX_COUNTER: 						return (byte)0x82;
			case CMD_STORE_NODE_INFO: 						return (byte)0x83;
			case CMD_STORE_HOME_ID: 						return (byte)0x84;
			case CMD_LOCK_ROUTE_RESPONSE: 					return (byte)0x90;	       
			default:
				return (byte)0xFF;
		}
		
	}
	
	
	public static JWaveCommandType getCommandType(int value){
		switch(value){
			case 0x00: return CMD_NONE;	
			case 0xFF: return CMD_UNKNOWN;		
			case 0x04: return CMD_APPL_COMMAND_HANDLER;
			case 0x49: return CMD_APPL_CONTROLLER_UPDATE;
			case 0xa1: return CMD_APPL_SLAVE_COMMAND_HANDLER;
			case 0xa8: return CMD_APPL_COMMAND_HANDLER_BRIDGE;	

			case 0x30: return CMD_CLOCK_SET;
			case 0x31: return CMD_CLOCK_GET;
			case 0x32: return CMD_CLOCK_COMPARE;
		
			case 0x20: return CMD_MEMORY_GET_ID;
			case 0x21: return CMD_MEMORY_GET_BYTE;
			case 0x22: return CMD_MEMORY_PUT_BYTE;
			case 0x23: return CMD_MEMORY_GET_BUFFER;
			case 0x24: return CMD_MEMORY_PUT_BUFFER;
			
			case 0x33: return CMD_RTC_TIMER_CREATE;
			case 0x34: return CMD_RTC_TIMER_READ;
			case 0x35: return CMD_RTC_TIMER_DELETE;
			case 0x36: return CMD_RTC_TIMER_CALL;
    	
			case 0x02: return CMD_SERIAL_GET_INIT_DATA;
			case 0x03: return CMD_SERIAL_APPL_NODE_INFORMATION;
			case 0x06: return CMD_SERIAL_SET_TIMEOUTS;
			case 0x07: return CMD_SERIAL_GET_CAPABILITIES;
			case 0x08: return CMD_SERIAL_SOFT_RESET;
			case 0x95: return CMD_SERIAL_TEST;
			case 0xa0: return CMD_SERIAL_SLAVE_NODE_INFO;
        
			case 0x70: return CMD_TIMER_START;
			case 0x71: return CMD_TIMER_RESTART;
			case 0x72: return CMD_TIMER_CANCEL;
			case 0x73: return CMD_TIMER_CALL;    
    
			case 0x05: return CMD_JWave_GET_CONTROLLER_CAPABILITIES;
			case 0x10: return CMD_JWave_SET_RF_RECEIVEMODE;
			case 0x11: return CMD_JWave_SET_SLEEPMODE;
			case 0x12: return CMD_JWave_SEND_NODE_INFORMATION;
			case 0x13: return CMD_JWave_SEND_DATA;
			case 0xa9: return CMD_JWave_SEND_DATA_BRIDGE;
			case 0x14: return CMD_JWave_SEND_DATA_MULTI;
			case 0xab: return CMD_JWave_SEND_DATA_MULTI_BRIDGE;
			case 0x15: return CMD_JWave_GET_VERSION;
			case 0x16: return CMD_JWave_SEND_DATA_ABORT;
			case 0x17: return CMD_JWave_SET_RF_POWER_LEVEL;
			case 0x18: return CMD_JWave_SEND_DATA_META;
			case 0xaa: return CMD_JWave_SEND_DATA_META_BRIDGE;
			case 0x19: return CMD_JWave_SEND_DATA_MR;
			case 0x1a: return CMD_JWave_SEND_DATA_META_MR;
			case 0x1b: return CMD_JWave_SET_ROUTING_INFO;    
			case 0xbe: return CMD_JWave_SEND_TEST_FRAME;
			case 0x41: return CMD_JWave_GET_NODE_PROTOCOL_INFO;
			case 0x42: return CMD_JWave_SET_DEFAULT;
			case 0x44: return CMD_JWave_REPLICATION_COMMAND_COMPLETE;
			case 0x45: return CMD_JWave_REPLICATION_SEND_DATA;
			case 0x46: return CMD_JWave_ASSIGN_RETURN_ROUTE;
			case 0x47: return CMD_JWave_DELETE_RETURN_ROUTE;
			case 0x48: return CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE;
			case 0x4a: return CMD_JWave_ADD_NODE_TO_NETWORK;
			case 0x4b: return CMD_JWave_REMOVE_NODE_FROM_NETWORK;
			case 0x4c: return CMD_JWave_CREATE_NEW_PRIMARY;
			case 0x4d: return CMD_JWave_CONTROLLER_CHANGE;
			case 0x4e: return CMD_JWave_REQUEST_NODE_NEIGHBOR_UPDATE_MR;
			case 0x4f: return CMD_JWave_ASSIGN_RETURN_ROUTE_MR;
			case 0x50: return CMD_JWave_SET_LEARN_MODE;
			case 0x51: return CMD_JWave_ASSIGN_SUC_RETURN_ROUTE;
			case 0x52: return CMD_JWave_ENABLE_SUC;
			case 0x53: return CMD_JWave_REQUEST_NETWORK_UPDATE;
			case 0x54: return CMD_JWave_SET_SUC_NODE_ID;
			case 0x55: return CMD_JWave_DELETE_SUC_RETURN_ROUTE;
			case 0x56: return CMD_JWave_GET_SUC_NODE_ID;
			case 0x57: return CMD_JWave_SEND_SUC_ID;
			case 0x58: return CMD_JWave_ASSIGN_SUC_RETURN_ROUTE_MR;
			case 0x59: return CMD_JWave_REDISCOVERY_NEEDED;
			case 0x60: return CMD_JWave_REQUEST_NODE_INFO;
			case 0x61: return CMD_JWave_REMOVE_FAILED_NODE_ID;
			case 0x62: return CMD_JWave_IS_FAILED_NODE;
			case 0x63: return CMD_JWave_REPLACE_FAILED_NODE;
			case 0x91: return CMD_JWave_SEND_DATA_ROUTE_DEMO;
			case 0xa2: return CMD_JWave_SEND_SLAVE_NODE_INFO;
			case 0xa3: return CMD_JWave_SEND_SLAVE_DATA;
			case 0xa4: return CMD_JWave_SET_SLAVE_LEARN_MODE;
			case 0xa5: return CMD_JWave_GET_VIRTUAL_NODES;
			case 0xa6: return CMD_JWave_IS_VIRTUAL_NODE;
			case 0xa7: return CMD_JWave_RESERVED_SSD;
			case 0x1c: return CMD_JWave_GET_RANDOM;
			case 0xd0: return CMD_JWave_SET_PROMISCUOUS_MODE;
    
			case 0x80: return CMD_GET_ROUTING_TABLE_LINE;
			case 0x81: return CMD_GET_TX_COUNTER;
			case 0x82: return CMD_RESET_TX_COUNTER;
			case 0x83: return CMD_STORE_NODE_INFO;
			case 0x84: return CMD_STORE_HOME_ID;
			case 0x90: return CMD_LOCK_ROUTE_RESPONSE;
			default:
				return CMD_UNKNOWN;
		}
		
	}
	
}
