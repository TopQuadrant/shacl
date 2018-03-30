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
package org.topbraid.shacl.js.model;

import java.util.Map;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.jenax.util.JenaDatatypes;

public class JSFactory {
	
	public static final String BLANK_NODE = "BlankNode";

	public static final String DATATYPE = "datatype";
	
	public static final String LANGUAGE = "language";
	
	public static final String LITERAL = "Literal";
	
	public static final String NAMED_NODE = "NamedNode";
	
	public static final String TERM_TYPE = "termType";
	
	public static final String VALUE = "value";

	
	@SuppressWarnings("rawtypes")
	public static Node getNode(Object obj) {
		if(obj == null) {
			return null;
		}
		else if(obj instanceof JSTerm) {
			return ((JSTerm)obj).getNode();
		}
		else if(obj instanceof Map) {
			Map som = (Map) obj;
			String value = (String) som.get(VALUE);
			if(value == null) {
				throw new IllegalArgumentException("Missing value");
			}
			String termType = (String) som.get(TERM_TYPE);
			if(NAMED_NODE.equals(termType)) {
				return NodeFactory.createURI(value);
			}
			else if(BLANK_NODE.equals(termType)) {
				return NodeFactory.createBlankNode(value);
			}
			else if(LITERAL.equals(termType)) {
				String lang = (String) som.get(LANGUAGE);
				Map dt = (Map)som.get(DATATYPE);
				String dtURI = (String)dt.get(VALUE);
				RDFDatatype datatype = TypeMapper.getInstance().getSafeTypeByName(dtURI);
				return NodeFactory.createLiteral(value, lang, datatype);
			}
			else {
				throw new IllegalArgumentException("Unsupported term type " + termType);
			}
		}
		else {
			return null;
		}
	}

	
	public static Node getNodeFlex(Object obj) {
		Node fromTerm = getNode(obj);
		if(fromTerm != null) {
			return fromTerm;
		}
		else if(obj instanceof Integer) {
			return JenaDatatypes.createInteger((Integer)obj).asNode();
		}
		else if(obj instanceof Number) {
			return NodeFactory.createLiteral(obj.toString(), TypeMapper.getInstance().getSafeTypeByName(XSD.decimal.getURI()));
		}
		else if(obj instanceof Boolean) {
			return ((Boolean)obj) ? JenaDatatypes.TRUE.asNode() : JenaDatatypes.FALSE.asNode();
		}
		else if(obj != null) {
			return NodeFactory.createLiteral(obj.toString());
		}
		else {
			return null;
		}
	}
	
	
	public static Node getNodeSafe(Object obj) {
		try {
			return getNode(obj);
		}
		catch(Exception ex) {
			return null;
		}
	}
	
	
	public static JSTerm asJSTerm(Node node) {
		if(node.isURI()) {
			return new JSNamedNode(node);
		}
		else if(node.isBlank()) {
			return new JSBlankNode(node);
		}
		else if(node.isLiteral()) {
			return new JSLiteral(node);
		}
		else {
			throw new IllegalArgumentException("Unsupported node type " + node);
		}
	}
	
	
	public static JSTriple asJSTriple(Triple triple) {
		return new JSTriple(triple);
	}
}
