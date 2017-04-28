package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

class SPARQLRule implements Rule {
	
	private Query query;
	
	
	SPARQLRule(Resource rule) {
		String rawString = JenaUtil.getStringProperty(rule, SH.construct);
		String queryString = SPARQLSubstitutions.withPrefixes(rawString, rule);
		query = ARQFactory.get().createQuery(queryString);
		if(!query.isConstructType()) {
			throw new IllegalArgumentException("Values of sh:construct must be CONSTRUCT queries");
		}
	}
	
	
	@Override
	public int execute(RuleEngine engine, List<RDFNode> focusNodes) {
		int sum = 0;
		Model inf = engine.getInferencesModel();
		for(RDFNode focusNode : focusNodes) {
			QuerySolutionMap bindings = new QuerySolutionMap();
			bindings.add(SH.thisVar.getVarName(), focusNode);
			try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, engine.getDataset(), bindings)) {
				Model constructed = qexec.execConstruct();
				int added = 0;
				for(Statement s : constructed.listStatements().toList()) {
					if(!inf.contains(s)) {
						added++;
						inf.add(s);
					}
				}
				sum += added;
			}
		}
		return sum;
	}
}
