package de.smahoo.jwave.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import de.smahoo.jwave.JWaveController;
import de.smahoo.jwave.JWaveErrorHandler;
import de.smahoo.jwave.utils.logger.LogTag;

public class JWaveDatagramTransceiver {

	private boolean waitingForAck = false;
	private boolean waitingForRes = false;
	private DataReceiver dataReceiver 	= null;
	private OutputStream outputStream 	= null;
	private JWaveDatagramTransceiverListener datagramListener = null;
	private ArrayBlockingQueue<JWaveDatagram> datagramQueue = null;	 
	private JWaveDatagram currentDatagramToSend = null;
	private JWaveDatagram datagramToSendWhenCanReceived = null;
	private int currSendCnt = 0;
	private byte currFuncId = 2;
	private JWaveErrorHandler errorHandler = null;
	//private JWaveDatagram latestReceivedDatagram = null;
	
	/**
	 * Do not use this constructor! It's for testing only.
	 */
	public JWaveDatagramTransceiver(){
		// for testing only
	}
	
	public JWaveDatagramTransceiver(final InputStream inputStream, final OutputStream outputStream, final JWaveDatagramTransceiverListener listener){
		dataReceiver = new DataReceiver(inputStream, this);
		dataReceiver.start();
		this.outputStream = outputStream;	
		this.datagramListener = listener;
		datagramQueue = new ArrayBlockingQueue<JWaveDatagram>(1024);
	}
	
	
	public void setErrorHandler(JWaveErrorHandler errorHandler){
		this.errorHandler = errorHandler;
	}
	
	public void terminate() throws IOException{				
		dataReceiver.terminate();
		long start = new Date().getTime();
		while (dataReceiver.isAlive()){
			try {				
			  Thread.sleep(10);
			} catch (Exception exc){
				JWaveController.log(LogTag.ERROR,exc.getMessage(),exc);
			}
			if ((new Date().getTime() - start) > 1000){
				throw new IOException("Timout for data receiver termination reached. Unable to terminate datagram receiver correctly");
			}
		}
	}
	
	public byte generateFuncId(){
		if (currFuncId > 250){
			currFuncId = 2;
		} else {
			currFuncId++;
		}
		
		return currFuncId;
	}
	
	public synchronized void send(JWaveDatagram datagram){
		try {
			datagram.setStatus(JWaveDatagramStatus.STATUS_TRANSMITTING);
			datagramQueue.put(datagram);
		} catch (Exception exc){
				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
			// FIXME: handle this exception. datagram will be lost ...
		}	
		
		if ((!isWaitingForAck())&&(!isWaitingForResponse())){
			sendNext();
		}
	}
	
	
	private synchronized boolean isWaitingForResponse(){
		return waitingForRes;
	}
	
	private synchronized boolean isWaitingForAck(){
		return waitingForAck;
	}
	
	
	private synchronized void onAckReceived(){		
		JWaveController.log(LogTag.DEBUG,"      ... Received ACK");
		if (currentDatagramToSend != null){
			currentDatagramToSend.setStatus(JWaveDatagramStatus.STATUS_TRANSMITTED);
			if (datagramListener != null){
				datagramListener.onDatagramTransmitted(currentDatagramToSend);
			}		
			if (!currentDatagramToSend.isRequest()){
				currentDatagramToSend = null;
			}
		}
		waitingForAck = false;
		if (!isWaitingForResponse()){
			sendNext();
		}		
	}
	
	private void sendNext(){
		currSendCnt = 0;
		if (!datagramQueue.isEmpty()){
			currentDatagramToSend = datagramQueue.poll();
			
		}
		sendCurrent();
	}
	
	private synchronized void sendCurrent(){
		
		if (currentDatagramToSend != null){
			try {				
				JWaveController.log(LogTag.DEBUG,"Sending Data: "+currentDatagramToSend.toHexString()+" | "+currentDatagramToSend.getCommandType().name());
				waitingForAck = true;
				waitingForRes = currentDatagramToSend.isRequest();
				currSendCnt++;	
				datagramToSendWhenCanReceived = currentDatagramToSend;
				send(currentDatagramToSend.toByteArray());
			} catch (Exception ioExc){
				JWaveController.log(LogTag.ERROR, ioExc.getMessage(),ioExc);
				// FIXME: handle exception			
			}
		}
	}
	
	private synchronized void onNackReceived(){
		JWaveController.log(LogTag.DEBUG,"      ... Received NACK");
		currentDatagramToSend.setStatus(JWaveDatagramStatus.STATUS_TRANSMISSION_FAILED);
		datagramListener.onDatagramTransmissionFailed(JWaveDatagram.TRANSMISSION_FAILED_NACK,currentDatagramToSend);
	}
	
	private synchronized void onCanReceived(){
		JWaveController.log(LogTag.DEBUG,"      ... Received CAN");
		if (datagramToSendWhenCanReceived != null){			
			currentDatagramToSend = datagramToSendWhenCanReceived;
			if (currSendCnt < 3){
				JWaveController.log(LogTag.DEBUG,"Resending datagram "+datagramToSendWhenCanReceived.toHexString()+" | "+datagramToSendWhenCanReceived.getCommandType().name());
				sendCurrent();
				return;
			} else {
				JWaveController.log(LogTag.WARN,"Transmission failed ("+currentDatagramToSend.toHexString()+")");
				currentDatagramToSend.setStatus(JWaveDatagramStatus.STATUS_TRANSMISSION_FAILED);
				datagramListener.onDatagramTransmissionFailed(JWaveDatagram.TRANSMISSION_FAILED_CAN,currentDatagramToSend);
			}
			
		}
		waitingForAck = false;
		waitingForRes = false;
		currentDatagramToSend = null;	
		sendNext();
			
		// FIXME: handle this shit
	}
	
	
	
