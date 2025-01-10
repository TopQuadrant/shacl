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
package org.topbraid.shacl.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaNodeUtil;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.optimize.OntologyOptimizations;
import org.topbraid.shacl.optimize.OptimizedMultiUnion;
import org.topbraid.shacl.targets.CustomTargetLanguage;
import org.topbraid.shacl.targets.CustomTargets;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

/**
 * Various SHACL-related utility methods that didn't fit elsewhere.
 * 
 * @author Holger Knublauch
 */
public class SHACLUtil {
	
	public final static Resource[] RESULT_TYPES = {
		DASH.FailureResult,
		DASH.SuccessResult,
		SH.ValidationResult
	};
	
	public final static String SHAPES_FILE_PART = ".shapes.";
	
	public static final String URN_X_SHACL = "urn:x-shacl:";
	
	private static final Set<Property> SPARQL_PROPERTIES = new HashSet<Property>();
	static {
		SPARQL_PROPERTIES.add(SH.ask);
		SPARQL_PROPERTIES.add(SH.construct);
		SPARQL_PROPERTIES.add(SH.select);
		SPARQL_PROPERTIES.add(SH.update);
	}
	
	private static Query propertyLabelQuery = ARQFactory.get().createQuery(
			"PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
			"PREFIX sh: <" + SH.NS + ">\n" +
			"SELECT ?label\n" +
			"WHERE {\n" +
			"    ?arg2 a ?type .\n" +
			"    ?type rdfs:subClassOf* ?class .\n" +
			"    ?shape <" + SH.targetClass + ">* ?class .\n" +
			"    ?shape <" + SH.property + ">|<" + SH.parameter + "> ?p .\n" +
			"    ?p <" + SH.path + "> ?arg1 .\n" +
			"    ?p rdfs:label ?label .\n" +
			"}");

	
	public static void addDirectPropertiesOfClass(Resource cls, Collection<Property> results) {
		for(Resource argument : JenaUtil.getResourceProperties(cls, SH.parameter)) {
			Resource predicate = argument.getPropertyResourceValue(SH.path);
			if(predicate != null && predicate.isURIResource() && !results.contains(predicate)) {
				results.add(JenaUtil.asProperty(predicate));
			}
		}
		for(Resource property : JenaUtil.getResourceProperties(cls, SH.property)) {
			Resource predicate = property.getPropertyResourceValue(SH.path);
			if(predicate != null && predicate.isURIResource() && !results.contains(predicate)) {
				results.add(JenaUtil.asProperty(predicate));
			}
		}
	}


	private static void addIncludes(Graph model, String uri, Set<Graph> graphs, Set<String> reachedURIs) {
		
		graphs.add(model);
		reachedURIs.add(uri);
		
		for(Triple t : model.find(null, OWL.imports.asNode(), null).toList()) {
			if(t.getObject().isURI()) {
				String includeURI = t.getObject().getURI();
				if(!reachedURIs.contains(includeURI)) {
					Model includeModel = ARQFactory.getNamedModel(includeURI);
					if(includeModel != null) {
						Graph includeGraph = includeModel.getGraph();
						addIncludes(includeGraph, includeURI, graphs, reachedURIs);
					}
				}
			}
		}
	}


	/**
	 * Adds all resources from a given sh:target to a given results Set of Nodes.
	 * @param target  the value of sh:target (parameterized or SPARQL target)
	 * @param dataset  the dataset to operate on
	 * @param results  the Set to add the resulting Nodes to
	 */
	public static void addNodesInTarget(Resource target, Dataset dataset, Set<Node> results) {
		for(RDFNode focusNode : getResourcesInTarget(target, dataset)) {
			results.add(focusNode.asNode());
		}
	}
	
	
	/**
	 * Creates an includes Model for a given input Model.
	 * The includes Model is the union of the input Model will all graphs linked via
	 * sh:include (or owl:imports), transitively. 
	 * @param model  the Model to create the includes Model for
	 * @param graphURI  the URI of the named graph represented by Model
	 * @return a Model including the semantics
	 */
	public static Model createIncludesModel(Model model, String graphURI) {
		Set<Graph> graphs = new HashSet<Graph>();
		Graph baseGraph = model.getGraph();
		
		addIncludes(baseGraph, graphURI, graphs, new HashSet<String>());
		
		if(graphs.size() == 1) {
			return model;
		}
		else {
			MultiUnion union = new MultiUnion(graphs.iterator());
			union.setBaseGraph(baseGraph);
			return ModelFactory.createModelForGraph(union);
		}
	}


