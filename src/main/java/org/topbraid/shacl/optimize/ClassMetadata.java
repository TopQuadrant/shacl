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
package org.topbraid.shacl.optimize;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaNodeUtil;
import org.topbraid.shacl.vocabulary.SH;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents some ontology-related data about a given class, so that it can be accessed
 * more efficiently than through the RDF triples.
 * <p>
 * In particular this includes information about the properties attached to the class,
 * as well as utilities to walk up the superclass hierarchy.
 *
 * @author Holger Knublauch
 */
public class ClassMetadata {

    public static Object createKey(Node classNode, String graphKey) {
        return new Key(classNode, graphKey);
    }


    private Node classNode;

    private String graphKey;

    private Map<Node, Set<PathMetadata>> groupPaths;

    private Map<PathMetadata, ClassPropertyMetadata> properties = new HashMap<>();

    private List<ClassMetadata> superClasses;


    public ClassMetadata(Node classNode, String graphKey) {
        this.classNode = classNode;
        this.graphKey = graphKey;
    }


    public synchronized Set<PathMetadata> getGroupPaths(Node group, Graph graph) {
        if (groupPaths == null) {
            groupPaths = new HashMap<>();
            if (JenaNodeUtil.isInstanceOf(classNode, SH.Shape.asNode(), graph)) {
                addGroupProperties(classNode, graph, SH.parameter.asNode());
                addGroupProperties(classNode, graph, SH.property.asNode());
            }
            ExtendedIterator<Triple> it = graph.find(null, SH.targetClass.asNode(), classNode);
            while (it.hasNext()) {
                Node shape = it.next().getSubject();
                addGroupProperties(shape, graph, SH.parameter.asNode());
                addGroupProperties(shape, graph, SH.property.asNode());
            }
        }
        return groupPaths.get(group);
    }


    private void addGroupProperties(Node nodeShape, Graph graph, Node systemPredicate) {
        ExtendedIterator<Triple> it = graph.find(nodeShape, systemPredicate, Node.ANY);
        while (it.hasNext()) {
            Node propertyShape = it.next().getObject();
            if (!graph.contains(propertyShape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
                Node group = JenaNodeUtil.getObject(propertyShape, SH.group.asNode(), graph);
                if (group != null) {
                    Node path = JenaNodeUtil.getObject(propertyShape, SH.path.asNode(), graph);
                    if (path != null) {
                        Set<PathMetadata> paths = groupPaths.get(group);
                        if (paths == null) {
                            paths = new HashSet<>();
                            groupPaths.put(group, paths);
                        }
                        if (path.isURI()) {
                            paths.add(new PathMetadata(path, false));
                        } else {
                            Node inverse = JenaNodeUtil.getObject(path, SH.inversePath.asNode(), graph);
                            if (inverse != null && inverse.isURI()) {
                                paths.add(new PathMetadata(inverse, true));
                            }
                        }
                    }
                }
            }
        }
    }


    public Node getPropertyDescription(Node property, boolean inverse, Graph graph) {
        return nearest(graph, new Function<ClassMetadata, Node>() {
            @Override
            public Node apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getDescription();
            }
        }, null);
    }


