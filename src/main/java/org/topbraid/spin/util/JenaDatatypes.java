/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.spin.vocabulary.RDFx;

import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.XSD;


/**
 * Some static utilities dealing with datatypes and literals.
 * 
 * @author Holger Knublauch
 */
public class JenaDatatypes {
	
	public static final Literal FALSE = ResourceFactory.createTypedLiteral("false", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.getURI()));
	
	public static final Literal TRUE = ResourceFactory.createTypedLiteral("true", TypeMapper.getInstance().getSafeTypeByName(XSD.xboolean.getURI()));

	private static Set<String> numericDatatypeURIs = new HashSet<String>();

	private static Set<String> otherDatatypeURIs = new HashSet<String>();

	static {
		numericDatatypeURIs.add(XSD.decimal.getURI());
		numericDatatypeURIs.add(XSD.duration.getURI());
		numericDatatypeURIs.add(XSD.gDay.getURI());
		numericDatatypeURIs.add(XSD.gMonth.getURI());
		numericDatatypeURIs.add(XSD.gMonthDay.getURI());
		numericDatatypeURIs.add(XSD.gYear.getURI());
		numericDatatypeURIs.add(XSD.gYearMonth.getURI());
		numericDatatypeURIs.add(XSD.integer.getURI());
		numericDatatypeURIs.add(XSD.negativeInteger.getURI());
		numericDatatypeURIs.add(XSD.nonNegativeInteger.getURI());
		numericDatatypeURIs.add(XSD.nonPositiveInteger.getURI());
		numericDatatypeURIs.add(XSD.positiveInteger.getURI());
		numericDatatypeURIs.add(XSD.unsignedByte.getURI());
		numericDatatypeURIs.add(XSD.unsignedInt.getURI());
		numericDatatypeURIs.add(XSD.unsignedLong.getURI());
		numericDatatypeURIs.add(XSD.unsignedShort.getURI());
		numericDatatypeURIs.add(XSD.xbyte.getURI());
		numericDatatypeURIs.add(XSD.xdouble.getURI());
		numericDatatypeURIs.add(XSD.xfloat.getURI());
		numericDatatypeURIs.add(XSD.xint.getURI());
		numericDatatypeURIs.add(XSD.xlong.getURI());
		numericDatatypeURIs.add(XSD.xshort.getURI());
	}

	static {
		otherDatatypeURIs.add(XSD.anyURI.getNameSpace() + "anySimpleType");
		otherDatatypeURIs.add(XSD.anyURI.getURI());
		otherDatatypeURIs.add(XSD.base64Binary.getURI());
		otherDatatypeURIs.add(XSD.date.getURI());
		otherDatatypeURIs.add(XSD.dateTime.getURI());
		otherDatatypeURIs.add(XSD.ENTITY.getURI());
		otherDatatypeURIs.add(XSD.hexBinary.getURI());
		otherDatatypeURIs.add(XSD.ID.getURI());
		otherDatatypeURIs.add(XSD.IDREF.getURI());
		otherDatatypeURIs.add(XSD.language.getURI());
		otherDatatypeURIs.add(XSD.Name.getURI());
		otherDatatypeURIs.add(XSD.NCName.getURI());
		otherDatatypeURIs.add(XSD.NMTOKEN.getURI());
		otherDatatypeURIs.add(XSD.normalizedString.getURI());
		otherDatatypeURIs.add(XSD.NOTATION.getURI());
		otherDatatypeURIs.add(XSD.QName.getURI());
		otherDatatypeURIs.add(XSD.time.getURI());
		otherDatatypeURIs.add(XSD.token.getURI());
		otherDatatypeURIs.add(XSD.xboolean.getURI());
		otherDatatypeURIs.add(XSD.xstring.getURI());
		otherDatatypeURIs.add(RDFx.HTML.getURI());
		otherDatatypeURIs.add(RDF.getURI() + "XMLLiteral");
	}

	
	public static Literal createInteger(int value) {
		return ResourceFactory.createTypedLiteral("" + value, TypeMapper.getInstance()
				.getSafeTypeByName(XSD.integer.getURI()));
	}


	/**
	 * Gets a List of all datatype URIs.
	 * @return a List the datatype URIs
	 */
	public static List<String> getDatatypeURIs() {
		List<String> list = new ArrayList<String>();
		list.addAll(otherDatatypeURIs);
		list.addAll(numericDatatypeURIs);
		list.add(RDFx.PlainLiteral.getURI());
		return list;
	}


	/**
	 * Checks if a given URI is a numeric datatype URI.
	 * @param datatypeURI  the URI of the datatype to test
	 * @return true if so
	 */
	public static boolean isNumeric(String datatypeURI) {
		return numericDatatypeURIs.contains(datatypeURI);
	}


	/**
	 * Checks if a given RDFNode represents a system XSD datatype such as xsd:int.
	 * Note: this will not return true on user-defined datatypes or rdfs:Literal.
	 * @param node  the node to test
	 * @return true if node is a datatype
	 */
	public static boolean isSystemDatatype(RDFNode node) {
		if (node instanceof Resource && node.isURIResource()) {
			String uri = ((Resource)node).getURI();
			return isNumeric(uri) || otherDatatypeURIs.contains(uri);
		}
		else {
			return false;
		}
	}
}
