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
package org.topbraid.shacl.rules;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.statistics.ExecStatistics;
import org.topbraid.jenax.statistics.ExecStatisticsManager;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.engine.AbstractEngine;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.util.OrderComparator;
import org.topbraid.shacl.validation.ValidationEngine;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import java.net.URI;
import java.util.*;

/**
 * A SHACL Rules engine with a pluggable architecture for different execution languages
 * including Triple rules, SPARQL rules and JavaScript rules.
 * <p>
 * In preparation for inclusion into SHACL 1.1, this engine also supports sh:values rules,
 * see <a href="https://www.topquadrant.com/graphql/values.html">Values</a> and treats sh:defaultValues as inferences.
 *
 * @author Holger Knublauch
 */
public class RuleEngine extends AbstractEngine {

    // true to skip sh:values rules from property shapes marked with dash:neverMaterialize true
    private boolean excludeNeverMaterialize;

    // true to skip all sh:values rules
    private boolean excludeValues;

    private Model inferences;

    private Set<Triple> pending = new HashSet<>();

    private Map<Rule, List<Resource>> rule2Conditions = new HashMap<>();

    private Map<Shape, List<Rule>> shape2Rules = new HashMap<>();


    public RuleEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Model inferences) {
        super(dataset, shapesGraph, shapesGraphURI);
        this.inferences = inferences;
    }


    public void executeAll() throws InterruptedException {
        List<Shape> ruleShapes = new ArrayList<>();
        for (Shape shape : shapesGraph.getRootShapes()) {
            if (shape.getShapeResource().hasProperty(SH.rule)) {
                ruleShapes.add(shape);
            } else {
                for (Resource ps : JenaUtil.getResourceProperties(shape.getShapeResource(), SH.property)) {
                    if (ps.hasProperty(SH.values)) {
                        ruleShapes.add(shape);
                        break;
                    }
                }
            }
        }
        executeShapes(ruleShapes, null);
    }


    public void executeAllDefaultValues() throws InterruptedException {
        // Add sh:defaultValues where applicable
        Model shapesModel = this.getShapesModel();
        Set<Property> defaultValuePredicates = new HashSet<>();
        shapesModel.listSubjectsWithProperty(SH.defaultValue).forEachRemaining(ps -> {
            Resource path = ps.getPropertyResourceValue(SH.path);
            if (path != null && path.isURIResource() && !ps.hasProperty(SH.deactivated, JenaDatatypes.TRUE)) {
                defaultValuePredicates.add(JenaUtil.asProperty(path));
            }
        });
        for (Property predicate : defaultValuePredicates) {
            Map<Node, NodeExpression> map = shapesGraph.getDefaultValueNodeExpressionsMap(predicate);
            for (Node shapeNode : map.keySet()) {
                Shape shape = shapesGraph.getShape(shapeNode);
                if (shape != null) {
                    NodeExpression expr = map.get(shapeNode);
                    List<RDFNode> targetNodes = new ArrayList<>(shape.getTargetNodes(getDataset()));
                    for (RDFNode targetNode : targetNodes) {
                        if (targetNode.isResource() && !targetNode.asResource().hasProperty(predicate)) {
                            ExtendedIterator<RDFNode> it = expr.eval(targetNode, this);
                            if (it.hasNext()) {
                                List<RDFNode> list = it.toList();
                                for (RDFNode value : list) {
                                    inferences.add(targetNode.asResource(), predicate, value);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Executes the rules attached to a given list of shapes, either for a dedicated
     * focus node or all target nodes of the shapes.
     *
     * @param ruleShapes the shapes to execute
     * @param focusNode  the (optional) focus node or null for all target nodes
     * @throws InterruptedException if the monitor has canceled this
     */
    public void executeShapes(List<Shape> ruleShapes, RDFNode focusNode) throws InterruptedException {

        if (ruleShapes.isEmpty()) {
            return;
        }

        Collections.sort(ruleShapes, new Comparator<Shape>() {
            @Override
            public int compare(Shape shape1, Shape shape2) {
                return shape1.getOrder().compareTo(shape2.getOrder());
            }
        });

        String baseMessage = null;
        if (monitor != null) {
            int rules = 0;
            for (Shape shape : ruleShapes) {
                rules += getShapeRules(shape).size();
            }
            baseMessage = "Executing " + rules + " SHACL rules from " + ruleShapes.size() + " shapes";
            monitor.beginTask(baseMessage, rules);
        }

        Double oldOrder = ruleShapes.get(0).getOrder();
        for (Shape shape : ruleShapes) {
            if (!oldOrder.equals(shape.getOrder())) {
                oldOrder = shape.getOrder();
                flushPending();
            }
            executeShape(shape, baseMessage, focusNode);
        }
        flushPending();
    }


    public void executeShape(Shape shape, String baseMessage, RDFNode focusNode) throws InterruptedException {

        if (shape.isDeactivated()) {
            return;
        }

        List<Rule> rules = getShapeRules(shape);
        if (rules.isEmpty()) {
            return;
        }

        List<RDFNode> targetNodes;
        if (focusNode != null) {
            targetNodes = Collections.singletonList(focusNode);
        } else {
            targetNodes = new ArrayList<>(shape.getTargetNodes(dataset));
        }

        if (!targetNodes.isEmpty()) {
            Number oldOrder = rules.get(0).getOrder();
            for (Rule rule : rules) {
                if (monitor != null) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                    monitor.setTaskName(baseMessage + " (at " + RDFLabels.get().getLabel(shape.getShapeResource()) + " with " + targetNodes.size() + " target nodes)");
                    monitor.subTask(rule.toString().replace("\n", " "));
                }
                if (!oldOrder.equals(rule.getOrder())) {
                    oldOrder = rule.getOrder();
                    // If new rdf:type triples have been inferred, recompute the target nodes (this is brute-force for now)
                    boolean recomputeTarget = focusNode == null && pending.stream().anyMatch(triple -> RDF.type.asNode().equals(triple.getPredicate()));
                    flushPending();
                    if (recomputeTarget) {
                        targetNodes = new ArrayList<>(shape.getTargetNodes(dataset));
                    }
                }
                List<Resource> conditions = rule2Conditions.get(rule);
                if (conditions != null && !conditions.isEmpty()) {
                    List<RDFNode> filtered = new LinkedList<>();
                    for (RDFNode targetNode : targetNodes) {
                        if (nodeConformsToAllShapes(targetNode, conditions)) {
                            filtered.add(targetNode);
                        }
                    }
                    executeRule(rule, filtered, shape);
                } else {
                    executeRule(rule, targetNodes, shape);
                }
                if (monitor != null) {
                    monitor.worked(1);
                }
            }
        }
    }


    private void executeRule(Rule rule, List<RDFNode> focusNodes, Shape shape) {
        JenaUtil.setGraphReadOptimization(true);
        try {
            if (ExecStatisticsManager.get().isRecording()) {
                long startTime = System.currentTimeMillis();
                rule.execute(this, focusNodes, shape);
                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime);
                String queryText = rule.toString();
                ExecStatisticsManager.get().add(Collections.singletonList(
                        new ExecStatistics(queryText, queryText, duration, startTime, rule.getContextNode())));
            } else {
                rule.execute(this, focusNodes, shape);
            }
        } finally {
            JenaUtil.setGraphReadOptimization(false);
        }
    }


    private void flushPending() {
        for (Triple triple : pending) {
            inferences.add(inferences.asStatement(triple));
        }
        pending.clear();
    }


    private List<Rule> getShapeRules(Shape shape) {
        return shape2Rules.computeIfAbsent(shape, s2 -> {
            List<Rule> rules = new LinkedList<>();
            List<Resource> raws = new LinkedList<>();
            for (Statement s : shape.getShapeResource().listProperties(SH.rule).toList()) {
                if (s.getObject().isResource() && !s.getResource().hasProperty(SH.deactivated, JenaDatatypes.TRUE)) {
                    raws.add(s.getResource());
                }
            }
            Collections.sort(raws, OrderComparator.get());
            for (Resource raw : raws) {
                RuleLanguage ruleLanguage = RuleLanguages.get().getRuleLanguage(raw);
                if (ruleLanguage == null) {
                    throw new IllegalArgumentException("Unsupported SHACL rule type for " + raw);
                }
                Rule rule = ruleLanguage.createRule(raw);
                rules.add(rule);
                List<Resource> conditions = JenaUtil.getResourceProperties(raw, SH.condition);
                rule2Conditions.put(rule, conditions);
            }
            if (!excludeValues) {
                for (Resource ps : JenaUtil.getResourceProperties(shape.getShapeResource(), SH.property)) {
                    if (!ps.hasProperty(SH.deactivated, JenaDatatypes.TRUE) && (!excludeNeverMaterialize || !ps.hasProperty(DASH.neverMaterialize, JenaDatatypes.TRUE))) {
                        Resource path = ps.getPropertyResourceValue(SH.path);
                        if (path != null && path.isURIResource()) {
                            for (Statement s : ps.listProperties(SH.values).toList()) {
                                NodeExpression expr = NodeExpressionFactory.get().create(s.getObject());
                                rules.add(new ValuesRule(expr, path.asNode(), false));
                            }
                        }
                    }
                }
            }
            return rules;
        });
    }


    public Model getInferencesModel() {
        return inferences;
    }


    @Override
    public Model getShapesModel() {
        return dataset.getNamedModel(shapesGraphURI.toString());
    }


    public void infer(Triple triple, Rule rule, Shape shape) {
        pending.add(triple);
    }


    private boolean nodeConformsToAllShapes(RDFNode focusNode, Iterable<Resource> shapes) {
        for (Resource shape : shapes) {
            ValidationEngine engine = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null);
            if (!engine.nodesConformToShape(Collections.singletonList(focusNode), shape.asNode())) {
                return false;
            }
        }
        return true;
    }


    /**
     * If set to true then all sh:values rules in property shapes marked with dash:neverMaterialize will be skipped.
     *
     * @param value the new flag (defaults to false)
     */
    public void setExcludeNeverMaterialize(boolean value) {
        this.excludeNeverMaterialize = value;
    }


    /**
     * If set to true then all sh:values rules will be skipped.
     *
     * @param value the new flag (defaults to false)
     */
    public void setExcludeValues(boolean value) {
        this.excludeValues = value;
    }


    @Override
    public void setProgressMonitor(ProgressMonitor value) {
        this.monitor = value;
    }
}
