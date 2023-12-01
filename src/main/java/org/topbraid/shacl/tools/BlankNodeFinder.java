package org.topbraid.shacl.tools;

import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.vocabulary.SH;

import java.util.*;

public class BlankNodeFinder {
    private static final Logger logger = LoggerFactory.getLogger(BlankNodeFinder.class);
    /**
     * Finds all blank nodes in the report that references the shapes graph
     * @param report The validation report
     * @param shapesGraph The shapes graph
     * @return A model containing all blank nodes
     */
    public static Model findBlankNodes(Model report, Model shapesGraph) {
        // Find all blank nodes that references the shapes graph

        Set<Statement> blankNodes = new HashSet<>();

        StmtIterator statements = report.listStatements(null, SH.result, (RDFNode) null);

        statements.mapWith(Statement::getResource).forEach(resource -> {
            try{
                StmtIterator reportStatements = report.listStatements(resource, null, (RDFNode) null);
                reportStatements.forEach(statement -> {
                    if (statement.getObject().isAnon()) {
                        Set<Statement> allBlankNodes = resolveAllBlankNodes(shapesGraph, statement.getResource(), blankNodes);
                        blankNodes.add(statement);
                        blankNodes.addAll(allBlankNodes);
                    }
                });
            }
            catch (Exception e){
                logger.error("Error while processing blank node: " + resource.toString());
            }
        });

        return ModelFactory.createDefaultModel().add(new ArrayList<>(blankNodes));
    }
    /**
     * From given subject, find all blank nodes that can be reached from it recursively
     * @param model The model to search in
     * @param subject The subject to start from(BLANK NODE)
     * @return A list of all blank nodes that can be reached from the given subject
     */
    private static Set<Statement> resolveAllBlankNodes(Model model, Resource subject, final Set<Statement> blankNodes) {
        final Queue<Resource> queue = new LinkedList<>();
        queue.add(subject);
        while (!queue.isEmpty()) {
            final Resource resource = queue.remove();
            final StmtIterator statements = model.listStatements(resource, null, (RDFNode) null);
            statements.forEachRemaining(statement -> {
                if (blankNodes.contains(statement)) {
                    return;
                }
                blankNodes.add(statement);
                if (statement.getObject().isAnon()) {
                    queue.add(statement.getResource());
                }
            });
        }

        return blankNodes;
    }
}
