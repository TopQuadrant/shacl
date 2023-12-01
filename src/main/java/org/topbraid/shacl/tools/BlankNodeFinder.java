package org.topbraid.shacl.tools;

import org.apache.jena.rdf.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.shacl.vocabulary.SH;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

        Model blankNodes = ModelFactory.createDefaultModel();

        Stream<Statement> statements = report.listStatements(null, SH.result, (RDFNode) null).toList().stream();

        statements.map(Statement::getResource).forEach(resource -> {
            try{
                Stream<Statement> reportStatements = report.listStatements(resource, null, (RDFNode) null).toList().stream();
                reportStatements.forEach(statement -> {
                    if (statement.getObject().isAnon()) {
                        List<Statement> allBlankNodes = resolveAllBlankNodes(shapesGraph, statement.getResource());
                        blankNodes.add(statement);
                        blankNodes.add(allBlankNodes);
                    }
                });
            }
            catch (Exception e){
                logger.error("Error while processing blank node: " + resource.toString());
            }
        });

        return blankNodes;
    }
    /**
     * From given subject, find all blank nodes that can be reached from it recursively
     * @param model The model to search in
     * @param subject The subject to start from(BLANK NODE)
     * @return A list of all blank nodes that can be reached from the given subject
     */
    private static List<Statement> resolveAllBlankNodes(Model model, Resource subject) {
        List<Statement> blankNodes = new ArrayList<>();
        Stream<Statement> statements = model.listStatements(subject, null, (RDFNode) null).toList().stream();
        statements.forEach(statement -> {
            blankNodes.add(statement);
            if (statement.getObject().isAnon()) {
                blankNodes.addAll(resolveAllBlankNodes(model, statement.getResource()));
            }
        });

        return blankNodes;
    }
}
