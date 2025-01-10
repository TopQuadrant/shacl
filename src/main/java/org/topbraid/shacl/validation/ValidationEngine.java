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
package org.topbraid.shacl.validation;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.path.P_Inverse;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.eval.PathEval;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.vocabulary.RDF;
import org.topbraid.jenax.util.ExceptionUtil;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.jenax.util.RDFLabels;
import org.topbraid.shacl.arq.SHACLPaths;
import org.topbraid.shacl.engine.*;
import org.topbraid.shacl.engine.filters.ExcludeMetaShapesFilter;
import org.topbraid.shacl.expr.NodeExpression;
import org.topbraid.shacl.expr.NodeExpressionFactory;
import org.topbraid.shacl.model.SHNodeShape;
import org.topbraid.shacl.model.SHPropertyShape;
import org.topbraid.shacl.targets.InstancesTarget;
import org.topbraid.shacl.targets.Target;
import org.topbraid.shacl.util.FailureLog;
import org.topbraid.shacl.util.SHACLPreferences;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.DASH;
import org.topbraid.shacl.vocabulary.SH;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A ValidationEngine uses a given shapes graph (represented via an instance of ShapesGraph)
 * and performs SHACL validation on a given Dataset.
 * <p>
 * Instances of this class should be created via the ValidatorFactory.
 *
 * @author Holger Knublauch
 */
public class ValidationEngine extends AbstractEngine {

    // The currently active ValidationEngine for cases where no direct pointer can be acquired, e.g. from HasShapeFunction
    private static ThreadLocal<ValidationEngine> current = new ThreadLocal<>();

    public static ValidationEngine getCurrent() {
        return current.get();
    }

    public static void setCurrent(ValidationEngine value) {
        current.set(value);
    }


    // Avoids repeatedly walking up/down the class hierarchy for sh:class constraints
    private ClassesCache classesCache;

    private ValidationEngineConfiguration configuration;

    // Can be used to drop certain focus nodes from validation
    private Predicate<RDFNode> focusNodeFilter;

    // The inferred triples if the shapes graph declares an entailment regime
    private Model inferencesModel;

    // The label function for rendering nodes in validation results (message templates etc)
    private Function<RDFNode, String> labelFunction = (node -> RDFLabels.get().getNodeLabel(node));

    // Avoids repeatedly fetching labels
    private Map<RDFNode, String> labelsCache = new ConcurrentHashMap<>();

    // Can be used to collect statistical data about execution time of constraint components and shapes
    private ValidationProfile profile;

    // The resulting validation report instance
    private Resource report;

    // Number of created results, e.g. for progress monitor
    private int resultsCount = 0;

    // Avoids repeatedly fetching the value nodes of a focus node / path combination
    private Map<ValueNodesCacheKey, Collection<RDFNode>> valueNodes = new WeakHashMap<>();

    // Number of created violations, e.g. for progress monitor
    private int violationsCount = 0;


    /**
     * Constructs a new ValidationEngine.
     *
     * @param dataset        the Dataset to operate on
     * @param shapesGraphURI the URI of the shapes graph (must be in the dataset)
     * @param shapesGraph    the ShapesGraph with the shapes to validate against
     * @param report         the sh:ValidationReport object in the results Model, or null to create a new one
     */
    protected ValidationEngine(Dataset dataset, URI shapesGraphURI, ShapesGraph shapesGraph, Resource report) {
        super(dataset, shapesGraph, shapesGraphURI);
        setConfiguration(new ValidationEngineConfiguration());
        if (report == null) {
            Model reportModel = JenaUtil.createMemoryModel();
            reportModel.setNsPrefixes(dataset.getDefaultModel()); // This can be very expensive in some databases
            reportModel.withDefaultMappings(shapesGraph.getShapesModel());
            this.report = reportModel.createResource(SH.ValidationReport);
        } else {
            this.report = report;
        }
    }


