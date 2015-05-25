package org.topbraid.shacl.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.model.SHACLArgument;
import org.topbraid.shacl.model.SHACLConstraintViolation;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLPropertyConstraint;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.compose.MultiUnion;
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

public class SHACLUtil {
	
	private final static Set<Resource> classesWithDefaultType = new HashSet<Resource>();
	static {
		classesWithDefaultType.add(SH.NativeConstraint);
		classesWithDefaultType.add(SH.Argument);
		classesWithDefaultType.add(SH.InversePropertyConstraint);
		classesWithDefaultType.add(SH.PropertyConstraint);
	}
	

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
		List<Property> properties = new LinkedList<Property>();
		properties.add(SH.argument);
		properties.add(SH.constraint);
		properties.add(SH.inverseProperty);
		properties.add(SH.property);
		return properties;
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
				else if(SH.inverseProperty.equals(s.getPredicate())) {
					return SH.InversePropertyConstraint.inModel(resource.getModel());
				}
				
				// TODO: maybe handle other properties
			}
		}
		finally {
			it.close();
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
