package org.topbraid.shacl.targets;

import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;

/**
 * A Target based on a sh:targetObjectsOf statement.
 * 
 * @author Holger Knublauch
 */
public class ObjectsOfTarget implements Target {

	private Property predicate;

	
	public ObjectsOfTarget(Property predicate) {
		this.predicate = predicate;
	}
	
	
	@Override
	public void addTargetNodes(Dataset dataset, Set<RDFNode> results) {
		dataset.getDefaultModel().listObjectsOfProperty(predicate).forEachRemaining(results::add);
	}


	@Override
	public boolean contains(Dataset dataset, RDFNode node) {
		return dataset.getDefaultModel().contains(null, predicate, node);
	}
}
