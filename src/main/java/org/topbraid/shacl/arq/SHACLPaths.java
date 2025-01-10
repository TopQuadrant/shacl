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
package org.topbraid.shacl.arq;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.path.P_Alt;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_OneOrMore1;
import org.apache.jena.sparql.path.P_OneOrMoreN;
import org.apache.jena.sparql.path.P_Path1;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.P_ZeroOrMore1;
import org.apache.jena.sparql.path.P_ZeroOrMoreN;
import org.apache.jena.sparql.path.P_ZeroOrOne;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.path.eval.PathEval;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Utilities to manage the conversion between SHACL paths and SPARQL 1.1 property paths.
 * 
 * @author Holger Knublauch
 */
public class SHACLPaths {
	
	private final static String ALTERNATIVE_PATH_SEPARATOR = "|";
	
	private final static String SEQUENCE_PATH_SEPARATOR = "/";
	
	
	public static void addValueNodes(RDFNode focusNode, Path path, Collection<RDFNode> results) {
		Set<Node> seen = new HashSet<>();
		Iterator<Node> it = PathEval.eval(focusNode.getModel().getGraph(), focusNode.asNode(), path, Context.emptyContext());
		while(it.hasNext()) {
			Node node = it.next();
			if(!seen.contains(node)) {
				seen.add(node);
				results.add(focusNode.getModel().asRDFNode(node));
			}
		}
	}
	
	
	public static void addValueNodes(RDFNode focusNode, Property predicate, Collection<RDFNode> results) {
		if(focusNode instanceof Resource) {
			StmtIterator it = ((Resource)focusNode).listProperties(predicate);
			while(it.hasNext()) {
				results.add(it.next().getObject());
			}
		}
	}

	
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
			sb.append("(");
			appendPathBlankNode(sb, path, separator);
			sb.append(")");
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
			if(path.getProperty(SH.inversePath).getObject().isAnon()) {
				sb.append("(");
				appendPath(sb, path.getPropertyResourceValue(SH.inversePath));
				sb.append(")");
			}
			else {
				appendPath(sb, path.getPropertyResourceValue(SH.inversePath));
			}
		}
		else if(path.hasProperty(SH.alternativePath)) {
			appendNestedPath(sb, path.getPropertyResourceValue(SH.alternativePath), ALTERNATIVE_PATH_SEPARATOR);
		}
		else if(path.hasProperty(SH.zeroOrMorePath)) {
			appendNestedPath(sb, path.getPropertyResourceValue(SH.zeroOrMorePath), SEQUENCE_PATH_SEPARATOR);
			sb.append("*");
		}
		else if(path.hasProperty(SH.oneOrMorePath)) {
			appendNestedPath(sb, path.getPropertyResourceValue(SH.oneOrMorePath), SEQUENCE_PATH_SEPARATOR);
			sb.append("+");
		}
		else if(path.hasProperty(SH.zeroOrOnePath)) {
			appendNestedPath(sb, path.getPropertyResourceValue(SH.zeroOrOnePath), SEQUENCE_PATH_SEPARATOR);
			sb.append("?");
		}
	}
	
	
	public static Resource clonePath(Resource path, Model targetModel) {
		if(path.isURIResource()) {
			return path.inModel(targetModel);
		}
		else {
			Resource clone = targetModel.createResource();
			for(Statement s : path.listProperties().toList()) {
				if(s.getSubject().hasProperty(RDF.first)) {
					if(RDF.first.equals(s.getPredicate()) || RDF.rest.equals(s.getPredicate())) {
						Resource newObject = clonePath(s.getResource(), targetModel);
						clone.addProperty(s.getPredicate(), newObject);
					}
				}
				else {
					Resource newObject = clonePath(s.getResource(), targetModel);
					clone.addProperty(s.getPredicate(), newObject);
				}
			}
			return clone;
		}
	}
	
	
	/**
	 * Creates SHACL RDF triples for a given Jena Path (which may have been created using getJenaPath).
	 * @param path  the Jena Path
	 * @param model  the Model to create the triples in
	 * @return the (root) Resource of the SHACL path
	 */
	public static Resource createPath(Path path, Model model) {
		if(path instanceof P_Alt) {
			Resource result = model.createResource();
			RDFList list = model.createList(Arrays.asList(new RDFNode[] {
				createPath(((P_Alt) path).getLeft(), model),
				createPath(((P_Alt) path).getRight(), model)
			}).iterator());
			result.addProperty(SH.alternativePath, list);
			return result;
		}
		else if(path instanceof P_Inverse) {
			Resource result = model.createResource();
			result.addProperty(SH.inversePath, createPath(((P_Inverse) path).getSubPath(), model));
			return result;
		}
		else if(path instanceof P_Link) {
			return (Resource) model.asRDFNode(((P_Link) path).getNode());
		}
		else if(path instanceof P_OneOrMore1 || path instanceof P_OneOrMoreN) {
			Resource result = model.createResource();
			result.addProperty(SH.oneOrMorePath, createPath(((P_Path1)path).getSubPath(), model));
			return result;
		}
		if(path instanceof P_Seq) {
			return model.createList(Arrays.asList(new RDFNode[] {
				createPath(((P_Seq) path).getLeft(), model),
				createPath(((P_Seq) path).getRight(), model)
			}).iterator());
		}
		else if(path instanceof P_ZeroOrMore1 || path instanceof P_ZeroOrMoreN) {
			Resource result = model.createResource();
			result.addProperty(SH.zeroOrMorePath, createPath(((P_Path1)path).getSubPath(), model));
			return result;
		}
		else if(path instanceof P_ZeroOrOne) {
			Resource result = model.createResource();
			result.addProperty(SH.zeroOrOnePath, createPath(((P_Path1)path).getSubPath(), model));
			return result;
		}
		else {
			throw new IllegalArgumentException("Path element not supported by SHACL syntax: " + path);
		}
	}
	
	
	public static Object getJenaPath(Resource path) throws QueryParseException {
		if(path.isURIResource()) {
			return path;
		}
		else {
			try {
				return getPath(path);
			}
			catch(Exception ex) {
				throw new IllegalArgumentException("Not a SPARQL 1.1 Path expression", ex);
			}
		}
	}

	
	/**
	 * Attempts to parse a given string into a Jena Path.
	 * Throws an Exception if the string cannot be parsed.
	 * @param string  the string to parse
	 * @param model  the Model to operate on (for prefixes)
	 * @return a Path or a Resource if this is a URI
	 */
	public static Object getJenaPath(String string, Model model) throws QueryParseException {
		Query query = ARQFactory.get().createQuery(model, "ASK { ?a \n" + string + "\n ?b }");
		Element element = query.getQueryPattern();
		if(element instanceof ElementGroup) {
			Element e = ((ElementGroup)element).getElements().get(0);
			if(e instanceof ElementPathBlock) {
				Path path = ((ElementPathBlock) e).getPattern().get(0).getPath();
				if(path instanceof P_Link && ((P_Link)path).isForward()) {
					return model.asRDFNode(((P_Link)path).getNode());
				}
				else {
					return path;
				}
			}
			else if(e instanceof ElementTriplesBlock) {
				return model.asRDFNode(((ElementTriplesBlock) e).getPattern().get(0).getPredicate());
			}
		}
		throw new QueryParseException("Not a SPARQL 1.1 Path expression", 2, 1);
	}
	
	
	public static Path getPath(Resource shaclPath) {
		if(shaclPath.isURIResource()) {
			return PathFactory.pathLink(shaclPath.asNode());
		}
		{
			Resource inversePath = shaclPath.getPropertyResourceValue(SH.inversePath);
			if(inversePath != null) {
				return PathFactory.pathInverse(getPath(inversePath));
			}
		}
		{
			Resource first = shaclPath.getPropertyResourceValue(RDF.first);
			if(first != null) {
				Resource rest = shaclPath.getPropertyResourceValue(RDF.rest);
				if(RDF.nil.equals(rest)) {
					return getPath(first);
				}
				else {
					return PathFactory.pathSeq(getPath(first), getPath(rest));
				}
			}
		}
		{
			Resource alternativePath = shaclPath.getPropertyResourceValue(SH.alternativePath);
			if(alternativePath != null) {
				ExtendedIterator<Path> members = alternativePath.as(RDFList.class).iterator().
						mapWith(node -> getPath(node.asResource()));
				try {
					return getPathAlt(members);
				}
				finally {
					members.close();
				}
			}
		}
		{
			Resource zeroOrMorePath = shaclPath.getPropertyResourceValue(SH.zeroOrMorePath);
			if(zeroOrMorePath != null) {
				return PathFactory.pathZeroOrMore1(getPath(zeroOrMorePath));
			}
		}
		{
			Resource oneOrMorePath = shaclPath.getPropertyResourceValue(SH.oneOrMorePath);
			if(oneOrMorePath != null) {
				return PathFactory.pathOneOrMore1(getPath(oneOrMorePath));
			}
		}
		{
			Resource zeroOrOnePath = shaclPath.getPropertyResourceValue(SH.zeroOrOnePath);
			if(zeroOrOnePath != null) {
				return PathFactory.pathZeroOrOne(getPath(zeroOrOnePath));
			}
		}
		throw new IllegalArgumentException("Malformed SHACL path expression");
	}
	
	
	private static Path getPathAlt(Iterator<Path> it) {
		Path first = it.next();
		if(it.hasNext()) {
			return PathFactory.pathAlt(first, getPathAlt(it));
		}
		else {
			return first;
		}
	}

	
	public static String getPathString(Resource path) {
		StringBuffer sb = new StringBuffer();
		appendPath(sb, path);
		return sb.toString();
	}
}
