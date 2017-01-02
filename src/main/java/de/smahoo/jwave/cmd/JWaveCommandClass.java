package de.smahoo.jwave.cmd;

import java.util.ArrayList;
import java.util.List;

public class JWaveCommandClass {

	protected int key;
	protected int version;
	protected String name;
	protected String help; 
	protected String comment;
	protected boolean readOnly;
	protected List<JWaveCommand> cmdList;
	
	protected JWaveCommandClass(){
		cmdList = new ArrayList<JWaveCommand>();
	}
	
	public JWaveCommand getCommand(int key){
		for (JWaveCommand cmd : cmdList){
			if (cmd.getKey() == key){
				return cmd;
			}
		}
		return null;
	}
	
	public JWaveCommand getCommand(String cmdName){
		for (JWaveCommand cmd : cmdList){
			if (cmd.getName().equalsIgnoreCase(cmdName)){
				return cmd;
			}
		}
		return null;
	}
	
	public List<JWaveCommand> getCommandList(){
		return cmdList;
	}
	
	public int getKey() {
		return key;
	}
	public int getVersion() {
		return version;
	}
	public String getName() {
		return name;
	}
	public String getHelp() {
		return help;
	}
	public String getComment() {
		return comment;
	}
	public boolean isReadOnly() {
		return readOnly;
	}
	
}
