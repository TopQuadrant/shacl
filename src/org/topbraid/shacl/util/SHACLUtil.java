package org.topbraid.shacl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLConstraintViolation;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLNativeConstraint;
import org.topbraid.shacl.model.SHACLPropertyConstraint;
import org.topbraid.shacl.model.SHACLResource;
import org.topbraid.shacl.model.SHACLRule;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Various SHACL-related utility methods that didn't fit elsewhere.
 * 
 * @author Holger Knublauch
 */
public class SHACLUtil {
	
	private final static Set<Resource> classesWithDefaultType = new HashSet<Resource>();
	static {
		classesWithDefaultType.add(SH.NativeConstraint);
		classesWithDefaultType.add(SH.NativeRule);
		classesWithDefaultType.add(SH.NativeScope);
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
		// TODO add pathConstraint
	}
	
	private final static List<Property> ruleProperties = new LinkedList<Property>();
	static {
		ruleProperties.add(SH.rule);
		// TODO add pathConstraint
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
		
		ExtendedIterator<Triple> includes = model.find(null, SH.include.asNode(), null);
		ExtendedIterator<Triple> imports = model.find(null, OWL.imports.asNode(), null);
		for(Triple t : includes.andThen(imports).toList()) {
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
	
	public static List<Property> getAllRuleProperties() {
		return ruleProperties;
	}
	
	
	public static List<SHACLConstraintViolation> getAllConstraintViolations(Model model) {
		List<SHACLConstraintViolation> results = new LinkedList<SHACLConstraintViolation>();
		// TODO: Not pretty code, not generic
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.Warning).toList()) {
			results.add(r.as(SHACLConstraintViolation.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.Error).toList()) {
			results.add(r.as(SHACLConstraintViolation.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.FatalError).toList()) {
			results.add(r.as(SHACLConstraintViolation.class));
		}
		return results;
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
				if(SH.property.equals(s.getPredicate())) {
					return SH.PropertyConstraint.inModel(resource.getModel());
				}
				else if(SH.argument.equals(s.getPredicate())) {
					return SH.Argument.inModel(resource.getModel());
				}
				else if(SH.constraint.equals(s.getPredicate())) {
					return SH.NativeConstraint.inModel(resource.getModel());
				}
				else if(SH.rule.equals(s.getPredicate())) {
					return SH.NativeRule.inModel(resource.getModel());
				}
				else if(SH.scope.equals(s.getPredicate())) {
					return SH.NativeScope.inModel(resource.getModel());
				}
				else if(SH.inverseProperty.equals(s.getPredicate())) {
					return SH.InversePropertyConstraint.inModel(resource.getModel());
				}
				
				// TODO: maybe handle other properties
				// TODO: handle rule template calls
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
	
	public static List<SHACLResource> getAllConstraintsAtClass(Resource cls) {
		List<SHACLResource> results = new LinkedList<SHACLResource>();
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.property)) {
				results.add(arg.as(SHACLPropertyConstraint.class));
			}
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.constraint)) {
				results.add(arg.as(SHACLNativeConstraint.class));
			}
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.inverseProperty)) {
				results.add(arg.as(SHACLPropertyConstraint.class));
			}
		}
		return results;
	}
	
	
	public static List<SHACLResource> getAllConstraintsAtInstance(Model m, Resource instance) {
		List<SHACLResource> constraints = new LinkedList<SHACLResource>();
		Resource instance2 = m.getResource(instance.getURI());

		for(Resource type : JenaUtil.getTypes(instance2)) {
			constraints.addAll(getAllConstraintsAtClass(type));
		}
		if(constraints != null) {
			return constraints;
		}
		return null;
	}
	
	
	public static List<SHACLRule> getAllRules(Model model) {
		List<SHACLRule> results = new LinkedList<SHACLRule>();
		// TODO: Not pretty code, not generic
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.Rule).toList()) {
			results.add(r.as(SHACLRule.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.NativeRule).toList()) {
			results.add(r.as(SHACLRule.class));
		}
		for(Resource r : model.listResourcesWithProperty(RDF.type, SH.TemplateRule).toList()) {
			results.add(r.as(SHACLRule.class));
		}
		return results;
	}
	
	public static List<SHACLRule> getRulesAtClass(Resource cls) {
		List<SHACLRule> results = new LinkedList<SHACLRule>();
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			for(Resource arg : JenaUtil.getResourceProperties(c, SH.rule)) {
				if(SHACLFactory.isNativeRule(arg))
					results.add(SHACLFactory.asNativeRule(arg));
				else if(SHACLFactory.isTemplateCall(arg))
					results.add(SHACLFactory.asTemplateRule(arg));
			}
		}
		return results;
	}
	
	
	public static List<SHACLRule> getRulesAtInstance(Resource instance, Property predicate) {
		List<SHACLRule> results = new LinkedList<SHACLRule>();
		for(Resource type : JenaUtil.getTypes(instance)) {
			results.addAll(getRulesAtClass(type));
			if(results != null) {
				return results;
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
		for(Resource c : JenaUtil.getAllSuperClassesStar(cls)) {
			addDirectPropertiesOfClass(c, results);
		}
		return results;
	}
	
	
	public static List<Resource> getTypes(Resource subject) {
		List<Resource> types = JenaUtil.getTypes(subject);
		if(types.isEmpty() && subject.isAnon()) {
			Resource defaultType = getDefaultTemplateType(subject);
			if(defaultType != null) {
				return Collections.singletonList(defaultType);
			}
		}
		return types;
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
}
