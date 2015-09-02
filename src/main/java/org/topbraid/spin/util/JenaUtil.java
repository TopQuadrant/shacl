/*******************************************************************************
 * Copyright (c) 2009 TopQuadrant, Inc.
 * All rights reserved. 
 *******************************************************************************/
package org.topbraid.spin.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;

import com.hp.hpl.jena.enhanced.EnhGraph;
import com.hp.hpl.jena.graph.Factory;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.StmtIteratorImpl;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;


/**
 * Some convenience methods to operate on Jena Models.
 * 
 * These methods are not as stable as the rest of the API, but
 * they may be of general use.
 * 
 * @author Holger Knublauch
 */
public class JenaUtil {
	
	// Unstable
	private static JenaUtilHelper helper = new JenaUtilHelper();

	
	private static Model dummyModel = JenaUtil.createDefaultModel();
	
	private static final String WITH_IMPORTS_PREFIX = "http://rdfex.org/withImports?uri=";
	
	

	/**
	 * Sets the helper which allows the behavior of some JenaUtil
	 * methods to be modified by the system using the SPIN library.
	 * Note: Should not be used outside of TopBraid - not stable.
	 * @param h  the JenaUtilHelper
	 * @return the old helper
	 */
	public static JenaUtilHelper setHelper(JenaUtilHelper h) {
		JenaUtilHelper old = helper;
		helper = h;
		return old;
	}
	
	
	/**
	 * Gets the current helper object.
	 * Note: Should not be used outside of TopBraid - not stable.
	 * @return the helper
	 */
	public static JenaUtilHelper getHelper() {
		return helper;
	}
	
	
	/**
	 * Adds all sub-properties of a given property as long as they don't have their own
	 * rdfs:domain.  This is useful to determine inheritance.
	 * @param property  the property ot add the sub-properties of
	 * @param results  the Set to add the results to
	 * @param reached  a Set used to track which ones were already reached
	 */
	public static void addDomainlessSubProperties(Resource property, Set<Property> results, Set<Resource> reached) {
		StmtIterator subs = property.getModel().listStatements(null, RDFS.subPropertyOf, property);
		while(subs.hasNext()) {
			Resource subProperty = subs.next().getSubject();
			if(!reached.contains(subProperty)) {
				reached.contains(subProperty);
				if (!subProperty.hasProperty(RDFS.domain)) {
					results.add(subProperty.getModel().getProperty(subProperty.getURI()));
					addDomainlessSubProperties(subProperty, results, reached);
				}
			}
		}
	}


	/**
	 * Populates a result set of resources reachable from a subject via zero or more steps with a given predicate.
	 * Implementation note: the results set need only implement {@link Collection#add(Object)}.
	 * @param results   The transitive objects reached from subject via triples with the given predicate
	 * @param subject
	 * @param predicate
	 */
	public static void addTransitiveObjects(Set<Resource> results, Resource subject, Property predicate) {
		helper.setGraphReadOptimization(true);
		try {
			addTransitiveObjects(results, new HashSet<Resource>(), subject, predicate);
		}
		finally {
			helper.setGraphReadOptimization(false);
		}
	}


	private static void addTransitiveObjects(Set<Resource> resources, Set<Resource> reached,
			Resource subject, Property predicate) {
		resources.add(subject);
		reached.add(subject);
		StmtIterator it = subject.listProperties(predicate);
		try {
			while (it.hasNext()) {
				RDFNode object = it.next().getObject();
				if (object instanceof Resource) {
					if (!reached.contains(object)) {
						addTransitiveObjects(resources, reached, (Resource)object, predicate);
					}
				}
			}
		}
		finally {
			it.close();
		}
	}


