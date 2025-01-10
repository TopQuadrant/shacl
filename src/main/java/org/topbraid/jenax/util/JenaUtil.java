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

package org.topbraid.jenax.util;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.GraphMemFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.StmtIteratorImpl;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.expr.nodevalue.NodeFunctions;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransform;
import org.apache.jena.sparql.syntax.syntaxtransform.ElementTransformSubst;
import org.apache.jena.sparql.syntax.syntaxtransform.ExprTransformNodeElement;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.NodeCmp;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.topbraid.jenax.progress.ProgressMonitor;

import java.util.*;
import java.util.function.BiFunction;


/**
 * Some convenience methods to operate on Jena Models.
 * <p>
 * These methods are not as stable as the rest of the API, but
 * they may be of general use.
 *
 * @author Holger Knublauch
 */
public class JenaUtil {

    // Unstable
    private static JenaUtilHelper helper = new JenaUtilHelper();

    // Leave this line under the helper line above!
    private static Model dummyModel = JenaUtil.createDefaultModel();

    public static final String WITH_IMPORTS_PREFIX = "http://rdfex.org/withImports?uri=";


    /**
     * Sets the helper which allows the behavior of some JenaUtil
     * methods to be modified by the system using the SPIN library.
     * Note: Should not be used outside of TopBraid - not stable.
     *
     * @param h the JenaUtilHelper
     * @return the old helper
     */
    public static JenaUtilHelper setHelper(JenaUtilHelper h) {
        JenaUtilHelper old = helper;
        helper = h;
        return old;
    }


    /**
     * Gets the current helper object.
     * Note: Should not be used outside of TopBraid - not stable.
     *
     * @return the helper
     */
    public static JenaUtilHelper getHelper() {
        return helper;
    }


    /**
     * Populates a result set of resources reachable from a subject via zero or more steps with a given predicate.
     * Implementation note: the results set need only implement {@link Collection#add(Object)}.
     *
     * @param results   The transitive objects reached from subject via triples with the given predicate
     * @param subject   the subject to start traversal at
     * @param predicate the predicate to walk
     */
    public static void addTransitiveObjects(Set<Resource> results, Resource subject, Property predicate) {
        helper.setGraphReadOptimization(true);
        try {
            addTransitiveObjects(results, new HashSet<Resource>(), subject, predicate);
        } finally {
            helper.setGraphReadOptimization(false);
        }
    }


    private static void addTransitiveObjects(Set<Resource> resources, Set<Resource> reached,
                                             Resource subject, Property predicate) {
        resources.add(subject);
        reached.add(subject);
        StmtIterator it = subject.listProperties(predicate);
        try {
            while (it.hasNext()) {
                RDFNode object = it.next().getObject();
                if (object instanceof Resource) {
                    if (!reached.contains(object)) {
                        addTransitiveObjects(resources, reached, (Resource) object, predicate);
                    }
                }
            }
        } finally {
            it.close();
        }
    }


    private static void addTransitiveSubjects(Set<Resource> reached, Resource object,
                                              Property predicate, ProgressMonitor monitor) {
        if (object != null) {
            reached.add(object);
            StmtIterator it = object.getModel().listStatements(null, predicate, object);
            try {
                while (it.hasNext()) {
                    if (monitor != null && monitor.isCanceled()) {
                        it.close();
                        return;
                    }
                    Resource subject = it.next().getSubject();
                    if (!reached.contains(subject)) {
                        addTransitiveSubjects(reached, subject, predicate, monitor);
                    }
                }
            } finally {
                it.close();
            }
        }
    }


    /**
     * Turns a QuerySolution into a Binding.
     *
     * @param map the input QuerySolution
     * @return a Binding or null if the input is null
     */
    public static Binding asBinding(final QuerySolution map) {
        if (map != null) {
            BindingBuilder builder = BindingBuilder.create();
            Iterator<String> varNames = map.varNames();
            while (varNames.hasNext()) {
                String varName = varNames.next();
                RDFNode node = map.get(varName);
                if (node != null) {
                    builder.add(Var.alloc(varName), node.asNode());
                }
            }
            return builder.build();
        } else {
            return null;
        }
    }


