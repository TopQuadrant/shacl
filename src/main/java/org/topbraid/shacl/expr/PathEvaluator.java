/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package org.topbraid.shacl.expr;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.expr.lib.DistinctExpression;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An object that computes the values of a sh:path node expression.
 * This implements consistent handling of inferred values.
 * <p>
 * Inferences are limited to simple forward paths consisting of a single predicate.
 *
 * @author Holger Knublauch
 */
public class PathEvaluator {

    private NodeExpression input;

    private boolean isInverse;

    private Path jenaPath;

    private Property predicate;


    /**
     * Constructs a PathEvaluator for a single "forward" property look-up.
     *
     * @param predicate the predicate
     */
    public PathEvaluator(Property predicate) {
        this.predicate = predicate;
    }


    /**
     * Constructs a PathEvaluator for an arbitrary SPARQL path (except single forward properties).
     *
     * @param path        the path
     * @param shapesModel the shapes Model
     */
    public PathEvaluator(Path path, Model shapesModel) {
        this.jenaPath = path;
        isInverse = jenaPath instanceof P_Inverse && ((P_Inverse) jenaPath).getSubPath() instanceof P_Link;
        if (isInverse) {
            P_Link link = (P_Link) ((P_Inverse) jenaPath).getSubPath();
            predicate = shapesModel.getProperty(link.getNode().getURI());
        }
    }


    public ExtendedIterator<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
        if (input == null) {
            ExtendedIterator<RDFNode> asserted = evalFocusNode(focusNode, context);
            return withDefaultValues(withInferences(asserted, focusNode, context), focusNode, context);
        } else {
            Iterator<RDFNode> it = input.eval(focusNode, context);
            if (it.hasNext()) {
                RDFNode first = it.next();
                ExtendedIterator<RDFNode> result = withDefaultValues(withInferences(evalFocusNode(first, context), first, context), first, context);
                while (it.hasNext()) {
                    RDFNode n = it.next();
                    result = result.andThen(withDefaultValues(withInferences(evalFocusNode(n, context), n, context), first, context));
                }
                return result;
            } else {
                return WrappedIterator.emptyIterator();
            }
        }
    }


    public ExtendedIterator<RDFNode> evalReverse(RDFNode valueNode, NodeExpressionContext context) {
        // See isReversible, this only supports trivial cases for now
        if (isInverse) {
            if (valueNode instanceof Literal) {
                return WrappedIterator.emptyIterator();
            } else {
                return context.getDataset().getDefaultModel().listObjectsOfProperty((Resource) valueNode, predicate);
            }
        } else {
            return context.getDataset().getDefaultModel().listSubjectsWithProperty(predicate, valueNode).mapWith(r -> (RDFNode) r);
        }
    }


    /**
     * Gets the executed Jena Path or null if this is just a simple forward property.
     *
     * @return the executed Jena Path
     */
    public Path getJenaPath() {
        return jenaPath;
    }


    /**
     * Gets the predicate if this is a simple forward property path.
     * Returns null for inverse paths.
     *
     * @return the predicate or null
     */
    public Property getPredicate() {
        if (predicate != null && !isInverse) {
            return predicate;
        } else {
            return null;
        }
    }


    /**
     * Checks if the values of this may be inferred.
     * This is the case if this uses a single forward property path and there are any sh:values or sh:defaultValue statements on
     * that predicate in the provided shapes graph.
     * The actual computation on whether the values are inferred depends on the actual focus node, which is why this is
     * only a "maybe".
     * This function may be used to exclude optimizations that are possible if we know that no inferences can exist.
     *
     * @param shapesGraph the ShapesGraph (which caches previous results)
     * @return true  if there may be sh:values statements
     */
    public boolean isMaybeInferred(ShapesGraph shapesGraph) {
        if (predicate != null && !isInverse) {
            return !shapesGraph.getValuesNodeExpressionsMap(predicate).isEmpty() || !shapesGraph.getDefaultValueNodeExpressionsMap(predicate).isEmpty();
        } else {
            return false;
        }
    }


    public boolean isReversible(ShapesGraph shapesGraph) {
        // Very conservative algorithm for now
        return input == null && !isMaybeInferred(shapesGraph) && jenaPath == null;
    }


    public void setInput(NodeExpression input) {
        this.input = input;
    }


    private ExtendedIterator<RDFNode> evalFocusNode(RDFNode focusNode, NodeExpressionContext context) {
        if (jenaPath == null) {
            if (focusNode.isLiteral()) {
                return WrappedIterator.emptyIterator();
            } else {
                return context.getDataset().getDefaultModel().listObjectsOfProperty((Resource) focusNode, predicate);
            }
        } else if (isInverse) {
            return context.getDataset().getDefaultModel().listSubjectsWithProperty(predicate, focusNode).mapWith(r -> (RDFNode) r);
        } else {
            // This ought to do lazy evaluation too
            List<RDFNode> results = new LinkedList<>();
            SHACLPaths.addValueNodes(focusNode.inModel(context.getDataset().getDefaultModel()), jenaPath, results);
            return WrappedIterator.create(results.iterator());
        }
    }


    private ExtendedIterator<RDFNode> withDefaultValues(ExtendedIterator<RDFNode> base, RDFNode focusNode, NodeExpressionContext context) {
        if (isInverse || predicate == null || base.hasNext()) {
            return base;
        } else {
            Map<Node, NodeExpression> map = context.getShapesGraph().getDefaultValueNodeExpressionsMap(predicate);
            if (map.isEmpty()) {
                return base;
            } else {
                ExtendedIterator<RDFNode> result = WrappedIterator.emptyIterator();
                int count = 0;
                for (Resource type : JenaUtil.getAllTypes((Resource) focusNode)) {
                    NodeExpression expr = map.get(type.asNode());
                    if (expr != null) {
                        result = result.andThen(expr.eval(focusNode, context));
                        count++;
                    }
                }
                if (count > 1) {
                    // Filter out duplicates in case multiple sh:defaultValue expressions exist
                    return DistinctExpression.distinct(result);
                } else {
                    return result;
                }
            }
        }
    }


    private ExtendedIterator<RDFNode> withInferences(ExtendedIterator<RDFNode> base, RDFNode focusNode, NodeExpressionContext context) {
        if (predicate != null && !isInverse && focusNode.isResource()) {
            Map<Node, NodeExpression> map = context.getShapesGraph().getValuesNodeExpressionsMap(predicate);
            if (!map.isEmpty()) {
                ExtendedIterator<RDFNode> result = base;
                boolean hasInferences = false;
                // TODO: support cases like metash:Resource (if it had no target): if the type has a sh:node then the value rules should be found
                //       even if declared in the super-shape
                for (Resource type : JenaUtil.getAllTypes((Resource) focusNode)) {
                    NodeExpression expr = map.get(type.asNode());
                    if (expr != null) {
                        result = result.andThen(expr.eval(focusNode, context));
                        hasInferences = true;
                    }
                }
                if (!hasInferences && map.get(RDFS.Resource.asNode()) != null) {
                    // This is to support cases like generic schema even if no rdf:type is present or it doesn't reach rdfs:Resource in the hierarchy
                    NodeExpression expr = map.get(RDFS.Resource.asNode());
                    result = result.andThen(expr.eval(focusNode, context));
                    hasInferences = true;
                }
                // Filter out duplicates in case the graph contains materialized inferences and because sh:values may return lists
                return DistinctExpression.distinct(result);
            }
        }
        return base;
    }
}