	private static void addTransitiveSubjects(Set<Resource> reached, Resource object,
			Property predicate, ProgressMonitor monitor) {
		if (object != null) {
			reached.add(object);
			StmtIterator it = object.getModel().listStatements(null, predicate, object);
			try {
				while (it.hasNext()) {
					if (monitor != null && monitor.isCanceled()) {
						it.close();
						return;
					}
					Resource subject = it.next().getSubject();
					if (!reached.contains(subject)) {
						addTransitiveSubjects(reached, subject, predicate, monitor);
					}
				}
			}
			finally {
				it.close();
			}
		}
	}
	
	
	/**
	 * Turns a QuerySolution into a Binding. 
	 * @param map  the input QuerySolution
	 * @return a Binding or null if the input is null
	 */
	public static Binding asBinding(final QuerySolution map) {
		if(map != null) {
			BindingHashMap result = new BindingHashMap();
			Iterator<String> varNames = map.varNames();
			while(varNames.hasNext()) {
				String varName = varNames.next();
				RDFNode node = map.get(varName);
				if(node != null) {
					result.add(Var.alloc(varName), node.asNode());
				}
			}
			return result;
		}
		else {
			return null;
		}
	}
	

	/**
	 * Turns a Binding into a QuerySolutionMap.
	 * @param binding  the Binding to convert
	 * @return a QuerySolutionMap
	 */
	public static QuerySolutionMap asQuerySolutionMap(Binding binding) {
		QuerySolutionMap map = new QuerySolutionMap();
		Iterator<Var> vars = binding.vars();
		while(vars.hasNext()) {
			Var var = vars.next();
			Node node = binding.get(var);
			if(node != null) {
				map.add(var.getName(), dummyModel.asRDFNode(node));
			}
		}
		return map;
	}
	
	
	/**
	 * Returns a set of resources reachable from an object via one or more reversed steps with a given predicate.
	 */
	public static Set<Resource> getAllTransitiveSubjects(Resource object, Property predicate,
			ProgressMonitor monitor) {
		Set<Resource> set = new HashSet<Resource>();
		helper.setGraphReadOptimization(true);
		try {
		   addTransitiveSubjects(set, object, predicate, monitor);
		}
		finally {
			helper.setGraphReadOptimization(false);
		}
		set.remove(object);
		return set;
	}
	
	
	/**
	 * Casts a Resource into a Property.
	 * @param resource  the Resource to cast
	 * @return resource as an instance of Property
	 */
	public static Property asProperty(Resource resource) {
		if(resource instanceof Property) {
			return (Property) resource;
		}
		else {
			return new PropertyImpl(resource.asNode(), (EnhGraph)resource.getModel());
		}
	}
	
	
	/**
	 * Creates a Set of Properties from a Collection of Resources.
	 * @param resources  the Resource to cast
	 * @return resource as an instance of Property
	 */
	public static Set<Property> asProperties(Collection<Resource> resources) {
		Set<Property> rslt = new HashSet<Property>();
		for (Resource r:resources) {
			rslt.add(asProperty(r));
		}
		return rslt;
	}


	public static void collectBaseGraphs(Graph graph, Set<Graph> baseGraphs) {
		if(graph instanceof MultiUnion) {
			MultiUnion union = (MultiUnion)graph;
			collectBaseGraphs(union.getBaseGraph(), baseGraphs);
			for(Object subGraph : union.getSubGraphs()) {
				collectBaseGraphs((Graph)subGraph, baseGraphs);
			}
		}
		else if(graph != null) {
			baseGraphs.add(graph);
		}
	}
	

	/**
	 * Creates a new Graph.  By default this will deliver a plain in-memory graph,
	 * but other implementations may deliver graphs with concurrency support and
	 * other features.
	 * @return a default graph
	 * @see #createDefaultModel()
	 */
	public static Graph createDefaultGraph() {
		return getHelper().createDefaultGraph();
	}


