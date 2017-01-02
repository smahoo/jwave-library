package de.smahoo.jwave.cmd.report;

import de.smahoo.jwave.cmd.JWaveCommand;
import de.smahoo.jwave.cmd.JWaveCommandClass;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public abstract class JWaveReport {

	protected JWaveNodeCommand nodeCmd = null;
	
	public JWaveReport(JWaveNodeCommand cmd){
		this.nodeCmd = cmd;
		evaluate(cmd);
	}
	
	public int getCommandClassKey(){
		return nodeCmd.getCommandClassKey();
	}
	
	public int getCommandKey(){
		return nodeCmd.getCommandKey();
	}
	
	public String getCommandClassName(){
		return nodeCmd.getCommandClass().getName();
	}
	
	public String getCommandName(){
		return nodeCmd.getCommand().getName();
	}
	
	public JWaveCommandClass getCommandClass(){
		return nodeCmd.getCommandClass();
	}
	
	public JWaveCommand getCommand(){
		return nodeCmd.getCommand();
	}
	
	abstract protected void evaluate(JWaveNodeCommand nodeCmd);
	
}