    /**
     * Checks if entailments are active for the current shapes graph and applies them for a given focus node.
     * This will only work for the sh:Rules entailment, e.g. to compute sh:values and sh:defaultValue.
     * If any inferred triples exist, the focus node will be returned attached to the model that includes those inferences.
     * The dataset used internally will also be switched to use that new model as its default model, so that if
     * a node gets validated it will "see" the inferred triples too.
     *
     * @param focusNode the focus node
     * @return the focus node, possibly in a different Model than originally
     */
    public RDFNode applyEntailments(Resource focusNode) {
        Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
        if (shapesModel.contains(null, SH.entailment, SH.Rules)) {

            // Create union of data model and inferences if called for the first time
            if (inferencesModel == null) {
                inferencesModel = JenaUtil.createDefaultModel();
                Model dataModel = dataset.getDefaultModel();
                MultiUnion multiUnion = new MultiUnion(new Graph[]{dataModel.getGraph(), inferencesModel.getGraph()});
                multiUnion.setBaseGraph(dataModel.getGraph());
                dataset.setDefaultModel(ModelFactory.createModelForGraph(multiUnion));
            }

            // Apply sh:values rules
            Map<Property, RDFNode> defaultValueMap = new HashMap<>();
            for (SHNodeShape nodeShape : SHACLUtil.getAllShapesAtNode(focusNode)) {
                if (!nodeShape.hasProperty(SH.deactivated, JenaDatatypes.TRUE)) {
                    for (SHPropertyShape ps : nodeShape.getPropertyShapes()) {
                        if (!ps.hasProperty(SH.deactivated, JenaDatatypes.TRUE)) {
                            Resource path = ps.getPath();
                            Statement values = ps.getProperty(SH.values);
                            if (values != null) {
                                NodeExpression ne = NodeExpressionFactory.get().create(values.getObject());
                                ne.eval(focusNode, this).forEachRemaining(v -> inferencesModel.getGraph().add(Triple.create(focusNode.asNode(), path.asNode(), v.asNode())));
                            }
                            Statement defaultValue = ps.getProperty(SH.defaultValue);
                            if (defaultValue != null) {
                                defaultValueMap.put(JenaUtil.asProperty(path), defaultValue.getObject());
                            }
                        }
                    }
                }
            }

            // Add sh:defaultValue where needed
            Model dataModel = dataset.getDefaultModel(); // This is now the union model
            Resource newFocusNode = focusNode.inModel(dataModel);
            for (Property predicate : defaultValueMap.keySet()) {
                if (!newFocusNode.hasProperty(predicate)) {
                    NodeExpression ne = NodeExpressionFactory.get().create(defaultValueMap.get(predicate));
                    ne.eval(focusNode, this).forEachRemaining(v -> inferencesModel.add(focusNode, predicate, v));
                }
            }
            return newFocusNode;
        }
        return focusNode;
    }


    public void addResultMessage(Resource result, Literal message, QuerySolution bindings) {
        result.addProperty(SH.resultMessage, SPARQLSubstitutions.withSubstitutions(message, bindings, getLabelFunction()));
    }


    // Note: does not set sh:path
    public Resource createResult(Resource type, Constraint constraint, RDFNode focusNode) {
        Resource result = report.getModel().createResource(type);
        report.addProperty(SH.result, result);
        result.addProperty(SH.resultSeverity, constraint.getSeverity());
        result.addProperty(SH.sourceConstraintComponent, constraint.getComponent());
        result.addProperty(SH.sourceShape, constraint.getShapeResource());
        if (focusNode != null) {
            result.addProperty(SH.focusNode, focusNode);
        }

        checkMaximumNumberFailures(constraint);

        resultsCount++;

        return result;
    }


    public Resource createValidationResult(Constraint constraint, RDFNode focusNode, RDFNode value, Supplier<String> defaultMessage) {
        Resource result = createResult(SH.ValidationResult, constraint, focusNode);
        if (value != null) {
            result.addProperty(SH.value, value);
        }
        if (!constraint.getShape().isNodeShape()) {
            result.addProperty(SH.resultPath, SHACLPaths.clonePath(constraint.getShapeResource().getPath(), result.getModel()));
        }
        Collection<RDFNode> messages = constraint.getMessages();
        if (messages.size() > 0) {
            messages.stream().forEach(message -> result.addProperty(SH.resultMessage, message));
        } else if (defaultMessage != null) {
            String m = defaultMessage.get();
            if (m != null) {
                result.addProperty(SH.resultMessage, m);
            }
        }
        return result;
    }


