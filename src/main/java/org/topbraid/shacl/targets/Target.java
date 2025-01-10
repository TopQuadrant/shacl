package org.topbraid.shacl.targets;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;

import java.util.Set;

/**
 * Interface for the various target types supported by SHACL, including <a href="https://www.w3.org/TR/shacl/#targets">SHACL Targets</a>
 * but also SHACL-AF and SHACL-JS extensions.
 *
 * @author Holger Knublauch
 */
public interface Target {

    /**
     * Adds target nodes to a given result collection.
     *
     * @param dataset the Dataset with the potential target nodes in the default graph
     * @param results the collection to add the results to
     */
    void addTargetNodes(Dataset dataset, Set<RDFNode> results);

    /**
     * Checks whether a given node is in the target.
     *
     * @param dataset the Dataset with the potential target node in the default graph
     * @param node    the potential target node
     * @return true if node is in this target
     */
    boolean contains(Dataset dataset, RDFNode node);
}