	public static URI createRandomShapesGraphURI() {
		return URI.create(URN_X_SHACL + UUID.randomUUID());
	}
	
	
	/**
	 * Gets all focus nodes from the default Model of a given dataset.
	 * This includes all targets of all defined targets as well as all instances of classes that
	 * are also shapes.
	 * @param dataset  the Dataset
	 * @param validateShapes  true to include the validation of constraint components
	 * @return a Set of focus Nodes
	 */
	public static Set<Node> getAllFocusNodes(Dataset dataset, boolean validateShapes) {

		Set<Node> results = new HashSet<Node>();
		
		// Add all instances of classes that are also shapes
		Model model = dataset.getDefaultModel();
		for(Resource shape : JenaUtil.getAllInstances(SH.Shape.inModel(model))) {
			if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
				for(Resource instance : JenaUtil.getAllInstances(shape)) {
					results.add(instance.asNode());
				}
			}
		}
		
		// Add all instances of classes mentioned in sh:targetClass triples
		for(Statement s : model.listStatements(null, SH.targetClass, (RDFNode)null).toList()) {
			if(s.getObject().isResource()) {
				if(validateShapes || (!JenaUtil.hasIndirectType(s.getSubject(), SH.ConstraintComponent) &&
						!SH.PropertyShape.equals(s.getObject())) &&
						!SH.Constraint.equals(s.getObject())) {
					for(Resource instance : JenaUtil.getAllInstances(s.getResource())) {
						results.add(instance.asNode());
					}
				}
			}
		}
		
		// Add all objects of sh:targetNode triples
		for(Statement s : model.listStatements(null, SH.targetNode, (RDFNode)null).toList()) {
			results.add(s.getObject().asNode());
		}
		
		// Add all target nodes of sh:target triples
		for(Statement s : model.listStatements(null, SH.target, (RDFNode)null).toList()) {
			if(s.getObject().isResource()) {
				Resource target = s.getResource();
				for(RDFNode focusNode : SHACLUtil.getResourcesInTarget(target, dataset)) {
					results.add(focusNode.asNode());
				}
			}
		}
		
		// Add all objects of the predicate used as sh:targetObjectsOf
		for(RDFNode property : model.listObjectsOfProperty(SH.targetObjectsOf).toList()) {
			if(property.isURIResource()) {
				Property predicate = JenaUtil.asProperty((Resource)property);
				for(RDFNode focusNode : model.listObjectsOfProperty(predicate).toList()) {
					results.add(focusNode.asNode());
				}
			}
		}
		
		// Add all subjects of the predicate used as sh:targetSubjectsOf
		for(RDFNode property : model.listObjectsOfProperty(SH.targetSubjectsOf).toList()) {
			if(property.isURIResource()) {
				Property predicate = JenaUtil.asProperty((Resource)property);
				for(RDFNode focusNode : model.listSubjectsWithProperty(predicate).toList()) {
					results.add(focusNode.asNode());
				}
			}
		}
		