	/**
	 * Wraps the result of {@link #createDefaultGraph()} into a Model and initializes namespaces. 
	 * @return a default Model
	 * @see #createDefaultGraph()
	 */
	public static Model createDefaultModel() {
		Model m = ModelFactory.createModelForGraph(createDefaultGraph());
		initNamespaces(m);
		return m;
	}
	
	
	/**
	 * Creates a memory Graph with no reification.
	 * @return a new memory graph
	 */
	public static Graph createMemoryGraph() {
		return Factory.createDefaultGraph();
	}
	
	
	/**
	 * Creates a memory Model with no reification.
	 * @return a new memory Model
	 */
	public static Model createMemoryModel() {
		return ModelFactory.createModelForGraph(createMemoryGraph());
	}

	
	public static MultiUnion createMultiUnion() {
		return helper.createMultiUnion();
	}

	
	public static MultiUnion createMultiUnion(Graph[] graphs) {
		return helper.createMultiUnion(graphs);
	}

	
	public static MultiUnion createMultiUnion(Iterator<Graph> graphs) {
		return helper.createMultiUnion(graphs);
	}
	
	
	/**
	 * Gets all instances of a given class and its subclasses.
	 * @param cls  the class to get the instances of
	 * @return the instances
	 */
	public static Set<Resource> getAllInstances(Resource cls) {
		JenaUtil.setGraphReadOptimization(true);
		try {
			Model model = cls.getModel();
			Set<Resource> classes = getAllSubClasses(cls);
			classes.add(cls);
			Set<Resource> results = new HashSet<Resource>();
			for(Resource subClass : classes) {
				StmtIterator it = model.listStatements(null, RDF.type, subClass);
				while (it.hasNext()) {
					results.add(it.next().getSubject());
				}
			}
			return results;
		}
		finally {
			JenaUtil.setGraphReadOptimization(false);
		}
	}

	
	public static Set<Resource> getAllSubClasses(Resource cls) {
		return getAllTransitiveSubjects(cls, RDFS.subClassOf);
	}

	
	/**
	 * Returns a set consisting of a given class and all its subclasses.
	 * Similar to rdfs:subClassOf*.
	 * @param cls  the class to return with its subclasses
	 * @return the Set of class resources
	 */
	public static Set<Resource> getAllSubClassesStar(Resource cls) {
		Set<Resource> results = getAllTransitiveSubjects(cls, RDFS.subClassOf);
		results.add(cls);
		return results;
	}
	
	
	public static Set<Resource> getAllSubProperties(Property superProperty) {
		return getAllTransitiveSubjects(superProperty, RDFS.subPropertyOf);
	}

	
	public static Set<Resource> getAllSuperClasses(Resource cls) {
		return getAllTransitiveObjects(cls, RDFS.subClassOf);
	}

	
	/**
	 * Returns a set consisting of a given class and all its superclasses.
	 * Similar to rdfs:subClassOf*.
	 * @param cls  the class to return with its superclasses
	 * @return the Set of class resources
	 */
	public static Set<Resource> getAllSuperClassesStar(Resource cls) {
		Set<Resource> results = getAllTransitiveObjects(cls, RDFS.subClassOf);
		results.add(cls);
		return results;
	}
	
	
	public static Set<Resource> getAllSuperProperties(Property subProperty) {
		return getAllTransitiveObjects(subProperty, RDFS.subPropertyOf);
	}

	
	/**
	 * Returns a set of resources reachable from a subject via one or more steps with a given predicate.
	 * 
	 */
	public static Set<Resource> getAllTransitiveObjects(Resource subject, Property predicate) {
		Set<Resource> set = new HashSet<Resource>();
		addTransitiveObjects(set, subject, predicate);
		set.remove(subject);
		return set;
	}

	
	private static Set<Resource> getAllTransitiveSubjects(Resource object, Property predicate) {
		return getAllTransitiveSubjects(object, predicate, null);
	}

	
	public static Set<Resource> getAllTypes(Resource instance) {
		Set<Resource> types = new HashSet<Resource>();
		StmtIterator it = instance.listProperties(RDF.type);
		try {
			while (it.hasNext()) {
				Resource type = it.next().getResource();
				types.add(type);
				types.addAll(getAllSuperClasses(type));
			}
		}
		finally {
			it.close();
		}
		return types;
	}


