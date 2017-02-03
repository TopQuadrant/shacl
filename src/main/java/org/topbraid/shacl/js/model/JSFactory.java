package org.topbraid.shacl.js.model;

import java.util.Map;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

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
