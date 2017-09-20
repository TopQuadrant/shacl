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

import java.util.Collections;
import java.util.List;

import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.FmtUtils;

public class ConstantTermExpression extends AtomicNodeExpression {
	
	private List<RDFNode> result;
	
	private RDFNode term;
	
	public ConstantTermExpression(RDFNode term) {
		this.result = Collections.singletonList(term);
		this.term = term;
	}


	@Override
	public List<RDFNode> eval(RDFNode focusNode, NodeExpressionContext context) {
		return result;
	}

	
	@Override
	public String toString() {
		return FmtUtils.stringForRDFNode(term);
	}
}