	private void sendAck(){		
		try {
			JWaveController.log(LogTag.DEBUG,"send ACK");
			outputStream.write(0x06);
			outputStream.flush();
		} catch (IOException ioExc){
			JWaveController.log(LogTag.ERROR, ioExc.getMessage(),ioExc);
			// FIXME: handle exception
		}
	}
	
	private void send(byte[] data) throws IOException{
		try {
			Thread.sleep(100);
		} catch (Exception exc){
			JWaveController.log(LogTag.ERROR,"Unable to wait before sending bytes,",exc);
		}
		outputStream.write(data);
		outputStream.flush();
		
	}
	
	
	private synchronized void onDatagramReceived(final JWaveDatagram datagram){
		datagram.status = JWaveDatagramStatus.STATUS_RECEIVED;
		
		JWaveController.log(LogTag.DEBUG,"      ... Received "+datagram.toHexString()+" | "+datagram.getCommandType().name());
		sendAck();
		
		if (datagramListener != null){
			// should be threaded
			
			// HINT: to many listeners could result in to many threads
			Thread t = new Thread() {
				
				@Override
				public void run() {
					datagramListener.onDatagramReceived(datagram);
					
				}
			};
			t.start();
			
		}
	
//		if (latestReceivedDatagram != null){			
//			if (latestReceivedDatagram.toHexString().equalsIgnoreCase(datagram.toHexString())){
//				Logger.print("!!! received same datagram more than one time, just sending Ack and do not further process datagram evaluation");
//				latestReceivedDatagram = datagram;
//				return;
//			}
//		}
//		
//		latestReceivedDatagram = datagram;
		if (isWaitingForResponse()){		
			waitingForRes = false;
			if (currentDatagramToSend != null){
				currentDatagramToSend.setResponse(datagram);
				if (datagramListener != null){
					datagramListener.onDatagramResponse(currentDatagramToSend, datagram);
				}
			}
			currentDatagramToSend = null;
			sendNext();
		}		
		
	}
	
	
	
	private void onSerialIOError(Exception exc){
		dataReceiver.terminate();
		if (errorHandler != null){
			errorHandler.onError(exc.getMessage(), exc);
		}
	}
	
	public class DataReceiver extends Thread{
		
		private JWaveDatagramTransceiver transceiver= null;
		private InputStream in = null;
		private boolean terminate = false;		
		
		public DataReceiver(InputStream inputStream,  JWaveDatagramTransceiver transceiver){
			this.transceiver = transceiver;
			this.in = inputStream;
		}
		
		public synchronized boolean doTermination(){
			return terminate;
		}
		
		public synchronized void terminate(){
			this.terminate = true;			
		}
		
		public void run(){
		   	int read;
	       	int length;
	       	int readbytes;
	       	byte[] buffer = new byte[1024];	    
	      
	       	
	       	while (!doTermination()) {
	       		try {
	       			if (in.available() > 0){
	       				if ((read = in.read()) != -1){
	       					switch (read) {
	       					case 0x06:	            			
	       						transceiver.onAckReceived();
	       						break;
	       					case 0x15:	            			
	       						transceiver.onNackReceived();
	       						break;
	       					case 0x18:	            			
	       						transceiver.onCanReceived();
	       						break;
	       					case 0x01:	       						
	       						// FIXME: could block
	       						length = in.read();
	       						
	       						// do not forget to add the first tow bytes which are already read
	       						buffer[0] = 0x01;
	       						buffer[1] = (byte)length;	            			
	       						// now read the next expected bytes
	       						
	       						readbytes = 2+ in.read(buffer,2,length);   
	       						// in case that not all bytes are received, read as long as all expected bytes are transmitted
	       						while (readbytes < length+2){
	       							readbytes = readbytes + in.read(buffer, readbytes,length+2-readbytes);            					
	       						}   
	       						// all bytes are received, try to generate a datagram and inform the transceiver
	       						// FIXME: buffer might contain corrupted data -> JWaveDatagramFactory.generateDatagram should throw parsing exception
	       						try {
	       							transceiver.onDatagramReceived(JWaveDatagramFactory.generateDatagram(buffer,0,length+2));
	       						} catch (Exception genExc){
	       							JWaveController.log(LogTag.ERROR, genExc.getMessage(),genExc);
	       						}	       					
	       						break;
	       					}
	       				}
	            	 
	            	} else {	            		
	            		// in.available is non blocking. To prevent 100% CPU load to a short sleep 
	            		Thread.sleep(50);
	            	}
	       		}  catch ( IOException exc ) {
	       			// FIXME: handle exception, e.g. send NACK in case that datagram was corrupted
	       			if (!terminate){
	       				JWaveController.log(LogTag.ERROR, exc.getMessage(),exc);
	       				onSerialIOError(exc);
	       			}
	            } catch (InterruptedException exc){
	            	// Do noting
	            } catch (Exception exc){
	            	// Any other exception
					 JWaveController.log(LogTag.ERROR,"Exception during IO-read : "+exc.getMessage(),exc);

				}

	       		
	       	}	
	       	
		}
		
	} // End of class DataReceiver

	
	
	
	
	

}
