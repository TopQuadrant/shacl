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
import org.topbraid.jenax.util.JenaDatatypes;
import org.topbraid.jenax.util.JenaNodeUtil;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.shacl.vocabulary.TOSH;

import java.util.LinkedList;
import java.util.List;

/**
 * Metadata about a property at a given class, possibly in the inverse direction.
 * Populated from SHACL constraints and plugins (currently including OWL restrictions
 * and - within the TopBraid ecosystem - SPIN constraints).
 *
 * @author Holger Knublauch
 */
public class ClassPropertyMetadata {

    private Node description;

    private Node editWidget;

    private boolean inverse;

    private Node localRange;

    private Integer maxCount;

    private Node name;

    private Node order;

    private Node predicate;

    private Node viewWidget;


    ClassPropertyMetadata(Node classNode, Node predicate, boolean inverse, Graph graph) {

        this.inverse = inverse;
        this.predicate = predicate;

        // Init from SHACL shapes
        if (SHACLUtil.exists(graph)) {
            if (JenaNodeUtil.isInstanceOf(classNode, SH.Shape.asNode(), graph)) {
                initFromShape(classNode, graph);
            }
            ExtendedIterator<Triple> it = graph.find(null, SH.targetClass.asNode(), classNode);
            while (it.hasNext()) {
                Node shape = it.next().getSubject();
                initFromShape(shape, graph);
            }
        }

        if (!inverse) {
            for (Plugin plugin : plugins) {
                plugin.init(this, classNode, graph);
            }
        }
    }


    public Node getDescription() {
        return description;
    }


    public Node getEditWidget() {
        return editWidget;
    }


    // Currently not supported for inverse properties (not used yet)
    public Node getLocalRange() {
        return localRange;
    }


    public Integer getMaxCount() {
        return maxCount;
    }


    public Node getName() {
        return name;
    }


    public Node getOrder() {
        return order;
    }


    public Node getPredicate() {
        return predicate;
    }


    public Node getViewWidget() {
        return viewWidget;
    }


    private void initFromShape(Node shape, Graph graph) {
        if (!graph.contains(shape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
            initFromShape(shape, SH.property.asNode(), graph);
            initFromShape(shape, SH.parameter.asNode(), graph);
        }
    }


    private void initFromShape(Node shape, Node systemPredicate, Graph graph) {
        ExtendedIterator<Triple> it = graph.find(shape, systemPredicate, Node.ANY);
        while (it.hasNext()) {
            Node propertyShape = it.next().getObject();
            if (!propertyShape.isLiteral()) {
                if (hasMatchingPath(propertyShape, graph)) {
                    if (!graph.contains(propertyShape, SH.deactivated.asNode(), JenaDatatypes.TRUE.asNode())) {
                        if (description == null) {
                            description = JenaNodeUtil.getObject(propertyShape, SH.description.asNode(), graph);
                        }
                        if (localRange == null) {
                            if (inverse) {
                                // Maybe: support inverse ranges
                            } else {
                                localRange = SHACLUtil.walkPropertyShapesHelper(propertyShape, graph);
                            }
                        }
                        if (maxCount == null) {
                            Node maxCountNode = JenaNodeUtil.getObject(propertyShape, SH.maxCount.asNode(), graph);
                            if (maxCountNode != null && maxCountNode.isLiteral()) {
                                Object value = maxCountNode.getLiteralValue();
                                if (value instanceof Number) {
                                    maxCount = ((Number) value).intValue();
                                }
                            }
                        }
                        if (name == null) {
                            name = JenaNodeUtil.getObject(propertyShape, SH.name.asNode(), graph);
                        }
                        if (order == null) {
                            order = JenaNodeUtil.getObject(propertyShape, SH.order.asNode(), graph);
                        }
                        if (viewWidget == null) {
                            viewWidget = JenaNodeUtil.getObject(propertyShape, TOSH.viewWidget.asNode(), graph);
                        }
                    }
                }
            }
        }
    }


    public boolean hasMatchingPath(Node propertyShape, Graph graph) {
        if (inverse) {
            Node path = JenaNodeUtil.getObject(propertyShape, SH.path.asNode(), graph);
            if (path != null && path.isBlank()) {
                return predicate.equals(JenaNodeUtil.getObject(path, SH.inversePath.asNode(), graph));
            } else {
                return false;
            }
        } else {
            return graph.contains(propertyShape, SH.path.asNode(), predicate);
        }
    }


    public boolean isInverse() {
        return inverse;
    }


    public void setLocalRange(Node value) {
        this.localRange = value;
    }


    public void setMaxCount(int value) {
        this.maxCount = value;
    }


    @Override
    public String toString() {
        return "ClassPropertyMetadata for " + (inverse ? "^" : "") + predicate;
    }


    // Abstraction layer for OWL and SPIN

    private static List<Plugin> plugins = new LinkedList<>();

    public static void register(Plugin plugin) {
        plugins.add(plugin);
    }

    static {
        register(new OWLClassPropertyMetadataPlugin());
    }

    public interface Plugin {
        void init(ClassPropertyMetadata cpm, Node classNode, Graph graph);
    }
}
