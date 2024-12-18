package org.topbraid.shacl.expr;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.shacl.vocabulary.SH;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Node expressions based on a SPARQL query, identified by sh:select or sh:ask.
 * <p>
 * This node expression type is not part of the SHACL-AF 1.0 document, but a candidate for 1.1.
 *
 * @author Holger Knublauch
 */
public abstract class AbstractSPARQLExpression extends AbstractInputExpression {

    private Query query;

    private String queryString;


    protected AbstractSPARQLExpression(Resource expr, Query query, NodeExpression input, String queryString) {
        super(expr, input);
        this.query = query;
        this.queryString = queryString;
    }


    @Override
    public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
        List<RDFNode> focusNodes;
        NodeExpression input = getInput();
        if (input != null) {
            focusNodes = input.eval(focusNode, context).toList();
        } else {
            focusNodes = Collections.singletonList(focusNode);
        }
        List<RDFNode> results = new LinkedList<>();
        for (RDFNode f : focusNodes) {
            QuerySolutionMap binding = new QuerySolutionMap();
            binding.add(SH.thisVar.getName(), f);
            try (QueryExecution qexec = ARQFactory.get().createQueryExecution(query, context.getDataset(), binding)) {
                if (query.isAskType()) {
                    results.add(qexec.execAsk() ? JenaDatatypes.TRUE : JenaDatatypes.FALSE);
                } else {
                    ResultSet rs = qexec.execSelect();
                    String varName = rs.getResultVars().get(0);
                    while (rs.hasNext()) {
                        RDFNode node = rs.next().get(varName);
                        if (node != null) {
                            results.add(node);
                        }
                    }
                }
            }
        }
        return WrappedIterator.create(results.iterator());
    }


    @Override
    public List<String> getFunctionalSyntaxArguments() {
        List<String> results = new LinkedList<>();
        results.add(FmtUtils.stringForNode(NodeFactory.createLiteralString(queryString)));
        NodeExpression input = getInput();
        if (input != null) {
            results.add(input.getFunctionalSyntax());
        }
        return results;
    }


    public Query getQuery() {
        return query;
    }


    public String getQueryString() {
        return queryString;
    }
}