    /**
     * Turns a Binding into a QuerySolutionMap.
     *
     * @param binding the Binding to convert
     * @return a QuerySolutionMap
     */
    public static QuerySolutionMap asQuerySolutionMap(Binding binding) {
        QuerySolutionMap map = new QuerySolutionMap();
        Iterator<Var> vars = binding.vars();
        while (vars.hasNext()) {
            Var var = vars.next();
            Node node = binding.get(var);
            if (node != null) {
                map.add(var.getName(), dummyModel.asRDFNode(node));
            }
        }
        return map;
    }


    /**
     * Returns a set of resources reachable from an object via one or more reversed steps with a given predicate.
     *
     * @param object    the object to start traversal at
     * @param predicate the predicate to walk
     * @param monitor   an optional progress monitor to allow cancelation
     * @return the reached resources
     */
    public static Set<Resource> getAllTransitiveSubjects(Resource object, Property predicate, ProgressMonitor monitor) {
        Set<Resource> set = new HashSet<>();
        helper.setGraphReadOptimization(true);
        try {
            addTransitiveSubjects(set, object, predicate, monitor);
        } finally {
            helper.setGraphReadOptimization(false);
        }
        set.remove(object);
        return set;
    }


    /**
     * Casts a Resource into a Property.
     *
     * @param resource the Resource to cast
     * @return resource as an instance of Property
     */
    public static Property asProperty(Resource resource) {
        if (resource instanceof Property) {
            return (Property) resource;
        } else {
            return new PropertyImpl(resource.asNode(), (EnhGraph) resource.getModel());
        }
    }


    public static void collectBaseGraphs(Graph graph, Set<Graph> baseGraphs) {
        if (graph instanceof MultiUnion) {
            MultiUnion union = (MultiUnion) graph;
            collectBaseGraphs(union.getBaseGraph(), baseGraphs);
            for (Object subGraph : union.getSubGraphs()) {
                collectBaseGraphs((Graph) subGraph, baseGraphs);
            }
        } else if (graph != null) {
            baseGraphs.add(graph);
        }
    }


    /**
     * Creates a new Graph. By default, this will deliver a plain in-memory graph,
     * but other implementations may deliver graphs with concurrency support and
     * other features.
     *
     * @return a default graph
     * @see #createDefaultModel()
     */
    public static Graph createDefaultGraph() {
        return helper.createDefaultGraph();
    }


    /**
     * Wraps the result of {@link #createDefaultGraph()} into a Model and initializes namespaces.
     *
     * @return a default Model
     * @see #createDefaultGraph()
     */
    public static Model createDefaultModel() {
        Model m = ModelFactory.createModelForGraph(createDefaultGraph());
        initNamespaces(m);
        return m;
    }


    /**
     * Creates a memory Graph with no reification.
     *
     * @return a new memory graph
     */
    public static Graph createMemoryGraph() {
        return GraphMemFactory.createDefaultGraph();
    }


    /**
     * Creates a memory Model with no reification.
     *
     * @return a new memory Model
     */
    public static Model createMemoryModel() {
        return ModelFactory.createModelForGraph(createMemoryGraph());
    }


    public static MultiUnion createMultiUnion() {
        return helper.createMultiUnion();
    }


    public static MultiUnion createMultiUnion(Graph[] graphs) {
        return helper.createMultiUnion(graphs);
    }


    public static MultiUnion createMultiUnion(Iterator<Graph> graphs) {
        return helper.createMultiUnion(graphs);
    }


    /**
     * Gets all instances of a given class and its subclasses.
     *
     * @param cls the class to get the instances of
     * @return the instances
     */
    public static Set<Resource> getAllInstances(Resource cls) {
        JenaUtil.setGraphReadOptimization(true);
        try {
            Model model = cls.getModel();
            Set<Resource> classes = getAllSubClasses(cls);
            classes.add(cls);
            Set<Resource> results = new HashSet<>();
            for (Resource subClass : classes) {
                StmtIterator it = model.listStatements(null, RDF.type, subClass);
                while (it.hasNext()) {
                    results.add(it.next().getSubject());
                }
            }
            return results;
        } finally {
            JenaUtil.setGraphReadOptimization(false);
        }
    }


    public static Set<Resource> getAllSubClasses(Resource cls) {
        return getAllTransitiveSubjects(cls, RDFS.subClassOf);
    }


    /**
     * Returns a set consisting of a given class and all its subclasses.
     * Similar to rdfs:subClassOf*.
     *
     * @param cls the class to return with its subclasses
     * @return the Set of class resources
     */
    public static Set<Resource> getAllSubClassesStar(Resource cls) {
        Set<Resource> results = getAllTransitiveSubjects(cls, RDFS.subClassOf);
        results.add(cls);
        return results;
    }


