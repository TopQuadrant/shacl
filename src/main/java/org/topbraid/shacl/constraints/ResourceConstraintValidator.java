package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableScope;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

/**
 * A SHACL constraint validator for individual resources - either against all shapes
 * derived from its Model, or against a specified Shape.
 * 
 * @author Holger Knublauch
 */
public class ResourceConstraintValidator extends AbstractConstraintValidator {
	
	private static ResourceConstraintValidator singleton = new ResourceConstraintValidator();
	
	public static ResourceConstraintValidator get() {
		return singleton;
	}
	
	public static void set(ResourceConstraintValidator value) {
		singleton = value;
	}


	/**
	 * Gets a Set of all Shapes that should be evaluated for a given resource.
	 * @param resource  the resource to get the shapes for
	 * @param dataset  the Dataset containing the resource
	 * @param shapesModel  the shapes Model
	 * @return a Set of shape resources
	 */
	public Set<Resource> getShapesForResource(Resource resource, Dataset dataset, Model shapesModel) {
		Set<Resource> shapes = new HashSet<Resource>();

		// sh:scopeNode
		shapes.addAll(shapesModel.listSubjectsWithProperty(SH.scopeNode, resource).toList());
		
		// property scopes
		for(Statement s : shapesModel.listStatements(null, SH.scopeProperty, (RDFNode)null).toList()) {
			if(resource.hasProperty(JenaUtil.asProperty(s.getResource()))) {
				shapes.add(s.getSubject());
			}
		}
		for(Statement s : shapesModel.listStatements(null, SH.scopeInverseProperty, (RDFNode)null).toList()) {
			if(resource.getModel().contains(null, JenaUtil.asProperty(s.getResource()), resource)) {
				shapes.add(s.getSubject());
			}
		}
		
		// rdf:type / sh:scopeClass|sh:context
		for(Resource type : JenaUtil.getAllTypes(resource)) {
			if(JenaUtil.hasIndirectType(type.inModel(shapesModel), SH.Shape)) {
				shapes.add(type);
			}
			for(Statement s : shapesModel.listStatements(null, SH.scopeClass, type).toList()) {
				shapes.add(s.getSubject());
			}
			for(Statement s : shapesModel.listStatements(null, SH.context, type).toList()) {
				shapes.add(s.getSubject());
			}
		}
		
		// sh:scope
		for(Statement s : shapesModel.listStatements(null, SH.scope, (RDFNode)null).toList()) {
			if(isInScope(resource, dataset, s.getResource())) {
				shapes.add(s.getSubject());
			}
		}
		
		return shapes;
	}

	
	/**
	 * Validates all SHACL constraints for a given resource.
	 * This always includes shapesGraph validation (sh:parameter etc).
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the resource to validate
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param constraintFilter  a filter that all SHACLConstraints must pass, or null for all constraints
	 * @param monitor  an optional progress monitor
	 * @return a Model with constraint violations
	 */
	public Model validateNode(Dataset dataset, URI shapesGraphURI, Node focusNode, Resource minSeverity, Predicate<SHConstraint> constraintFilter, Function<RDFNode,String> labelFunction, ProgressMonitor monitor) throws InterruptedException {
		
		Model results = JenaUtil.createMemoryModel();
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		List<Property> properties = SHACLUtil.getAllConstraintProperties(true);
		
		Resource resource = (Resource) dataset.getDefaultModel().asRDFNode(focusNode);
		Set<Resource> shapes = getShapesForResource(resource, dataset, shapesModel);
		for(Resource shape : shapes) {
			if(monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}
			addResourceViolations(dataset, shapesGraphURI, focusNode, shape.asNode(), properties, minSeverity, constraintFilter, results, labelFunction, monitor);
		}
		
		return results;
	}

	
	/**
	 * Validates a given resource against a given Shape.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the resource to validate
	 * @param shape  the sh:Shape to validate against
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param constraintFilter  a filter that all SHACLConstraints must pass, or null for all constraints
	 * @param labelFunction  an optional function used to insert resource labels into message templates
	 * @param monitor  an optional progress monitor
	 * @return a Model with constraint violations
	 */
	public Model validateNodeAgainstShape(Dataset dataset, URI shapesGraphURI, Node focusNode, Node shape, Resource minSeverity, Predicate<SHConstraint> constraintFilter, Function<RDFNode,String> labelFunction, ProgressMonitor monitor) {
		Model results = JenaUtil.createMemoryModel();
		Model oldResults = getCurrentResultsModel();
		setCurrentResultsModel(results);
		addResourceViolations(dataset, shapesGraphURI, focusNode, shape, SHACLUtil.getAllConstraintProperties(true), minSeverity, constraintFilter, results, labelFunction, monitor);
		setCurrentResultsModel(oldResults);
		return results;
	}


	private void addQueryResults(Model results, 
			SHConstraint constraint,
			Resource shape,
			RDFNode focusNode,
			Dataset dataset,
			URI shapesGraphURI,
			Resource minSeverity,
			Function<RDFNode,String> labelFunction,
			ProgressMonitor monitor) {
		
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			
			Resource severity = executable.getSeverity();
			if(SHACLUtil.hasMinSeverity(severity, minSeverity)) {
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				notifyValidationStarting(shape, executable, focusNode, lang, results);
				lang.executeConstraint(dataset, shape, shapesGraphURI, executable, focusNode, results, labelFunction);
				notifyValidationFinished(shape, executable, focusNode, lang, results);
			}
		}
	}


	private void addResourceViolations(Dataset dataset, URI shapesGraphURI, Node resourceNode, Node shapeNode,
			List<Property> constraintProperties, Resource minSeverity, Predicate<SHConstraint> constraintFilter, Model results,
			Function<RDFNode,String> labelFunction, ProgressMonitor monitor) {
		
		RDFNode resource = dataset.getDefaultModel().asRDFNode(resourceNode);
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		Resource shape = (Resource) shapesModel.asRDFNode(shapeNode);
		for(Property constraintProperty : constraintProperties) {
			for(Resource c : JenaUtil.getResourceProperties(shape, constraintProperty)) {
				if(c.hasProperty(RDF.type, SH.SPARQLConstraint)) {
					SHConstraint constraint = SHFactory.asSPARQLConstraint(c);
					if(constraintFilter == null || constraintFilter.test(constraint)) {
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraphURI, minSeverity, labelFunction, monitor);
					}
				}
				else if(SHFactory.isParameterizableConstraint(c)) {
					SHConstraint constraint = SHFactory.asParameterizableConstraint(c);
					if(constraintFilter == null || constraintFilter.test(constraint)) {
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraphURI, minSeverity, labelFunction, monitor);
					}
				}
			}
		}
		// This would be active if argument may be sh:NodeConstraints
		/*if(!shape.hasProperty(RDF.type)) {
			SHACLConstraint constraint = shape.as(SHACLNodeConstraint.class);
			addQueryResults(results, constraint, shape, resource, dataset, shapesGraphURI, minSeverity, monitor);
		}*/
	}
	
	
	private boolean isInScope(Resource focusNode, Dataset dataset, Resource scope) {
		SHParameterizableScope parameterizableScope = null;
		Resource executable = scope;
		if(SHFactory.isParameterizableInstance(scope)) {
			parameterizableScope = SHFactory.asParameterizableScope(scope);
			executable = parameterizableScope.getParameterizable();
		}
		ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForScope(executable);
		return lang.isNodeInScope(focusNode, dataset, executable, parameterizableScope);
	}
}
