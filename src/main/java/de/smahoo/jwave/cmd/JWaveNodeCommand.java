package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveNodeCommand {

	protected JWaveCommandClass cmdClass = null;
	protected JWaveCommand cmd = null;
	protected HashMap<JWaveCommandParameter, byte[]> params = null;
	protected boolean received = false;
	protected int funcId = -1;
	protected HashMap<JWaveCommandParameter, Integer> paramSizes = null;
	protected byte[] paramValues = null;
	protected int expectedResponses = 1;	
	
	public JWaveNodeCommand(JWaveCommand cmd){
		this.cmd = cmd;
		this.cmdClass = cmd.getCommandClass();
//		if (cmd.getCommandClass().getKey() == 0x98){
//			System.out.println("Securityzeugs");
//			System.out.println("Command = "+cmd.getName()+" "+cmd.getKey());
//			for (JWaveCommandParameter param : cmd.getParamList()){
//				System.out.println("param "+param.getName());
//			}
//		}
		params = new HashMap<JWaveCommandParameter, byte[]>();
		paramSizes = new HashMap<JWaveCommandParameter,Integer>();
	
		int size = 0;
		if (!cmd.getParamList().isEmpty()){
			for (JWaveCommandParameter param : cmd.getParamList()){
				params.put(param,null);
				size = JWaveCommandParameterType.getSize(param.getType());
				if (size > 0){
					paramSizes.put(param, size);
				
				} else {
					paramSizes.put(param,null);
				
				}
			}		
		}
	
	}
	
	public int getFuncId(){
		return funcId;
	}
	
	public void setFuncId(int funcId){
		this.funcId = funcId;
	}
	
	public int getExpectedResponses(){
		return expectedResponses;
	}
	
	public void setExpectedResponses(int responses){
		this.expectedResponses = responses;
	}
	
	public boolean hasParams(){
		return !params.isEmpty();
	}

	
	public boolean isComplete(){
		if (cmdClass == null){
			return false;
		}
		
		if (cmd == null){
			return false;
		}
		
		if (hasParams()){
			for (JWaveCommandParameter p : getParamList()){
				if (this.getParamValue(p) == null){
					return false;
				}
			}
		} 		
		
		return true;
	}	
		
	
	public void setParamValue(int key, int value) throws JWaveConvertException{
		setParamValue(cmd.getParam(key), value);
	}
	
	public void setParamValue(int key, byte[] bytes) throws JWaveConvertException{
		setParamValue(cmd.getParam(key), bytes);
	}
		
	
	public void setParamValue(JWaveCommandParameter param, int value) throws JWaveConvertException{
		setParamValue(param, JWaveCommandParameterType.toByteArray(param.getType(),value));
		
	}
	
	public void setParamValue(JWaveCommandParameter param, byte[] bytes){
		if (params.containsKey(param)){
			params.remove(param);
		}
		params.put(param,bytes);
		paramSizes.put(param,bytes.length);
	}
	
	protected void setParamValues() throws JWaveConvertException {
		int index = 0;		
		for (int i=0; i<params.size(); i++){
			index = setParamValue(getParam(i),paramValues,index);
		}
	}
	
	public byte[] getParamValues(){
		return paramValues;
	}
	
	public void setParamValues(byte[] vals) throws JWaveConvertException{
		// create copy to prevent modification from parent
		byte [] values = vals.clone();
		
		if (paramValues == null){
			paramValues = values;	
		}
		
		setParamValues();		
		
		if (!isComplete()){
		
		
			JWaveCommandParameter paramWithoutValueSize = null;
			int sizeCnt = 0;
			int size = 0;
			int numberOfUnknownSizeParams = 0;
		
			for (Entry<JWaveCommandParameter,byte[]> entry : this.params.entrySet()){
				size = paramSizes.get(entry.getKey());
				if (size > 0){
					sizeCnt =+ size;
				} else {
					numberOfUnknownSizeParams++;
					paramWithoutValueSize = entry.getKey();
				}
			}
		
			if (numberOfUnknownSizeParams == 1){
				paramSizes.put(paramWithoutValueSize, paramValues.length-sizeCnt);
			}
			setParamValues();
		}
		
	}
	
	private JWaveCommandParameter getParam(int id){
			
		for (Entry<JWaveCommandParameter,byte[]> entry : params.entrySet()){
			if (entry.getKey().getKey() == id){
				return entry.getKey();
			}
		}
		return null;
	}
	
	public void setParamValue(JWaveCommandParameter param, String value) throws JWaveConvertException{
		setParamValue(param, JWaveCommandParameterType.toByteArray(param.getType(), value));
	}
	
	protected int setParamValue(JWaveCommandParameter param, byte[] values, int index) throws JWaveConvertException{
		
		int size=-1;
	
		
		
		if (paramSizes.get(param) == null){
			if (param.getCommand().getCommandClass().getKey() == 0x8f){ // MultiCmd
				size = calculateVariantGroupSize(param,values,index);
			} else {
				size = JWaveCommandParameterType.getSize(param.getType());
			}
			paramSizes.put(param,size);
		} else {
			size = paramSizes.get(param);
		}
			
		if (size > 0){			
			if (values.length >= index+size){
				byte[] val = getBytes(values, index, index+size);
				setParamValue(param, val);
			}
		}
		
		
		
		return index+size;
	}
	
	protected int calculateVariantGroupSize(JWaveCommandParameter param, byte[] values, int index){
		int size = 1;		
		int cmdCount = values[index-1];
		int currCmdCount = 0;
	
		while ((size < values.length) && (currCmdCount < cmdCount)){		
			size = size+ values[size+index-1];
			currCmdCount++;
		}
		
		return size;
	}
	
	public void setParamSize(int key, int size){
		JWaveCommandParameter param = getParam(key);
		if (param != null){
			paramSizes.remove(param);
			paramSizes.put(param,size);
			try {
				this.setParamValues();
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
				// FIXME
			}
		}
	}
	
	protected byte[] getBytes(byte[] values, int start, int end){
		int len = end-start;
		byte[] bytes = new byte[len];
		
		for (int i=0; i<len; i++){
			bytes[i] = values[i+start];
		}
		
		return bytes;
	}
	
	public byte[] getParamValue(int key){
		return getParamValue(cmd.getParam(key));
	}
	
	public byte[] getParamValue(JWaveCommandParameter param){
		return params.get(param);
	}
	
	public JWaveCommandClass getCommandClass(){
		return cmdClass;
	}
	
	public int getCommandClassKey(){
		return cmdClass.getKey();
	}
	
	public JWaveCommand getCommand(){
		return cmd;
	}
	
	public int getCommandKey(){
		return cmd.getKey();
	}
	
	public List<JWaveCommandParameter> getParamList(){
		List<JWaveCommandParameter> pList = new ArrayList<JWaveCommandParameter>();
		for (JWaveCommandParameter p : params.keySet()){
			pList.add(p);
		}
		return pList;
	}	
	
	
	
	public byte[] toByteArray(){
		
		int len = 0;
		for (Entry<JWaveCommandParameter,byte[]> entry : params.entrySet()){
			//len += JWaveCommandParameterType.getSize(entry.getKey().getType());
			if (entry.getValue() != null){
				len += entry.getValue().length;
			} else {
				// FIXME shouldn't happen
				
			}
		}
		
		byte[] bytes = new byte[len+2];
		bytes[0] = (byte)cmdClass.getKey();
		bytes[1] = (byte)cmd.getKey();
		if (len > 0){
			int index = 2;
			byte[] tmp;
			for (int i = 0; i< params.size(); i++){
				tmp = getParamValue(i);
				if (tmp != null){
					index = addBytes(tmp,bytes,index);
				} else {
					// FIXME throw Exception
				}
			}			
		}
		
		
		return bytes;
	}
	
	
	protected int addBytes(byte[] value, byte[] buffer, int index){
		
		for (int i = 0; i<value.length; i++){
			buffer[index+i] = value[i]; 
		}
		
		return index+value.length;
	}
	
	public boolean isReceived(){
		return received;
	}
	
	
	
}
