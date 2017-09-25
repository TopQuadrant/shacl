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

import java.util.List;

import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.arq.DatasetWithDifferentDefaultModel;
import org.topbraid.spin.model.Query;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.TemplateCall;
import org.topbraid.spin.vocabulary.SPIN;

import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.DatasetImpl;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.engine.iterator.QueryIterConcat;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.IterLib;

/**
 * The built-in magic property spin:construct.
 * 
 * Executes a given sp:Construct and binds the resulting s,p,o values to
 * the variables on the right hand side.
 * 
 * @author Holger Knublauch
 */
public class ConstructPFunction extends PropertyFunctionBase {
	
	private final static String NAME = SPIN.PREFIX + ":" + SPIN.construct.getLocalName();

	@Override
	public QueryIterator exec(Binding binding, PropFuncArg argSubject,
			Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {

		argSubject = Substitute.substitute(argSubject, binding);
		argObject = Substitute.substitute(argObject, binding);

		List<Node> objects = SPINFunctionUtil.getNodes(argObject);
		if(objects.size() != 3) {
			throw new ExprEvalException(NAME + " must have three nodes on the right side");
		}
		Model model = ModelFactory.createModelForGraph(execCxt.getActiveGraph());
		QuerySolutionMap initialBinding = SPINFunctionUtil.getInitialBinding(SPINFunctionUtil.getNodes(argSubject).toArray(new Node[0]), model);
		Query spinQuery;
		Resource qot = SPINFunctionUtil.getQueryOrTemplateCall(argSubject, model);
		if(SPINFactory.isTemplateCall(qot)) {
			TemplateCall templateCall = SPINFactory.asTemplateCall(qot);
			spinQuery = SPINFactory.asQuery(templateCall.getTemplate().getBody());
			SPINFunctionUtil.addBindingsFromTemplateCall(initialBinding, templateCall);
		}
		else {
			spinQuery = SPINFactory.asQuery(qot);
		}
		org.apache.jena.query.Query arqQuery = ARQFactory.get().createQuery(spinQuery);
		Dataset dataset = new DatasetWithDifferentDefaultModel(model, DatasetImpl.wrap(execCxt.getDataset()));
		try(QueryExecution qexec = ARQFactory.get().createQueryExecution(arqQuery, dataset, initialBinding)) {
		    Model result = qexec.execConstruct();
		    QueryIterConcat concat = new QueryIterConcat(execCxt);
		    for(Triple triple : GraphUtil.findAll(result.getGraph()).toList()) {
		        BindingMap bindingMap = new BindingHashMap(binding);
		        if(perhapsAdd(objects.get(0), triple.getSubject(), bindingMap)) {
		            if(perhapsAdd(objects.get(1), triple.getPredicate(), bindingMap)) {
		                if(perhapsAdd(objects.get(2), triple.getObject(), bindingMap)) {
		                    concat.add(IterLib.result(bindingMap, execCxt));
		                }
		            }
		        }
		    }
		    return concat;
		}
	}
	
	
	private boolean perhapsAdd(Node matchNode, Node tripleNode, BindingMap bindingMap) {
		if(matchNode == null) {
			if(tripleNode != null) {
				return false;
			}
		}
		else if(matchNode.isVariable()) {
			if(tripleNode != null) {
				bindingMap.add((Var)matchNode, tripleNode);
			}
		}
		else if(tripleNode == null || !matchNode.matches(tripleNode)) {
			return false;
		}
		return true;
	}
}
