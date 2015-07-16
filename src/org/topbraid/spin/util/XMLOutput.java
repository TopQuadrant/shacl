/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved.
 *******************************************************************************/
package org.topbraid.spin.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;

/**
 * Simple utilities for XML.
 * 
 * @author Jeremy Carroll
 */
public class XMLOutput {

	private static final String DEFAULT_METHOD = "xml";
	private static final String DEFAULT_INDENT = "2";
	public static boolean USE_SAXON = false;
	/**
	 * TopBraid code should always use this transformer factory which is public for 
	 * that intent.
	 */
	private static final TransformerFactory xformFactory;
	static {
		TransformerFactory xf = null;
		if (USE_SAXON) {
			try {
				xf = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl",null);
			    System.err.println("Using saxon");
			} catch (TransformerFactoryConfigurationError e) {
				System.err.println("Failed to load Saxon, using Xalan: "+e.getMessage());
			}
		}
		if (xf==null) {
		  xf = TransformerFactory.newInstance();
		}
		xformFactory = xf;
	}
	public static TransformerFactory getTransformerFactory() {
		return xformFactory;
	}

	/**
	 * Serializes an XML Node to an OutputStream (as UTF-8).
	 * @throws IOException
	 */
	public static void printNode(Node node, OutputStream out) throws IOException{
		printNode(node, new StreamResult(out));
		out.write('\n');
		out.flush();
	}


	/**
	 * Serializes an XML Node to a String.
	 * @return The Node as a string
	 * @throws IOException
	 */
	public static String toString(Node node) throws IOException {
		return toString(node, DEFAULT_INDENT, DEFAULT_METHOD);
	}

	
	public static String toString(Node node, String indent, String method) throws IOException {
		StringWriter rslt = new StringWriter();
		printNode(node, new StreamResult(rslt), indent, method);
		return rslt.toString();
	}

	
	/**
	 * Serializes an XML Node to a Writer.
	 * @throws IOException
	 */
	public static void printNode(Node node, Writer pw) throws IOException {
		printNode(node, new StreamResult(pw));
		pw.write('\n');
		pw.flush();
	}


	private static void printNode(Node node, StreamResult streamResult) throws IOException {
		printNode(node, streamResult, DEFAULT_INDENT, DEFAULT_METHOD);
	}
	

	private static void printNode(Node node, StreamResult streamResult, String indent, String method) throws IOException {
		Transformer xform = null;
		try {
			xform = xformFactory.newTransformer();
			xform.setOutputProperty(OutputKeys.INDENT, "yes");
			xform.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent);
			xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			xform.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			xform.setOutputProperty(OutputKeys.METHOD, method);
			// Setting the document type - fix for bug 5878
			if( node instanceof Document ) {
				DocumentType docType = ((Document) node).getDoctype();
				if( docType != null ) {
					String publicId = docType.getPublicId();
					String systemId = docType.getSystemId();
					if( publicId != null && systemId != null ) {
						xform.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
						xform.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
					}
				}
			}
		}
		catch (TransformerConfigurationException e) {
			throw ExceptionUtil.throwRootCauseUnchecked(e);
		}
		try {
			xform.transform(new DOMSource(node), streamResult);
		}
		catch (TransformerException e) {
			throw ExceptionUtil.throwDeepCauseChecked(e, IOException.class);
		}
	}

	
	/**
	 * Serializes an XML Node as a byte array (as UTF-8)
	 * @param node  the XML Node to convert
	 * @return the result byte array
	 * @throws IOException
	 */
	public static byte[] toByteArray(Node node) throws IOException {
		ByteArrayOutputStream rslt = new ByteArrayOutputStream();
		printNode(node,new StreamResult(rslt));
		return rslt.toByteArray();
	}
}