    public static Set<Resource> getAllSubProperties(Property superProperty) {
        return getAllTransitiveSubjects(superProperty, RDFS.subPropertyOf);
    }


    public static Set<Resource> getAllSuperClasses(Resource cls) {
        return getAllTransitiveObjects(cls, RDFS.subClassOf);
    }


    /**
     * Returns a set consisting of a given class and all its superclasses.
     * Similar to rdfs:subClassOf*.
     *
     * @param cls the class to return with its superclasses
     * @return the Set of class resources
     */
    public static Set<Resource> getAllSuperClassesStar(Resource cls) {
        Set<Resource> results = getAllTransitiveObjects(cls, RDFS.subClassOf);
        results.add(cls);
        return results;
    }


    public static Set<Resource> getAllSuperProperties(Property subProperty) {
        return getAllTransitiveObjects(subProperty, RDFS.subPropertyOf);
    }


    /**
     * Returns a set of resources reachable from a subject via one or more steps with a given predicate.
     *
     * @param subject   the subject to start at
     * @param predicate the predicate to traverse
     * @return the reached resources
     */
    public static Set<Resource> getAllTransitiveObjects(Resource subject, Property predicate) {
        Set<Resource> set = new HashSet<>();
        addTransitiveObjects(set, subject, predicate);
        set.remove(subject);
        return set;
    }


    private static Set<Resource> getAllTransitiveSubjects(Resource object, Property predicate) {
        return getAllTransitiveSubjects(object, predicate, null);
    }


    public static Set<Resource> getAllTypes(Resource instance) {
        Set<Resource> types = new HashSet<>();
        StmtIterator it = instance.listProperties(RDF.type);
        try {
            while (it.hasNext()) {
                Resource type = it.next().getResource();
                types.add(type);
                types.addAll(getAllSuperClasses(type));
            }
        } finally {
            it.close();
        }
        return types;
    }


    /**
     * Gets the "base graph" of a Model, walking into MultiUnions if needed.
     *
     * @param model the Model to get the base graph of
     * @return the base graph or null if the model contains a MultiUnion that doesn't declare one
     */
    public static Graph getBaseGraph(final Model model) {
        return getBaseGraph(model.getGraph());
    }


    public static Graph getBaseGraph(Graph graph) {
        Graph baseGraph = graph;
        while (baseGraph instanceof MultiUnion) {
            baseGraph = ((MultiUnion) baseGraph).getBaseGraph();
        }
        return baseGraph;
    }


    public static Model getBaseModel(Model model) {
        Graph baseGraph = getBaseGraph(model);
        if (baseGraph == model.getGraph()) {
            return model;
        } else {
            return ModelFactory.createModelForGraph(baseGraph);
        }
    }


    /**
     * For a given subject resource and a given collection of (label/comment) properties this finds the most
     * suitable value of either property for a given list of languages (usually from the current user's preferences).
     * For example, if the user's languages are [ "en-AU" ] then the function will prefer "mate"@en-AU over
     * "friend"@en and never return "freund"@de.  The function falls back to literals that have no language
     * if no better literal has been found.
     *
     * @param resource   the subject resource
     * @param langs      the allowed languages
     * @param properties the properties to check
     * @return the best suitable value or null
     */
    public static Literal getBestStringLiteral(Resource resource, List<String> langs, Iterable<Property> properties) {
        return getBestStringLiteral(resource, langs, properties, (r, p) -> r.listProperties(p));
    }


