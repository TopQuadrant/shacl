package org.topbraid.shacl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.constraints.ExecutionLanguage;
import org.topbraid.shacl.constraints.ExecutionLanguageSelector;
import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLPropertyConstraint;
import org.topbraid.shacl.model.SHACLResult;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

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

/**
 * Various SHACL-related utility methods that didn't fit elsewhere.
 * 
 * @author Holger Knublauch
 */
public class SHACLUtil {
	
	public final static String MACROS_FILE_PART = ".macros.";
	
	public final static String SHAPES_FILE_PART = ".shapes.";
	
	private final static Set<Resource> classesWithDefaultType = new HashSet<Resource>();
	static {
		classesWithDefaultType.add(SH.NodeConstraint);
		classesWithDefaultType.add(SH.Argument);
		classesWithDefaultType.add(SH.InversePropertyConstraint);
		classesWithDefaultType.add(SH.PropertyConstraint);
	}
	
	private final static List<Property> constraintProperties = new LinkedList<Property>();
	static {
		constraintProperties.add(SH.argument);
		constraintProperties.add(SH.constraint);
		constraintProperties.add(SH.inverseProperty);
		constraintProperties.add(SH.property);
	}
	
	private static Query propertyLabelQuery = ARQFactory.get().createQuery(
			"PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
			"PREFIX sh: <" + SH.NS + ">\n" +
			"SELECT ?label\n" +
			"WHERE {\n" +
			"    ?arg2 a ?type .\n" +
			"    ?type rdfs:subClassOf* ?class .\n" +
			"    ?shape sh:scopeClass* ?class .\n" +
			"    ?shape sh:property|sh:argument ?p .\n" +
			"    ?p sh:predicate ?arg1 .\n" +
			"    ?p rdfs:label ?label .\n" +
			"}");