    private void checkMaximumNumberFailures(Constraint constraint) {
        if (SH.Violation.equals(constraint.getShape().getSeverity())) {
            this.violationsCount++;
            if (configuration.getValidationErrorBatch() != -1 && violationsCount >= configuration.getValidationErrorBatch()) {
                throw new MaximumNumberViolations(violationsCount);
            }
        }
    }


    public ClassesCache getClassesCache() {
        return classesCache;
    }


    public ValidationEngineConfiguration getConfiguration() {
        return configuration;
    }


    public String getLabel(RDFNode node) {
        return labelsCache.computeIfAbsent(node, n -> getLabelFunction().apply(n));
    }


    public Function<RDFNode, String> getLabelFunction() {
        return labelFunction;
    }


    public ValidationProfile getProfile() {
        return profile;
    }


    /**
     * Gets the validation report as a Resource in the report Model.
     *
     * @return the report Resource
     */
    public Resource getReport() {
        return report;
    }


    /**
     * Gets a Set of all shapes that should be evaluated for a given resource.
     *
     * @param focusNode   the focus node to get the shapes for
     * @param dataset     the Dataset containing the resource
     * @param shapesModel the shapes Model
     * @return a Set of shape resources
     */
    private Set<Resource> getShapesForNode(RDFNode focusNode, Dataset dataset, Model shapesModel) {

        Set<Resource> shapes = new HashSet<>();

        for (Shape rootShape : shapesGraph.getRootShapes()) {
            for (Target target : rootShape.getTargets()) {
                if (!(target instanceof InstancesTarget)) {
                    if (target.contains(dataset, focusNode)) {
                        shapes.add(rootShape.getShapeResource());
                    }
                }
            }
        }

        // rdf:type / sh:targetClass
        if (focusNode instanceof Resource) {
            for (Resource type : JenaUtil.getAllTypes((Resource) focusNode)) {
                if (JenaUtil.hasIndirectType(type.inModel(shapesModel), SH.Shape)) {
                    shapes.add(type);
                }
                for (Statement s : shapesModel.listStatements(null, SH.targetClass, type).toList()) {
                    shapes.add(s.getSubject());
                }
            }
        }

        return shapes;
    }


    public ValidationReport getValidationReport() {
        return new ResourceValidationReport(report);
    }


    public Collection<RDFNode> getValueNodes(Constraint constraint, RDFNode focusNode) {
        if (constraint.getShape().isNodeShape()) {
            return Collections.singletonList(focusNode);
        } else {
            // We use a cache here because many shapes contains for example both sh:datatype and sh:minCount, and fetching
            // the value nodes each time may be expensive, esp for sh:minCount/maxCount constraints.
            ValueNodesCacheKey key = new ValueNodesCacheKey(focusNode, constraint.getShape().getPath());
            return valueNodes.computeIfAbsent(key, k -> getValueNodesHelper(focusNode, constraint));
        }
    }


    private Collection<RDFNode> getValueNodesHelper(RDFNode focusNode, Constraint constraint) {
        Property predicate = constraint.getShape().getPredicate();
        if (predicate != null) {
            List<RDFNode> results = new LinkedList<>();
            if (focusNode instanceof Resource) {
                Iterator<Statement> it = ((Resource) focusNode).listProperties(predicate);
                while (it.hasNext()) {
                    results.add(it.next().getObject());
                }
            }
            return results;
        } else {
            Path jenaPath = constraint.getShape().getJenaPath();
            if (jenaPath instanceof P_Inverse && ((P_Inverse) jenaPath).getSubPath() instanceof P_Link) {
                List<RDFNode> results = new LinkedList<>();
                Property inversePredicate = ResourceFactory.createProperty(((P_Link) ((P_Inverse) jenaPath).getSubPath()).getNode().getURI());
                Iterator<Statement> it = focusNode.getModel().listStatements(null, inversePredicate, focusNode);
                while (it.hasNext()) {
                    results.add(it.next().getSubject());
                }
                return results;
            }
            Set<RDFNode> results = new HashSet<>();
            Iterator<Node> it = PathEval.eval(focusNode.getModel().getGraph(), focusNode.asNode(), jenaPath, Context.emptyContext());
            while (it.hasNext()) {
                Node node = it.next();
                results.add(focusNode.getModel().asRDFNode(node));
            }
            return results;
        }
    }


