package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.Collection;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveMultiNodeCommand extends JWaveNodeCommand {
	protected Collection<JWaveNodeCommand> cmds = null;
	
	public JWaveMultiNodeCommand(JWaveCommand cmd){
		super(cmd);
		cmds = new ArrayList<JWaveNodeCommand>();
		init();
	}
	
	public JWaveMultiNodeCommand(JWaveCommand cmd, Collection<JWaveNodeCommand> cmds){
		this(cmd);
		for (JWaveNodeCommand c : cmds){
			addJWaveNodeCmd(c);
		}
		
	}	
	
	public void addJWaveNodeCmd(JWaveNodeCommand cmd){
		cmds.add(cmd);
		init();
	}
	
	public Collection<JWaveNodeCommand> getNodeCmdList(){
		return cmds;
	}
	
	protected void init(){		
		try {
			if (cmds != null){
				this.setParamValue(0,cmds.size());
				this.setParamValue(1,generateVariableGroupParamBytes());
			}
			
		} catch (Exception exc){			
			JWaveController.log(LogTag.ERROR,"unable to init Variable_Group Parameter Values",exc);
		}
	}
	
	
	protected byte[] generateVariableGroupParamBytes(){
		byte[] result = new byte[0];
		
		for (JWaveNodeCommand cmd : cmds){
			result = combine(result,generateSubCmdBytes(cmd));
		}		
		return result;
	}
	
	protected byte[] generateSubCmdBytes(JWaveNodeCommand cmd){
		byte[] cmdBytes = cmd.toByteArray();
		byte[] result = new byte[cmdBytes.length + 1];
		result[0] = (byte)cmdBytes.length;
		
		for (int i = 0; i< cmdBytes.length; i++){
			result[i+1] = cmdBytes[i];
		}
		return result;
	}
	
	protected byte[] combine(byte[] first, byte[] last){
		byte[] result = new byte[first.length+last.length];
		for (int i = 0; i< first.length; i++){
			result[i] = first[i];
		}
		for (int i = 0; i< last.length; i++){
			result[i+first.length] = last[i];
		}
		return result;
	}
}
