package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLTemplateCall;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

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

		// sh:nodeShape
		shapes.addAll(JenaUtil.getResourceProperties(resource, SH.nodeShape));
		
		// rdf:type / sh:scopeClass
		for(Resource type : JenaUtil.getAllTypes(resource)) {
			if(JenaUtil.hasIndirectType(type.inModel(shapesModel), SH.Shape)) {
				shapes.add(type);
			}
			for(Statement s : shapesModel.listStatements(null, SH.scopeClass, type).toList()) {
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
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraphURI  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the resource to validate
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param monitor  an optional progress monitor
	 * @return a Model with constraint violations
	 */
	public Model validateNode(Dataset dataset, URI shapesGraphURI, Node focusNode, Resource minSeverity, ProgressMonitor monitor) {
		
		Model results = JenaUtil.createMemoryModel();
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		List<Property> properties = SHACLUtil.getAllConstraintProperties();
		
		Resource resource = (Resource) dataset.getDefaultModel().asRDFNode(focusNode);
		Set<Resource> shapes = getShapesForResource(resource, dataset, shapesModel);
		for(Resource shape : shapes) {
			addResourceViolations(dataset, shapesGraphURI, focusNode, shape.asNode(), properties, minSeverity, results, monitor);
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
	 * @param monitor  an optional progress monitor
	 * @return a Model with constraint violations
	 */
	public Model validateNodeAgainstShape(Dataset dataset, URI shapesGraphURI, Node focusNode, Node shape, Resource minSeverity, ProgressMonitor monitor) {
		Model results = JenaUtil.createMemoryModel();
		Model oldResults = getCurrentResultsModel();
		setCurrentResultsModel(results);
		addResourceViolations(dataset, shapesGraphURI, focusNode, shape, SHACLUtil.getAllConstraintProperties(), minSeverity, results, monitor);
		setCurrentResultsModel(oldResults);
		return results;
	}


	private void addQueryResults(Model results, 
			SHACLConstraint constraint,
			Resource shape,
			RDFNode focusNode,
			Dataset dataset,
			URI shapesGraphURI,
			Resource minSeverity,
			ProgressMonitor monitor) {
		
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			
			Resource severity = executable.getSeverity();
			if(SHACLUtil.hasMinSeverity(severity, minSeverity)) {
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				notifyValidationStarting(shape, executable, focusNode, lang, results);
				lang.executeConstraint(dataset, shape, shapesGraphURI, constraint, executable, focusNode, results);
				notifyValidationFinished(shape, executable, focusNode, lang, results);
			}
		}
	}


	private void addResourceViolations(Dataset dataset, URI shapesGraphURI, Node resourceNode, Node shapeNode,
			List<Property> constraintProperties, Resource minSeverity, Model results,
			ProgressMonitor monitor) {
		
		RDFNode resource = dataset.getDefaultModel().asRDFNode(resourceNode);
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		Resource shape = (Resource) shapesModel.asRDFNode(shapeNode);
		for(Property constraintProperty : constraintProperties) {
			for(Resource c : JenaUtil.getResourceProperties(shape, constraintProperty)) {
				Resource type = JenaUtil.getType(c);
				if(type == null) {
					type = SHACLUtil.getDefaultTemplateType(c);
				}
				if(type != null) {
					if(JenaUtil.hasSuperClass(type,  SH.NativeConstraint)) {
						SHACLConstraint constraint = SHACLFactory.asNativeConstraint(c);
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraphURI, minSeverity, monitor);
					}
					else if(JenaUtil.hasIndirectType(type, SH.ConstraintTemplate)) {
						SHACLConstraint constraint = SHACLFactory.asTemplateConstraint(c);
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraphURI, minSeverity, monitor);
					}
				}
			}
		}
	}
	
	
	private boolean isInScope(Resource focusNode, Dataset dataset, Resource scope) {
		SHACLTemplateCall templateCall = null;
		Resource executable = scope;
		if(SHACLFactory.isTemplateCall(scope)) {
			templateCall = SHACLFactory.asTemplateCall(scope);
			executable = templateCall.getTemplate();
		}
		ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForScope(executable);
		return lang.isNodeInScope(focusNode, dataset, executable, templateCall);
	}
}
