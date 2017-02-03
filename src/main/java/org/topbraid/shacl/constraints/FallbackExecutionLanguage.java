package org.topbraid.shacl.constraints;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.model.SHConstraint;
import org.topbraid.shacl.model.SHConstraintComponent;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

/**
 * An ExecutionLanguage that serves as fall-back, if a constraint or parameterizable does not define
 * any valid executable property such as sh:sparql.
 * This will simply report a failure that the constraint could not be evaluated.
 * 
 * @author Holger Knublauch
 */
public class FallbackExecutionLanguage implements ExecutionLanguage {

	
	@Override
	public SHConstraint asConstraint(Resource c) {
		return null;
	}


	@Override
	public boolean canExecuteConstraint(ConstraintExecutable executable) {
		return true;
	}

	
	@Override
	public boolean canExecuteTarget(Resource executable) {
		return true;
	}


	@Override
	public boolean executeConstraint(Dataset dataset, Resource shape,
			URI shapesGraphURI, ConstraintExecutable executable,
			RDFNode focusNode, Resource report, Function<RDFNode,String> labelFunction, List<Resource> resultsList) {
		
		if(executable instanceof ComponentConstraintExecutable) {
			SHConstraintComponent cc = ((ComponentConstraintExecutable)executable).getComponent();
			if(SH.PropertyConstraintComponent.equals(cc) || ExecutionLanguageSelector.get().isConstraintComponentWithLanguage(cc)) {
				return false;
			}
		}
		
		Resource result = report.getModel().createResource(DASH.FailureResult);
		report.addProperty(SH.result, result);
		result.addProperty(SH.resultMessage, "No suitable validator found for constraint");
		result.addProperty(SH.sourceConstraint, executable.getConstraint());
		result.addProperty(SH.sourceShape, shape);
		if(executable instanceof ComponentConstraintExecutable) {
			result.addProperty(SH.sourceConstraintComponent, ((ComponentConstraintExecutable)executable).getComponent());
		}
		if(focusNode != null) {
			result.addProperty(SH.focusNode, focusNode);
		}
		resultsList.add(result);
		FailureLog.get().logFailure("No suitable validator found for constraint");
		return true;
	}


	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset,
			Resource executable, SHParameterizableTarget parameterizableTarget) {
		return Collections.emptyList();
	}


	@Override
	public Resource getConstraintComponent() {
		return null;
	}


	@Override
	public Resource getExecutableType() {
		return null;
	}


	@Override
	public Property getParameter() {
		return null;
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset,
			Resource executable, SHParameterizableTarget parameterizableTarget) {
		return false;
	}
}
