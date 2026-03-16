package org.topbraid.shacl.targets;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Set;

/**
 * A Target based on a sh:targetNode statement.
 *
 * @author Holger Knublauch
 */
public class NodeTarget implements Target {

    private RDFNode node;


    public NodeTarget(RDFNode node) {
        this.node = node;
    }


    @Override
    public void addTargetNodes(Dataset dataset, Set<RDFNode> results) {
        results.add(node.inModel(dataset.getDefaultModel()));
    }


    @Override
    public boolean contains(Dataset dataset, RDFNode node) {
        return this.node.equals(node);
    }
}
