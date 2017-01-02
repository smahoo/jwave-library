package de.smahoo.jwave.test.utils;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.smahoo.jwave.utils.xml.XmlConvertionException;
import de.smahoo.jwave.utils.xml.XmlUtils;

public class TestXmlUtils {

	protected String testFolder = null;
	protected String sep = null;
	
	@Before
	public void init() {
		sep = System.getProperty("file.separator");
		String folder = System.getProperty("user.dir");
		testFolder = folder+sep+"test"+sep+"persistence"+sep;
		File f = new File(testFolder);
		if (f.exists()){
			deleteFile(f);
		}
		f.mkdirs();
	}
	
	@After
	public void cleanUp(){
		deleteFile(new File(testFolder));
	}
	
		
	/**
	 * Testing the generation, persistence, parsing
	 * This tests covers the complete XMLUtils functionality
	 */
	@Test
	public void testXml_postive() throws IOException, XmlConvertionException{
		Document doc = XmlUtils.createDocument();
		assertNotNull(doc);
		Element root = doc.createElement("root");
		Element elem = doc.createElement("test");
		elem.setAttribute("attrname", "attrvalue");
		
		root.appendChild(elem);
		
		doc.appendChild(root);
		File xmlFile = new File(testFolder+"test.xml");
		assertNotNull(xmlFile);
		
		
		XmlUtils.saveXml(xmlFile, doc);
		
		
		Document doc2 = null;
		doc2 = XmlUtils.loadXml(xmlFile);
		
		
		assertNotNull(doc2);
		Element rootResult = doc2.getDocumentElement();
		
		assertNotNull(rootResult);
		assertEquals(rootResult.getTagName(),"root");
		assertTrue(rootResult.hasChildNodes());
		
		NodeList nodeList = rootResult.getChildNodes();
		int elemCnt = 0;
		
		Element elemResult = null;
		for (int i = 0; i<nodeList.getLength(); i++){
			if (nodeList.item(i) instanceof Element){
				elemResult = (Element)nodeList.item(i);
				elemCnt++;
			}
		}
		assertEquals(elemCnt,1);
		assertNotNull(elemResult);
		assertTrue(elem.hasAttribute("attrname"));
		assertEquals(elem.getAttribute("attrname"),"attrvalue");
		
	}
	
	
	
	
	@Test
	public void testloadXml_negative_noFile(){
		Document doc = XmlUtils.createDocument();
		assertNotNull(doc);
		Element root = doc.createElement("root");
		Element elem = doc.createElement("test");
		elem.setAttribute("attrname", "attrvalue");
		
		root.appendChild(elem);
		
		doc.appendChild(root);
		File xmlFile = new File(testFolder+"test.xml");
		assertNotNull(xmlFile);
		
		try {
			XmlUtils.saveXml(xmlFile, doc);
		} catch (Exception exc){
			fail("Unable to save Xml."+exc.getMessage());
		}
		
		xmlFile = new File(testFolder+"test_fail.xml");
		
		
		boolean exceptionThrown = false;
		try {
			XmlUtils.loadXml(xmlFile);
		} catch (Exception exc){
			exceptionThrown = true;
			assertTrue(exc instanceof IOException);			
		}
		
		assertTrue(exceptionThrown);						
	}
	
	
	
	@Test
	public void testloadXml_negative_noXmlContent(){
		File xmlFile = new File(testFolder+"test.xml");
		String xmlText = "this is no xml file content";
		assertTrue(saveToFile(xmlText,xmlFile));
		
		boolean exceptionThrown = false;		
		try {
			XmlUtils.loadXml(xmlFile);
		} catch (Exception exc){			
			exceptionThrown = true;
			assertTrue(exc instanceof IOException);			
		}
		
		assertTrue(exceptionThrown);		
	}
	
	
	
	@Test
	public void test_XmlConvertionException(){
		XmlConvertionException exc = new XmlConvertionException();
		assertNotNull(exc);
		assertNull(exc.getMessage());
		assertNull(exc.getXmlElement());
		assertFalse(exc.hasXmlElement());
		
		exc = new XmlConvertionException("message");
		assertNotNull(exc);
		assertNotNull(exc.getMessage());
		assertEquals(exc.getMessage(),"message");
		assertFalse(exc.hasXmlElement());
		
		
		exc = new XmlConvertionException("message",new XmlConvertionException());
		assertNotNull(exc);
		assertNotNull(exc.getMessage());
		assertEquals(exc.getMessage(),"message");
		assertFalse(exc.hasXmlElement());
		
		Document doc = XmlUtils.createDocument();
		assertNotNull(doc);
		Element elem = doc.createElement("test");
		exc = new XmlConvertionException("message", elem);
		assertNotNull(exc);
		assertNotNull(exc.getMessage());
		assertEquals(exc.getMessage(),"message");
		assertTrue(exc.hasXmlElement());
		assertEquals(exc.getXmlElement(),elem);			
	}
		
	
	//// HELPER  
	
	protected void deleteFile(File f){
		if (!f.exists());
		if (f.isDirectory()){
			File[] childs = f.listFiles();
			for (int i = 0; i<childs.length; i++){
				deleteFile(childs[i]);
			}
		}
		f.delete();
	}
	
	protected boolean saveToFile(String text, File file){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(text);   
			writer.close();
		} catch (Exception exc){
			return false;
		}
		return true;
	}

}
