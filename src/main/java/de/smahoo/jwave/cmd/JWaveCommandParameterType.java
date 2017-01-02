package de.smahoo.jwave.cmd;


public enum JWaveCommandParameterType {
	ARRAY,		// unsupported yet
	BITMASK,	
	BIT_24,
	BIT_32,
	BYTE,	
	CONST,		// unsupported yet
	DWORD,		// unsupported yet
	ENUM,		// unsupported yet
	ENUM_ARRAY,	// unsupported yet
	MARKER,		// unsupported yet
	MULTI_ARRAY,	// unsupported yet
	STRUCT_BYTE,	
	VARIANT,// unsupported yet
	VARIANT_GROUP,
	WORD;
	
	
	public static byte[] toByteArray(JWaveCommandParameterType type, String value) throws JWaveConvertException{
		if (value == null){
			return toByteArray(type,0);
		}
		String valueStr = value.replace("0x","");
		valueStr = valueStr.replace(" ","");
		int val; 
		try {				
			val = Integer.parseInt(valueStr,16);			
		} catch (NumberFormatException exc){
			throw new JWaveConvertException("Unable to convert '"+value+"' into a number!",exc);
		}	
		
		switch (type){
		case BIT_24:			
		case BIT_32:
		case WORD:
		case BYTE:
			return toByteArray(type, val);		
		case STRUCT_BYTE:
		default:
			return toByteArray(type,val);				
		}
	}
	
	public static byte[] toByteArray(JWaveCommandParameterType type, int value) throws JWaveConvertException{
		switch (type){			
		case BIT_24:
			return generateBit24(value);
		case BIT_32:
			return generateBit32(value);
		case WORD: 
			return generateWord(value);		
		case BYTE:
		case STRUCT_BYTE:
		default:
			return generateByte((byte)value);				
		}
	}
	
	public static int getSize(JWaveCommandParameterType type) {
		switch (type){		
		case BIT_24:
			return 3;
		case BIT_32:
			return 4;
		case WORD:
			return 2;
		case BYTE:
		case CONST:
		case BITMASK:
		case STRUCT_BYTE:
			return 1;
		default:
			return -1;				
		}
	}
	
	public static byte toByte(byte[] bytes) throws JWaveConvertException{
		if (bytes.length == 1){
			return bytes[0];
		}		
		throw new JWaveConvertException("Unable to convert byte. Array size of 1 expected, but array was of size "+bytes.length);
		//return bytes[bytes.length-1];
	}
	
	public static int toInteger(byte[] bytes) {
		
		if ( bytes.length == 4){
			return   bytes[3] & 0xFF |
		            (bytes[2] & 0xFF) << 8 |
		            (bytes[1] & 0xFF) << 16 |
		            (bytes[0] & 0xFF) << 24;	
		}
		if ( bytes.length == 3){
			return   bytes[2] & 0xFF |
		            (bytes[1] & 0xFF) << 8 |
		            (bytes[0] & 0xFF) << 16;		            	
		}
		if ( bytes.length == 2){
			return   bytes[1] & 0xFF |
		            (bytes[0] & 0xFF) << 8;		            		            	
		}
		return bytes[0] & 0xFF;
		 
	}
	
	public static byte[] generateWord(int value) throws JWaveConvertException{
		byte[] bytes = new byte[2];		
		
		
		bytes[0] = (byte)(value >>> 8);		
		bytes[1] = (byte)(value);	
		
		return bytes;
	}
	
	public static byte[] generateBit32(int value) throws JWaveConvertException{
		byte[] bytes = new byte[4];		
		
		bytes[0] = (byte)(value >>> 24);
		bytes[1] = (byte)(value >>> 16);
		bytes[2] = (byte)(value >>> 8);		
		bytes[3] = (byte)(value);	
		
		return bytes;
	}
	
	public static byte[] generateBit24(int value) throws JWaveConvertException{
		byte[] bytes = new byte[3];		
		
		bytes[0] = (byte)(value >>> 16);
		bytes[1] = (byte)(value >>> 8);		
		bytes[2] = (byte)(value);	
		
		return bytes;
	}
	
	public static byte[] generateByte(byte value) {
		byte[] bytes = new byte[1];
		bytes[0] = value;
		return bytes;
	}
	
}