	/**
	 * Gets the "base graph" of a Model, walking into MultiUnions if needed.
	 * @param model  the Model to get the base graph of
	 * @return the base graph or null if the model contains a MultiUnion that doesn't declare one
	 */
	public static Graph getBaseGraph(final Model model) {
		return getBaseGraph(model.getGraph());
	}
	
	
	public static Graph getBaseGraph(Graph graph) {
		Graph baseGraph = graph;
		while(baseGraph instanceof MultiUnion) {
			baseGraph = ((MultiUnion)baseGraph).getBaseGraph();
		}
		return baseGraph;
	}
	
	
	public static Model getBaseModel(Model model) {
		Graph baseGraph = getBaseGraph(model);
		if(baseGraph == model.getGraph()) {
			return model;
		}
		else {
			return ModelFactory.createModelForGraph(baseGraph);
		}
	}


	/**
	 * Gets the "first" declared rdfs:range of a given property.
	 * If multiple ranges exist, the behavior is undefined.
	 * Note that this method does not consider ranges defined on
	 * super-properties.
	 * @param property  the property to get the range of
	 * @return the "first" range Resource or null
	 */
	public static Resource getFirstDirectRange(Resource property) {
		return JenaUtil.getPropertyResourceValue(property, RDFS.range);
	}


	private static Resource getFirstRange(Resource property, Set<Resource> reached) {
		Resource directRange = getFirstDirectRange(property);
		if(directRange != null) {
			return directRange;
		}
		StmtIterator it = property.listProperties(RDFS.subPropertyOf);
		while (it.hasNext()) {
			Statement ss = it.next();
			if (ss.getObject().isURIResource()) {
				Resource superProperty = ss.getResource();
				if (!reached.contains(superProperty)) {
					reached.add(superProperty);
					Resource r = getFirstRange(superProperty, reached);
					if (r != null) {
						it.close();
						return r;
					}
				}
			}
		}
		return null;
	}


