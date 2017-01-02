package de.smahoo.jwave.simulation;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * 
 * @author mathias.runge@domoone.de
 *
 */
public class JWaveStreamReplyRule extends JWaveAbstractStreamRule{

	
	List<String> replyStreams = null;
	int currentReply = -1;
	
	public JWaveStreamReplyRule(String conditionStr){
		super(conditionStr);
		replyStreams = new ArrayList<String>();
	}
	
	public void addReaction(String bytes){
		replyStreams.add(bytes);
	}
	
	public List<String> getReactionBytesList(){
		return replyStreams;
	}
	
	
	public String getFirst(){
		currentReply = -1;
		return getNext();
	}
	
	public boolean hasNext(){
		return (replyStreams.size()-1 > currentReply);
	}
	
	public String getNext(){
		if (hasNext()){
			currentReply++;
			return replyStreams.get(currentReply);
		}
		return null;
	}
}
