package org.topbraid.shacl.targets;

import java.util.Collection;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;

public interface Target {

	void addTargetNodes(Dataset dataset, Collection<RDFNode> results);
	
	boolean contains(Dataset dataset, RDFNode node);
}
