package de.smahoo.jwave.utils.logger;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Standard implementation of Logger. Everything will be printed on the default
 * output. 
 * 
 * @author Mathias Runge (mathias.runge@smahoo.de)
 *
 */
public class LoggerConsolePrinter implements Logger{

	protected String name;
	
	public LoggerConsolePrinter(String name){
		this.name = name;
	}
	
	@Override
	public void log(LogTag tag, String message) {
		print(tag,message,null);
		
	}

	@Override
	public void log(LogTag tag, String message, Throwable throwable) {
		print(tag,message,throwable);
		
	}
	
	public String getName(){
		return name;
	}
	
	
	protected void print(LogTag tag, String message,Throwable throwable){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");			
		System.out.println(formatter.format(new Date())+" | "+getName()+" | "+tag.name()+" | "+message);
		if (throwable != null){
			throwable.printStackTrace();
		}
	}
	
	
}
