package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.topbraid.shacl.constraints.sparql.SPARQLExecutionLanguage;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.model.SHShape;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

/**
 * A SHACL constraint validator for individual nodes - either against all shapes
 * derived from its Model, or against a specified Shape.
 * 
 * @author Holger Knublauch
 */
public class NodeConstraintValidator extends AbstractConstraintValidator {
	
	public NodeConstraintValidator() {
		this(JenaUtil.createMemoryModel());
	}
	
	public NodeConstraintValidator(Model resultsModel) {
		super(resultsModel);
	}


	/**
	 * Gets a Set of all Shapes that should be evaluated for a given resource.
	 * @param focusNode  the resource to get the shapes for
	 * @param dataset  the Dataset containing the resource
	 * @param shapesModel  the shapes Model
	 * @return a Set of shape resources
	 */
	public Set<Resource> getShapesForNode(RDFNode focusNode, Dataset dataset, Model shapesModel) {
		Set<Resource> shapes = new HashSet<Resource>();

		// sh:targetNode
		shapes.addAll(shapesModel.listSubjectsWithProperty(SH.targetNode, focusNode).toList());
		
		// property targets
		if(focusNode instanceof Resource) {
			for(Statement s : shapesModel.listStatements(null, SH.targetSubjectsOf, (RDFNode)null).toList()) {
				if(((Resource)focusNode).hasProperty(JenaUtil.asProperty(s.getResource()))) {
					shapes.add(s.getSubject());
				}
			}
		}
		for(Statement s : shapesModel.listStatements(null, SH.targetObjectsOf, (RDFNode)null).toList()) {
			if(focusNode.getModel().contains(null, JenaUtil.asProperty(s.getResource()), focusNode)) {
				shapes.add(s.getSubject());
			}
		}
		
		// rdf:type / sh:targetClass
		if(focusNode instanceof Resource) {
			for(Resource type : JenaUtil.getAllTypes((Resource)focusNode)) {
				if(JenaUtil.hasIndirectType(type.inModel(shapesModel), SH.Shape)) {
					shapes.add(type);
				}
				for(Statement s : shapesModel.listStatements(null, SH.targetClass, type).toList()) {
					shapes.add(s.getSubject());
				}
			}
		}
		
		// sh:target
		for(Statement s : shapesModel.listStatements(null, SH.target, (RDFNode)null).toList()) {
			if(isInTarget(focusNode, dataset, s.getResource())) {
				shapes.add(s.getSubject());
			}
		}
		
		return shapes;
	}

	
	/**
	 * Validates all SHACL constraints for a given node.
	 * This always includes shapesGraph validation (sh:parameter etc).
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the node to validate
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param constraintFilter  a filter that all SHACLConstraints must pass, or null for all constraints
	 * @param monitor  an optional progress monitor
	 * @return a Model with constraint violations
	 */
	public Model validateNode(Dataset dataset, URI shapesGraphURI, Node focusNode, Resource minSeverity, Predicate<SHConstraint> constraintFilter, Function<RDFNode,String> labelFunction, boolean validateShapes, ProgressMonitor monitor) throws InterruptedException {
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		List<Property> properties = SHACLUtil.getAllConstraintProperties(validateShapes);
		
		RDFNode focusRDFNode = (Resource) dataset.getDefaultModel().asRDFNode(focusNode);
		Set<Resource> shapes = getShapesForNode(focusRDFNode, dataset, shapesModel);
		for(Resource shape : shapes) {
			if(monitor != null && monitor.isCanceled()) {
				throw new InterruptedException();
			}
			addResourceViolations(dataset, shapesGraphURI, focusNode, shape.asNode(), properties, minSeverity, constraintFilter, labelFunction, monitor);
		}
		
		return resultsModel;
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
		addResourceViolations(dataset, shapesGraphURI, focusNode, shape, SHACLUtil.getAllConstraintProperties(true), minSeverity, constraintFilter, labelFunction, monitor);
		return resultsModel;
	}


