package org.topbraid.shacl.arq;

import java.util.Iterator;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.util.JenaUtil;

/**
 * Utilties to manage the conversion between SHACL paths and SPARQL 1.1 property paths.
 * 
 * @author Holger Knublauch
 */
public class SHACLPaths {
	
	private final static String ALTERNATIVE_PATH_SEPARATOR = "|";
	
	private final static String SEQUENCE_PATH_SEPARATOR = "/";

	
	/**
	 * Renders a given path into a given StringBuffer, using the prefixes supplied by the
	 * Path's Model.
	 * @param sb  the StringBuffer to write into
	 * @param path  the path resource
	 */
	public static void appendPath(StringBuffer sb, Resource path) {
		if(path.isURIResource()) {
			sb.append(FmtUtils.stringForNode(path.asNode(), path.getModel()));
		}
		else {
			appendPathBlankNode(sb, path, SEQUENCE_PATH_SEPARATOR);
		}
	}
	
	
	private static void appendNestedPath(StringBuffer sb, Resource path, String separator) {
		if(path.isURIResource()) {
			sb.append(FmtUtils.stringForNode(path.asNode(), path.getModel()));
		}
		else {
			appendPathBlankNode(sb, path, SEQUENCE_PATH_SEPARATOR);
		}
	}
	
	
	private static void appendPathBlankNode(StringBuffer sb, Resource path, String separator) {
		if(path.hasProperty(RDF.first)) {
			Iterator<RDFNode> it = path.as(RDFList.class).iterator();
			while(it.hasNext()) {
				Resource item = (Resource) it.next();
				appendNestedPath(sb, item, SEQUENCE_PATH_SEPARATOR);
				if(it.hasNext()) {
					sb.append(" ");
					sb.append(separator);
					sb.append(" ");
				}
			}
		}
		else if(path.hasProperty(SH.inversePath)) {
			sb.append("^");
			appendPath(sb, JenaUtil.getResourceProperty(path, SH.inversePath));
		}
		else if(path.hasProperty(SH.alternativePath)) {
			appendNestedPath(sb, JenaUtil.getResourceProperty(path, SH.alternativePath), ALTERNATIVE_PATH_SEPARATOR);
		}
		else if(path.hasProperty(SH.zeroOrMorePath)) {
			appendNestedPath(sb, JenaUtil.getResourceProperty(path, SH.zeroOrMorePath), SEQUENCE_PATH_SEPARATOR);
			sb.append("*");
		}
		else if(path.hasProperty(SH.oneOrMorePath)) {
			appendNestedPath(sb, JenaUtil.getResourceProperty(path, SH.oneOrMorePath), SEQUENCE_PATH_SEPARATOR);
			sb.append("+");
		}
		else if(path.hasProperty(SH.zeroOrOnePath)) {
			sb.append("?");
			appendNestedPath(sb, JenaUtil.getResourceProperty(path, SH.zeroOrOnePath), SEQUENCE_PATH_SEPARATOR);
		}
	}
	
	
	public static Resource clonePath(Resource path, Model targetModel) {
		if(path.isURIResource()) {
			return path.inModel(targetModel);
		}
		else {
			Resource clone = targetModel.createResource();
			for(Statement s : path.listProperties().toList()) {
				Resource newObject = clonePath(s.getResource(), targetModel);
				clone.addProperty(s.getPredicate(), newObject);
			}
			return clone;
		}
	}
	
	
	public static String getPathString(Resource path) {
		StringBuffer sb = new StringBuffer();
		appendPath(sb, path);
		return sb.toString();
	}
}
