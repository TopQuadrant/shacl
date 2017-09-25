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

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.AbstractFunction;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionFactory;

/**
 * The built-in function spin:ask.
 * 
 * Can be used to invoke a given sp:Ask query.
 * 
 * @author Holger Knublauch
 */
public class AskFunction extends AbstractFunction implements FunctionFactory {
	
	@Override
	public Function create(String uri) {
		return this;
	}

	
	@Override
	protected NodeValue exec(Node[] nodes, FunctionEnv env) {
		if(nodes.length == 0) {
			throw new ExprEvalException("Missing arguments");
		}
		Model model = ModelFactory.createModelForGraph(env.getActiveGraph());
		QuerySolutionMap initialBinding = SPINFunctionUtil.getInitialBinding(nodes, model);
		Query spinQuery;
		Resource queryOrTemplateCall = model.asRDFNode(nodes[0]).asResource();
		if(SPINFactory.isTemplateCall(queryOrTemplateCall)) {
			TemplateCall templateCall = SPINFactory.asTemplateCall(queryOrTemplateCall);
			spinQuery = SPINFactory.asQuery(templateCall.getTemplate().getBody());
			SPINFunctionUtil.addBindingsFromTemplateCall(initialBinding, templateCall);
		}
		else {
			spinQuery = SPINFactory.asQuery(queryOrTemplateCall);
		}
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(env.getDataset()));
		org.apache.jena.query.Query arqQuery = ARQFactory.get().createQuery(spinQuery);
		try (QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, initialBinding)) {
		    boolean result = qexec.execAsk();
		    return NodeValue.makeBoolean(result);
		}
	}
}
