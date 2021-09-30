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
package org.topbraid.shacl.arq.functions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetGraph;
import org.topbraid.shacl.multifunctions.AbstractMultiFunction1;
import org.topbraid.shacl.util.SHACLUtil;
import org.topbraid.shacl.vocabulary.TOSH;

/**
 * The property function tosh:targetContains.
 * Binds the variable on the right hand side with all focus nodes produced by the
 * SHACL target on the left hand side.
 * 
 * 		(?myTarget ?shapesGraph) tosh:targetContains ?focusNode .
 * 
 * @author Holger Knublauch
 */
public class TargetContainsMultiFunction extends AbstractMultiFunction1 {
	
	public TargetContainsMultiFunction() {
		super(TOSH.targetContains.getURI(), Arrays.asList("target", "shapesGraph"), "focusNode"); 
	}

	@Override
	protected Iterator<Node> executeIterator(List<Node> args, Graph activeGraph, DatasetGraph dataset) {
		
		Node targetNode = args.get(0);
		Node shapesGraphNode = args.get(1);
		
		Model model = ModelFactory.createModelForGraph(dataset.getGraph(shapesGraphNode));
		Resource target = (Resource) model.asRDFNode(targetNode);

		Set<Node> focusNodes = new HashSet<>();
		SHACLUtil.addNodesInTarget(target, DatasetFactory.wrap(dataset), focusNodes);
		return focusNodes.iterator();
	}
}
