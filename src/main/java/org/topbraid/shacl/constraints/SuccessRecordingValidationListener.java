package org.topbraid.shacl.constraints;

import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TSH;
import org.topbraid.spin.util.JenaUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * A ValidationListener that records all successfully executed constraint/shape pairs
 * in a Model that could later be merged into the validation results.
 * 
 * Note that this assumes single-threaded execution.
 * 
 * @author Holger Knublauch
 */
public class SuccessRecordingValidationListener implements ValidationListener {
	
	private Model model = JenaUtil.createMemoryModel();
	
	private long oldSize;
	
	
	public Model getResults() {
		return model;
	}

	
	@Override
	public void validationFinished(Resource shape,
			ConstraintExecutable executable, Resource focusNode,
			ExecutionLanguage lang, Model results) {
		if(results.size() == oldSize) {
			Resource success = model.createResource(TSH.SuccessResult);
			success.addProperty(SH.sourceShape, shape);
			success.addProperty(SH.sourceConstraint, executable.getTemplateCall() != null ? executable.getTemplateCall() : executable.getResource());
			if(focusNode != null) {
				success.addProperty(SH.focusNode, focusNode);
			}
		}
	}

	
	@Override
	public void validationStarting(Resource shape,
			ConstraintExecutable executable, Resource focusNode,
			ExecutionLanguage lang, Model results) {
		oldSize = results.size();
	}
}
