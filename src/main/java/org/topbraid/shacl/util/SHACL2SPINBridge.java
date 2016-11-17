package org.topbraid.shacl.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.ObjectPropertyPath;
import org.topbraid.spin.constraints.SimplePropertyPath;
import org.topbraid.spin.constraints.SubjectPropertyPath;
import org.topbraid.spin.util.JenaUtil;
import org.topbraid.spin.vocabulary.SPIN;

/**
 * Utility to perform both SHACL and SPIN validation for tools that are supporting both,
 * at least for a transition period.  This class may get removed once the ties between
 * the SHACL API and the SPIN API get severed.
 * 
 * @author Holger Knublauch
 */
public class SHACL2SPINBridge {

	
	public static ConstraintViolation createConstraintViolation(Resource shResult) {
		String message = JenaUtil.getStringProperty(shResult, SH.resultMessage);
		Resource root = JenaUtil.getResourceProperty(shResult, SH.focusNode);
		List<SimplePropertyPath> paths = new LinkedList<SimplePropertyPath>();
		if(root != null) {
			Resource path = JenaUtil.getResourceProperty(shResult, SH.resultPath);
			if(path != null) {
				if(path.isURIResource()) {
					paths.add(new ObjectPropertyPath(root, JenaUtil.asProperty(path)));
				}
				else {
					Resource inverse = JenaUtil.getResourceProperty(path, SH.inversePath);
					if(inverse != null && inverse.isURIResource()) {
						paths.add(new SubjectPropertyPath(root, JenaUtil.asProperty(inverse)));
					}
				}
			}
		}
		ConstraintViolation cv = new ConstraintViolation(root, paths, null, message, null);
		if(shResult.hasProperty(SH.resultSeverity, SH.Violation)) {
			cv.setLevel(SPIN.Error);
		}
		else if(shResult.hasProperty(SH.resultSeverity, SH.Warning)) {
			cv.setLevel(SPIN.Warning);
		}
		else if(shResult.hasProperty(SH.resultSeverity, SH.Info)) {
			cv.setLevel(SPIN.Info);
		}
		return cv;
	}

	
	public static List<ConstraintViolation> createConstraintViolations(Model resultsModel) {
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		for(Resource shResult : JenaUtil.getAllInstances(SH.ValidationResult.inModel(resultsModel))) {
			results.add(createConstraintViolation(shResult));
		}
		return results;
	}


	public static Resource createValidationResult(ConstraintViolation cv, Model results) {
		Resource result = results.createResource(SPIN.Fatal.equals(cv.getLevel()) ? DASH.FailureResult : SH.ValidationResult);
		if(SPIN.Info.equals(cv.getLevel())) {
			result.addProperty(SH.resultSeverity, SH.Info);
		}
		else if(SPIN.Warning.equals(cv.getLevel())) {
			result.addProperty(SH.resultSeverity, SH.Warning);
		}
		Resource root = cv.getRoot();
		if(root != null) {
			result.addProperty(SH.focusNode, root);
		}
		Collection<SimplePropertyPath> paths = cv.getPaths();
		if(paths.size() == 1) {
			SimplePropertyPath path = paths.iterator().next();
			result.addProperty(SH.focusNode, root);
			if(path instanceof ObjectPropertyPath) {
				result.addProperty(SH.resultPath, path.getPredicate());
			}
			else {
				Resource inverse = result.getModel().createResource();
				inverse.addProperty(SH.inversePath, path.getPredicate());
				result.addProperty(SH.resultPath, inverse);
			}
			if(cv.getValue() != null) {
				result.addProperty(SH.value, cv.getValue());
			}
		}
		String message = cv.getMessage();
		if(message != null) {
			result.addProperty(SH.resultMessage, message);
		}
		return result;
	}
}
