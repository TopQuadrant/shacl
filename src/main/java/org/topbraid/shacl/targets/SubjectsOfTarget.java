package org.topbraid.shacl.targets;

import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A Target based on a sh:targetSubjectsOf statement.
 * 
 * @author Holger Knublauch
 */
public class SubjectsOfTarget implements Target {

	private Property predicate;
	

	public SubjectsOfTarget(Property predicate) {
		this.predicate = predicate;
	}
	
	
	@Override
	public void addTargetNodes(Dataset dataset, Set<RDFNode> results) {
		dataset.getDefaultModel().listSubjectsWithProperty(predicate).forEachRemaining(results::add);
	}


	@Override
	public boolean contains(Dataset dataset, RDFNode node) {
		return node instanceof Resource && dataset.getDefaultModel().contains((Resource)node, predicate, (RDFNode)null);
	}
}
