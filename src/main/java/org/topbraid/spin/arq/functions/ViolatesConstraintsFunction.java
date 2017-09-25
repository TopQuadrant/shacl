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
package org.topbraid.spin.arq.functions;

import java.util.LinkedList;
import java.util.List;

import org.topbraid.spin.arq.AbstractFunction2;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.model.QueryOrTemplateCall;
import org.topbraid.spin.progress.NullProgressMonitor;
import org.topbraid.spin.progress.ProgressMonitor;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;

public class ViolatesConstraintsFunction extends AbstractFunction2  implements FunctionFactory {
	
	@Override
	public Function create(String uri) {
		return this;
	}

	
	@Override
	protected NodeValue exec(Node instanceNode, Node classNode, FunctionEnv env) {
		
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		
		// Collect all constraints defined at the class and its superclasses
		Resource cls = (Resource)model.asRDFNode(classNode);
		
		List<QueryOrTemplateCall> qots = ConstructViolationsPFunction.getConstraints(cls);

		Resource instance = (Resource) model.asRDFNode(instanceNode);
		ProgressMonitor monitor = new NullProgressMonitor();
		List<ConstraintViolation> results = new LinkedList<ConstraintViolation>();
		for(QueryOrTemplateCall qot : qots) {
			if(qot.getTemplateCall() != null) {
				SPINConstraints.addTemplateCallResults(results, qot, instance, false, monitor);
			}
			else if(qot.getQuery() != null) {
				SPINConstraints.addQueryResults(results, qot, instance, false, null, monitor);
			}
			if(!results.isEmpty()) {
				return NodeValue.makeBoolean(true);
			}
		}

		return NodeValue.makeBoolean(false);
	}
}