	/**
	 * Gets the "first" declared rdfs:range of a given property.
	 * If multiple ranges exist, the behavior is undefined.
	 * This method walks up to super-properties if no direct match exists.
	 * @param property  the property to get the range of
	 * @return the "first" range Resource or null
	 */
	public static Resource getFirstRange(Resource property) {
		return getFirstRange(property, new HashSet<Resource>());
	}
	
	
	public static Set<Resource> getImports(Resource graph) {
		Set<Resource> results = new HashSet<Resource>();
		for(Property importProperty : ImportProperties.get().getImportProperties()) {
			results.addAll(JenaUtil.getResourceProperties(graph, importProperty));
		}
		return results;
	}

	
	public static Integer getIntegerProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null && s.getObject().isLiteral()) {
			return s.getInt();
		}
		else {
			return null;
		}
	}
	
	
	public static RDFList getListProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null && s.getObject().canAs(RDFList.class)) {
			return s.getResource().as(RDFList.class);
		}
		else {
			return null;
		}
	}
	
	
	public static List<Literal> getLiteralProperties(Resource subject, Property predicate) {
		List<Literal> results = new LinkedList<Literal>();
		StmtIterator it = subject.listProperties(predicate);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getObject().isLiteral()) {
				results.add(s.getLiteral());
			}
		}
		return results;
	}
	
	
	/**
	 * Gets the local range of a given property at a given class, considering things like
	 * rdfs:range, owl:allValuesFrom restrictions, spl:Argument and others.
	 * Optionally returns a suitable default range (rdfs:Resource or xsd:string) if no other is defined.
	 * @param property  the Property to get the range of
	 * @param type  the class to get the range at
	 * @param graph  the Graph to operate on
	 * @param useDefault  true to fall back to a suitable default
	 * @return a suitable range; may be null if useDefault == false
	 */
	public static Node getLocalRange(Node property, Node type, Graph graph, boolean useDefault) {
		return LocalRangeAtClassNativeFunction.run(type, property, graph, useDefault);
	}
	
	
	/*
	public static Resource getLocalRangeAtInstance(Resource resource, Property property) {
		for(Resource type : JenaUtil.getTypes(resource)) {
			Node range = JenaUtil.getLocalRange(property.asNode(), type.asNode(), resource.getModel().getGraph());
			if(range != null) {
				return (Resource) resource.getModel().asRDFNode(range);
			}
		}
		return null;
	}*/


	/**
	 * Overcomes a bug in Jena: if the base model does not declare a default namespace then the
	 * default namespace of an import is returned!
	 * 
	 * @param model the Model to operate on
	 * @param prefix the prefix to get the URI of
	 * @return the URI of prefix
	 */
	public static String getNsPrefixURI(Model model, String prefix) {
		if ("".equals(prefix) && model.getGraph() instanceof MultiUnion) {
			Graph baseGraph = ((MultiUnion)model.getGraph()).getBaseGraph();
			if(baseGraph != null) {
				return baseGraph.getPrefixMapping().getNsPrefixURI(prefix);
			}
			else {
				return model.getNsPrefixURI(prefix);
			}
		}
		else {
			return model.getNsPrefixURI(prefix);
		}
	}
	
	
	public static RDFNode getProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null) {
			return s.getObject();
		}
		else {
			return null;
		}
	}
	
	
	public static List<Resource> getReferences(Property predicate, Resource object) {
		List<Resource> results = new LinkedList<Resource>();
		StmtIterator it = object.getModel().listStatements(null, predicate, object);
		while(it.hasNext()) {
			Statement s = it.next();
			results.add(s.getSubject());
		}
		return results;
	}
	
	
	public static Resource getResourceProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null && s.getObject().isResource()) {
			return s.getResource();
		}
		else {
			return null;
		}
	}
	
	
	public static List<Resource> getResourceProperties(Resource subject, Property predicate) {
		List<Resource> results = new LinkedList<Resource>();
		StmtIterator it = subject.listProperties(predicate);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getObject().isResource()) {
				results.add(s.getResource());
			}
		}
		return results;
	}
	
	
	public static String getStringProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null && s.getObject().isLiteral()) {
			return s.getString();
		}
		else {
			return null;
		}
	}

	public static boolean getBooleanProperty(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null && s.getObject().isLiteral()) {
			return s.getBoolean();
		}
		else {
			return false;
		}
	}

	public static List<Graph> getSubGraphs(MultiUnion union) {
		List<Graph> results = new LinkedList<Graph>();
		results.add(union.getBaseGraph());
		for(Object subGraph : union.getSubGraphs()) {
			results.add((Graph)subGraph);
		}
		return results;
	}
	
	
	/**
	 * Gets a Set of all superclasses (rdfs:subClassOf) of a given Resource. 
	 * @param subClass  the subClass Resource
	 * @return a Collection of class resources
	 */
	public static Collection<Resource> getSuperClasses(Resource subClass) {
		NodeIterator it = subClass.getModel().listObjectsOfProperty(subClass, RDFS.subClassOf);
		Set<Resource> results = new HashSet<Resource>();
		while (it.hasNext()) {
			RDFNode node = it.nextNode();
			if (node instanceof Resource) {
				results.add((Resource)node);
			}
		}
		return results;
	}
	

	/**
	 * Gets the "first" type of a given Resource.
	 * @param instance  the instance to get the type of
	 * @return the type or null
	 */
	public static Resource getType(Resource instance) {
		return getResourceProperty(instance, RDF.type);
	}
	
	
	/**
	 * Gets a Set of all rdf:types of a given Resource. 
	 * @param instance  the instance Resource
	 * @return a Collection of type resources
	 */
	public static List<Resource> getTypes(Resource instance) {
		return JenaUtil.getResourceProperties(instance, RDF.type);
	}
	
	
	/**
	 * Checks whether a given Resource is an instance of a given type, or
	 * a subclass thereof.  Make sure that the expectedType parameter is associated
	 * with the right Model, because the system will try to walk up the superclasses
	 * of expectedType.  The expectedType may have no Model, in which case
	 * the method will use the instance's Model.
	 * @param instance  the Resource to test
	 * @param expectedType  the type that instance is expected to have
	 * @return true if resource has rdf:type expectedType
	 */
	public static boolean hasIndirectType(Resource instance, Resource expectedType) {
		
		if(expectedType.getModel() == null) {
			expectedType = expectedType.inModel(instance.getModel());
		}
		
		StmtIterator it = instance.listProperties(RDF.type);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getObject().isResource()) {
				Resource actualType = s.getResource();
				if(actualType.equals(expectedType) || JenaUtil.hasSuperClass(actualType, expectedType)) {
					it.close();
					return true;
				}
			}
		}
		return false;
	}
	

	/**
	 * Checks whether a given class has a given (transitive) super class.
	 * @param subClass  the sub-class
	 * @param superClass  the super-class
	 * @return true if subClass has superClass (somewhere up the tree)
	 */
	public static boolean hasSuperClass(Resource subClass, Resource superClass) {
		return hasSuperClass(subClass, superClass, new HashSet<Resource>());
	}
	
	
	private static boolean hasSuperClass(Resource subClass, Resource superClass, Set<Resource> reached) {
		for(Statement s : subClass.listProperties(RDFS.subClassOf).toList()) {
			if(superClass.equals(s.getObject())) {
				return true;
			}
			else if(!reached.contains(s.getResource())) {
				reached.add(s.getResource());
				if(hasSuperClass(s.getResource(), superClass, reached)) {
					return true;
				}
			}
		}
		return false;
	}
	

	/**
	 * Checks whether a given property has a given (transitive) super property.
	 * @param subProperty  the sub-property
	 * @param superProperty  the super-property
	 * @return true if subProperty has superProperty (somewhere up the tree)
	 */
	public static boolean hasSuperProperty(Property subProperty, Property superProperty) {
		return getAllSuperProperties(subProperty).contains(superProperty);
	}


	/**
	 * Sets the usual default namespaces for rdf, rdfs, owl and xsd.
	 * @param graph  the Graph to modify
	 */
	public static void initNamespaces(Graph graph) {
		PrefixMapping prefixMapping = graph.getPrefixMapping();
		initNamespaces(prefixMapping);
	}
	
	
	/**
	 * Sets the usual default namespaces for rdf, rdfs, owl and xsd.
	 * @param prefixMapping  the Model to modify
	 */
	public static void initNamespaces(PrefixMapping prefixMapping) {
		prefixMapping.setNsPrefix("rdf", RDF.getURI());
		prefixMapping.setNsPrefix("rdfs", RDFS.getURI());
		prefixMapping.setNsPrefix("owl", OWL.getURI());
		prefixMapping.setNsPrefix("xsd", XSD.getURI());
	}


	/**
	 * Checks whether a given graph (possibly a MultiUnion) only contains
	 * GraphMemBase instances.
	 * @param graph  the Graph to test
	 * @return true  if graph is a memory graph
	 */
	public static boolean isMemoryGraph(Graph graph) {
		if(graph instanceof MultiUnion) {
			for(Graph subGraph : JenaUtil.getSubGraphs((MultiUnion)graph)) {
				if(!isMemoryGraph(subGraph)) {
					return false;
				}
			}
			return true;
		}
		else  {
			return helper.isMemoryGraph(graph);
		}
	}


	/**
	 * Checks if a given property is multi-valued according to owl:FunctionalProperty,
	 * OWL cardinality restrictions, spl:Argument or spl:ObjectCountPropertyConstraint. 
	 * @param property  the Property check
	 * @param type  the context class to start traversal at (may be null)
	 * @return true  if property may have multiple values
	 */
	public static boolean isMulti(Property property, Resource type) {
		return isMulti(property.asNode(), type != null ? type.asNode() : null, property.getModel().getGraph());
	}
	
	
	public static boolean isMulti(Node property, Node type, Graph graph) {
		return IsMultiFunctionHelper.isMulti(property, type, graph);
	}
	
	
	/**
	 * Gets an Iterator over all Statements of a given property or its sub-properties
	 * at a given subject instance.  Note that the predicate and subject should be
	 * both attached to a Model to avoid NPEs.
	 * @param subject  the subject (may be null)
	 * @param predicate  the predicate
	 * @return a StmtIterator
	 */
	public static StmtIterator listAllProperties(Resource subject, Property predicate) {
		List<Statement> results = new LinkedList<Statement>();
		helper.setGraphReadOptimization(true);
		try {
			listAllProperties(subject, predicate, new HashSet<Property>(), results);
		}
		finally {
			helper.setGraphReadOptimization(false);
		}
		return new StmtIteratorImpl(results.iterator());
	}
	
	
	private static void listAllProperties(Resource subject, Property predicate, Set<Property> reached,
			List<Statement> results) {
		reached.add(predicate);
		StmtIterator sit;
		Model model;
		if (subject != null) {
			sit = subject.listProperties(predicate);
			model = subject.getModel();
		}
		else {
			model = predicate.getModel();
			sit = model.listStatements(null, predicate, (RDFNode)null);
		}
		while (sit.hasNext()) {
			results.add(sit.next());
		}

		// Iterate into direct subproperties
		StmtIterator it = model.listStatements(null, RDFS.subPropertyOf, predicate);
		while (it.hasNext()) {
			Statement sps = it.next();
			if (!reached.contains(sps.getSubject())) {
				Property subProperty = asProperty(sps.getSubject());
				listAllProperties(subject, subProperty, reached, results);
			}
		}
	}
	
	
	public static String withImports(String uri) {
		if(!uri.startsWith(WITH_IMPORTS_PREFIX)) {
			return WITH_IMPORTS_PREFIX + uri;
		}
		else {
			return uri;
		}
	}
	
	
	public static String withoutImports(String uri) {
		if(uri.startsWith(WITH_IMPORTS_PREFIX)) {
			return uri.substring(WITH_IMPORTS_PREFIX.length());
		}
		else {
			return uri;
		}
	}
	
	
	/**
	 * This indicates that no further changes to the model are needed.
	 * Some implementations may give runtime exceptions if this is violated.
	 * @param m
	 * @return
	 */
	public static Model asReadOnlyModel(Model m) {
		return helper.asReadOnlyModel(m);
	}
	
	
	/**
	 * This indicates that no further changes to the graph are needed.
	 * Some implementations may give runtime exceptions if this is violated.
	 * @param m
	 * @return
	 */
	public static Graph asReadOnlyGraph(Graph g) {
		return helper.asReadOnlyGraph(g);
	}
	
	
	// Internal to TopBraid only
	public static OntModel createOntologyModel(OntModelSpec spec, Model base) {
		return helper.createOntologyModel(spec,base);
	}

	
	// Internal to TopBraid only
	public static OntModel createOntologyModel() {
		return helper.createOntologyModel();
	}


	// Internal to TopBraid only
	public static OntModel createOntologyModel(OntModelSpec spec) {
		return helper.createOntologyModel(spec);
	}

	
	/**
	 * Replacement for {@link Resource#getPropertyResourceValue(Property)}
	 * which leaves an unclosed iterator.
	 * @param subject  the subject
	 * @param p  the predicate
	 * @return the value or null
	 */
	public static Resource getPropertyResourceValue(Resource subject, Property p) {
		StmtIterator it = subject.listProperties( p );
		try {
			while (it.hasNext()) {
				RDFNode n = it.next().getObject();
				if (n.isResource()) {
					return (Resource) n;
				}
			}
			return null;
		}
		finally {
			it.close();
		}
	}
	
	
	/**
	 * Allows some environments, e.g. TopBraid, to prioritize
	 * a block of code for reading graphs, with no update occurring.
	 * The top of the block should call this with <code>true</code>
	 * with a matching call with <code>false</code> in a finally
	 * block.
	 * 
	 * Note: Unstable - don't use outside of TopBraid.
	 * 
	 * @param onOrOff
	 */
	public static void setGraphReadOptimization(boolean onOrOff) {
		getHelper().setGraphReadOptimization(onOrOff);
	}

	
	/**
	 * Ensure that we there is a read-only, thread safe version of the
	 * graph.  If the graph is not, then create a deep clone that is
	 * both.
	 * 
	 * Note: Unstable - don't use outside of TopBraid.
	 * 
	 * @param g The given graph
	 * @return A read-only, thread safe version of the given graph.
	 */
	public static Graph deepCloneForReadOnlyThreadSafe(Graph g) {
		return helper.deepCloneReadOnlyGraph(g);
	}


	/**
	 * Calls a SPARQL expression and returns the result, using some initial bindings.
	 *
	 * @param expression     the expression to execute (must contain absolute URIs)
	 * @param initialBinding the initial bindings for the unbound variables
	 * @param dataset        the query Dataset or null for default
	 * @return the result or null
	 */
	public static Node invokeExpression(String expression, QuerySolution initialBinding, Dataset dataset) {
	    if (dataset == null) {
	        dataset = ARQFactory.get().getDataset(ModelFactory.createDefaultModel());
	    }
	    Query query = ARQFactory.get().createExpressionQuery(expression);
	    QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, initialBinding);
	    ResultSet rs = qexec.execSelect();
	    Node result = null;
	    if (rs.hasNext()) {
	        QuerySolution qs = rs.next();
	        String firstVarName = rs.getResultVars().get(0);
	        RDFNode rdfNode = qs.get(firstVarName);
	        if (rdfNode != null) {
	            result = rdfNode.asNode();
	        }
	    }
	    qexec.close();
	    return result;
	}


	/**
	 * Calls a given SPARQL function with one argument.
	 *
	 * @param function the URI resource of the function to call
	 * @param argument the first argument
	 * @param dataset  the Dataset to operate on or null for default
	 * @return the result of the function call
	 */
	public static Node invokeFunction1(Resource function, RDFNode argument, Dataset dataset) {
	    final String expression = "<" + function + ">(?arg1)";
	    QuerySolutionMap initialBinding = new QuerySolutionMap();
	    initialBinding.add("arg1", argument);
	    return invokeExpression(expression, initialBinding, dataset);
	}


	public static RDFNode toRDFNode(Node node) {
		if(node != null) {
			return dummyModel.asRDFNode(node);
		} else {
			return null;
		}
	}


	public static Node invokeFunction1(Resource function, Node argument, Dataset dataset) {
		return invokeFunction1(function, toRDFNode(argument), dataset);
	}


	/**
	 * Calls a given SPARQL function with two arguments.
	 *
	 * @param function  the URI resource of the function to call
	 * @param argument1 the first argument
	 * @param argument2 the second argument
	 * @param dataset   the Dataset to operate on or null for default
	 * @return the result of the function call
	 */
	public static Node invokeFunction2(Resource function, RDFNode argument1, RDFNode argument2, Dataset dataset) {
	    final String expression = "<" + function + ">(?arg1, ?arg2)";
	    QuerySolutionMap initialBinding = new QuerySolutionMap();
	    if(argument1 != null) {
	    	initialBinding.add("arg1", argument1);
	    }
	    if(argument2 != null) {
	    	initialBinding.add("arg2", argument2);
	    }
	    return invokeExpression(expression, initialBinding, dataset);
	}


	public static Node invokeFunction2(Resource function, Node argument1, Node argument2, Dataset dataset) {
		return invokeFunction2(function, toRDFNode(argument1), toRDFNode(argument2), dataset);
	}


	public static Node invokeFunction3(Resource function, RDFNode argument1, RDFNode argument2, RDFNode argument3, Dataset dataset) {
		
	    final String expression = "<" + function + ">(?arg1, ?arg2, ?arg3)";
	    QuerySolutionMap initialBinding = new QuerySolutionMap();
	    initialBinding.add("arg1", argument1);
	    if(argument2 != null) {
	    	initialBinding.add("arg2", argument2);
	    }
		if(argument3 != null) {
			initialBinding.add("arg3", argument3);
		}
	    return invokeExpression(expression, initialBinding, dataset);
	}


	public static Node invokeFunction3(Resource function, Node argument1, Node argument2, Node argument3, Dataset dataset) {
		return invokeFunction3(function, toRDFNode(argument1), toRDFNode(argument2), toRDFNode(argument3), dataset);
	}
}
