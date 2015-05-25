package org.topbraid.shacl.constraints;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class ResourceConstraintValidator {
	
	private static ResourceConstraintValidator singleton = new ResourceConstraintValidator();
	
	public static ResourceConstraintValidator get() {
		return singleton;
	}
	
	public static void set(ResourceConstraintValidator value) {
		singleton = value;
	}


	public void addResourceViolations(Dataset dataset, Resource shapesGraph, Node resourceNode, Node shapeNode,
			List<Property> constraintProperties, Resource minSeverity, Model results,
			ProgressMonitor monitor) {
		
		Resource resource = (Resource) dataset.getDefaultModel().asRDFNode(resourceNode);
		Model shapesModel = dataset.getNamedModel(shapesGraph.getURI());
		Resource shape = (Resource) shapesModel.asRDFNode(shapeNode);
		for(Property constraintProperty : constraintProperties) {
			for(Resource c : JenaUtil.getResourceProperties(shape, constraintProperty)) {
				Resource type = JenaUtil.getType(c);
				if(type == null && c.isAnon()) {
					type = SHACLUtil.getDefaultTemplateType(c);
				}
				if(type != null) {
					if(SH.NativeConstraint.equals(type)) {
						SHACLConstraint constraint = SHACLFactory.asNativeConstraint(c);
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraph, minSeverity, monitor);
					}
					else if(JenaUtil.hasIndirectType(type, SH.ConstraintTemplate)) {
						SHACLConstraint constraint = SHACLFactory.asTemplateConstraint(c);
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraph, minSeverity, monitor);
					}
				}
			}
		}
	}


	private void addQueryResults(Model results, 
			SHACLConstraint constraint,
			Resource shape,
			Resource focusNode,
			Dataset dataset,
			Resource shapesGraph,
			Resource minSeverity,
			ProgressMonitor monitor) {
		
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			
			Resource severity = executable.getSeverity();
			if(minSeverity == null || minSeverity.equals(severity) || JenaUtil.hasSuperClass(severity, minSeverity)) {
				
				if(executable instanceof NativeConstraintExecutable) {
					NativeConstraintExecutable e = (NativeConstraintExecutable)executable;
					ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
					lang.executeNative(dataset, shape, shapesGraph, results, constraint, focusNode, null, null, e);
				}
				else {
					TemplateConstraintExecutable e = (TemplateConstraintExecutable)executable;
					ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
					lang.executeTemplate(dataset, shape, shapesGraph, results, constraint, focusNode, null, null, e);
				}
			}
		}
	}

	
	/**
	 * Validates all SHACL constraints for a given resource.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraph  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the Resource to validate
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param monitor  an optional progress monitor
	 * @return a List of constraint violations
	 */
	public Model validateNode(Dataset dataset, Resource shapesGraph, Node focusNode, Resource minSeverity, ProgressMonitor monitor) {
		
		Model results = JenaUtil.createMemoryModel();
		
		Model shapesModel = dataset.getNamedModel(shapesGraph.getURI());
		
		List<Property> properties = SHACLUtil.getAllConstraintProperties();
		
		Resource resource = (Resource) dataset.getDefaultModel().asRDFNode(focusNode);
		Set<Resource> shapes = new HashSet<Resource>();
		for(Resource type : JenaUtil.getAllTypes(resource)) {
			if(JenaUtil.hasIndirectType(type.inModel(shapesModel), SH.Shape)) {
				shapes.add(type);
			}
			for(Statement s : shapesModel.listStatements(null, SH.scopeClass, type).toList()) {
				shapes.add(s.getSubject());
			}
		}
		shapes.addAll(JenaUtil.getResourceProperties(resource, SH.nodeShape));
		for(Resource shape : shapes) {
			addResourceViolations(dataset, shapesGraph, focusNode, shape.asNode(), properties, minSeverity, results, monitor);
		}
		
		return results;
	}

	
	/**
	 * Validates a given resource against a given Shape.
	 * @param dataset  the Dataset to operate on
	 * @param shapesGraph  the URI of the shapes graph (must be in the dataset)
	 * @param focusNode  the Resource to validate
	 * @param shape  the sh:Shape to validate against
	 * @param minSeverity  the minimum severity level or null for all constraints
	 * @param monitor  an optional progress monitor
	 * @return a List of constraint violations
	 */
	public Model validateNodeAgainstShape(Dataset dataset, Resource shapesGraph, Node focusNode, Node shape, Resource minSeverity, ProgressMonitor monitor) {
		Model results = JenaUtil.createMemoryModel();
		addResourceViolations(dataset, shapesGraph, focusNode, shape, SHACLUtil.getAllConstraintProperties(), minSeverity, results, monitor);
		return results;
	}
}
