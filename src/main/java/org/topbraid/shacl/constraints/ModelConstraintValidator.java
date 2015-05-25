package org.topbraid.shacl.constraints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * An engine that can validate all constraints defined for a given Model.
 * 
 * @author Holger Knublauch
 */
public class ModelConstraintValidator {
	
	public static final String SCOPE_VAR_NAME = "SCOPE_SHAPE";

	private static ModelConstraintValidator singleton = new ModelConstraintValidator();
	
	public static ModelConstraintValidator get() {
		return singleton;
	}
	
	public static void set(ModelConstraintValidator value) {
		singleton = value;
	}

	
	/**
	 * Validates all resources in a given Model.
	 * @param dataset  the Dataset to validate
	 * @param monitor  an optional ProgressMonitor
	 * @return a Model containing sh:ConstraintViolations
	 */
	public Model validateModel(Dataset dataset, Resource shapesGraph, Resource minSeverity, boolean filtered, ProgressMonitor monitor) throws InterruptedException {
		
		if(dataset.getDefaultModel() == null) {
			throw new IllegalArgumentException("Dataset requires a default model");
		}
		
		Model shapesModel = dataset.getNamedModel(shapesGraph.getURI());
		
		if(monitor != null) {
			monitor.subTask("Preparing execution plan");
		}
		
		List<Property> constraintProperties = SHACLUtil.getAllConstraintProperties();
		Map<Resource,List<SHACLConstraint>> map = buildShape2ConstraintsMap(shapesModel, constraintProperties, filtered);
		if(monitor != null) {
			monitor.subTask("");
		}
		
		if(monitor != null) {
			monitor.beginTask("Validating constraints for " + map.size() + " shapes...", map.size());
		}
		
		Model results = JenaUtil.createMemoryModel();
		for(Resource shape : map.keySet()) {
			for(SHACLConstraint constraint : map.get(shape)) {
				validateConstraintForShape(dataset, shapesGraph, minSeverity, constraint, shape, results, monitor);
				if(monitor != null) {
					monitor.worked(1);
					if(monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			}
		}
		
		return results;
	}
	
	
	private Map<Resource,List<SHACLConstraint>> buildShape2ConstraintsMap(Model shapesModel, List<Property> constraintProperties, boolean filtered) {
		Map<Resource,List<SHACLConstraint>> map = new HashMap<Resource,List<SHACLConstraint>>();
		for(Property constraintProperty : constraintProperties) {
			for(Statement s : shapesModel.listStatements(null, constraintProperty, (RDFNode)null).toList()) {
				if(s.getObject().isResource()) {
					Resource cls = s.getSubject();
					if(!filtered || ModelClassesFilter.get().accept(cls)) {
						List<SHACLConstraint> list = map.get(cls);
						if(list == null) {
							list = new LinkedList<SHACLConstraint>();
							map.put(cls, list);
						}
						Resource c = s.getResource(); 
						Resource type = JenaUtil.getType(c);
						if(type == null && c.isAnon()) {
							type = SHACLUtil.getDefaultTemplateType(c);
						}
						if(type != null) {
							if(SH.NativeConstraint.equals(type)) {
								list.add(SHACLFactory.asNativeConstraint(c));
							}
							else if(JenaUtil.hasIndirectType(type, SH.ConstraintTemplate)) {
								list.add(SHACLFactory.asTemplateConstraint(c));
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	
	private void validateConstraintForShape(Dataset dataset, Resource shapesGraph, Resource minSeverity, SHACLConstraint constraint, Resource shape, Model results, ProgressMonitor monitor) {

		boolean hasNodeShape = dataset.getDefaultModel().contains(null, SH.nodeShape, shape);
		
		Set<Resource> scopeClasses = new HashSet<Resource>();
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			scopeClasses.add(shape);
		}
		scopeClasses.addAll(JenaUtil.getResourceProperties(shape, SH.scopeClass));
		
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			
			Resource severity = executable.getSeverity();
			if(minSeverity == null || minSeverity.equals(severity) || JenaUtil.hasSuperClass(severity, minSeverity)) {
				
				// Execute for all resources with matching sh:nodeShape
				if(hasNodeShape) {
					
					if(monitor != null) {
						monitor.subTask("Validating sh:nodeShape at Shape " + SPINLabels.get().getLabel(shape));
					}
					if(executable instanceof NativeConstraintExecutable) {
						NativeConstraintExecutable e = (NativeConstraintExecutable)executable;
						ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
						lang.executeNative(dataset, shape, shapesGraph, results, constraint, null, SH.nodeShape, shape, e);
					}
					else {
						TemplateConstraintExecutable e = (TemplateConstraintExecutable)executable;
						ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
						lang.executeTemplate(dataset, shape, shapesGraph, results, constraint, null, SH.nodeShape, shape, e);
					}
				}
				
				for(Resource cls : scopeClasses) {
					for(Resource c : JenaUtil.getAllSubClassesStar(cls)) {
						if(dataset.getDefaultModel().contains(null, RDF.type, c)) {
							
							if(monitor != null) {
								monitor.subTask("Validating at Class " + SPINLabels.get().getLabel(c));
							}
							if(executable instanceof NativeConstraintExecutable) {
								NativeConstraintExecutable e = (NativeConstraintExecutable)executable;
								ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
								lang.executeNative(dataset, shape, shapesGraph, results, constraint, null, RDF.type, c, e);
							}
							else {
								TemplateConstraintExecutable e = (TemplateConstraintExecutable)executable;
								ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguage(e);
								lang.executeTemplate(dataset, shape, shapesGraph, results, constraint, null, RDF.type, c, e);
							}
						}
					}
				}
			}
		}
	}
}