	private boolean addQueryResults( 
			SHConstraint constraint,
			Resource shape,
			RDFNode focusNode,
			Dataset dataset,
			URI shapesGraphURI,
			Resource minSeverity,
			Function<RDFNode,String> labelFunction,
			List<Resource> resultsList,
			ProgressMonitor monitor) {
		
		boolean violations = false;
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			Resource severity = executable.getSeverity();
			if(SHACLUtil.hasMinSeverity(severity, minSeverity)) {
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				violations |= lang.executeConstraint(dataset, shape, shapesGraphURI, executable, focusNode, resultsModel, labelFunction, resultsList);
			}
		}
		return violations;
	}


	private void addResourceViolations(Dataset dataset, URI shapesGraphURI, Node focusNode, Node shapeNode,
			List<Property> constraintProperties, Resource minSeverity, Predicate<SHConstraint> constraintFilter,
			Function<RDFNode,String> labelFunction, ProgressMonitor monitor) {
		
		if(shapesGraphURI == null) {
			shapesGraphURI = DefaultShapesGraphProvider.get().getDefaultShapesGraphURI(dataset);
		}
		
		RDFNode focusRDFNode = dataset.getDefaultModel().asRDFNode(focusNode);
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		SHShape shape = SHFactory.asShape(shapesModel.asRDFNode(shapeNode));
		
		List<Resource> resultsList = new LinkedList<Resource>();
		
		boolean violations = false;
		if(constraintFilter == null || constraintFilter.test(shape)) {
			violations |= addQueryResults(shape, shape, focusRDFNode, dataset, shapesGraphURI, minSeverity, labelFunction, resultsList, monitor);
		}
		
		for(Property constraintProperty : constraintProperties) {
			for(Resource c : JenaUtil.getResourceProperties(shape, constraintProperty)) {
				if(c.hasProperty(RDF.type, SH.SPARQLConstraint)) {
					SHConstraint constraint = SHFactory.asSPARQLConstraint(c);
					if(constraintFilter == null || constraintFilter.test(constraint)) {
						violations |= addQueryResults(constraint, shape, focusRDFNode, dataset, shapesGraphURI, minSeverity, labelFunction, resultsList, monitor);
					}
				}
				else if(SHFactory.isParameterizableConstraint(c)) {
					SHConstraint constraint = SHFactory.asParameterizableConstraint(c);
					if(constraintFilter == null || constraintFilter.test(constraint)) {
						violations |= addQueryResults(constraint, shape, focusRDFNode, dataset, shapesGraphURI, minSeverity, labelFunction, resultsList, monitor);
					}
				}
			}
		}
		
		if(SPARQLExecutionLanguage.createDetails) {
			Resource result = resultsModel.createResource(violations ? SH.ValidationResult : DASH.SuccessResult);
			result.addProperty(SH.focusNode, focusRDFNode);
			result.addProperty(SH.sourceShape, shape);
			result.addProperty(SH.sourceConstraint, shape);
			result.addProperty(SH.resultMessage, "Does " + (violations ? "not " : "") + "have shape");
			for(Resource r : resultsList) {
				result.addProperty(SH.detail, r);
			}
			if(violations) {
				Resource severity = JenaUtil.getResourceProperty(shape, SH.severity);
				if(severity == null) {
					severity = SH.Violation;
				}
				result.addProperty(SH.severity, severity);
			}
		}
	}
	
	
	private boolean isInTarget(RDFNode focusNode, Dataset dataset, Resource target) {
		SHParameterizableTarget parameterizableTarget = null;
		Resource executable = target;
		if(SHFactory.isParameterizableInstance(target)) {
			parameterizableTarget = SHFactory.asParameterizableTarget(target);
			executable = parameterizableTarget.getParameterizable();
		}
		ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForTarget(executable);
		return lang.isNodeInTarget(focusNode, dataset, executable, parameterizableTarget);
	}
}