		return results;
	}
	
	
	public static List<SHResult> getAllTopLevelResults(Model model) {
		List<SHResult> results = new LinkedList<SHResult>();
		for(Resource type : RESULT_TYPES) {
			for(Resource r : model.listResourcesWithProperty(RDF.type, type).toList()) {
				if(!model.contains(null, SH.detail, r)) {
					results.add(r.as(SHResult.class));
				}
			}
		}
		return results;
	}
	
	
	/**
	 * Gets all (transitive) superclasses including shapes that reference a class via sh:targetClass.
	 * @param cls  the class to start at
	 * @return a Set of classes and shapes
	 */
	public static Set<Resource> getAllSuperClassesAndShapesStar(Resource cls) {
		Set<Resource> results = new HashSet<Resource>();
		getAllSuperClassesAndShapesStarHelper(cls, results);
		return results;
	}
	
	
	private static void getAllSuperClassesAndShapesStarHelper(Resource node, Set<Resource> results) {
		if(!results.contains(node)) {
			results.add(node);
			{
				StmtIterator it = node.listProperties(RDFS.subClassOf);
				while(it.hasNext()) {
					Statement s = it.next();
					if(s.getObject().isResource()) {
						getAllSuperClassesAndShapesStarHelper(s.getResource(), results);
					}
				}
			}
			{
				StmtIterator it = node.getModel().listStatements(null, SH.targetClass, node);
				while(it.hasNext()) {
					getAllSuperClassesAndShapesStarHelper(it.next().getSubject(), results);
				}
			}
		}
	}
	
	
	public static SHConstraintComponent getConstraintComponentOfValidator(Resource validator) {
		for(Statement s : validator.getModel().listStatements(null, null, validator).toList()) {
			if(SH.validator.equals(s.getPredicate()) || SH.nodeValidator.equals(s.getPredicate()) || SH.propertyValidator.equals(s.getPredicate())) {
				return s.getSubject().as(SHConstraintComponent.class);
			}
		}
		return null;
	}
	
	
	public static Resource getDefaultTypeForConstraintPredicate(Property predicate) {
		if(SH.property.equals(predicate)) {
			return SH.PropertyShape;
		}
		else if(SH.parameter.equals(predicate)) {
			return SH.Parameter;
		}
		else {
			throw new IllegalArgumentException();
		}
	}
	
	
	public static SHParameter getParameterAtClass(Resource cls, Property predicate) {
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.parameter)) {
				if(arg.hasProperty(SH.path, predicate)) {
					return SHFactory.asParameter(arg);
				}
			}
		}
		return null;
	}
	
	
	public static SHParameter getParameterAtInstance(Resource instance, Property predicate) {
		for(Resource type : JenaUtil.getTypes(instance)) {
			SHParameter argument = getParameterAtClass(type, predicate);
			if(argument != null) {
				return argument;
			}
		}
		return null;
	}


	// Simplified to only check for sh:property and sh:parameter (not sh:node etc)
	public static Resource getResourceDefaultType(Resource resource) {
		if(resource.getModel().contains(null, SH.property, resource)) {
			return SH.PropertyShape.inModel(resource.getModel());
		}
		else if(resource.getModel().contains(null, SH.parameter, resource)) {
			return SH.Parameter.inModel(resource.getModel());
		}
		/*
		StmtIterator it = resource.getModel().listStatements(null, null, resource);
		try {
			while(it.hasNext()) {
				Statement s = it.next();
				Resource defaultValueType = JenaUtil.getResourceProperty(s.getPredicate(), DASH.defaultValueType);
				if(defaultValueType != null) {
					return defaultValueType;
				}
			}
		}
		finally {
			it.close();
		}*/
		return null;
	}
	
	
	/**
	 * Gets any locally-defined label for a given property.
	 * The labels are expected to be attached to shapes associated with a given
	 * context resource (instance).
	 * That context resource may for example be the subject of the current UI form.
	 * @param property  the property to get the label of
	 * @param context  the context instance
	 * @return the local label or null if it should fall back to a global label
	 */
	public static String getLocalPropertyLabel(Resource property, Resource context) {
		QuerySolutionMap binding = new QuerySolutionMap();
		binding.add("arg1", property);
		binding.add("arg2", context);
		try(QueryExecution qexec = ARQFactory.get().createQueryExecution(propertyLabelQuery, property.getModel(), binding)) {
		    ResultSet rs = qexec.execSelect();
		    if(rs.hasNext()) {
		        return rs.next().get("label").asLiteral().getLexicalForm();
		    }
		}
		return null;
	}
	
	public static SHPropertyShape getPropertyConstraintAtClass(Resource cls, Property predicate) {
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.property)) {
				if(arg.hasProperty(SH.path, predicate)) {
					return SHFactory.asPropertyShape(arg);
				}
			}
		}
		return null;
	}
	
	
	public static SHPropertyShape getPropertyConstraintAtInstance(Resource instance, Property predicate) {
		for(Resource type : JenaUtil.getTypes(instance)) {
			SHPropertyShape property = getPropertyConstraintAtClass(type, predicate);
			if(property != null) {
				return property;
			}
		}
		return null;
	}
	
	
	/**
	 * Gets all the predicates of all declared sh:properties and sh:parameters
	 * of a given class, including inherited ones.
	 * @param cls  the class to get the predicates of
	 * @return the declared predicates
	 */
	public static List<Property> getAllPropertiesOfClass(Resource cls) {
		List<Property> results = new LinkedList<Property>();
		for(Resource c : getAllSuperClassesAndShapesStar(cls)) {
			addDirectPropertiesOfClass(c, results);
		}
		return results;
	}


	/**
	 * Gets all nodes from a given sh:target.
	 * @param target  the value of sh:target (parameterizable or SPARQL target)
	 * @param dataset  the dataset to operate on
	 * @return an Iterable over the resources
	 */
	public static Iterable<RDFNode> getResourcesInTarget(Resource target, Dataset dataset) {
		Resource type = JenaUtil.getType(target);
		Resource executable;
		SHParameterizableTarget parameterizableTarget = null;
		if(SHFactory.isParameterizableInstance(target)) {
			executable = type;
			parameterizableTarget = SHFactory.asParameterizableTarget(target);
		}
		else {
			executable = target;
		}
		CustomTargetLanguage plugin = CustomTargets.get().getLanguageForTarget(executable);
		if(plugin != null) {
			Set<RDFNode> results = new HashSet<>();
			plugin.createTarget(executable, parameterizableTarget).addTargetNodes(dataset, results);
			return results;
		}
		else {
			return new ArrayList<>();
		}
	}
	
	
	/**
	 * Gets all shapes associated with a given focus node.
	 * This looks for all shapes based on class-based targets.
	 * Future versions will also look for property-based targets.
	 * @param node  the (focus) node
	 * @return a List of shapes
	 */
	public static List<SHNodeShape> getAllShapesAtNode(RDFNode node) {
		return getAllShapesAtNode(node, node instanceof Resource ? JenaUtil.getTypes((Resource)node) : null);
	}
	
	
	public static List<SHNodeShape> getAllShapesAtNode(RDFNode node, Iterable<Resource> types) {
		List<SHNodeShape> results = new LinkedList<>();
		if(node instanceof Resource) {
			Set<Resource> reached = new HashSet<>();
			for(Resource type : types) {
				addAllShapesAtClassOrShape(type, results, reached);
			}
		}
		
		// TODO: support sh:targetObjectsOf and sh:targetSubjectsOf
		
		return results;
	}
	
	
	/**
	 * Gets all sh:Shapes that have a given class in their target, including ConstraintComponents
	 * and the class or shape itself if it is marked as sh:Shape.
	 * Also walks up the class hierarchy.
	 * @param clsOrShape  the class or Shape to get the shapes of
	 * @return the shapes, ordered by the most specialized (subclass) first
	 */
	@SuppressWarnings("unchecked")
	public static List<SHNodeShape> getAllShapesAtClassOrShape(Resource clsOrShape) {
		String key = OntologyOptimizations.get().getKeyIfEnabledFor(clsOrShape.getModel().getGraph());
		if(key != null) {
			key += ".getAllShapesAtClassOrShape(" + clsOrShape + ")";
			return (List<SHNodeShape>) OntologyOptimizations.get().getOrComputeObject(key, (cacheKey) -> {
				List<SHNodeShape> results = new LinkedList<SHNodeShape>();
				addAllShapesAtClassOrShape(clsOrShape, results, new HashSet<Resource>());
				return results;
			});
		}
		else {
			List<SHNodeShape> results = new LinkedList<SHNodeShape>();
			addAllShapesAtClassOrShape(clsOrShape, results, new HashSet<Resource>());
			return results;
		}
	}



	private static void addAllShapesAtClassOrShape(Resource clsOrShape, List<SHNodeShape> results, Set<Resource> reached) {
		addDirectShapesAtClassOrShape(clsOrShape, results);
		reached.add(clsOrShape);
		for(Resource superClass : JenaUtil.getSuperClasses(clsOrShape)) {
			if(!reached.contains(superClass)) {
				addAllShapesAtClassOrShape(superClass, results, reached);
			}
		}
	}
	
	
	/**
	 * Gets the directly associated sh:Shapes that have a given class in their target,
	 * including ConstraintComponents and the class or shape itself if it is marked as sh:Shape.
	 * Does not walk up the class hierarchy.
	 * @param clsOrShape  the class or Shape to get the shapes of
	 * @return the shapes
	 */
	public static Collection<SHNodeShape> getDirectShapesAtClassOrShape(Resource clsOrShape) {
		List<SHNodeShape> results = new LinkedList<SHNodeShape>();
		addDirectShapesAtClassOrShape(clsOrShape, results);
		return results;
	}


	private static void addDirectShapesAtClassOrShape(Resource clsOrShape, List<SHNodeShape> results) {
		if(JenaUtil.hasIndirectType(clsOrShape, SH.Shape) && !results.contains(clsOrShape)) {
			SHNodeShape shape = SHFactory.asNodeShape(clsOrShape);
			if(!shape.isDeactivated()) {
				results.add(shape);
			}
		}
		// More correct would be: if(JenaUtil.hasIndirectType(clsOrShape, RDFS.Class)) {
		{
			StmtIterator it = clsOrShape.getModel().listStatements(null, SH.targetClass, clsOrShape);
			while(it.hasNext()) {
				Resource subject = it.next().getSubject();
				if(!results.contains(subject)) {
					SHNodeShape shape = SHFactory.asNodeShape(subject);
					if(!shape.isDeactivated()) {
						results.add(shape);
					}
				}
			}
		}
	}
	
	
	public static Set<Resource> getDirectShapesAtResource(Resource resource) {
		Set<Resource> shapes = new HashSet<>();
		for(Resource type : JenaUtil.getResourceProperties(resource, RDF.type)) {
			if(JenaUtil.hasIndirectType(type, SH.NodeShape)) {
				shapes.add(type);
			}
			Set<Resource> ts = JenaUtil.getAllSuperClassesStar(type);
			for(Resource s : ts) {
				{
					StmtIterator it = type.getModel().listStatements(null, DASH.applicableToClass, s);
					while(it.hasNext()) {
						Resource shape = it.next().getSubject();
						shapes.add(shape);
					}
				}
				{
					StmtIterator it = type.getModel().listStatements(null, SH.targetClass, s);
					while(it.hasNext()) {
						Resource shape = it.next().getSubject();
						shapes.add(shape);
					}
				}
			}
		}
		return shapes;
	}
	
	
	public static List<RDFNode> getTargetNodes(Resource shape, Dataset dataset) {
		return getTargetNodes(shape, dataset, false);
	}

	
	public static List<RDFNode> getTargetNodes(Resource shape, Dataset dataset, boolean includeApplicableToClass) {
		
		Model dataModel = dataset.getDefaultModel();

		Set<RDFNode> results = new HashSet<RDFNode>();
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			results.addAll(JenaUtil.getAllInstances(shape.inModel(dataModel)));
		}
		
		for(Resource targetClass : JenaUtil.getResourceProperties(shape, SH.targetClass)) {
			results.addAll(JenaUtil.getAllInstances(targetClass.inModel(dataModel)));
		}
		
		for(RDFNode targetNode : shape.getModel().listObjectsOfProperty(shape, SH.targetNode).toList()) {
			results.add(targetNode.inModel(dataModel));
		}
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetSubjectsOf)) {
			for(Statement s : dataModel.listStatements(null, JenaUtil.asProperty(sof), (RDFNode)null).toList()) {
				results.add(s.getSubject());
			}
		}
		
		for(Resource sof : JenaUtil.getResourceProperties(shape, SH.targetObjectsOf)) {
			for(Statement s : dataModel.listStatements(null, JenaUtil.asProperty(sof), (RDFNode)null).toList()) {
				results.add(s.getObject());
			}
		}
		
		for(Resource target : JenaUtil.getResourceProperties(shape, SH.target)) {
			for(RDFNode targetNode : SHACLUtil.getResourcesInTarget(target, dataset)) {
				results.add(targetNode);
			}
		}

		if(includeApplicableToClass) {
			for(Resource targetClass : JenaUtil.getResourceProperties(shape, DASH.applicableToClass)) {
				results.addAll(JenaUtil.getAllInstances(targetClass.inModel(dataModel)));
			}
		}

		return new ArrayList<RDFNode>(results);
	}

	
	public static List<Resource> getTypes(Resource subject) {
		List<Resource> types = JenaUtil.getTypes(subject);
		if(types.isEmpty()) {
			Resource defaultType = getResourceDefaultType(subject);
			if(defaultType != null) {
				return Collections.singletonList(defaultType);
			}
		}
		return types;
	}
	
	
	public static boolean hasMinSeverity(Resource severity, Resource minSeverity) {
		if(minSeverity == null || SH.Info.equals(minSeverity)) {
			return true;
		}
		if(SH.Warning.equals(minSeverity)) {
			return !SH.Info.equals(severity);
		}
		else { // SH.Error
			return SH.Violation.equals(severity);
		}
	}
	
	
	public static boolean isDeactivated(Resource resource) {
		return resource.hasProperty(SH.deactivated, JenaDatatypes.TRUE);
	}
	
	
	public static boolean isParameterAtInstance(Resource subject, Property predicate) {
		for(Resource type : getTypes(subject)) {
			Resource arg = getParameterAtClass(type, predicate);
			if(arg != null) {
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean isSPARQLProperty(Property property) {
		return SPARQL_PROPERTIES.contains(property);
	}
	
	
	/**
	 * Checks whether the SHACL vocabulary is present in a given Model.
	 * The condition is that the SHACL namespace must be declared and
	 * sh:Constraint must have an rdf:type.
	 * @param model  the Model to check
	 * @return true if SHACL is present
	 */
	public static boolean exists(Model model) {
		return model != null && exists(model.getGraph());
	}
	
	
	public static boolean exists(Graph graph) {
		if(graph instanceof OptimizedMultiUnion) {
			return ((OptimizedMultiUnion)graph).getIncludesSHACL();
		}
		else {
	    	return graph != null &&
	        		SH.NS.equals(graph.getPrefixMapping().getNsPrefixURI(SH.PREFIX)) && 
	        		graph.contains(SH.Shape.asNode(), RDF.type.asNode(), Node.ANY);
		}
	}


	public static URI withShapesGraph(Dataset dataset) {
		URI shapesGraphURI = createRandomShapesGraphURI();
		Model shapesModel = createShapesModel(dataset);
		dataset.addNamedModel(shapesGraphURI.toString(), shapesModel);
		return shapesGraphURI;
	}


	/**
	 * Creates a shapes Model for a given input Model.
	 * The shapes Model is the union of the input Model with all graphs referenced via
	 * the sh:shapesGraph property (and transitive includes or shapesGraphs of those).
	 * @param model  the Model to create the shapes Model for
	 * @return a shapes graph Model
	 */
	private static Model createShapesModel(Dataset dataset) {
		
		Model model = dataset.getDefaultModel();
		Set<Graph> graphs = new HashSet<Graph>();
		Graph baseGraph = model.getGraph();
		graphs.add(baseGraph);
		
		for(Statement s : model.listStatements(null, SH.shapesGraph, (RDFNode)null).toList()) {
			if(s.getObject().isURIResource()) {
				String graphURI = s.getResource().getURI();
				Model sm = dataset.getNamedModel(graphURI);
				graphs.add(sm.getGraph());
				// TODO: Include includes of sm
			}
		}
		
		if(graphs.size() > 1) {
			MultiUnion union = new MultiUnion(graphs.iterator());
			union.setBaseGraph(baseGraph);
			return ModelFactory.createModelForGraph(union);
		}
		else {
			return model;
		}
	}


	public static boolean isInTarget(RDFNode focusNode, Dataset dataset, Resource target) {
		SHParameterizableTarget parameterizableTarget = null;
		Resource executable = target;
		if(SHFactory.isParameterizableInstance(target)) {
			parameterizableTarget = SHFactory.asParameterizableTarget(target);
			executable = parameterizableTarget.getParameterizable();
		}
		CustomTargetLanguage plugin = CustomTargets.get().getLanguageForTarget(executable);
		if(plugin != null) {
			return plugin.createTarget(executable, parameterizableTarget).contains(dataset, focusNode);
		}
		else {
			return false;
		}
	}


	public static Node walkPropertyShapesHelper(Node propertyShape, Graph graph) {
		Node valueType = JenaNodeUtil.getObject(propertyShape, SH.class_.asNode(), graph);
		if(valueType != null) {
			return valueType;
		}
		Node datatype = JenaNodeUtil.getObject(propertyShape, SH.datatype.asNode(), graph);
		if(datatype != null) {
			return datatype;
		}
		ExtendedIterator<Triple> ors = graph.find(propertyShape, SH.or.asNode(), Node.ANY);
		while(ors.hasNext()) {
			Node or = ors.next().getObject();
			Node first = JenaNodeUtil.getObject(or, RDF.first.asNode(), graph);
			if(!first.isLiteral()) {
				Node cls = JenaNodeUtil.getObject(first, SH.class_.asNode(), graph);
				if(cls != null) {
					ors.close();
					return cls;
				}
				datatype = JenaNodeUtil.getObject(first, SH.datatype.asNode(), graph);
				if(datatype != null) {
					ors.close();
					return datatype;
				}
			}
		}
		if(graph.contains(propertyShape, SH.node.asNode(), DASH.ListShape.asNode())) {
			return RDF.List.asNode();
		}
		return null;
	}
}