    public static Literal getBestStringLiteral(Resource resource, List<String> langs, Iterable<Property> properties, BiFunction<Resource, Property, ExtendedIterator<Statement>> getter) {
        String prefLang = langs.isEmpty() ? null : langs.get(0);
        Literal label = null;
        int bestLang = -1;
        for (Property predicate : properties) {
            ExtendedIterator<Statement> it = getter.apply(resource, predicate);
            while (it.hasNext()) {
                RDFNode object = it.next().getObject();
                if (object.isLiteral()) {
                    Literal literal = (Literal) object;
                    String lang = literal.getLanguage();
                    if (lang.length() == 0 && label == null) {
                        label = literal;
                    } else if (prefLang != null && prefLang.equalsIgnoreCase(lang)) {
                        it.close();
                        return literal;
                    } else {
                        // 1) Never use a less suitable language
                        // 2) Never replace an already existing label (esp: skos:prefLabel) unless new lang is better
                        // 3) Fall back to more special languages if no other was found (e.g. use en-GB if only "en" is accepted)
                        int startLang = bestLang < 0 ? langs.size() - 1 : (label != null ? bestLang - 1 : bestLang);
                        for (int i = startLang; i > 0; i--) {
                            String langi = langs.get(i);
                            if (langi.equalsIgnoreCase(lang)) {
                                label = literal;
                                bestLang = i;
                            } else if (label == null && lang.contains("-") && NodeFunctions.langMatches(lang, langi)) {
                                label = literal;
                            }
                        }
                    }
                }
            }
        }
        return label;
    }


    /**
     * Gets the "first" declared rdfs:range of a given property.
     * If multiple ranges exist, the behavior is undefined.
     * Note that this method does not consider ranges defined on
     * super-properties.
     *
     * @param property the property to get the range of
     * @return the "first" range Resource or null
     */
    public static Resource getFirstDirectRange(Resource property) {
        return property.getPropertyResourceValue(RDFS.range);
    }


