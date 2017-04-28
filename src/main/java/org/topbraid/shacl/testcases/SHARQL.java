package org.topbraid.shacl.testcases;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * A simple SHACL-SPARQL validation engine.
 * By default only supports sh:ConstraintComponents, sh:property and sh:sparql.
 * 
 * If other SHACL Core features are required, a suitable graph with user-defined
 * constraint components needs to be added to the shapes graph.
 */
public class SHARQL {
	
	private final static String SH = "http://www.w3.org/ns/shacl#";
	
	private Model data;
	
	private Resource report;
	
	private Model results = ModelFactory.createDefaultModel();
	
	private Model shapes;

	public SHARQL(Model data, Model shapes) {
		this.data = data;
		this.shapes = shapes;
		report = results.createResource(sh("ValidationReport"));
	}
	
	public Resource validate() {
		for(Resource shape : getShapesWithTarget()) {
			validateShape(shape, getTargetNodes(shape));
		}
		report.addProperty(sh("conforms"), ResourceFactory.createTypedLiteral(!results.contains(report, sh("result"), (RDFNode)null)));
		return report;
	}
	
	private void validateShape(Resource shape, Collection<RDFNode> targetNodes) {
		log("Validating shape " + shape);
		for(Resource property : getResources(shape, sh("property"))) {
			validateShape(property, targetNodes); // Recurse into sh:property constraints
		}
		for(RDFNode targetNode : targetNodes) {
			for(Resource sparql : getResources(shape, sh("sparql"))) {
				validateSPARQL(shape, sparql, targetNode);
			}
			for(Resource component : getInstances(sh("ConstraintComponent"))) {
				validateComponent(shape, component, targetNode);
			}
		}
	}
	
	private void validateComponent(Resource shape, Resource component, RDFNode focusNode) {
		
		// Walk through all declared parameters of the given constraint component
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		initialBinding.add("this", focusNode);
		for(Resource param : getResources(component, sh("parameter"))) {
			Property predicate = param.getPropertyResourceValue(sh("path")).as(Property.class);
			boolean optional = param.hasProperty(sh("optional"), ResourceFactory.createTypedLiteral(true));
			Statement value = shape.getProperty(predicate);
			if(value != null) {
				initialBinding.add(predicate.getLocalName(), value.getObject());
			}
			else if(!optional) {
				return;
			}
		}
		
		// See 6.2.3
		Resource shapePath = shape.getPropertyResourceValue(sh("path"));
		Resource validator = null;
		if(shapePath != null) {
			validator = getResource(component, sh("propertyValidator"));
		}
		else {
			validator = getResource(component, sh("nodeValidator"));
		}
		if(validator == null) {
			validator = getResource(component, sh("validator"));
			if(validator == null) {
				return;
			}
		}
		
		Property queryPred = isInstanceOf(validator, sh("SPARQLAskValidator")) ? sh("ask") : sh("select");
		Query query = createQuery(validator, queryPred, shapePath);
		if(query.isAskType()) {
			List<RDFNode> valueNodes;
			if(shapePath == null) {
				valueNodes = Collections.singletonList(focusNode);
			}
			else {
				valueNodes = getObjects((Resource)focusNode.inModel(data), shapePath.as(Property.class));
			}
			for(RDFNode valueNode : valueNodes) {
				QueryExecution qexec = QueryExecutionFactory.create(query, data, initialBinding);
				initialBinding.add("value", valueNode);
				if(!qexec.execAsk()) {
					Resource result = results.createResource(sh("ValidationResult"));
					report.addProperty(sh("result"), result);
					result.addProperty(sh("sourceConstraintComponent"), component);
					result.addProperty(sh("sourceShape"), shape);
					Statement severity = shape.getProperty(sh("severity"));
					if(severity != null) {
						result.addProperty(sh("resultSeverity"), severity.getObject());
					}
					else {
						result.addProperty(sh("resultSeverity"), sh("Violation"));
					}
					result.addProperty(sh("focusNode"), focusNode);
					if(shapePath != null) {
						result.addProperty(sh("resultPath"), shapePath); // Should really be a clone
					}
					result.addProperty(sh("value"), valueNode);
					if(validator.hasProperty(sh("message"))) {
						for(RDFNode m : getObjects(validator, sh("message"))) {
							result.addProperty(sh("resultMessage"), m);
						}
					}
					else {
						for(RDFNode m : getObjects(component, sh("message"))) {
							result.addProperty(sh("resultMessage"), m);
						}
					}
				}
			}
		}
		else {
			QueryExecution qexec = QueryExecutionFactory.create(query, data, initialBinding);
			ResultSet rs = qexec.execSelect();
			while(rs.hasNext()) {
				QuerySolution qs = rs.next();
				Resource result = results.createResource(sh("ValidationResult"));
				report.addProperty(sh("result"), result);
				result.addProperty(sh("sourceConstraintComponent"), component);
				result.addProperty(sh("sourceShape"), shape);
				Statement severity = shape.getProperty(sh("severity"));
				if(severity != null) {
					result.addProperty(sh("resultSeverity"), severity.getObject());
				}
				else {
					result.addProperty(sh("resultSeverity"), sh("Violation"));
				}
				result.addProperty(sh("focusNode"), qs.get("this"));
				RDFNode path = qs.get("path");
				if(path != null && path.isURIResource()) {
					result.addProperty(sh("resultPath"), path);
				}
				else {
					if(shapePath != null) {
						result.addProperty(sh("resultPath"), shapePath); // Should really be a clone
					}
				}
				RDFNode value = qs.get("value");
				if(value != null) {
					result.addProperty(sh("value"), value);
				}
				else {
					result.addProperty(sh("value"), focusNode);
				}
				RDFNode message = qs.get("message");
				if(message != null) {
					result.addProperty(sh("resultMessage"), message);
				}
				else if(validator.hasProperty(sh("message"))) {
					for(RDFNode m : getObjects(validator, sh("message"))) {
						result.addProperty(sh("resultMessage"), m);
					}
				}
				else {
					for(RDFNode m : getObjects(component, sh("message"))) {
						result.addProperty(sh("resultMessage"), m);
					}
				}
			}
		}
	}
	
