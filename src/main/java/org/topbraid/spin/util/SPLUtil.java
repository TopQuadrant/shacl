/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Argument;
import org.topbraid.spin.vocabulary.SP;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


/**
 * Utilities related to the spl namespace.
 * 
 * @author Holger Knublauch
 */
public class SPLUtil {
	
	private static void addDefaultValuesForType(Resource cls, Map<Property,RDFNode> results, Set<Resource> reached) {
		
		reached.add(cls);
		
		StmtIterator it = cls.listProperties(SPIN.rule);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			if(s.getObject().isResource()) {
				Resource templateCall = s.getResource();
				if(templateCall.hasProperty(RDF.type, SPL.InferDefaultValue)) {
					Statement predicateS = templateCall.getProperty(SPL.predicate);
					if(predicateS != null && predicateS.getObject().isURIResource()) {
						Property predicate = cls.getModel().getProperty(predicateS.getResource().getURI());
						if(!results.containsKey(predicate)) {
							Statement v = templateCall.getProperty(SPL.defaultValue);
							if(v != null) {
								results.put(predicate, v.getObject());
							}
						}
					}
				}
			}
		}
		
		for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
			if(!reached.contains(superClass)) {
				addDefaultValuesForType(superClass, results, reached);
			}
		}
	}
	
	
	/**
	 * Gets any declared spl:Argument that is attached to the types of a given
	 * subject (or its superclasses) via spin:constraint, that has a given predicate
	 * as its spl:predicate.
	 * @param subject  the instance to get an Argument of
	 * @param predicate  the predicate to match
	 * @return the Argument or null if none found for that type
	 */
	public static Argument getArgument(Resource subject, Property predicate) {
		Set<Resource> reached = new HashSet<Resource>();
		for(Resource type : JenaUtil.getAllTypes(subject)) {
			Argument arg = getArgumentHelper(type, predicate, reached);
			if(arg != null) {
				return arg;
			}
		}
		return null;
	}
	
	
	private static Argument getArgumentHelper(Resource type, Property predicate, Set<Resource> reached) {
		
		if(reached.contains(type)) {
			return null;
		}
		
		reached.add(type);
		
		// Check if current type has a matching spin:constraint declaration
		StmtIterator it = type.listProperties(SPIN.constraint);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getObject().isAnon() && 
					s.getResource().hasProperty(SPL.predicate, predicate) &&
					s.getResource().hasProperty(RDF.type, SPL.Argument)) {
				it.close();
				return s.getResource().as(Argument.class);
			}
		}
		
		// Walk up superclasses
		for(Statement ss : type.listProperties(RDFS.subClassOf).toList()) {
			if(ss.getObject().isResource()) {
				Argument arg = getArgumentHelper(ss.getResource(), predicate, reached);
				if(arg != null) {
					return arg;
				}
			}
		}
		return null;
	}

	
	private static RDFNode getDefaultValueForType(Resource cls, Property predicate, Set<Resource> reached) {
		reached.add(cls);
		StmtIterator it = cls.listProperties(SPIN.rule);
		while(it.hasNext()) {
			Statement s = it.nextStatement();
			if(s.getObject().isResource()) {
				Resource templateCall = s.getResource();
				if(templateCall.hasProperty(RDF.type, SPL.InferDefaultValue)) {
					if(templateCall.hasProperty(SPL.predicate, predicate)) {
						Statement v = templateCall.getProperty(SPL.defaultValue);
						if(v != null) {
							it.close();
							return v.getObject();
						}
					}
				}
			}
		}
		
		for(Resource superClass : JenaUtil.getSuperClasses(cls)) {
			if(!reached.contains(superClass)) {
				RDFNode value = getDefaultValueForType(superClass, predicate, reached);
				if(value != null) {
					return value;
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * Creates a Map from Properties to RDFNodes based on declared
	 * spl:InferDefaultValues.
	 * @param subject
	 * @return a Map from Properties to their default values (no null values)
	 */
	public static Map<Property,RDFNode> getDefaultValues(Resource subject) {
		Map<Property,RDFNode> results = new HashMap<Property,RDFNode>();
		Set<Resource> reached = new HashSet<Resource>();
		for(Resource type : JenaUtil.getTypes(subject)) {
			addDefaultValuesForType(type, results, reached);
		}
		return results;
	}


	/**
	 * Same as <code>getObject(subject, predicate, false)</code>.
	 * @see #getObject(Resource, Property, boolean)
	 */
	public static RDFNode getObject(Resource subject, Property predicate) {
		return getObject(subject, predicate, false);
	}
	

	/**
	 * Gets the (first) value of a subject/predicate combination.
	 * If no value exists, then it checks whether any spl:InferDefaultValue
	 * has been defined for the type(s) of the subject.
	 * No need to run inferences first.
	 * @param subject  the subject to get the object of
	 * @param predicate  the predicate
	 * @param includeSubProperties  true to also check for sub-properties of predicate
	 * @return the object or null
	 */
	public static RDFNode getObject(Resource subject, Property predicate, boolean includeSubProperties) {
		Statement s = subject.getProperty(predicate);
		if(s != null) {
			return s.getObject();
		}
		else {
			Set<Resource> reached = new HashSet<Resource>();
			for(Resource type : JenaUtil.getTypes(subject)) {
				RDFNode object = getDefaultValueForType(type, predicate, reached);
				if(object != null) {
					return object;
				}
			}
			if(includeSubProperties) {
				for(Resource subProperty : JenaUtil.getAllSubProperties(predicate)) {
					Property pred = subProperty.as(Property.class);
					RDFNode value = getObject(subject, pred, false);
					if(value != null) {
						return value;
					}
				}
			}
			
			return null;
		}
	}
	
	
	public static Property getPrimaryKeyProperty(Resource cls) {
		Node result = JenaUtil.invokeFunction1(SPL.primaryKeyProperty, cls.asNode(), ARQFactory.get().getDataset(cls.getModel()));
		if(result != null) {
			return cls.getModel().getProperty(result.getURI());
		}
		else {
			return null;
		}
	}
	
	
	public static String getPrimaryKeyURIStart(Resource cls) {
		Node result = JenaUtil.invokeFunction1(SPL.primaryKeyURIStart, cls.asNode(), ARQFactory.get().getDataset(cls.getModel()));
		if(result != null) {
			return result.getLiteralLexicalForm();
		}
		else {
			return null;
		}
	}
	
	
	public static boolean hasPrimaryKey(Resource cls) {
		return JenaUtil.invokeFunction1(SPL.primaryKeyProperty, cls.asNode(), ARQFactory.get().getDataset(cls.getModel())) != null;
	}
	
	
	/**
	 * Checks whether a given Resource is an instance of spl:Argument (or a subclass
	 * thereof.
	 * @param resource  the Resource to test
	 * @return true if resource is an argument
	 */
	public static boolean isArgument(Resource resource) {
		return JenaUtil.hasIndirectType(resource, SPL.Argument.inModel(resource.getModel()));
	}
	
	
	/**
	 * Checks if a given Property is a defined spl:Argument of a given subject Resource.
	 * @param subject  the subject
	 * @param predicate  the Property to test
	 * @return true  if an spl:Argument exists in the type hierarchy of subject
	 */
	public static boolean isArgumentPredicate(Resource subject, Property predicate) {
		StmtIterator args = null;
		StmtIterator classes = null;
		JenaUtil.setGraphReadOptimization(true);
		try {
			if(SP.exists(subject.getModel())) {
				Model model = predicate.getModel();
				args = model.listStatements(null, SPL.predicate, predicate);
				while(args.hasNext()) {
					Resource arg = args.next().getSubject();
					if(arg.hasProperty(RDF.type, SPL.Argument)) {
						classes = model.listStatements(null, SPIN.constraint, arg);
						while(classes.hasNext()) {
							Resource cls = classes.next().getSubject();
							if(JenaUtil.hasIndirectType(subject, cls)) {
								return true;
							}
						}
					}
				}
			}
			return false;
		} finally {
			if (classes != null) {
			   classes.close();
			}
			if (args != null) {
			   args.close();
			}
			JenaUtil.setGraphReadOptimization(false);
		}
	}
}
