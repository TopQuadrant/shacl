package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHFactory;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.system.SPINLabels;
import org.topbraid.spin.util.JenaUtil;

/**
 * An engine that can validate all constraints defined for a given Model,
 * where the Model is expected to be the default graph of a given Dataset.
 * The engine will look at SHACL declarations inside of the Model to figure
 * out which shapes to validate.
 * 
 * @author Holger Knublauch
 */
public class ModelConstraintValidator extends AbstractConstraintValidator {
	
	public static final String FILTER_VAR_NAME = "FILTER_SHAPE";

	public ModelConstraintValidator() {
		super(JenaUtil.createMemoryModel());
	}
	
	
	/**
	 * Validates all resources in a given Model, which is expected to be the default
	 * graph of a given Dataset.
	 * @param dataset  the Dataset to validate
	 * @param shapesGraphURI  the URI of the shapes graph in the dataset
	 * @param minSeverity  the minimum severity, e.g. sh:Error or null for all
	 * @param validateShapes  true to also check schema-level resources (sh:parameter etc)
	 * @param monitor  an optional ProgressMonitor
	 * @return a Model containing violation results - empty if OK
	 */
	public Model validateModel(Dataset dataset, URI shapesGraphURI, Resource minSeverity, boolean validateShapes, Function<RDFNode,String> labelFunction, ProgressMonitor monitor) throws InterruptedException {
		
		if(dataset.getDefaultModel() == null) {
			throw new IllegalArgumentException("Dataset requires a default model");
		}
		
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		
		if(monitor != null) {
			monitor.subTask("Preparing execution plan");
		}
		
		List<Property> constraintProperties = SHACLUtil.getAllConstraintProperties(validateShapes);
		Map<Resource,List<SHConstraint>> map = buildShape2ConstraintsMap(shapesModel, dataset.getDefaultModel(), constraintProperties, validateShapes);
		if(monitor != null) {
			monitor.subTask("");
		}
		
		if(monitor != null) {
			monitor.beginTask("Validating constraints for " + map.size() + " shapes...", map.size());
		}
		
		resultsModel.setNsPrefixes(dataset.getDefaultModel());
		for(Resource shape : map.keySet()) {
			for(SHConstraint constraint : map.get(shape)) {
				validateConstraintForShape(dataset, shapesGraphURI, minSeverity, constraint, shape, labelFunction, monitor);
				if(monitor != null) {
					monitor.worked(1);
					if(monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			}
		}
		
		return resultsModel;
	}
	
	
	private Map<Resource,List<SHConstraint>> buildShape2ConstraintsMap(Model shapesModel, Model dataModel, List<Property> constraintProperties, boolean validateShapes) {
		Map<Resource,List<SHConstraint>> map = new HashMap<Resource,List<SHConstraint>>();
		
		// Collect all shapes, as identified by target and/or type
		Set<Resource> shapes = new HashSet<Resource>();
		collectShapes(shapes, shapesModel, SH.target);
		collectShapes(shapes, shapesModel, SH.targetClass);
		collectShapes(shapes, shapesModel, SH.targetNode);
		collectShapes(shapes, shapesModel, SH.targetObjectsOf);
		collectShapes(shapes, shapesModel, SH.targetSubjectsOf);
		for(Resource shape : JenaUtil.getAllInstances(shapesModel.getResource(SH.Shape.getURI()))) {
			if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
				shapes.add(shape);
			}
		}
		for(Resource shape : shapes) {
			List<SHConstraint> list = new LinkedList<SHConstraint>();
			list.add(SHFactory.asShape(shape));
			map.put(shape, list);
		}
		
		for(Property constraintProperty : constraintProperties) {
			for(Statement s : shapesModel.listStatements(null, constraintProperty, (RDFNode)null).toList()) {
				if(s.getObject().isResource()) {
					Resource shape = s.getSubject();
					if(hasTarget(shape, dataModel, validateShapes)) {
						List<SHConstraint> list = map.get(shape);
						if(list == null) {
							list = new LinkedList<SHConstraint>();
							map.put(shape, list);
						}
						Resource c = s.getResource(); 
						Resource type = JenaUtil.getType(c);
						if(type == null) {
							type = SHACLUtil.getResourceDefaultType(c);
						}
						if(type != null) {
							if(SH.SPARQLConstraint.equals(type)) {
								list.add(SHFactory.asSPARQLConstraint(c));
							}
							else if(JenaUtil.hasSuperClass(type, SH.Constraint)) {
								// Only execute property constraints if sh:predicate is present
								if(c.hasProperty(SH.predicate) || c.hasProperty(SH.path)) {
									list.add(SHFactory.asParameterizableConstraint(c));
								}
							}
						}
					}
				}
			}
		}
		return map;
	}
	
	
	private void collectShapes(Set<Resource> shapes, Model shapesModel, Property predicate) {
		shapes.addAll(shapesModel.listSubjectsWithProperty(predicate).toList());
	}
	
	
	// Used to filter out targetless shapes that are only used as part of other shapes
	private boolean hasTarget(Resource shape, Model dataModel, boolean validateShapes) {
		if(JenaUtil.hasIndirectType(shape, RDFS.Class)) {
			return true;
		}
		else if(shape.hasProperty(SH.target)) {
			return true;
		}
		else if(shape.hasProperty(SH.targetClass)) {
			if(validateShapes) {
				return true;
			}
			else {
				return !JenaUtil.hasIndirectType(shape, SH.ConstraintComponent);
			}
		}
		else if(shape.hasProperty(SH.targetSubjectsOf)) {
			return true;
		}
		else if(shape.hasProperty(SH.targetObjectsOf)) {
			return true;
		}
		else if(shape.hasProperty(SH.targetNode)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	private void validateConstraintForShape(Dataset dataset, URI shapesGraphURI, Resource minSeverity, SHConstraint constraint, Resource shape, Function<RDFNode,String> labelFunction, ProgressMonitor monitor) {
		for(ConstraintExecutable executable : constraint.getExecutables()) {
			Resource severity = executable.getSeverity();
			if(SHACLUtil.hasMinSeverity(severity, minSeverity)) {
				if(monitor != null) {
					monitor.subTask("Validating Shape " + SPINLabels.get().getLabel(shape));
				}
				ExecutionLanguage lang = ExecutionLanguageSelector.get().getLanguageForConstraint(executable);
				lang.executeConstraint(dataset, shape, shapesGraphURI, executable, null, resultsModel, labelFunction, new LinkedList<Resource>());
			}
		}
	}
}
