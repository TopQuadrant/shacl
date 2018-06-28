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
package org.topbraid.shacl.validation.sparql;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.jenax.util.ARQFactory;
import org.topbraid.jenax.util.JenaUtil;
import org.topbraid.shacl.model.SHParameterizableTarget;
import org.topbraid.shacl.util.ExecutionPlatform;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.validation.TargetPlugin;
import org.topbraid.shacl.vocabulary.SH;

public class SPARQLTargetPlugin implements TargetPlugin {

	@Override
	public boolean canExecuteTarget(Resource target) {
		return target.hasProperty(SH.select) && ExecutionPlatform.canExecute(target);
	}

	
	@Override
	public Iterable<RDFNode> executeTarget(Dataset dataset, Resource target,
			SHParameterizableTarget parameterizableTarget) {

		String sparql = JenaUtil.getStringProperty(target, SH.select);
		String queryString = SPARQLSubstitutions.withPrefixes(sparql, target);
		Query query;
		try {
			query = getSPARQLWithSelect(target);
		}
		catch(QueryParseException ex) {
			throw new SHACLException("Invalid SPARQL target (" + ex.getLocalizedMessage() + "):\n" + queryString);
		}
		
		QuerySolutionMap bindings = null;
		if(parameterizableTarget != null) {
			bindings = new QuerySolutionMap();
			parameterizableTarget.addBindings(bindings);
		}
		try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
		    Set<RDFNode> results = new HashSet<>();
		    ResultSet rs = qexec.execSelect();
		    List<String> varNames = rs.getResultVars();
		    while(rs.hasNext()) {
		        QuerySolution qs = rs.next();
		        for(String varName : varNames) {
		            RDFNode value = qs.get(varName);
		            if(value != null) {
		                results.add(value);
		            }
		        }
		    }
		    return results;
		}
	}


	@Override
	public boolean isNodeInTarget(RDFNode focusNode, Dataset dataset, Resource executable, SHParameterizableTarget parameterizableTarget) {
		String ask = JenaUtil.getStringProperty(executable, SH.ask);
		if(ask != null) {
			String queryString = SPARQLSubstitutions.withPrefixes(ask, executable);
			Query query;
			try {
				query = ARQFactory.get().createQuery(queryString);
			}
			catch(QueryParseException ex) {
				throw new SHACLException("Invalid SPARQL target (" + ex.getLocalizedMessage() + "):\n" + queryString);
			}
	
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.thisVar.getVarName(), focusNode);
			if(parameterizableTarget != null) {
				parameterizableTarget.addBindings(bindings);
			}
			try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
			    return qexec.execAsk();
			}
		}
		else {
			// If sh:select exists only, then we expect run the query with ?this pre-bound
			String sparql = JenaUtil.getStringProperty(executable, SH.select);
			String queryString = SPARQLSubstitutions.withPrefixes(sparql, executable);
			Query query;
			try {
				query = ARQFactory.get().createQuery(queryString);
			}
			catch(QueryParseException ex) {
				throw new SHACLException("Invalid SPARQL target (" + ex.getLocalizedMessage() + "):\n" + queryString);
			}
	
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.thisVar.getVarName(), focusNode);
			if(parameterizableTarget != null) {
				parameterizableTarget.addBindings(bindings);
			}
			try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
			    ResultSet rs = qexec.execSelect();
			    return rs.hasNext();
			}
		}
	}
	
	
	private static Query getSPARQLWithSelect(Resource host) {
		String sparql = JenaUtil.getStringProperty(host, SH.select);
		if(sparql == null) {
			throw new SHACLException("Missing sh:select at " + host);
		}
		try {
			return ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(sparql, host));
		}
		catch(Exception ex) {
			return ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes("SELECT ?this WHERE {" + sparql + "}", host));
		}
	}
}
