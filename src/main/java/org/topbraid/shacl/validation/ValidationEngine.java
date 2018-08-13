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
package org.topbraid.shacl.validation;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.*;
import org.topbraid.shacl.engine.filters.ExcludeMetaShapesFilter;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLPreferences;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A ValidationEngine uses a given shapes graph (represented via an instance of VShapesGraph)
 * and performs SHACL validation on a given Dataset.
 * 
 * Instances of this class should be created via the ValidatorFactory.
 * 
 * @author Holger Knublauch
 */
public class ValidationEngine extends AbstractEngine implements ConfigurableEngine {
	
	// The currently active ValidationEngine for cases where no direct pointer can be acquired, e.g. from HasShapeFunction
	private static ThreadLocal<ValidationEngine> current = new ThreadLocal<>();
	
	public static ValidationEngine getCurrent() {
		return current.get();
	}
	
	public static void setCurrent(ValidationEngine value) {
		current.set(value);
	}
	
	
	private ValidationEngineConfiguration configuration;
	
	private Predicate<RDFNode> focusNodeFilter;
	
	private Function<RDFNode,String> labelFunction = (node -> node.toString());
	
	private Resource report;

	private int violationsCount = 0;


	/**
	 * Constructs a new ValidationEngine.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param shapesGraph  the ShapesGraph with the shapes to validate against
	 * @param report  the sh:ValidationReport object in the results Model, or null to create a new one
	 */
	protected ValidationEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report) {
		super(dataset, shapesGraph, shapesGraphURI);
		setConfiguration(new ValidationEngineConfiguration());
		if(report == null) {
			Model reportModel = JenaUtil.createMemoryModel();
			reportModel.setNsPrefixes(dataset.getDefaultModel());
			this.report = reportModel.createResource(SH.ValidationReport);
		}
		else {
			this.report = report;
		}
	}

	
	public Function<RDFNode,String> getLabelFunction() {
		return labelFunction;
	}
	
	
	public void setLabelFunction(Function<RDFNode,String> value) {
		this.labelFunction = value;
	}
	
	
	public void addResultMessage(Resource result, Literal message, QuerySolution bindings) {
		result.addProperty(SH.resultMessage, SPARQLSubstitutions.withSubstitutions(message, bindings, getLabelFunction()));
	}
	
	
	public Resource createResult(Resource type, Constraint constraint, RDFNode focusNode) {
		Resource result = report.getModel().createResource(type);
		report.addProperty(SH.result, result);
		result.addProperty(SH.resultSeverity, constraint.getShapeResource().getSeverity());
		result.addProperty(SH.sourceConstraintComponent, constraint.getComponent());
		result.addProperty(SH.sourceShape, constraint.getShapeResource());
		if(focusNode != null) {
			result.addProperty(SH.focusNode, focusNode);
		}

		checkMaximumNumberFailures(constraint);

		return result;
	}

	private void checkMaximumNumberFailures(Constraint constraint) {
		if (constraint.getShapeResource().getSeverity() == SH.Violation) {
			this.violationsCount++;
			if (configuration.getValidationErrorBatch() != -1 && violationsCount == configuration.getValidationErrorBatch()) {
				throw new MaximumNumberViolations(violationsCount);
			}
		}
	}


	/**
	 * Gets the validation report as a Resource in the report Model.
	 * @return the report Resource
	 */
	public Resource getReport() {
		return report;
	}


	/**
	 * Gets a Set of all Shapes that should be evaluated for a given resource.
	 * @param focusNode  the resource to get the shapes for
	 * @param dataset  the Dataset containing the resource
	 * @param shapesModel  the shapes Model
	 * @return a Set of shape resources
	 */
	private Set<Resource> getShapesForNode(RDFNode focusNode, Dataset dataset, Model shapesModel) {
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
			if(SHACLUtil.isInTarget(focusNode, dataset, s.getResource())) {
				shapes.add(s.getSubject());
			}
		}
		
		return shapes;
	}

	
	public List<RDFNode> getValueNodes(Constraint constraint, RDFNode focusNode) {
		Resource path = JenaUtil.getResourceProperty(constraint.getShapeResource(), SH.path);
		if(path == null) {
			return Collections.singletonList(focusNode);
		}
		else {
			List<RDFNode> results = new LinkedList<>();
			Path jenaPath = constraint.getShape().getJenaPath();
			if(jenaPath != null) {
				SHACLPaths.addValueNodes(focusNode, jenaPath, results);
			}
			else {
				SHACLPaths.addValueNodes(focusNode, JenaUtil.asProperty(path), results);
			}
			return results;
		}
	}

	
	/**
	 * Sets a filter that can be used to skip certain focus node from validation.
	 * The filter must return true if the given candidate focus node shall be validated,
	 * and false to skip it.
	 * @param value  the new filter
	 */
	public void setFocusNodeFilter(Predicate<RDFNode> value) {
		this.focusNodeFilter = value;
	}
	
	
	public void updateConforms() {
		boolean conforms = true;
		StmtIterator it = report.listProperties(SH.result);
		while(it.hasNext()) {
			Statement s = it.next();
			if(s.getResource().hasProperty(RDF.type, SH.ValidationResult)) {
				conforms = false;
				it.close();
				break;
			}
		}
		report.removeAll(SH.conforms);
		report.addProperty(SH.conforms, conforms ? JenaDatatypes.TRUE : JenaDatatypes.FALSE);
	}

	
	/**
	 * Validates all target nodes against all of their shapes.
	 * To further narrow down which nodes to validate, use {{@link #setFocusNodeFilter(Predicate)}.
	 * @return an instance of sh:ValidationReport in the results Model
	 */
	public Resource validateAll() throws InterruptedException {
		boolean nested = SHACLScriptEngineManager.begin();
		try {
			List<Shape> rootShapes = shapesGraph.getRootShapes();
			if(monitor != null) {
				monitor.beginTask("Validating " + rootShapes.size() + " shapes", rootShapes.size());
			}
			int i = 0;
			for(Shape shape : rootShapes) {

				if(monitor != null) {
					monitor.subTask("Shape " + (++i) + ": " + getLabelFunction().apply(shape.getShapeResource()));
				}
				
				List<RDFNode> focusNodes = SHACLUtil.getTargetNodes(shape.getShapeResource(), dataset);
				if(focusNodeFilter != null) {
					List<RDFNode> filteredFocusNodes = new LinkedList<RDFNode>();
					for(RDFNode focusNode : focusNodes) {
						if(focusNodeFilter.test(focusNode)) {
							filteredFocusNodes.add(focusNode);
						}
					}
					focusNodes = filteredFocusNodes;
				}
				if(!focusNodes.isEmpty()) {
					if(!shapesGraph.isIgnored(shape.getShapeResource().asNode())) {
						for(Constraint constraint : shape.getConstraints()) {
							validateNodesAgainstConstraint(focusNodes, constraint);
						}
					}
				}
				if(monitor != null) {
					monitor.worked(1);
					if(monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			}
		}
		catch(MaximumNumberViolations ex) {
			// ignore
		}
		finally {
			SHACLScriptEngineManager.end(nested);
		}
		updateConforms();
		return report;
	}
	
	
	/**
	 * Validates a given focus node against all of the shapes that have matching targets.
	 * @param focusNode  the node to validate
	 * @return an instance of sh:ValidationReport in the results Model
	 */
	public Resource validateNode(Node focusNode) throws InterruptedException {
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		RDFNode focusRDFNode = dataset.getDefaultModel().asRDFNode(focusNode);
		Set<Resource> shapes = getShapesForNode(focusRDFNode, dataset, shapesModel);
		boolean nested = SHACLScriptEngineManager.begin();
		try {
			for(Resource shape : shapes) {
				if(monitor != null && monitor.isCanceled()) {
					throw new InterruptedException();
				}
				validateNodesAgainstShape(Collections.singletonList(focusRDFNode), shape.asNode());
			}
		}
		finally {
			SHACLScriptEngineManager.end(nested);
		}
		
		return report;
	}

	
	/**
	 * Validates a given list of focus node against a given Shape.
	 * @param focusNodes  the nodes to validate
	 * @param shape  the sh:Shape to validate against
	 * @return an instance of sh:ValidationReport in the results Model
	 */
	public Resource validateNodesAgainstShape(List<RDFNode> focusNodes, Node shape) {
		if(!shapesGraph.isIgnored(shape)) {
			Shape vs = shapesGraph.getShape(shape);
			if(!vs.getShapeResource().isDeactivated()) {
				boolean nested = SHACLScriptEngineManager.begin();
				ValidationEngine oldEngine = current.get();
				current.set(this);
				try {
					for(Constraint constraint : vs.getConstraints()) {
						validateNodesAgainstConstraint(focusNodes, constraint);
					}
				}
				finally {
					current.set(oldEngine);
					SHACLScriptEngineManager.end(nested);
				}
			}
		}
		return report;
	}

	
	/**
	 * Validates a given list of focus node against a given Shape, and stops as soon
	 * as one validation result is reported.  No results are recorded.
	 * @param focusNodes  the nodes to validate
	 * @param shape  the sh:Shape to validate against
	 * @return true if there were no validation results, false for violations
	 */
	public boolean nodesConformToShape(List<RDFNode> focusNodes, Node shape) {
		if(!shapesGraph.isIgnored(shape)) {
			Resource oldReport = report;
			report = JenaUtil.createMemoryModel().createResource();
			try {
				Shape vs = shapesGraph.getShape(shape);
				if(!vs.getShapeResource().isDeactivated()) {
					boolean nested = SHACLScriptEngineManager.begin();
					try {
						for(Constraint constraint : vs.getConstraints()) {
							validateNodesAgainstConstraint(focusNodes, constraint);
							if(report.hasProperty(SH.result)) {
								return false;
							}
						}
					}
					finally {
						SHACLScriptEngineManager.end(nested);
					}
				}
			}
			finally {
				this.report = oldReport;
			}
		}
		return true;
	}
	
	
	protected void validateNodesAgainstConstraint(List<RDFNode> focusNodes, Constraint constraint) {
		ConstraintExecutor executor = constraint.getExecutor();
		if(executor != null) {
			if(SHACLPreferences.isProduceFailuresMode()) {
				try {
					executor.executeConstraint(constraint, this, focusNodes);
				}
				catch(Exception ex) {
					Resource result = createResult(DASH.FailureResult, constraint, null);
					result.addProperty(SH.resultMessage, "Exception during validation: " + ExceptionUtil.getStackTrace(ex));
				}
			}
			else {
				executor.executeConstraint(constraint, this, focusNodes);
			}
		}
		else {
			FailureLog.get().logWarning("No suitable validator found for constraint " + constraint);
		}
	}


	@Override
	public ValidationEngineConfiguration getConfiguration() {
		return configuration;
	}

	
	@Override
	public void setConfiguration(ValidationEngineConfiguration configuration) {
		this.configuration = configuration;
		if(!configuration.getValidateShapes()) {
			shapesGraph.setShapeFilter(new ExcludeMetaShapesFilter());
		}
	}
}
