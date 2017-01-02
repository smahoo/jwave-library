package de.smahoo.jwave.cmd.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.smahoo.jwave.cmd.JWaveCommandClass;
import de.smahoo.jwave.cmd.JWaveCommandClassSpecification;
import de.smahoo.jwave.cmd.JWaveNodeCommand;

public class JWaveReportSecurityCommandsSupported extends JWaveReport{

	byte[] cmdClassesSupported;
	byte[] cmdClassesControled;
	
	int reportsToFollow;
	
	public JWaveReportSecurityCommandsSupported(JWaveNodeCommand cmd) {
		super(cmd);	
	}

	@Override
	protected void evaluate(JWaveNodeCommand nodeCmd) {
		// 00 98 03 00 72 86 98 62 4c 4e 63 8b 85 71 70 75 80 8a ef		
		byte[] bytes = nodeCmd.getParamValues();
		
		reportsToFollow = bytes[0];
		int markerPos = -1;
		for (int i= 1; i< bytes.length-1; i++){
			if (bytes[i] == 0x0E){
				markerPos = i;
			}
		}
		if (markerPos == -1){
			cmdClassesControled = new byte[1];
			markerPos = bytes.length;
		}
		cmdClassesSupported = Arrays.copyOfRange(bytes, 1, markerPos);		
	}

	
	public int getReportsToFollow(){
		return reportsToFollow;
	}
	
	public byte[] getAllCommandClassKeys(){
		byte[] all = new byte[cmdClassesControled.length+cmdClassesSupported.length];
		System.arraycopy(cmdClassesSupported,0,all,0,cmdClassesSupported.length);
		System.arraycopy(cmdClassesControled,0, all,cmdClassesSupported.length,cmdClassesControled.length);
		return all;
	}
	
	public byte[] getSecuritySupportedCommandClassKeys(){
		return Arrays.copyOf(cmdClassesSupported, cmdClassesSupported.length);
	}
	
	public byte[] getSecurityControledCommandClassKeys(){
		return Arrays.copyOf(cmdClassesControled, cmdClassesControled.length);
	}
	
	public Collection<JWaveCommandClass> getAllCommandClasses(JWaveCommandClassSpecification spec){
		List<JWaveCommandClass> classList = new ArrayList<JWaveCommandClass>();
		classList.addAll(this.getControledCommandClasses(spec));
		classList.addAll(this.getSupportedCommandClasses(spec));
		return classList;
	}
	
	public Collection<JWaveCommandClass> getSupportedCommandClasses(JWaveCommandClassSpecification spec){
		List<JWaveCommandClass> classList = new ArrayList<JWaveCommandClass>();
		JWaveCommandClass tmp = null;
		for (int i = 0; i< cmdClassesSupported.length-1; i++){
			tmp = spec.getCommandClass(cmdClassesSupported[i] & 0xFF);
			if (tmp != null){
				classList.add(tmp);
			} else {
				// TODO
				// Do not what to do know. It returns the command classes according the the given spec
				// if the spec doesn't contain the command class, what to do? throwing an exception 
				// feels wrong
			}
		}
		return classList;
	}
	
	public Collection<JWaveCommandClass> getControledCommandClasses(JWaveCommandClassSpecification spec){
		List<JWaveCommandClass> classList = new ArrayList<JWaveCommandClass>();
		JWaveCommandClass tmp = null;
		for (int i = 0; i< cmdClassesControled.length-1; i++){
			tmp = spec.getCommandClass(cmdClassesControled[i] & 0xFF);
			if (tmp != null){
				classList.add(tmp);
			} else {
				// TODO
				// Do not what to do know. It returns the command classes according the the given spec
				// if the spec doesn't contain the command class, what to do? throwing an exception 
				// feels wrong
			}
		}
		return classList;
	}
	
	
}
