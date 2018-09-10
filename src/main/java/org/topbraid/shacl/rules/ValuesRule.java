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

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.RDFNode;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.expr.NodeExpression;

class ValuesRule implements Rule {
	
	private NodeExpression expr;
	
	private boolean inverse;
	
	private Node predicate;

	
	ValuesRule(NodeExpression expr, Node predicate, boolean inverse) {
		this.expr = expr;
		this.predicate = predicate;
		this.inverse = inverse;
	}


	@Override
	public void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape) {
		ProgressMonitor monitor = ruleEngine.getProgressMonitor();
		for(RDFNode focusNode : focusNodes) {
			
			if(monitor != null && monitor.isCanceled()) {
				return;
			}

			Iterator<RDFNode> objects = expr.eval(focusNode, ruleEngine);
			while(objects.hasNext()) {
				RDFNode object = objects.next();
				Triple triple = inverse ?
						Triple.create(object.asNode(), predicate, focusNode.asNode()) :
						Triple.create(focusNode.asNode(), predicate, object.asNode());
				ruleEngine.infer(triple, this, shape);
			}	
		}
	}
	
	
	@Override
	public Node getContextNode() {
		return expr.getRDFNode().asNode();
	}


	@Override
	public Number getOrder() {
		return 0;
	}


	@Override
    public String toString() {
		String label = expr.toString();
		return label;
	}
}
