/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.topbraid.jenax.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.XSD;


/**
 * Some static utilities dealing with datatypes and literals.
 * 
 * @author Holger Knublauch
 */
public class JenaDatatypes {
	
	public static final Literal FALSE = ResourceFactory.createTypedLiteral("false", XSDDatatype.XSDboolean);
	
	public static final Literal TRUE = ResourceFactory.createTypedLiteral("true", XSDDatatype.XSDboolean);

	private static Set<String> floatDatatypeURIs = new HashSet<>();

	private static Set<String> numericDatatypeURIs = new HashSet<>();

	private static Set<String> otherDatatypeURIs = new HashSet<>();

	static {
		floatDatatypeURIs.add(XSD.decimal.getURI());
		floatDatatypeURIs.add(XSD.xdouble.getURI());
		floatDatatypeURIs.add(XSD.xfloat.getURI());
	}

	static {
		numericDatatypeURIs.add(XSD.decimal.getURI());
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
		otherDatatypeURIs.add(XSD.dateTimeStamp.getURI());
		otherDatatypeURIs.add(XSD.duration.getURI());
		otherDatatypeURIs.add(XSD.ENTITY.getURI());
		otherDatatypeURIs.add(XSD.gDay.getURI());
		otherDatatypeURIs.add(XSD.gMonth.getURI());
		otherDatatypeURIs.add(XSD.gMonthDay.getURI());
		otherDatatypeURIs.add(XSD.gYear.getURI());
		otherDatatypeURIs.add(XSD.gYearMonth.getURI());
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
		otherDatatypeURIs.add(RDF.langString.getURI());
		otherDatatypeURIs.add(RDF.HTML.getURI());
		otherDatatypeURIs.add(RDF.JSON.getURI());
		otherDatatypeURIs.add(RDF.xmlLiteral.getURI()) ;
	}

	
	public static Literal createDecimal(int value) {
		return ResourceFactory.createTypedLiteral("" + value, XSDDatatype.XSDdecimal);
	}

	
	public static Literal createInteger(int value) {
		return ResourceFactory.createTypedLiteral("" + value, XSDDatatype.XSDinteger);
	}


	/**
	 * Gets a List of all datatype URIs.
	 * @return a List the datatype URIs
	 */
	public static List<String> getDatatypeURIs() {
		List<String> list = new ArrayList<String>();
		list.addAll(otherDatatypeURIs);
		list.addAll(numericDatatypeURIs);
		return list;
	}


	/**
	 * Checks if a given URI is a numeric floating point datatype URI:
	 * xsd:decimal, xsd:float or xsd:double.
	 * @param datatypeURI  the URI of the datatype to test
	 * @return true if so
	 */
	public static boolean isFloat(String datatypeURI) {
		return floatDatatypeURIs.contains(datatypeURI);
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
