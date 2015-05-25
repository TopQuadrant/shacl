package org.topbraid.shacl.constraints;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SHACL;
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
					if(SHACL.NativeConstraint.equals(type)) {
						SHACLConstraint constraint = SHACLFactory.asNativeConstraint(c);
						addQueryResults(results, constraint, shape, resource, dataset, shapesGraph, minSeverity, monitor);
					}
					else if(JenaUtil.hasIndirectType(type, SHACL.ConstraintTemplate)) {
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
	 * @param testResource  the Resource to validate
	 * @param monitor  an optional progress monitor
	 * @return a List of constraint violations
	 */
	public Model validateResource(Dataset dataset, Resource shapesGraph, Node resourceNode, Resource minSeverity, ProgressMonitor monitor) {
		
		Model results = JenaUtil.createMemoryModel();
		
		Model shapesModel = dataset.getNamedModel(shapesGraph.getURI());
		
		List<Property> properties = SHACLUtil.getAllConstraintProperties();
		
		Resource resource = (Resource) dataset.getDefaultModel().asRDFNode(resourceNode);
		Set<Resource> shapes = new HashSet<Resource>();
		for(Resource type : JenaUtil.getAllTypes(resource)) {
			if(JenaUtil.hasIndirectType(type.inModel(shapesModel), SHACL.Shape)) {
				shapes.add(type);
			}
			for(Statement s : shapesModel.listStatements(null, SHACL.scopeClass, type).toList()) {
				shapes.add(s.getSubject());
			}
		}
		shapes.addAll(JenaUtil.getResourceProperties(resource, SHACL.nodeShape));
		for(Resource shape : shapes) {
			addResourceViolations(dataset, shapesGraph, resourceNode, shape.asNode(), properties, minSeverity, results, monitor);
		}
		
		return results;
	}
}