	private void validateSPARQL(Resource shape, Resource sparql, RDFNode focusNode) {
		Resource shapePath = shape.getPropertyResourceValue(sh("path"));
		Query query = createQuery(sparql, sh("select"), shapePath);
		QuerySolutionMap initialBinding = new QuerySolutionMap();
		initialBinding.add("this", focusNode);
		QueryExecution qexec = QueryExecutionFactory.create(query, data, initialBinding);
		ResultSet rs = qexec.execSelect();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			Resource result = results.createResource(sh("ValidationResult"));
			report.addProperty(sh("result"), result);
			result.addProperty(sh("sourceConstraintComponent"), sh("SPARQLConstraintComponent"));
			result.addProperty(sh("sourceConstraint"), sparql);
			result.addProperty(sh("sourceShape"), shape);
			Statement severity = shape.getProperty(sh("severity"));
			if(severity != null) {
				result.addProperty(sh("resultSeverity"), severity.getObject());
			}
			else {
				result.addProperty(sh("resultSeverity"), sh("Violation"));
			}
			result.addProperty(sh("focusNode"), qs.get("this"));
			RDFNode path = qs.get("path");
			if(path != null && path.isURIResource()) {
				result.addProperty(sh("resultPath"), path);
			}
			else {
				if(shapePath != null) {
					result.addProperty(sh("resultPath"), shapePath); // Should really be a clone
				}
			}
			RDFNode value = qs.get("value");
			if(value != null) {
				result.addProperty(sh("value"), value);
			}
			else {
				result.addProperty(sh("value"), focusNode);
			}
			RDFNode message = qs.get("message");
			if(message != null) {
				result.addProperty(sh("resultMessage"), message);
			}
			else if(sparql.hasProperty(sh("message"))) {
				for(RDFNode m : getObjects(sparql, sh("message"))) {
					result.addProperty(sh("resultMessage"), m);
				}
			}
		}
	}
	
	private Query createQuery(Resource sparql, Property predicate, Resource path) {
		StringBuffer sb = new StringBuffer();
		for(Resource ontology : getResources(sparql, sh("prefixes"))) {
			appendPrefixes(ontology, sb);
		}
		sb.append(sparql.getProperty(predicate).getString());
		String str = path != null ? 
				sb.toString().replaceAll("\\$PATH", "<" + path + ">").replaceAll("\\?PATH", "<" + path + ">") :
				sb.toString();
		return QueryFactory.create(str);
	}
	
	private void appendPrefixes(Resource ontology, StringBuffer sb) {
		for(Resource declare : getResources(ontology, sh("declare"))) {
			String prefix = declare.getProperty(sh("prefix")).getString();
			String ns = declare.getProperty(sh("namespace")).getString();
			sb.append("PREFIX " + prefix + ": <" + ns + ">\n");
		}
		for(Resource imports : getResources(ontology, OWL.imports)) {
			appendPrefixes(imports, sb);
		}
	}

	// Get all instances of a given class including subclasses
	private Set<Resource> getInstances(Resource type) {
		Set<Resource> results = new HashSet<>();
		for(Resource subClass : getSubClasses(type)) {
			results.addAll(getSubjects(subClass.inModel(data), RDF.type));
		}
		return results;
	}
	
	// Get all subclasses of a given class including the class itself
	private Set<Resource> getSubClasses(Resource cls) {
		Set<Resource> results = new HashSet<>();
		addSubClasses(cls, results);
		return results;
	}
	
	// Helper method of getSubClasses
	private void addSubClasses(Resource cls, Set<Resource> results) {
		if(!results.contains(cls)) {
			results.add(cls);
			for(Resource subClass : getSubjects(cls, RDFS.subClassOf)) {
				addSubClasses(subClass, results);
			}
		}
	}
	
	// Utility to get all objects for a given subject/predicate
	private List<RDFNode> getObjects(Resource subject, Property predicate) {
		return subject.getModel().listObjectsOfProperty(subject, predicate).toList();
	}
	
	// Utility to get a resource value of a given subject/predicate
	private Resource getResource(Resource subject, Property predicate) {
		Statement s = subject.getProperty(predicate);
		if(s != null) {
			return s.getResource();
		}
		else {
			return null;
		}
	}
	
	// Utility to get all object resources for a given object/predicate
	private List<Resource> getResources(Resource subject, Property predicate) {
		List<Resource> results = new LinkedList<>();
		for(Statement s : subject.listProperties(predicate).toList()) {
			results.add(s.getResource());
		}
		return results;
	}
	
	// Utility to get all subjects for a given object/predicate
	private List<Resource> getSubjects(RDFNode object, Property predicate) {
		return object.getModel().listSubjectsWithProperty(predicate, object).toList();
	}
	
	// Utility to test if a given node is an instance of type
	private boolean isInstanceOf(Resource node, Resource type) {
		for(RDFNode t : getObjects(node, RDF.type)) {
			if(getSubClasses(type).contains(t)) {
				return true;
			}
		}
		return false;
	}
	
	// Gets all shapes with a target triple, i.e. subjects of sh:targetClass etc triples
	private Set<Resource> getShapesWithTarget() {
		Set<Resource> results = new HashSet<>();
		for(Statement s : shapes.listStatements(null, sh("targetClass"), (RDFNode)null).toList()) {
			results.add(s.getSubject());
		}
		for(Statement s : shapes.listStatements(null, sh("targetNode"), (RDFNode)null).toList()) {
			results.add(s.getSubject());
		}
		for(Statement s : shapes.listStatements(null, sh("targetSubjectsOf"), (RDFNode)null).toList()) {
			results.add(s.getSubject());
		}
		for(Statement s : shapes.listStatements(null, sh("targetObjectsOf"), (RDFNode)null).toList()) {
			results.add(s.getSubject());
		}
		return results;
	}
	
	// Gets all target nodes of a given shape
	private Set<RDFNode> getTargetNodes(Resource shape) {
		Set<RDFNode> results = new HashSet<>();
		results.addAll(getObjects(shape, sh("targetNode")));
		for(Resource targetClass : getResources(shape, sh("targetClass"))) {
			results.addAll(getInstances(targetClass.inModel(data)));
		}
		for(Resource predicate : getResources(shape, sh("targetSubjectsOf"))) {
			for(Statement t : data.listStatements(null, predicate.as(Property.class), (RDFNode)null).toList()) {
				results.add(t.getSubject());
			}
		}
		for(Resource predicate : getResources(shape, sh("targetObjectsOf"))) {
			for(Statement t : data.listStatements(null, predicate.as(Property.class), (RDFNode)null).toList()) {
				results.add(t.getObject());
			}
		}
		return results;
	}
	
	private void log(String message) {
		System.out.println(" --- SHARQL log: " + message);
	}
	
	// Get a Property/Resource in the SHACL namespace and in the shapes graph
	private Property sh(String localName) {
		return shapes.createProperty(SH + localName);
	}
}