    /**
     * Validates a given list of focus nodes against a given Shape, and stops as soon
     * as one validation result is reported.  No results are recorded.
     *
     * @param focusNodes the nodes to validate
     * @param shape      the sh:Shape to validate against
     * @return true if there were no validation results, false for violations
     */
    public boolean nodesConformToShape(List<RDFNode> focusNodes, Node shape) {
        if (!shapesGraph.isIgnored(shape)) {
            Resource oldReport = report;
            report = JenaUtil.createMemoryModel().createResource();
            try {
                Shape vs = shapesGraph.getShape(shape);
                if (!vs.isDeactivated()) {
                    boolean nested = SHACLScriptEngineManager.get().begin();
                    try {
                        for (Constraint constraint : vs.getConstraints()) {
                            validateNodesAgainstConstraint(focusNodes, constraint);
                            if (report.hasProperty(SH.result)) {
                                return false;
                            }
                        }
                    } finally {
                        SHACLScriptEngineManager.get().end(nested);
                    }
                }
            } finally {
                this.report = oldReport;
            }
        }
        return true;
    }


    public void setClassesCache(ClassesCache value) {
        this.classesCache = value;
    }


    /**
     * Sets a filter that can be used to skip certain focus node from validation.
     * The filter must return true if the given candidate focus node shall be validated,
     * and false to skip it.
     *
     * @param value the new filter
     */
    public void setFocusNodeFilter(Predicate<RDFNode> value) {
        this.focusNodeFilter = value;
    }


    public void setLabelFunction(Function<RDFNode, String> value) {
        this.labelFunction = value;
    }


