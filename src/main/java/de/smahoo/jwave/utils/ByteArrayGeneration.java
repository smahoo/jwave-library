package de.smahoo.jwave.utils;

import java.util.StringTokenizer;


/**
 * The communication between the physical Z-Wave controller and the library is based on byte streams. This class helps
 * the generation of those streams from text and vise versa.
 *
 *  @author Mathias Runge (mathias.runge@smahoo.de)
 */
public class ByteArrayGeneration {

	private  final static int BYTE_BASE = 16;
	private final static int MAX_BYTE_VALUE = 0xFF;

	/**
	 * Generate an array of bytes out of a text of bytes.
	 * @param strBytes byte array (example : "4F 22 d4 01 05")
	 * @return array of byte with values given in strBytes
	 */
	public static byte[] generateByteArray(String strBytes){
		StringTokenizer tokenizer = new StringTokenizer(strBytes," ");
		byte[] byteArray = new byte[tokenizer.countTokens()];
		int index = 0;
		
		while (tokenizer.hasMoreElements()){
			byteArray[index] = (byte)Integer.parseInt(tokenizer.nextToken(),BYTE_BASE);
			index++;
		}			
		return byteArray;
	}

	/**
	 * Generates an array of int values out of a text of bytes.
	 * @param strBytes byte array (example : "4F 22 d4 01 05")
	 * @return array of integer values
	 */
	public static int[] generateIntArray(String strBytes){
		StringTokenizer tok = new StringTokenizer(strBytes," ");
		int[] intArray = new int[tok.countTokens()];
		int index = 0;
		
		while (tok.hasMoreElements()){
			intArray[index] = Integer.parseInt(tok.nextToken(),BYTE_BASE);
			index++;
		}			
		return intArray;
	}


	/**
	 * Generates a text (example : "4F 22 d4 01 05") out of an array of integer values.
	 * @param values array of integer values
	 * @param start array index where generation of text starts
	 * @param length amount of values that have to be processed
	 * @return string of hexadecimal values with 2 digits, separated with space
	 */
	public static String toHexString(int[] values, int start, int length){
        if (values == null){
            return "";
        }
		checkIndexes(values.length,start,length);
		StringBuffer res =new StringBuffer();
		String tmp;
		boolean firstDigit = true;
		if (values != null){
			for (int i= start; i<length; i++){
				tmp = Integer.toHexString((values[i]&MAX_BYTE_VALUE));
				if (tmp.length() <2){
					tmp = "0"+tmp;
				}
				if (firstDigit){
					res.append(tmp);
					firstDigit = false;
				} else {
					res.append(" "+tmp);
				}
			}
		}
		
		return res.toString();
	}

	/**
	 * Generates a text (example : "4F 22 d4 01 05") out of an array of integer values.
	 * @param values array of integer values
	 * @return string of hexadecimal values with 2 digits, separated with space
	 */
	public static String toHexString(int[] values){
		if (values == null){
			return "";
		}
		return toHexString(values,0,values.length);
	}

	/**
	 * Generates a text (example : "4F 22 d4 01 05") out of a range  of an byte array
	 * @param values array of byte values
	 * @param start start index of byte
	 * @param length amount of bytes thate have to be processed
	 * @return string of hexadecimal values with 2 digits, separated with space
	 */
	public static String toHexString(byte[] values, int start, int length){
        if (values == null){
            return "";
        }
		checkIndexes(values.length,start,length);
		StringBuffer res =new StringBuffer();
		String tmp;
		boolean firstDigit = true;
		if (values != null){
			for (int i= start; i<length; i++){
				tmp = Integer.toHexString((values[i]&MAX_BYTE_VALUE));
				if (tmp.length() <2){
					tmp = "0"+tmp;
				}
				if (firstDigit){
					res.append(tmp);
					firstDigit = false;
				} else {
					res.append(" "+tmp);
				}
			}
		}
		
		return res.toString();
	}

	/**
	 * Generates a text (example : "4F 22 d4 01 05") out of an array of byte values.
	 * @param bytes array with byte values
	 * @return text of hexadecimal values with 2 digits, separated with space
	 */
	public static String toHexString(byte[] bytes){
		if (bytes == null){
			return "";
		}
		return toHexString(bytes,0,bytes.length);
	}


	private static void checkIndexes(int length, int start, int count){
		if (start > length){
			throw new ArrayIndexOutOfBoundsException("Unable to process array. Starting point ("+start+") is out of array range ("+length+").");
		}
		if (start+length > length){
			throw new ArrayIndexOutOfBoundsException("Unable to process array. End point ("+(start+count)+") is out of array range ("+length+").");
		}
	}

}
