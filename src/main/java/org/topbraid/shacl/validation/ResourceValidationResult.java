package org.topbraid.shacl.validation;

import java.util.Collection;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A ValidationResult backed by an existing Resource.
 * 
 * @author Holger Knublauch
 */
public class ResourceValidationResult implements ValidationResult {
	
	private Resource result;

	
	public ResourceValidationResult(Resource result) {
		this.result = result;
	}

	
	@Override
	public RDFNode getFocusNode() {
		return JenaUtil.getProperty(result, SH.focusNode);
	}

	
	@Override
	public String getMessage() {
		return JenaUtil.getStringProperty(result, SH.resultMessage);
	}

	@Override
	public Collection<RDFNode> getMessages() {
		return result.listProperties(SH.resultMessage).mapWith(s -> s.getObject()).toList();
	}

	@Override
	public Resource getPath() {
		return JenaUtil.getResourceProperty(result, SH.resultPath);
	}
	
	public Resource getResource() {
		return result;
	}

	@Override
	public Resource getSeverity() {
		return JenaUtil.getResourceProperty(result, SH.resultSeverity);
	}

	@Override
	public Resource getSourceConstraint() {
		return JenaUtil.getResourceProperty(result, SH.sourceConstraint);
	}

	@Override
	public Resource getSourceConstraintComponent() {
		return JenaUtil.getResourceProperty(result, SH.sourceConstraintComponent);
	}

	@Override
	public Resource getSourceShape() {
		return JenaUtil.getResourceProperty(result, SH.sourceShape);
	}

	@Override
	public RDFNode getValue() {
		return JenaUtil.getProperty(result, SH.value);
	}
}