    public void updateConforms() {
        boolean conforms = true;
        StmtIterator it = report.listProperties(SH.result);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getResource().hasProperty(RDF.type, SH.ValidationResult)) {
                conforms = false;
                it.close();
                break;
            }
        }
        if (report.hasProperty(SH.conforms)) {
            report.removeAll(SH.conforms);
        }
        report.addProperty(SH.conforms, conforms ? JenaDatatypes.TRUE : JenaDatatypes.FALSE);
    }


    /**
     * Validates all target nodes against all of their shapes.
     * To further narrow down which nodes to validate, use {@link #setFocusNodeFilter(Predicate)}.
     *
     * @return an instance of sh:ValidationReport in the results Model
     * @throws InterruptedException if the monitor has canceled this
     */
    public Resource validateAll() throws InterruptedException {
        List<Shape> rootShapes = shapesGraph.getRootShapes();
        return validateShapes(rootShapes);
    }


    /**
     * Validates a given focus node against all of the shapes that have matching targets.
     *
     * @param focusNode the node to validate
     * @return an instance of sh:ValidationReport in the results Model
     * @throws InterruptedException if the monitor has canceled this
     */
    public Resource validateNode(Node focusNode) throws InterruptedException {

        Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());

        RDFNode focusRDFNode = dataset.getDefaultModel().asRDFNode(focusNode);
        Set<Resource> shapes = getShapesForNode(focusRDFNode, dataset, shapesModel);
        boolean nested = SHACLScriptEngineManager.get().begin();
        try {
            for (Resource shape : shapes) {
                if (monitor != null && monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                validateNodesAgainstShape(Collections.singletonList(focusRDFNode), shape.asNode());
            }
        } finally {
            SHACLScriptEngineManager.get().end(nested);
        }

        return report;
    }


    /**
     * Validates a given list of focus node against a given Shape.
     *
     * @param focusNodes the nodes to validate
     * @param shape      the sh:Shape to validate against
     * @return an instance of sh:ValidationReport in the results Model
     */
    public Resource validateNodesAgainstShape(List<RDFNode> focusNodes, Node shape) {
        if (!shapesGraph.isIgnored(shape)) {
            Shape vs = shapesGraph.getShape(shape);
            if (!vs.isDeactivated()) {
                boolean nested = SHACLScriptEngineManager.get().begin();
                ValidationEngine oldEngine = current.get();
                current.set(this);
                try {
                    for (Constraint constraint : vs.getConstraints()) {
                        validateNodesAgainstConstraint(focusNodes, constraint);
                    }
                } finally {
                    current.set(oldEngine);
                    SHACLScriptEngineManager.get().end(nested);
                }
            }
        }
        return report;
    }


    /**
     * Validates all target nodes of a given collection of shapes against these shapes.
     * To further narrow down which nodes to validate, use {@link #setFocusNodeFilter(Predicate)}.
     *
     * @return an instance of sh:ValidationReport in the results Model
     * @throws InterruptedException if the monitor has canceled this
     */
    public Resource validateShapes(Collection<Shape> shapes) throws InterruptedException {
        boolean nested = SHACLScriptEngineManager.get().begin();
        try {
            if (monitor != null) {
                monitor.beginTask("Validating " + shapes.size() + " shapes", shapes.size());
            }
            if (classesCache == null) {
                // If we are doing everything then the cache should be used, but not for validation of individual focus nodes
                classesCache = new ClassesCache();
            }
            int i = 0;
            for (Shape shape : shapes) {

                if (monitor != null) {
                    String label = "Shape " + (++i) + ": " + getLabelFunction().apply(shape.getShapeResource());
                    if (resultsCount > 0) {
                        label = "" + resultsCount + " results. " + label;
                    }
                    monitor.subTask(label);
                }

                Collection<RDFNode> focusNodes = shape.getTargetNodes(dataset);
                if (focusNodeFilter != null) {
                    List<RDFNode> filteredFocusNodes = new LinkedList<>();
                    for (RDFNode focusNode : focusNodes) {
                        if (focusNodeFilter.test(focusNode)) {
                            filteredFocusNodes.add(focusNode);
                        }
                    }
                    focusNodes = filteredFocusNodes;
                }
                if (!focusNodes.isEmpty()) {
                    for (Constraint constraint : shape.getConstraints()) {
                        validateNodesAgainstConstraint(focusNodes, constraint);
                    }
                }
                if (monitor != null) {
                    monitor.worked(1);
                    if (monitor.isCanceled()) {
                        throw new InterruptedException();
                    }
                }
            }
        } catch (MaximumNumberViolations ex) {
            // Ignore as this is just our way to stop validation when max number of violations is reached
        } finally {
            SHACLScriptEngineManager.get().end(nested);
        }
        updateConforms();
        return report;
    }


    protected void validateNodesAgainstConstraint(Collection<RDFNode> focusNodes, Constraint constraint) {
        if (configuration != null && configuration.isSkippedConstraintComponent(constraint.getComponent())) {
            return;
        }

        ConstraintExecutor executor;
        try {
            executor = constraint.getExecutor();
        } catch (Exception ex) {
            Resource result = createResult(DASH.FailureResult, constraint, constraint.getShapeResource());
            result.addProperty(SH.resultMessage, "Failed to create validator: " + ExceptionUtil.getDeepMessage(ex));
            return;
        }
        if (executor != null) {
            if (SHACLPreferences.isProduceFailuresMode()) {
                try {
                    executor.executeConstraint(constraint, this, focusNodes);
                } catch (Exception ex) {
                    Resource result = createResult(DASH.FailureResult, constraint, constraint.getShapeResource());
                    result.addProperty(SH.resultMessage, "Exception during validation: " + ExceptionUtil.getDeepMessage(ex));
                }
            } else {
                executor.executeConstraint(constraint, this, focusNodes);
            }
        } else {
            FailureLog.get().logWarning("No suitable validator found for constraint " + constraint);
        }
    }


    public void setConfiguration(ValidationEngineConfiguration configuration) {
        this.configuration = configuration;
        if (!configuration.getValidateShapes()) {
            shapesGraph.setShapeFilter(new ExcludeMetaShapesFilter());
        }
    }


    public void setProfile(ValidationProfile profile) {
        this.profile = profile;
    }


    // Used to avoid repeated computation of value nodes for a focus node / path combination
    private static class ValueNodesCacheKey {

        Resource path;

        RDFNode focusNode;


        ValueNodesCacheKey(RDFNode focusNode, Resource path) {
            this.path = path;
            this.focusNode = focusNode;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ValueNodesCacheKey) {
                return path.equals(((ValueNodesCacheKey) o).path) && focusNode.equals(((ValueNodesCacheKey) o).focusNode);
            } else {
                return false;
            }
        }


        @Override
        public int hashCode() {
            return path.hashCode() + focusNode.hashCode();
        }


        @Override
        public String toString() {
            return focusNode.toString() + " . " + path;
        }
    }
}