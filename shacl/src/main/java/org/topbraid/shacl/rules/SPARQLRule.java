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

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.jenax.progress.ProgressMonitor;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;

public class SPARQLRule extends AbstractRule {
	
	private Query query;
	
	
	public SPARQLRule(Resource rule) {
		super(rule);
		String rawString = JenaUtil.getStringProperty(rule, SH.construct);
		String queryString = SPARQLSubstitutions.withPrefixes(rawString, rule);
		query = ARQFactory.get().createQuery(queryString);
		if(!query.isConstructType()) {
			throw new IllegalArgumentException("Values of sh:construct must be CONSTRUCT queries");
		}
	}
	
	
	@Override
	public void execute(RuleEngine ruleEngine, List<RDFNode> focusNodes, Shape shape) {
		ProgressMonitor monitor = ruleEngine.getProgressMonitor();
		for(RDFNode focusNode : focusNodes) {
			
			if(monitor != null && monitor.isCanceled()) {
				return;
			}

			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.thisVar.getVarName(), focusNode);
			try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, ruleEngine.getDataset(), bindings)) {
				Iterator<Triple> it = qexec.execConstructTriples();
				while(it.hasNext()) {
					Triple triple = it.next();
					ruleEngine.infer(triple, this, shape);
				}
			}
		}
	}
	
	
	public Query getQuery() {
		return query;
	}
	
	
	@Override
    public String toString() {
		String label = JenaUtil.getStringProperty(getResource(), RDFS.label);
		if(label == null) {
			Statement s = getResource().getProperty(SH.construct);
			if(s != null && s.getObject().isLiteral()) {
				label = "\n" + s.getString();
			}
			else {
				label = "(Missing SPARQL query)";
			}
		}
		return getLabelStart("SPARQL") + label;
	}
}
