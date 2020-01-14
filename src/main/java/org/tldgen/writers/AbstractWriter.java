package org.tldgen.writers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.event.SaxonOutputKeys;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Parent class of any library writer
 * @author icoloma
 *
 */
public abstract class AbstractWriter {
	
	/** TLD File ident */
	private String indentSpaces = "4";
	
	/** true to prettify output */
	private boolean formatOutput = false;

	private static Logger log = LoggerFactory.getLogger(AbstractWriter.class);
	
	static {
		// set saxon as the default XSLT engine
		System.setProperty("javax.xml.transform.TransformerFactory", TransformerFactoryImpl.class.getName());
	}

	/**
	 * Prettyprints the XML contents to the specified file
	 */
	protected void formatAndWriteXml(String xml, String filename) {
		try {

			log.info("Formatting XML and writing to file " + filename);
			if (formatOutput) {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
				transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,"-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN");
				transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,"http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd");
				//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", tabSpaces);
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty(OutputKeys.METHOD, "xml");
				transformer.setOutputProperty(SaxonOutputKeys.INDENT_SPACES /*"{http://saxon.sf.net/}indent-spaces" */, indentSpaces);
				InputSource input = new InputSource(new StringReader(xml));

				Document document = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder()
						.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())));

				transformer.transform(new SAXSource(input), new StreamResult(new File(filename)));
				return;
			}
		} catch (TransformerException e) {
			log.warn("Error indenting output for '" + filename + "'. Either set formatOutput=false or check that javadoc contains only well-formed XML. The file will be saved as-is (" + e.getMessageAndLocation() + ")");
			log.debug(e.getMessageAndLocation() + "\n" + xml);
		} catch (Exception e) {

		}

		try {
			FileUtils.writeStringToFile(new File(filename), xml);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return;

	}
	/*
	protected void formatAndWriteXml(String xml, String filename) throws XMLStreamException {
		Tidy tidy = new Tidy();
        tidy.setXHTML(true);
        tidy.setQuiet(true);
        tidy.setSmartIndent(true);
        tidy.setSpaces(2);
        tidy.setTabsize(1);
        tidy.setDropEmptyParas(true);
        tidy.setWraplen(200);
        tidy.setOnlyErrors(true);
        
        FileOutputStream out = null;
        try {
			out = new FileOutputStream(filename);
			tidy.parse(new ByteArrayInputStream(xml.getBytes()), out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(out);
		}

	}*/

	public void setIndentSpaces(String tabSpaces) {
		this.indentSpaces = tabSpaces;
	}

	public void setFormatOutput(boolean prettyPrint) {
		this.formatOutput = prettyPrint;
	}
	
}
