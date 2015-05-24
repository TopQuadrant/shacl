/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.model.Argument;
import org.topbraid.spin.model.Module;
import org.topbraid.spin.vocabulary.SPIN;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Utilities on SPIN modules.
 * 
 * @author Holger Knublauch
 */
public class ModulesUtil {
	

	/**
	 * Gets the spin:body of a module, including inherited ones if the
	 * direct body is null. 
	 * @param module  the module to get the body of
	 * @return the body or null
	 */
	public static RDFNode getBody(Resource module) {
		Statement s = module.getProperty(SPIN.body);
		if(s == null) {
			return getSuperClassesBody(module, new HashSet<Resource>());
		}
		else {
			return s.getObject();
		}
	}

	
	/**
	 * Attempts to find "good" default bindings for a collection of RDFNode values
	 * at a given module.  For each argument, this algorithm checks whether each
	 * value would match the argument's type.
	 * @param module  the module Resource to check
	 * @param values  the potential values
	 * @return a Map of argProperty properties to a subset of the values
	 */
	public static Map<Property,RDFNode> getPotentialBindings(Module module, RDFNode[] values) {
		Map<Property,RDFNode> results = new HashMap<Property,RDFNode>();
		for(Argument argument : module.getArguments(false)) {
			Property argProperty = argument.getPredicate();
			if(argProperty != null) {
				Resource argType = argument.getValueType();
				if(argType != null) {
					for(RDFNode value : values) {
						if(value instanceof Resource) {
							Resource resource = (Resource) value;
							if(JenaUtil.hasIndirectType(resource, argType)) {
								results.put(argProperty, resource);
							}
						}
						else {
							Literal literal = (Literal) value;
							if(argType.getURI().equals(literal.getDatatypeURI())) {
								results.put(argProperty, literal);
							}
						}
					}
				}
			}
		}
		return results;
	}
	
	
	private static RDFNode getSuperClassesBody(Resource module, Set<Resource> reached) {
		StmtIterator it = module.listProperties(RDFS.subClassOf);
		while(it.hasNext()) {
			Statement next = it.nextStatement();
			if(next.getObject().isResource()) {
				Resource superClass = next.getResource();
				if(!reached.contains(superClass)) {
					reached.add(superClass);
					Statement s = superClass.getProperty(SPIN.body);
					if(s != null) {
						it.close();
						return s.getObject();
					}
					else {
						RDFNode body = getSuperClassesBody(module, reached);
						if(body != null) {
							it.close();
							return body;
						}
					}
				}
			}
		}
		return null;
	}
}
