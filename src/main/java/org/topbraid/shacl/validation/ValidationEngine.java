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

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.eval.PathEval;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.AbstractEngine;
import org.topbraid.shacl.engine.ConfigurableEngine;
import org.topbraid.shacl.engine.Constraint;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.engine.filters.ExcludeMetaShapesFilter;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.targets.InstancesTarget;
import org.topbraid.shacl.targets.Target;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLPreferences;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

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
	
	private ClassesCache classesCache;
	
	private ValidationEngineConfiguration configuration;
	
	private Predicate<RDFNode> focusNodeFilter;
	
	private Function<RDFNode,String> labelFunction = (node -> RDFLabels.get().getNodeLabel(node));
	
	private Map<RDFNode,String> labelsCache = new ConcurrentHashMap<>();
	
	private Resource report;
	
	private Map<ValueNodesCacheKey,Collection<RDFNode>> valueNodes = new WeakHashMap<>();
	
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
	
	
	// Note: does not set sh:path
	public Resource createResult(Resource type, Constraint constraint, RDFNode focusNode) {
		Resource result = report.getModel().createResource(type);
		report.addProperty(SH.result, result);
		result.addProperty(SH.resultSeverity, constraint.getShape().getSeverity());
		result.addProperty(SH.sourceConstraintComponent, constraint.getComponent());
		result.addProperty(SH.sourceShape, constraint.getShapeResource());
		if(focusNode != null) {
			result.addProperty(SH.focusNode, focusNode);
		}

		checkMaximumNumberFailures(constraint);

		return result;
	}
	
	
	public Resource createValidationResult(Constraint constraint, RDFNode focusNode, RDFNode value, Supplier<String> defaultMessage) {
		Resource result = createResult(SH.ValidationResult, constraint, focusNode);
		if(value != null) {
			result.addProperty(SH.value, value);
		}
		if(!constraint.getShape().isNodeShape()) {
			result.addProperty(SH.resultPath, SHACLPaths.clonePath(constraint.getShapeResource().getPath(), result.getModel()));
		}
		Collection<RDFNode> messages = constraint.getShape().getMessages();
		if(messages.size() > 0) {
			messages.stream().forEach(message -> result.addProperty(SH.resultMessage, message));
		}
		else if(defaultMessage != null) {
			result.addProperty(SH.resultMessage, defaultMessage.get());
		}
		return result;
	}

	
	private void checkMaximumNumberFailures(Constraint constraint) {
		if (constraint.getShape().getSeverity() == SH.Violation) {
			this.violationsCount++;
			if (configuration.getValidationErrorBatch() != -1 && violationsCount == configuration.getValidationErrorBatch()) {
				throw new MaximumNumberViolations(violationsCount);
			}
		}
	}
	
	
	public ClassesCache getClassesCache() {
		return classesCache;
	}
	
	
	public String getLabel(RDFNode node) {
		return labelsCache.computeIfAbsent(node, n -> getLabelFunction().apply(n));
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

		Set<Resource> shapes = new HashSet<>();
		
		for(Shape rootShape : shapesGraph.getRootShapes()) {
			for(Target target : rootShape.getTargets()) {
				if(!(target instanceof InstancesTarget)) {
					if(target.contains(dataset, focusNode)) {
						shapes.add(rootShape.getShapeResource());
					}
				}
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
		
		return shapes;
	}
	
	
	public ValidationReport getValidationReport() {
		return new ResourceValidationReport(report);
	}

	
	public Collection<RDFNode> getValueNodes(Constraint constraint, RDFNode focusNode) {
		if(constraint.getShape().isNodeShape()) {
			return Collections.singletonList(focusNode);			
		}
		else {
			// We use a cache here because many shapes contains for example both sh:datatype and sh:minCount, and fetching
			// the value nodes each time may be expensive, esp for sh:minCount/maxCount constraints.
			ValueNodesCacheKey key = new ValueNodesCacheKey(focusNode, constraint.getShape().getPath());
			return valueNodes.computeIfAbsent(key, k -> computeValueNodes(focusNode, constraint));
		}
	}

	
	private Collection<RDFNode> computeValueNodes(RDFNode focusNode, Constraint constraint) {
		Property predicate = constraint.getShape().getPredicate();
		if(predicate != null) {
			List<RDFNode> results = new LinkedList<>();
			if(focusNode instanceof Resource) {
				Iterator<Statement> it = ((Resource)focusNode).listProperties(predicate);
				while(it.hasNext()) {
					results.add(it.next().getObject());
				}
			}
			return results;
		}
		else {
			Path jenaPath = constraint.getShape().getJenaPath();
			if(jenaPath instanceof P_Inverse && ((P_Inverse)jenaPath).getSubPath() instanceof P_Link) {
				List<RDFNode> results = new LinkedList<>();
				Property inversePredicate = ResourceFactory.createProperty(((P_Link)((P_Inverse)jenaPath).getSubPath()).getNode().getURI());
				Iterator<Statement> it = focusNode.getModel().listStatements(null, inversePredicate, focusNode);
				while(it.hasNext()) {
					results.add(it.next().getSubject());
				}
				return results;					
			}
			Set<RDFNode> results = new HashSet<>();
			Iterator<Node> it = PathEval.eval(focusNode.getModel().getGraph(), focusNode.asNode(), jenaPath, Context.emptyContext);
			while(it.hasNext()) {
				Node node = it.next();
				results.add(focusNode.getModel().asRDFNode(node));
			}
			return results;
		}
	}
	
	
	public void setClassesCache(ClassesCache value) {
		this.classesCache = value;
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
		if(report.hasProperty(SH.conforms)) {
			report.removeAll(SH.conforms);
		}
		report.addProperty(SH.conforms, conforms ? JenaDatatypes.TRUE : JenaDatatypes.FALSE);
	}

	
	/**
	 * Validates all target nodes against all of their shapes.
	 * To further narrow down which nodes to validate, use {{@link #setFocusNodeFilter(Predicate)}.
	 * @return an instance of sh:ValidationReport in the results Model
	 * @throws InterruptedException if the monitor has canceled this
	 */
	public Resource validateAll() throws InterruptedException {
		boolean nested = SHACLScriptEngineManager.begin();
		try {
			List<Shape> rootShapes = shapesGraph.getRootShapes();
			if(monitor != null) {
				monitor.beginTask("Validating " + rootShapes.size() + " shapes", rootShapes.size());
			}
			if(classesCache == null) {
				// If we are doing everything then the cache should be used, but not for individual nodes
				classesCache = new ClassesCache();
			}
			int i = 0;
			for(Shape shape : rootShapes) {

				if(monitor != null) {
					monitor.subTask("Shape " + (++i) + ": " + getLabelFunction().apply(shape.getShapeResource()));
				}
				
				Collection<RDFNode> focusNodes = shape.getTargetNodes(dataset);
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
					for(Constraint constraint : shape.getConstraints()) {
						validateNodesAgainstConstraint(focusNodes, constraint);
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
	 * @throws InterruptedException if the monitor has canceled this
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
			if(!vs.isDeactivated()) {
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
				if(!vs.isDeactivated()) {
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
	
	
	protected void validateNodesAgainstConstraint(Collection<RDFNode> focusNodes, Constraint constraint) {
		if(configuration != null && configuration.isSkippedConstraintComponent(constraint.getComponent())) {
			return;
		}
		
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
	
	
	private static class ValueNodesCacheKey {
		
		Resource path;
		
		RDFNode focusNode;
		
		
		ValueNodesCacheKey(RDFNode focusNode, Resource path) {
			this.path = path;
			this.focusNode = focusNode;
		}
		
		
		public boolean equals(Object o) {
			if(o instanceof ValueNodesCacheKey) {
				return path.equals(((ValueNodesCacheKey)o).path) && focusNode.equals(((ValueNodesCacheKey)o).focusNode);
			}
			else {
				return false;
			}
		}


		@Override
		public int hashCode() {
			return path.hashCode() + focusNode.hashCode();
		}
		
		
		@Override
		public String toString() {
			return focusNode.toString() + " . " + path;
		}
	}
}
