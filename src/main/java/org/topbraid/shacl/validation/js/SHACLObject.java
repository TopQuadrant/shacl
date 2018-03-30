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
package org.topbraid.shacl.validation.js;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.ExprNotComparableException;
import org.apache.jena.sparql.expr.NodeValue;
import org.topbraid.shacl.engine.ShapesGraph;
import org.topbraid.shacl.js.model.JSTerm;
import org.topbraid.shacl.validation.ValidationEngineFactory;
import org.topbraid.shacl.vocabulary.SH;

public class SHACLObject {
	
	private Dataset dataset;
	
	private URI shapesGraphURI;
	
	
	public SHACLObject(URI shapesGraphURI, Dataset dataset) {
		this.shapesGraphURI = shapesGraphURI;
		this.dataset = dataset;
	}
	
	
	public Integer compareNodes(JSTerm node1, JSTerm node2) {
		try {
			if(node1.isURI() && node2.isURI()) {
				return node1.getUri().compareTo(node2.getUri());
			}
			else {
				return NodeValue.compare(NodeValue.makeNode(node1.getNode()), NodeValue.makeNode(node2.getNode()));
			}
		}
		catch(ExprNotComparableException ex) {
			return null;
		}
	}
	
	
	public boolean nodeConformsToShape(JSTerm node, JSTerm shape) {
		try {
		Model shapesModel = dataset.getNamedModel(shapesGraphURI.toString());
		ShapesGraph shapesGraph = new ShapesGraph(shapesModel);
		List<RDFNode> focusNodes = Collections.singletonList(dataset.getDefaultModel().asRDFNode(node.getNode()));
		Resource report = ValidationEngineFactory.get().create(dataset, shapesGraphURI, shapesGraph, null).
				validateNodesAgainstShape(focusNodes, shape.getNode());
		return !report.hasProperty(SH.result);
		}
		catch(StackOverflowError ex) {
			return false;
		}
	}
}
