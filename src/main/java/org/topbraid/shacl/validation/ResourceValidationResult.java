package org.topbraid.shacl.validation;

import java.util.Collection;
import java.util.List;

import org.apache.jena.rdf.model.Property;
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
		return result.getPropertyResourceValue(SH.resultPath);
	}
	
	@Override
	public List<RDFNode> getPropertyValues(Property predicate) {
		return result.listProperties(predicate).mapWith(s -> s.getObject()).toList();
	}

	public Resource getResource() {
		return result;
	}

	@Override
	public Resource getSeverity() {
		return result.getPropertyResourceValue(SH.resultSeverity);
	}

	@Override
	public Resource getSourceConstraint() {
		return result.getPropertyResourceValue(SH.sourceConstraint);
	}

	@Override
	public Resource getSourceConstraintComponent() {
		return result.getPropertyResourceValue(SH.sourceConstraintComponent);
	}

	@Override
	public Resource getSourceShape() {
		return result.getPropertyResourceValue(SH.sourceShape);
	}

	@Override
	public RDFNode getValue() {
		return JenaUtil.getProperty(result, SH.value);
	}


	@Override
	public int hashCode() {
		return result.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		// This currently only checks equality with another instance of ResourceValidationResult
		return obj instanceof ResourceValidationResult && result.equals(((ResourceValidationResult)obj).result);
	}
}