    public Node getPropertyEditWidget(Node property, boolean inverse, Graph graph) {
        return nearest(graph, new Function<ClassMetadata, Node>() {
            @Override
            public Node apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getEditWidget();
            }
        }, null);
    }


    public Node getPropertyLocalRange(Node property, boolean inverse, Graph graph) {
        return nearest(graph, new Function<ClassMetadata, Node>() {
            @Override
            public Node apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getLocalRange();
            }
        }, null);
    }


    public Integer getPropertyMaxCount(Node property, boolean inverse, Graph graph) {
        return (Integer) nearestObject(graph, new Function<ClassMetadata, Object>() {
            @Override
            public Object apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getMaxCount();
            }
        }, new HashSet<Node>());
    }


    public Node getPropertyName(Node property, boolean inverse, Graph graph) {
        return nearest(graph, new Function<ClassMetadata, Node>() {
            @Override
            public Node apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getName();
            }
        }, null);
    }


    public Node getPropertyViewWidget(Node property, boolean inverse, Graph graph) {
        return nearest(graph, new Function<ClassMetadata, Node>() {
            @Override
            public Node apply(ClassMetadata cm) {
                return cm.getProperty(property, inverse, graph).getViewWidget();
            }
        }, null);
    }


    public synchronized Iterable<ClassMetadata> getSuperClasses(Graph graph) {
        if (superClasses == null) {
            superClasses = new LinkedList<>();
            ExtendedIterator<Triple> it = graph.find(classNode, RDFS.subClassOf.asNode(), Node.ANY);
            while (it.hasNext()) {
                Node superClass = it.next().getObject();
                superClasses.add(OntologyOptimizations.get().getClassMetadata(superClass, graph, graphKey));
            }
        }
        return superClasses;
    }


    public synchronized ClassPropertyMetadata getProperty(Node predicate, boolean inverse, Graph graph) {
        PathMetadata pm = new PathMetadata(predicate, inverse);
        return getProperty(pm, graph);
    }


    public synchronized ClassPropertyMetadata getProperty(PathMetadata pm, Graph graph) {
        ClassPropertyMetadata result = properties.get(pm);
        if (result == null) {
            result = new ClassPropertyMetadata(classNode, pm.getPredicate(), pm.isInverse(), graph);
            properties.put(pm, result);
        }
        return result;
    }


    /**
     * Walks this and its superclasses until it finds one where the given Supplier returns a value.
     *
     * @param graph
     * @param graph
     * @param visited
     * @return the nearest supplied value
     */
    private Node nearest(Graph graph, Function<ClassMetadata, Node> supplier, Set<Node> visited) {
        Node result = supplier.apply(this);
        if (result != null) {
            return result;
        }
        if (visited == null) {
            visited = new HashSet<>();
        }
        visited.add(classNode);
        for (ClassMetadata superClass : getSuperClasses(graph)) {
            if (!visited.contains(superClass.classNode)) {
                result = superClass.nearest(graph, supplier, visited);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    private Object nearestObject(Graph graph, Function<ClassMetadata, Object> supplier, Set<Node> visited) {
        if (!visited.contains(classNode)) {
            Object result = supplier.apply(this);
            if (result != null) {
                return result;
            }
            visited.add(classNode);
            for (ClassMetadata superClass : getSuperClasses(graph)) {
                result = superClass.nearestObject(graph, supplier, visited);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }


    public void walkSuperClasses(Graph graph, Consumer<ClassMetadata> consumer, Set<Node> visited) {
        if (!visited.contains(classNode)) {
            consumer.accept(this);
            visited.add(classNode);
            for (ClassMetadata superClassMetadata : getSuperClasses(graph)) {
                superClassMetadata.walkSuperClasses(graph, consumer, visited);
            }
        }
    }


    public boolean walkSuperClassesUntil(Graph graph, Predicate<ClassMetadata> predicate, Set<Node> visited) {
        if (!visited.contains(classNode)) {
            if (predicate.test(this)) {
                return true;
            } else {
                visited.add(classNode);
                for (ClassMetadata superClassMetadata : getSuperClasses(graph)) {
                    if (superClassMetadata.walkSuperClassesUntil(graph, predicate, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "ClassMetadata for " + classNode + " with " + properties.size() + " properties";
    }


    private static class Key {

        private Node classNode;

        private String graphKey;


        Key(Node classNode, String graphKey) {
            this.classNode = classNode;
            this.graphKey = graphKey;
        }


        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                return classNode.equals(((Key) obj).classNode) && graphKey.equals(((Key) obj).graphKey);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return classNode.hashCode() + graphKey.hashCode();
        }


        @Override
        public String toString() {
            return graphKey + ".classMetadata." + classNode;
        }
    }
}
