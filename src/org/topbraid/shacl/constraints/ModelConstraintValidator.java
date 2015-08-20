package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.topbraid.shacl.model.SHACLConstraint;
import org.topbraid.shacl.model.SHACLFactory;
import org.topbraid.shacl.model.SHACLRule;
import org.topbraid.shacl.rules.RuleExecutable;
import org.topbraid.shacl.util.ModelPrinter;
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
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * An engine that can validate all constraints defined for a given Model,
 * where the Model is expected to be the default graph of a given Dataset.
 * The engine will look at SHACL declarations inside of the Model to figure
 * out which shapes to validate.
 * 
 * @author Holger Knublauch
 */
public class ModelConstraintValidator {
	
	public static final String FILTER_VAR_NAME = "FILTER_SHAPE";

	private static ModelConstraintValidator singleton = new ModelConstraintValidator();
	
	public static ModelConstraintValidator get() {
		return singleton;
	}
	
	public static void set(ModelConstraintValidator value) {
		singleton = value;
	}

	
	/**
	 * Validates all resources in a given Model, which is expected to be the default
	 * graph of a given Dataset.
	 * @param dataset  the Dataset to validate
	 * @param shapesGraphURI  the URI of the shapes graph in the dataset
	 * @param minSeverity  the minimum severity, e.g. sh:Error or null for all
	 * @param filtered  true to exclude checking schema-level resources
	 * @param monitor  an optional ProgressMonitor
	 * @return a Model containing violation results - empty if OK
	 */
	public Model validateModel(Dataset dataset, URI shapesGraphURI, Resource minSeverity, boolean filtered, ProgressMonitor monitor) throws InterruptedException {
		
		if(dataset.getDefaultModel() == null) {
			throw new IllegalArgumentException("Dataset requires a default model");
		}
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		if(monitor != null) {
			monitor.subTask("Preparing execution plan");
		}
		
		List<Property> ruleProperties = SHACLUtil.getAllRuleProperties();
		Map<Resource,List<SHACLRule>> rulemap = buildShape2RulesMap(shapesModel, dataset.getDefaultModel(), ruleProperties, filtered);
		if(monitor != null) {
			monitor.subTask("");
		}
		
		if(monitor != null) {
			monitor.beginTask("Validating rules for " + rulemap.size() + " shapes...", rulemap.size());
		}
		
		List<Property> constraintProperties = SHACLUtil.getAllConstraintProperties();
		Map<Resource,List<SHACLConstraint>> map = buildShape2ConstraintsMap(shapesModel, dataset.getDefaultModel(), constraintProperties, filtered);

		if(monitor != null) {
			monitor.subTask("");
		}
		
		if(monitor != null) {
			monitor.beginTask("Validating constraints for " + map.size() + " shapes...", map.size());
		}
		
		Model results = JenaUtil.createMemoryModel();

		for(Resource shape : rulemap.keySet()) {
			for(SHACLRule rule : rulemap.get(shape)) {
				executeRuleForShape(dataset, shapesGraphURI, rule, shape, results, monitor, map);
			}
		}
		for(Resource shape : map.keySet()) {
			for(SHACLConstraint constraint : map.get(shape)) {
				validateConstraintForShape(dataset, shapesGraphURI, minSeverity, constraint, shape, results, monitor);
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
	
	
	private Map<Resource,List<SHACLConstraint>> buildShape2ConstraintsMap(Model shapesModel, Model dataModel, List<Property> constraintProperties, boolean filtered) {
		Map<Resource,List<SHACLConstraint>> map = new HashMap<Resource,List<SHACLConstraint>>();
		for(Property constraintProperty : constraintProperties) {
			for(Statement s : shapesModel.listStatements(null, constraintProperty, (RDFNode)null).toList()) {
				if(s.getObject().isResource()) {
					Resource shape = s.getSubject();
					if((!filtered || ModelClassesFilter.get().accept(shape)) && hasScope(shape, dataModel)) {
						List<SHACLConstraint> list = map.get(shape);
						if(list == null) {
							list = new LinkedList<SHACLConstraint>();
							map.put(shape, list);
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
							// TODO add property constraint if
						}
					}
				}
			}
		}
		return map;
	}
	
	private Map<Resource,List<SHACLRule>> buildShape2RulesMap(Model shapesModel, Model dataModel, List<Property> ruleProperties, boolean filtered) {
		Map<Resource,List<SHACLRule>> map = new HashMap<Resource,List<SHACLRule>>();
		for(Property ruleProperty : ruleProperties) {
			for(Statement s : shapesModel.listStatements(null, ruleProperty, (RDFNode)null).toList()) {
				if(s.getObject().isResource()) {
					Resource shape = s.getSubject();
					if((!filtered || ModelClassesFilter.get().accept(shape)) && hasScope(shape, dataModel)) {
						List<SHACLRule> list = map.get(shape);
						if(list == null) {
							list = new LinkedList<SHACLRule>();
							map.put(shape, list);
						}
						Resource c = s.getResource(); 
						Resource type = JenaUtil.getType(c);
						if(type == null && c.isAnon()) {
							type = SHACLUtil.getDefaultTemplateType(c);
						}
						if(type != null) {
							if(SH.NativeRule.equals(type)) {
								list.add(SHACLFactory.asNativeRule(c));
							}
							else if(JenaUtil.hasIndirectType(type, SH.RuleTemplate)) {
								list.add(SHACLFactory.asTemplateRule(c));
							}
							// TODO add property constraint if
						}
					}
				}
			}
		}
		return map;
	}
	
	
	// Used to filter out scopeless shapes that are only used as part of other shapes
	private boolean hasScope(Resource shape, Model dataModel) {
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			return true;
		}
		else if(shape.hasProperty(SH.scope)) {
			return true;
		}
		else if(shape.hasProperty(SH.scopeClass)) {
			return true;
		}
		else if(dataModel.contains(null, SH.nodeShape, shape)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private void validateConstraintForShape(Dataset dataset, URI shapesGraphURI, Resource minSeverity, SHACLConstraint constraint, Resource shape, Model results, ProgressMonitor monitor) {
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			Resource severity = executable.getSeverity();
			if(minSeverity == null || minSeverity.equals(severity) || JenaUtil.hasSuperClass(severity, minSeverity)) {
				if(monitor != null) {
					monitor.subTask("Validating Shape " + SPINLabels.get().getLabel(shape));
				}
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				lang.executeConstraint(dataset, shape, shapesGraphURI, constraint, executable, null, results);
			}
		}
	}
	
	private void executeRuleForShape(Dataset dataset, URI shapesGraphURI, SHACLRule rule, Resource shape, Model results, ProgressMonitor monitor, Map<Resource,List<SHACLConstraint>> map) {
		for(RuleExecutable executable : rule.getExecutables()) {
			
			//if(minSeverity == null || minSeverity.equals(severity) || JenaUtil.hasSuperClass(severity, minSeverity)) {
				if(monitor != null) {
					monitor.subTask("Executing Rules for Shape " + SPINLabels.get().getLabel(shape));
				}
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForRule(executable);
				lang.executeRule(dataset, shape, shapesGraphURI, rule, executable, null, results, map);
				
			//}
		}
	}
}
