package de.smahoo.jwave.utils.xml;

import org.w3c.dom.Element;


/**
 * @author Mathias Runge (mathias.runge@smahoo.de)
 */
public class XmlConvertionException extends Exception{

	Element elem = null;
	
	public XmlConvertionException(){
		this(null,null,null);
	}
	
	public XmlConvertionException(String msg){
		this(msg,null,null);
	}
	
	public XmlConvertionException(String msg, Element elem){
		this(msg,elem,null);
	}
	
	public XmlConvertionException(String msg, Throwable throwable){
		this(msg,null,throwable);
	}
	
	public XmlConvertionException(String msg, Element elem, Throwable throwable){
		super(msg,throwable);
		this.elem = elem;
	}
	
	public boolean hasXmlElement(){
		return elem != null;
	}
	
	public Element getXmlElement(){
		return elem;
	}
	
}
