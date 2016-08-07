package org.topbraid.shacl.util;

import java.net.URI;
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
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.ExecutionLanguageSelector;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameter;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.model.SHPropertyConstraint;
import org.topbraid.shacl.model.SHResult;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.util.OptimizedMultiUnion;

/**
 * Various SHACL-related utility methods that didn't fit elsewhere.
 * 
 * @author Holger Knublauch
 */
public class SHACLUtil {
	
	public final static String MACROS_FILE_PART = ".macros.";
	
	public final static String SHAPES_FILE_PART = ".shapes.";
	
	private static final Set<Property> SPARQL_PROPERTIES = new HashSet<Property>();
	static {
		SPARQL_PROPERTIES.add(SH.ask);
		SPARQL_PROPERTIES.add(SH.construct);
		SPARQL_PROPERTIES.add(SH.select);
		SPARQL_PROPERTIES.add(SH.update);
	}

	private final static Set<Resource> classesWithDefaultType = new HashSet<Resource>();
	static {
		classesWithDefaultType.add(SH.Shape);
		classesWithDefaultType.add(SH.Parameter);
		classesWithDefaultType.add(SH.PropertyConstraint);
		classesWithDefaultType.add(SH.SPARQLConstraint);
	}
	
	private final static List<Property> constraintProperties = new LinkedList<Property>();
	static {
		constraintProperties.add(SH.property);
		constraintProperties.add(SH.sparql);
	}
	
	private final static List<Property> constraintPropertiesIncludingParameter = new LinkedList<Property>();
	static {
		constraintPropertiesIncludingParameter.addAll(constraintProperties);
		constraintPropertiesIncludingParameter.add(SH.parameter);
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
			"    ?p <" + SH.predicate + "> ?arg1 .\n" +
			"    ?p rdfs:label ?label .\n" +
			"}");

	public static void addDirectPropertiesOfClass(Resource cls, Collection<Property> results) {
		for(Resource argument : JenaUtil.getResourceProperties(cls, SH.parameter)) {
			Resource predicate = JenaUtil.getPropertyResourceValue(argument, SH.predicate);
			if(predicate != null && predicate.isURIResource() && !results.contains(predicate)) {
				results.add(JenaUtil.asProperty(predicate));
			}
		}
		for(Resource property : JenaUtil.getResourceProperties(cls, SH.property)) {
			Resource predicate = JenaUtil.getPropertyResourceValue(property, SH.predicate);
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


	public static List<Property> getAllConstraintProperties(boolean validateShapes) {
		return validateShapes ? constraintPropertiesIncludingParameter : constraintProperties;
	}
	
	
	public static List<SHResult> getAllResults(Model model) {
		List<SHResult> results = new LinkedList<SHResult>();
		// TODO: Not pretty code, not generic
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.ValidationResult).toList()) {
			results.add(r.as(SHResult.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, DASH.FailureResult).toList()) {
			results.add(r.as(SHResult.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, DASH.SuccessResult).toList()) {
			results.add(r.as(SHResult.class));
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
			if(SH.validator.equals(s.getPredicate()) || SH.shapeValidator.equals(s.getPredicate()) || SH.propertyValidator.equals(s.getPredicate())) {
				return s.getSubject().as(SHConstraintComponent.class);
			}
		}
		return null;
	}
	
	
	public static Resource getDefaultTypeForConstraintPredicate(Property predicate) {
		if(SH.property.equals(predicate)) {
			return SH.PropertyConstraint;
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
				if(arg.hasProperty(SH.predicate, predicate)) {
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

	
	public static Resource getResourceDefaultType(Resource resource) {
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
		}
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
	
	public static SHPropertyConstraint getPropertyConstraintAtClass(Resource cls, Property predicate) {
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.property)) {
				if(arg.hasProperty(SH.predicate, predicate)) {
					return SHFactory.asPropertyConstraint(arg);
				}
			}
		}
		return null;
	}
	
	
	public static SHPropertyConstraint getPropertyConstraintAtInstance(Resource instance, Property predicate) {
		for(Resource type : JenaUtil.getTypes(instance)) {
			SHPropertyConstraint property = getPropertyConstraintAtClass(type, predicate);
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
	 */
	public static Iterable<RDFNode> getResourcesInTarget(Resource target, Dataset dataset) {
		Resource type = JenaUtil.getType(target);
		Resource executable;
		SHParameterizableTarget parameterizableTarget = null;
		if(SHFactory.isSPARQLTarget(target)) {
			executable = target;
		}
		else {
			executable = type;
			parameterizableTarget = SHFactory.asParameterizableTarget(target);
		}
		ExecutionLanguage language = ExecutionLanguageSelector.get().getLanguageForTarget(executable);
		return language.executeTarget(dataset, executable, parameterizableTarget);
	}
	
	
	/**
	 * Gets all shapes associated with a given focus node.
	 * This looks for all shapes based on class-based targets.
	 * Future versions will also look for property-based targets.
	 * @param node  the (focus) node
	 * @return a List of shapes
	 */
	public static List<SHShape> getAllShapesAtNode(RDFNode node) {
		return getAllShapesAtNode(node, node instanceof Resource ? JenaUtil.getTypes((Resource)node) : null);
	}
	
	
	public static List<SHShape> getAllShapesAtNode(RDFNode node, Iterable<Resource> types) {
		List<SHShape> results = new LinkedList<SHShape>();
		if(node instanceof Resource) {
			Set<Resource> reached = new HashSet<Resource>();
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
	public static List<SHShape> getAllShapesAtClassOrShape(Resource clsOrShape) {
		List<SHShape> results = new LinkedList<SHShape>();
		Set<Resource> reached = new HashSet<Resource>();
		addAllShapesAtClassOrShape(clsOrShape, results, reached);
		return results;
	}
	
	
	private static void addAllShapesAtClassOrShape(Resource clsOrShape, List<SHShape> results, Set<Resource> reached) {
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
	public static Collection<SHShape> getDirectShapesAtClassOrShape(Resource clsOrShape) {
		List<SHShape> results = new LinkedList<SHShape>();
		addDirectShapesAtClassOrShape(clsOrShape, results);
		return results;
	}


	private static void addDirectShapesAtClassOrShape(Resource clsOrShape, List<SHShape> results) {
		if(JenaUtil.hasIndirectType(clsOrShape, SH.Shape) && !results.contains(clsOrShape)) {
			results.add(SHFactory.asShape(clsOrShape));
		}
		// More correct would be: if(JenaUtil.hasIndirectType(clsOrShape, RDFS.Class)) {
		{
			StmtIterator it = clsOrShape.getModel().listStatements(null, SH.targetClass, clsOrShape);
			while(it.hasNext()) {
				Resource subject = it.next().getSubject();
				if(!results.contains(subject)) {
					SHShape shape = SHFactory.asShape(subject);
					if(!shape.isDeactivated()) {
						results.add(shape);
					}
				}
			}
		}
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
	
	
	public static boolean isClassWithDefaultType(Resource cls) {
		return classesWithDefaultType.contains(cls);
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
	
	
	public static Model withDefaultValueTypeInferences(Model model) {
		return ModelFactory.createModelForGraph(new MultiUnion(new Graph[] {
				model.getGraph(),
				DASH.createDefaultValueTypesModel(model).getGraph()
		}));
	}


	public static URI withShapesGraph(Dataset dataset) {
		URI shapesGraphURI = URI.create("urn:x-shacl:" + UUID.randomUUID());
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
}
