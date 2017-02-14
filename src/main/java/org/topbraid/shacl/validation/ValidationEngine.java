package org.topbraid.shacl.validation;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.js.SHACLScriptEngineManager;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaDatatypes;
import org.topbraid.spin.util.JenaUtil;

/**
 * A ValidationEngine uses a given shapes graph (represented via an instance of VShapesGraph)
 * and performs SHACL validation on a given Dataset.
 * 
 * Instances of this class should be created via the ValidatorFactory.
 * 
 * @author Holger Knublauch
 */
public class ValidationEngine {
	
	private Dataset dataset;
	
	private Map<Constraint,ConstraintExecutor> executors = new HashMap<>();
	
	private Function<RDFNode,String> labelFunction;
	
	private ProgressMonitor monitor;
	
	private Resource report;
	
	private ShapesGraph shapesGraph;
	
	private URI shapesGraphURI;
	

	
	/**
	 * Constructs a new ValidationEngine.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param shapesGraph  the ShapesGraph with the shapes to validate against
	 * @param report  the sh:ValidationReport object in the results Model, or null to create a new one
	 */
	protected ValidationEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report) {
		this.dataset = dataset;
		this.shapesGraph = shapesGraph;
		
		if(shapesGraphURI == null) {
			shapesGraphURI = DefaultShapesGraphProvider.get().getDefaultShapesGraphURI(dataset);
		}

		this.shapesGraphURI = shapesGraphURI;
		if(report == null) {
			Model reportModel = JenaUtil.createDefaultModel();
			reportModel.setNsPrefixes(dataset.getDefaultModel());
			this.report = reportModel.createResource(SH.ValidationReport);
		}
		else {
			this.report = report;
		}
	}
	
	
	public Dataset getDataset() {
		return dataset;
	}
	
	
	private ConstraintExecutor getExecutor(Constraint constraint) {
		ConstraintExecutor executor = executors.get(constraint);
		if(executor == null) {
			executor = ConstraintExecutors.get().getExecutor(constraint, this);
			executors.put(constraint, executor);
		}
		return executor;
	}

	
	public Function<RDFNode,String> getLabelFunction() {
		return labelFunction;
	}
	
	
	public void setLabelFunction(Function<RDFNode,String> value) {
		this.labelFunction = value;
	}
	
	
	public ProgressMonitor getProgressMonitor() {
		return monitor;
	}
	
	
	public void setProgressMonitor(ProgressMonitor value) {
		this.monitor = value;
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
		return result;
	}
	
	
	public URI getShapesGraphURI() {
		return shapesGraphURI;
	}
	
	
	public List<RDFNode> getTargetNodes(Resource shape) {
		
		Set<RDFNode> results = new HashSet<RDFNode>();
		
		Model dataModel = dataset.getDefaultModel();
		
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			results.addAll(JenaUtil.getAllInstances(shape.inModel(dataModel)));
		}
		
		for(Resource targetClass : JenaUtil.getResourceProperties(shape, SH.targetClass)) {
			results.addAll(JenaUtil.getAllInstances(targetClass.inModel(dataModel)));
		}
		
		results.addAll(shape.getModel().listObjectsOfProperty(shape, SH.targetNode).toList());
		
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

		return new ArrayList<RDFNode>(results);
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
			if(isInTarget(focusNode, dataset, s.getResource())) {
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
			List<RDFNode> results = new LinkedList<RDFNode>();
			if(path.isURIResource()) {
				if(focusNode instanceof Resource) {
					StmtIterator it = focusNode.getModel().listStatements((Resource)focusNode, JenaUtil.asProperty(path), (RDFNode)null);
					while(it.hasNext()) {
						results.add(it.next().getObject());
					}
				}
			}
			else {
				String pathString = SHACLPaths.getPathString(path);
				String queryString = "SELECT DISTINCT ?value { $this " + pathString + " ?value }";
				Query query = ARQFactory.get().createQuery(path.getModel(), queryString);
				QueryExecution qexec = ARQFactory.get().createQueryExecution(query, focusNode.getModel());
				QuerySolutionMap qs = new QuerySolutionMap();
				qs.add("this", focusNode);
				qexec.setInitialBinding(qs);
				ResultSet rs = qexec.execSelect();
				while(rs.hasNext()) {
					results.add(rs.next().get("value"));
				}
				qexec.close();
			}
			return results;
		}
	}
	
	
	private boolean isInTarget(RDFNode focusNode, Dataset dataset, Resource target) {
		SHParameterizableTarget parameterizableTarget = null;
		Resource executable = target;
		if(SHFactory.isParameterizableInstance(target)) {
			parameterizableTarget = SHFactory.asParameterizableTarget(target);
			executable = parameterizableTarget.getParameterizable();
		}
		TargetPlugin plugin = TargetPlugins.get().getLanguageForTarget(executable);
		if(plugin != null) {
			return plugin.isNodeInTarget(focusNode, dataset, executable, parameterizableTarget);
		}
		else {
			return false;
		}
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
	 * @return an instance of sh:ValidationReport in the results Model
	 */
	public Resource validateAll() throws InterruptedException {
		boolean nested = SHACLScriptEngineManager.begin();
		try {
			List<Shape> rootShapes = shapesGraph.getRootShapes();
			if(monitor != null) {
				monitor.beginTask("Validating " + rootShapes.size() + " shapes", rootShapes.size());
			}
			for(Shape shape : rootShapes) {
				if(monitor != null) {
					monitor.subTask("Shape " + getLabelFunction().apply(shape.getShapeResource()));
				}
				List<RDFNode> focusNodes = getTargetNodes(shape.getShapeResource());
				if(!focusNodes.isEmpty()) {
					for(Constraint constraint : shape.getConstraints()) {
						for(RDFNode focusNode : focusNodes) {
							validateNodeAgainstConstraint(focusNode.asNode(), constraint);
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
				validateNodeAgainstShape(focusNode, shape.asNode());
			}
		}
		finally {
			SHACLScriptEngineManager.end(nested);
		}
		
		return report;
	}

	
	/**
	 * Validates a given focus node against a given Shape.
	 * @param focusNode  the resource to validate
	 * @param shape  the sh:Shape to validate against
	 * @return an instance of sh:ValidationReport in the results Model
	 */
	public Resource validateNodeAgainstShape(Node focusNode, Node shape) {
		if(!shapesGraph.isIgnored(shape)) {
			boolean nested = SHACLScriptEngineManager.begin();
			try {
				Shape vs = shapesGraph.getShape(shape);
				for(Constraint constraint : vs.getConstraints()) {
					validateNodeAgainstConstraint(focusNode, constraint);
				}
			}
			finally {
				SHACLScriptEngineManager.end(nested);
			}
		}
		return report;
	}
	
	
	private void validateNodeAgainstConstraint(Node focusNode, Constraint constraint) {
		ConstraintExecutor executor = getExecutor(constraint);
		if(executor != null) {
			List<RDFNode> focusNodes = Collections.singletonList(dataset.getDefaultModel().asRDFNode(focusNode));
			executor.executeConstraint(constraint, this, focusNodes);
		}
		else {
			FailureLog.get().logFailure("No suitable validator found for constraint " + constraint);
		}
	}
}