    private static Resource getFirstRange(Resource property, Set<Resource> reached) {
        Resource directRange = getFirstDirectRange(property);
        if (directRange != null) {
            return directRange;
        }
        StmtIterator it = property.listProperties(RDFS.subPropertyOf);
        while (it.hasNext()) {
            Statement ss = it.next();
            if (ss.getObject().isURIResource()) {
                Resource superProperty = ss.getResource();
                if (!reached.contains(superProperty)) {
                    reached.add(superProperty);
                    Resource r = getFirstRange(superProperty, reached);
                    if (r != null) {
                        it.close();
                        return r;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Gets the "first" declared rdfs:range of a given property.
     * If multiple ranges exist, the behavior is undefined.
     * This method walks up to super-properties if no direct match exists.
     *
     * @param property the property to get the range of
     * @return the "first" range Resource or null
     */
    public static Resource getFirstRange(Resource property) {
        return getFirstRange(property, new HashSet<>());
    }


    public static Set<Resource> getImports(Resource graph) {
        Set<Resource> results = new HashSet<>();
        for (Property importProperty : ImportProperties.get().getImportProperties()) {
            results.addAll(JenaUtil.getResourceProperties(graph, importProperty));
        }
        return results;
    }


    public static Integer getIntegerProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().isLiteral()) {
            return s.getInt();
        } else {
            return null;
        }
    }


    public static RDFList getListProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().canAs(RDFList.class)) {
            return s.getResource().as(RDFList.class);
        } else {
            return null;
        }
    }


    public static List<Literal> getLiteralProperties(Resource subject, Property predicate) {
        List<Literal> results = new LinkedList<>();
        StmtIterator it = subject.listProperties(predicate);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isLiteral()) {
                results.add(s.getLiteral());
            }
        }
        return results;
    }


    /**
     * Walks up the class hierarchy starting at a given class until one of them
     * returns a value for a given Function.
     *
     * @param cls      the class to start at
     * @param function the Function to execute on each class
     * @param <T>      the requested result type
     * @return the "first" non-null value, or null
     */
    public static <T> T getNearest(Resource cls, java.util.function.Function<Resource, T> function) {
        T result = function.apply(cls);
        if (result != null) {
            return result;
        }
        return getNearest(cls, function, new HashSet<>());
    }


    private static <T> T getNearest(Resource cls, java.util.function.Function<Resource, T> function, Set<Resource> reached) {
        reached.add(cls);
        StmtIterator it = cls.listProperties(RDFS.subClassOf);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isResource() && !reached.contains(s.getResource())) {
                T result = function.apply(s.getResource());
                if (result == null) {
                    result = getNearest(s.getResource(), function, reached);
                }
                if (result != null) {
                    it.close();
                    return result;
                }
            }
        }
        return null;
    }


    /**
     * Overcomes a design mismatch with Jena: if the base model does not declare a default namespace then the
     * default namespace of an import is returned - this is not desirable for TopBraid-like scenarios.
     *
     * @param model  the Model to operate on
     * @param prefix the prefix to get the URI of
     * @return the URI of prefix
     */
    public static String getNsPrefixURI(Model model, String prefix) {
        if ("".equals(prefix) && model.getGraph() instanceof MultiUnion) {
            Graph baseGraph = ((MultiUnion) model.getGraph()).getBaseGraph();
            if (baseGraph != null) {
                return baseGraph.getPrefixMapping().getNsPrefixURI(prefix);
            } else {
                return model.getNsPrefixURI(prefix);
            }
        } else {
            return model.getNsPrefixURI(prefix);
        }
    }


    public static RDFNode getProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null) {
            return s.getObject();
        } else {
            return null;
        }
    }


    public static Resource getResourcePropertyWithType(Resource subject, Property predicate, Resource type) {
        StmtIterator it = subject.listProperties(predicate);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isResource() && JenaUtil.hasIndirectType(s.getResource(), type)) {
                it.close();
                return s.getResource();
            }
        }
        return null;
    }


    public static List<Resource> getResourceProperties(Resource subject, Property predicate) {
        List<Resource> results = new LinkedList<>();
        StmtIterator it = subject.listProperties(predicate);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isResource()) {
                results.add(s.getResource());
            }
        }
        return results;
    }


    public static Resource getURIResourceProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().isURIResource()) {
            return s.getResource();
        } else {
            return null;
        }
    }


    public static List<Resource> getURIResourceProperties(Resource subject, Property predicate) {
        List<Resource> results = new LinkedList<>();
        StmtIterator it = subject.listProperties(predicate);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isURIResource()) {
                results.add(s.getResource());
            }
        }
        return results;
    }


    public static String getStringProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().isLiteral()) {
            return s.getString();
        } else {
            return null;
        }
    }


    public static boolean getBooleanProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().isLiteral()) {
            return s.getBoolean();
        } else {
            return false;
        }
    }


    public static Double getDoubleProperty(Resource subject, Property predicate) {
        Statement s = subject.getProperty(predicate);
        if (s != null && s.getObject().isLiteral()) {
            return s.getDouble();
        } else {
            return null;
        }
    }


    public static double getDoubleProperty(Resource subject, Property predicate, double defaultValue) {
        Double d = getDoubleProperty(subject, predicate);
        if (d != null) {
            return d;
        } else {
            return defaultValue;
        }
    }


    public static List<Graph> getSubGraphs(MultiUnion union) {
        List<Graph> results = new LinkedList<>();
        Graph baseGraph = union.getBaseGraph();
        if (baseGraph != null) {
            results.add(baseGraph);
        }
        results.addAll(union.getSubGraphs());
        return results;
    }


    /**
     * Gets a Set of all superclasses (rdfs:subClassOf) of a given Resource.
     *
     * @param subClass the subClass Resource
     * @return a Collection of class resources
     */
    public static Collection<Resource> getSuperClasses(Resource subClass) {
        NodeIterator it = subClass.getModel().listObjectsOfProperty(subClass, RDFS.subClassOf);
        Set<Resource> results = new HashSet<>();
        while (it.hasNext()) {
            RDFNode node = it.nextNode();
            if (node instanceof Resource) {
                results.add((Resource) node);
            }
        }
        return results;
    }


    /**
     * Gets the "first" type of a given Resource.
     *
     * @param instance the instance to get the type of
     * @return the type or null
     */
    public static Resource getType(Resource instance) {
        return instance.getPropertyResourceValue(RDF.type);
    }


    /**
     * Gets a Set of all rdf:types of a given Resource.
     *
     * @param instance the instance Resource
     * @return a Collection of type resources
     */
    public static List<Resource> getTypes(Resource instance) {
        return JenaUtil.getResourceProperties(instance, RDF.type);
    }


    /**
     * Checks whether a given Resource is an instance of a given type, or
     * a subclass thereof.  Make sure that the expectedType parameter is associated
     * with the right Model, because the system will try to walk up the superclasses
     * of expectedType.  The expectedType may have no Model, in which case
     * the method will use the instance's Model.
     *
     * @param instance     the Resource to test
     * @param expectedType the type that instance is expected to have
     * @return true if resource has rdf:type expectedType
     */
    public static boolean hasIndirectType(Resource instance, Resource expectedType) {

        if (expectedType.getModel() == null) {
            expectedType = expectedType.inModel(instance.getModel());
        }

        StmtIterator it = instance.listProperties(RDF.type);
        while (it.hasNext()) {
            Statement s = it.next();
            if (s.getObject().isResource()) {
                Resource actualType = s.getResource();
                if (actualType.equals(expectedType) || JenaUtil.hasSuperClass(actualType, expectedType)) {
                    it.close();
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks whether a given class has a given (transitive) super class.
     *
     * @param subClass   the sub-class
     * @param superClass the super-class
     * @return true if subClass has superClass (somewhere up the tree)
     */
    public static boolean hasSuperClass(Resource subClass, Resource superClass) {
        return hasSuperClass(subClass, superClass, new HashSet<>());
    }


    private static boolean hasSuperClass(Resource subClass, Resource superClass, Set<Resource> reached) {
        StmtIterator it = subClass.listProperties(RDFS.subClassOf);
        while (it.hasNext()) {
            Statement s = it.next();
            if (superClass.equals(s.getObject())) {
                it.close();
                return true;
            } else if (!reached.contains(s.getResource())) {
                reached.add(s.getResource());
                if (hasSuperClass(s.getResource(), superClass, reached)) {
                    it.close();
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks whether a given property has a given (transitive) super property.
     *
     * @param subProperty   the sub-property
     * @param superProperty the super-property
     * @return true if subProperty has superProperty (somewhere up the tree)
     */
    public static boolean hasSuperProperty(Property subProperty, Property superProperty) {
        return getAllSuperProperties(subProperty).contains(superProperty);
    }


    /**
     * Sets the usual default namespaces for rdf, rdfs, owl and xsd.
     *
     * @param graph the Graph to modify
     */
    public static void initNamespaces(Graph graph) {
        PrefixMapping prefixMapping = graph.getPrefixMapping();
        initNamespaces(prefixMapping);
    }


    /**
     * Sets the usual default namespaces for rdf, rdfs, owl and xsd.
     *
     * @param prefixMapping the Model to modify
     */
    public static void initNamespaces(PrefixMapping prefixMapping) {
        ensurePrefix(prefixMapping, "rdf", RDF.getURI());
        ensurePrefix(prefixMapping, "rdfs", RDFS.getURI());
        ensurePrefix(prefixMapping, "owl", OWL.getURI());
        ensurePrefix(prefixMapping, "xsd", XSD.getURI());
    }

    private static void ensurePrefix(PrefixMapping prefixMapping, String prefix, String uristr) {
        // set if not present, or if different
        if (!uristr.equals(prefixMapping.getNsPrefixURI(prefix))) {
            prefixMapping.setNsPrefix(prefix, uristr);
        }
    }

    /**
     * Checks whether a given graph (possibly a MultiUnion) only contains
     * GraphMemBase instances.
     *
     * @param graph the Graph to test
     * @return true  if graph is a memory graph
     */
    public static boolean isMemoryGraph(Graph graph) {
        if (graph instanceof MultiUnion) {
            for (Graph subGraph : JenaUtil.getSubGraphs((MultiUnion) graph)) {
                if (!isMemoryGraph(subGraph)) {
                    return false;
                }
            }
            return true;
        } else {
            return helper.isMemoryGraph(graph);
        }
    }


    /**
     * Gets an Iterator over all Statements of a given property or its sub-properties
     * at a given subject instance.  Note that the predicate and subject should be
     * both attached to a Model to avoid NPEs.
     *
     * @param subject   the subject (may be null)
     * @param predicate the predicate
     * @return a StmtIterator
     */
    public static StmtIterator listAllProperties(Resource subject, Property predicate) {
        List<Statement> results = new LinkedList<>();
        helper.setGraphReadOptimization(true);
        try {
            listAllProperties(subject, predicate, new HashSet<>(), results);
        } finally {
            helper.setGraphReadOptimization(false);
        }
        return new StmtIteratorImpl(results.iterator());
    }


    private static void listAllProperties(Resource subject, Property predicate, Set<Property> reached,
                                          List<Statement> results) {
        reached.add(predicate);
        StmtIterator sit;
        Model model;
        if (subject != null) {
            sit = subject.listProperties(predicate);
            model = subject.getModel();
        } else {
            model = predicate.getModel();
            sit = model.listStatements(null, predicate, (RDFNode) null);
        }
        while (sit.hasNext()) {
            results.add(sit.next());
        }

        // Iterate into direct subproperties
        StmtIterator it = model.listStatements(null, RDFS.subPropertyOf, predicate);
        while (it.hasNext()) {
            Statement sps = it.next();
            if (!reached.contains(sps.getSubject())) {
                Property subProperty = asProperty(sps.getSubject());
                listAllProperties(subject, subProperty, reached, results);
            }
        }
    }


    /**
     * This indicates that no further changes to the model are needed.
     * Some implementations may give runtime exceptions if this is violated.
     *
     * @param m the Model to get as a read-only variant
     * @return A read-only model
     */
    public static Model asReadOnlyModel(Model m) {
        return helper.asReadOnlyModel(m);
    }


    /**
     * This indicates that no further changes to the graph are needed.
     * Some implementations may give runtime exceptions if this is violated.
     *
     * @param g the Graph to get as a read-only variant
     * @return a read-only graph
     */
    public static Graph asReadOnlyGraph(Graph g) {
        return helper.asReadOnlyGraph(g);
    }


    // Internal to TopBraid only
    public static OntModel createOntologyModel(OntModelSpec spec, Model base) {
        return helper.createOntologyModel(spec, base);
    }


    /**
     * Allows some environments, e.g. TopBraid, to prioritize
     * a block of code for reading graphs, with no update occurring.
     * The top of the block should call this with <code>true</code>
     * with a matching call with <code>false</code> in a finally
     * block.
     * <p>
     * Note: Unstable - don't use outside of TopBraid.
     *
     * @param onOrOff true to switch on
     */
    public static void setGraphReadOptimization(boolean onOrOff) {
        helper.setGraphReadOptimization(onOrOff);
    }


    /**
     * Ensure that we there is a read-only, thread safe version of the
     * graph.  If the graph is not, then create a deep clone that is
     * both.
     * <p>
     * Note: Unstable - don't use outside of TopBraid.
     *
     * @param g The given graph
     * @return A read-only, thread safe version of the given graph.
     */
    public static Graph deepCloneForReadOnlyThreadSafe(Graph g) {
        return helper.deepCloneReadOnlyGraph(g);
    }


    /**
     * Calls a SPARQL expression and returns the result, using some initial bindings.
     *
     * @param expression     the expression to execute (must contain absolute URIs)
     * @param initialBinding the initial bindings for the unbound variables
     * @param dataset        the query Dataset or null for default
     * @return the result or null
     */
    public static Node invokeExpression(String expression, QuerySolution initialBinding, Dataset dataset) {
        if (dataset == null) {
            dataset = ARQFactory.get().getDataset(ModelFactory.createDefaultModel());
        }
        Query query = ARQFactory.get().createExpressionQuery(expression);
        try (QueryExecution qexec = ARQFactory.get().createQueryExecution(query, dataset, initialBinding)) {
            ResultSet rs = qexec.execSelect();
            Node result = null;
            if (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String firstVarName = rs.getResultVars().get(0);
                RDFNode rdfNode = qs.get(firstVarName);
                if (rdfNode != null) {
                    result = rdfNode.asNode();
                }
            }
            return result;
        }
    }


    /**
     * Calls a given SPARQL function with no arguments.
     *
     * @param function the URI resource of the function to call
     * @param dataset  the Dataset to operate on or null for default
     * @return the result of the function call
     */
    public static Node invokeFunction0(Resource function, Dataset dataset) {
        ExprList args = new ExprList();
        return invokeFunction(function, args, dataset);
    }


    /**
     * Calls a given SPARQL function with one argument.
     *
     * @param function the URI resource of the function to call
     * @param argument the first argument
     * @param dataset  the Dataset to operate on or null for default
     * @return the result of the function call
     */
    public static Node invokeFunction1(Resource function, RDFNode argument, Dataset dataset) {
        ExprList args = new ExprList();
        args.add(argument != null ? NodeValue.makeNode(argument.asNode()) : new ExprVar("arg1"));
        return invokeFunction(function, args, dataset);
    }


    public static Node invokeFunction1(Resource function, Node argument, Dataset dataset) {
        return invokeFunction1(function, toRDFNode(argument), dataset);
    }


    /**
     * Calls a given SPARQL function with two arguments.
     *
     * @param function  the URI resource of the function to call
     * @param argument1 the first argument
     * @param argument2 the second argument
     * @param dataset   the Dataset to operate on or null for default
     * @return the result of the function call
     */
    public static Node invokeFunction2(Resource function, RDFNode argument1, RDFNode argument2, Dataset dataset) {
        ExprList args = new ExprList();
        args.add(argument1 != null ? NodeValue.makeNode(argument1.asNode()) : new ExprVar("arg1"));
        args.add(argument2 != null ? NodeValue.makeNode(argument2.asNode()) : new ExprVar("arg2"));
        return invokeFunction(function, args, dataset);
    }


    public static Node invokeFunction2(Resource function, Node argument1, Node argument2, Dataset dataset) {
        return invokeFunction2(function, toRDFNode(argument1), toRDFNode(argument2), dataset);
    }


    public static Node invokeFunction3(Resource function, RDFNode argument1, RDFNode argument2, RDFNode argument3, Dataset dataset) {
        ExprList args = new ExprList();
        args.add(argument1 != null ? NodeValue.makeNode(argument1.asNode()) : new ExprVar("arg1"));
        args.add(argument2 != null ? NodeValue.makeNode(argument2.asNode()) : new ExprVar("arg2"));
        args.add(argument3 != null ? NodeValue.makeNode(argument3.asNode()) : new ExprVar("arg3"));
        return invokeFunction(function, args, dataset);
    }


    private static Node invokeFunction(Resource function, ExprList args, Dataset dataset) {

        if (dataset == null) {
            dataset = ARQFactory.get().getDataset(ModelFactory.createDefaultModel());
        }

        E_Function expr = new E_Function(function.getURI(), args);
        DatasetGraph dsg = dataset.asDatasetGraph();
        Context cxt = ARQ.getContext().copy();
        cxt.set(ARQConstants.sysCurrentTime, NodeFactoryExtra.nowAsDateTime());
        FunctionEnv env = new ExecutionContext(cxt, dsg.getDefaultGraph(), dsg, null);
        try {
            NodeValue r = expr.eval(BindingRoot.create(), env);
            if (r != null) {
                return r.asNode();
            }
        } catch (ExprEvalException ex) {
        }
        return null;
    }


    public static Node invokeFunction3(Resource function, Node argument1, Node argument2, Node argument3, Dataset dataset) {
        return invokeFunction3(function, toRDFNode(argument1), toRDFNode(argument2), toRDFNode(argument3), dataset);
    }


    /**
     * Temp patch for a bug in Jena's syntaxtransform, also applying substitutions on
     * HAVING clauses.
     *
     * @param query         the Query to transform
     * @param substitutions the variable bindings
     * @return a new Query with the bindings applied
     */
    public static Query queryWithSubstitutions(Query query, final Map<Var, Node> substitutions) {
        Query result = QueryTransformOps.transform(query, substitutions);

        // TODO: Replace this hack once there is a Jena patch
        if (result.hasHaving()) {
            NodeTransform nodeTransform = new NodeTransform() {
                @Override
                public Node apply(Node node) {
                    Node n = substitutions.get(node);
                    if (n == null) {
                        return node;
                    }
                    return n;
                }
            };
            ElementTransform eltrans = new ElementTransformSubst(substitutions);
            ExprTransform exprTrans = new ExprTransformNodeElement(nodeTransform, eltrans);
            List<Expr> havingExprs = result.getHavingExprs();
            for (int i = 0; i < havingExprs.size(); i++) {
                Expr old = havingExprs.get(i);
                Expr neo = ExprTransformer.transform(exprTrans, old);
                if (neo != old) {
                    havingExprs.set(i, neo);
                }
            }
        }
        return result;
    }


    public static void sort(List<Resource> nodes) {
        Collections.sort(nodes, new Comparator<Resource>() {
            @Override
            public int compare(Resource o1, Resource o2) {
                return NodeCmp.compareRDFTerms(o1.asNode(), o2.asNode());
            }
        });
    }


    public static RDFNode toRDFNode(Node node) {
        if (node != null) {
            return dummyModel.asRDFNode(node);
        } else {
            return null;
        }
    }


    public static String withImports(String uri) {
        if (!uri.startsWith(WITH_IMPORTS_PREFIX)) {
            return WITH_IMPORTS_PREFIX + uri;
        } else {
            return uri;
        }
    }


    public static String withoutImports(String uri) {
        if (uri.startsWith(WITH_IMPORTS_PREFIX)) {
            return uri.substring(WITH_IMPORTS_PREFIX.length());
        } else {
            return uri;
        }
    }
}
