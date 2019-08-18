package org.topbraid.shacl.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;
import org.topbraid.shacl.vocabulary.SH;

/**
 * A ValidationReport based on a sh:ValidationReport instance in an RDF graph.
 * 
 * @author Holger Knublauch
 */
public class ResourceValidationReport implements ValidationReport {
	
	private Resource report;
	
	private List<ValidationResult> results = new ArrayList<>();
	

	public ResourceValidationReport(Resource report) {
		this.report = report;
		report.listProperties(SH.result).forEachRemaining(s -> results.add(new ResourceValidationResult(s.getResource())));
	}

	
	@Override
	public boolean conforms() {
		return results.isEmpty();
	}
	
	
	public Resource getResource() {
		return report;
	}

	
	@Override
	public List<ValidationResult> results() {
		return results;
	}
}
