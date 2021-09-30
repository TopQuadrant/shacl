package org.topbraid.shacl.targets;

import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.JenaUtil;

/**
 * A Target based on a sh:targetClass or implicit target class statement.
 * 
 * @author Holger Knublauch
 */
public class InstancesTarget implements Target {

	private Resource type;

	
	public InstancesTarget(Resource type) {
		this.type = type;
	}
	
	
	@Override
	public void addTargetNodes(Dataset dataset, Set<RDFNode> results) {
		results.addAll(JenaUtil.getAllInstances(type.inModel(dataset.getDefaultModel())));
	}


	@Override
	public boolean contains(Dataset dataset, RDFNode node) {
		return node instanceof Resource && JenaUtil.hasIndirectType((Resource)node, type);
	}
}