	public static void addDirectPropertiesOfClass(Resource cls, Collection<Property> results) {
		for(Resource argument : JenaUtil.getResourceProperties(cls, SH.argument)) {
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
	 * Adds all resources from a given sh:scope to a given results Set of Nodes.
	 * @param scope  the value of sh:scope (template call or native scope)
	 * @param dataset  the dataset to operate on
	 * @param results  the Set to add the resulting Nodes to
	 */
	public static void addNodesInScope(Resource scope, Dataset dataset, Set<Node> results) {
		for(RDFNode focusNode : getResourcesInScope(scope, dataset)) {
			results.add(focusNode.asNode());
		}
	}
	
	
	/**
	 * Runs the rule to infer missing rdf:type triples for certain blank nodes.
	 * @param model  the input Model
	 * @return a new Model containing the inferred triples
	 */
	public static Model createDefaultValueTypesModel(Model model) {
		String sparql = JenaUtil.getStringProperty(SH.DefaultValueTypeRule.inModel(model), SH.sparql);
		if(sparql == null) {
			throw new IllegalArgumentException("Shapes graph does not include " + SH.PREFIX + ":" + SH.DefaultValueTypeRule);
		}
		Model resultModel = JenaUtil.createMemoryModel();
		MultiUnion multiUnion = new MultiUnion(new Graph[] {
			model.getGraph(),
			resultModel.getGraph()
		});
		Model unionModel = ModelFactory.createModelForGraph(multiUnion);
		Query query = ARQFactory.get().createQuery(model, sparql);
		QueryExecution qexec = ARQFactory.get().createQueryExecution(query, unionModel);
		qexec.execConstruct(resultModel);
		qexec.close();
		return resultModel;
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


	public static List<Property> getAllConstraintProperties() {
		return constraintProperties;
	}
	
	
	public static List<SHACLResult> getAllResults(Model model) {
		List<SHACLResult> results = new LinkedList<SHACLResult>();
		// TODO: Not pretty code, not generic
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.ValidationResult).toList()) {
			results.add(r.as(SHACLResult.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, DASH.FailureResult).toList()) {
			results.add(r.as(SHACLResult.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, DASH.SuccessResult).toList()) {
			results.add(r.as(SHACLResult.class));
		}
		return results;
	}
	
	
	/**
	 * Gets all (transitive) superclasses including shapes that reference a class via sh:scopeClass.
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
				StmtIterator it = node.getModel().listStatements(null, SH.scopeClass, node);
				while(it.hasNext()) {
					getAllSuperClassesAndShapesStarHelper(it.next().getSubject(), results);
				}
			}
		}
	}
	
	
	public static SHACLArgument getArgumentAtClass(Resource cls, Property predicate) {
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.argument)) {
				if(arg.hasProperty(SH.predicate, predicate)) {
					return SHACLFactory.asArgument(arg);
				}
			}
		}
		return null;
	}
	
	
	public static SHACLArgument getArgumentAtInstance(Resource instance, Property predicate) {
		for(Resource type : JenaUtil.getTypes(instance)) {
			SHACLArgument argument = getArgumentAtClass(type, predicate);
			if(argument != null) {
				return argument;
			}
		}
		return null;
	}

	
	public static Resource getDefaultTemplateType(Resource resource) {
		StmtIterator it = resource.getModel().listStatements(null, null, resource);
		try {
			while(it.hasNext()) {
				Statement s = it.next();
				Resource defaultValueType = JenaUtil.getResourceProperty(s.getPredicate(), SH.defaultValueType);
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
		QueryExecution qexec = ARQFactory.get().createQueryExecution(propertyLabelQuery, property.getModel(), binding);
		ResultSet rs = qexec.execSelect();
		try {
			if(rs.hasNext()) {
				return rs.next().get("label").asLiteral().getLexicalForm();
			}
		}
		finally {
			qexec.close();
		}
		return null;
	}
	
	
	public static SHACLPropertyConstraint getPropertyConstraintAtClass(Resource cls, Property predicate) {
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.property)) {
				if(arg.hasProperty(SH.predicate, predicate)) {
					return SHACLFactory.asPropertyConstraint(arg);
				}
			}
		}
		return null;
	}
	
	
	public static SHACLPropertyConstraint getPropertyConstraintAtInstance(Resource instance, Property predicate) {
		for(Resource type : JenaUtil.getTypes(instance)) {
			SHACLPropertyConstraint property = getPropertyConstraintAtClass(type, predicate);
			if(property != null) {
				return property;
			}
		}
		return null;
	}
	
	
	/**
	 * Gets all the predicates of all declared sh:properties and sh:arguments
	 * of a given class, including inherited ones.
	 * @param cls  the class to get the predicates of
	 * @return the declared predicates
	 */
	public static List<Property> getPropertiesOfClass(Resource cls) {
		List<Property> results = new LinkedList<Property>();
		for(Resource c : getAllSuperClassesAndShapesStar(cls)) {
			addDirectPropertiesOfClass(c, results);
		}
		return results;
	}


	/**
	 * Gets all nodes from a given sh:scope.
	 * @param scope  the value of sh:scope (template call or native scope)
	 * @param dataset  the dataset to operate on
	 */
	public static Iterable<RDFNode> getResourcesInScope(Resource scope, Dataset dataset) {
		Resource type = JenaUtil.getType(scope);
		Resource executable;
		SHACLTemplateCall templateCall = null;
		if(SHACLFactory.isNativeScope(scope)) {
			executable = scope;
		}
		else {
			executable = type;
			templateCall = SHACLFactory.asTemplateCall(scope);
		}
		ExecutionLanguage language = ExecutionLanguageSelector.get().getLanguageForScope(executable);
		return language.executeScope(dataset, executable, templateCall);
	}
	
	
	public static List<Resource> getTypes(Resource subject) {
		List<Resource> types = JenaUtil.getTypes(subject);
		if(types.isEmpty()) {
			Resource defaultType = getDefaultTemplateType(subject);
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
	
	
	public static boolean isArgumentAtInstance(Resource subject, Property predicate) {
		for(Resource type : getTypes(subject)) {
			Resource arg = getArgumentAtClass(type, predicate);
			if(arg != null) {
				return true;
			}
		}
		return false;
	}
	
	
	public static boolean isClassWithDefaultType(Resource cls) {
		return classesWithDefaultType.contains(cls);
	}
	
	
	/**
	 * Checks whether the SHACL vocabulary is present in a given Model.
	 * The condition is that the SHACL namespace must be declared and
	 * sh:Constraint must have an rdf:type.
	 * @param model  the Model to check
	 * @return true if SHACL is present
	 */
	public static boolean exists(Model model) {
    	return model != null &&
        		SH.NS.equals(model.getNsPrefixURI(SH.PREFIX)) && 
        		model.contains(SH.NativeConstraint, RDF.type, (RDFNode)null);
	}
	
	
	public static Model withDefaultValueTypeInferences(Model model) {
		return ModelFactory.createModelForGraph(new MultiUnion(new Graph[] {
				model.getGraph(),
				SHACLUtil.createDefaultValueTypesModel(model).getGraph()
		}));
	}
}
