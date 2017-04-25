package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.util.JenaUtil;

public class SPARQLSelectRuleLanguage implements RuleLanguage {
	
	@Override
	public int execute(Resource rule, RuleEngine engine, List<RDFNode> focusNodes) {
		String rawString = JenaUtil.getStringProperty(rule, SH.select);
		String queryString = SPARQLSubstitutions.withPrefixes(rawString, rule);
		Query query = ARQFactory.get().createQuery(queryString);
		if(!query.isSelectType()) {
			throw new IllegalArgumentException("Values of sh:select must be SELECT queries");
		}
		
		Resource pred = JenaUtil.getResourceProperty(rule, SH.predicate);
		if(pred == null || !pred.isURIResource()) {
			throw new IllegalArgumentException("SPARQL SELECT rule requires a sh:predicate URI");
		}
		Property predicate = JenaUtil.asProperty(pred);

		int sum = 0;
		Model inf = engine.getInferencesModel();
		for(RDFNode focusNode : focusNodes) {
			if(focusNode instanceof Resource) {
				QuerySolutionMap bindings = new QuerySolutionMap();
				bindings.add(SH.thisVar.getVarName(), focusNode);
				try(QueryExecution qexec = ARQFactory.get().createQueryExecution(query, engine.getDataset(), bindings)) {
					ResultSet rs = qexec.execSelect();
					List<String> varNames = rs.getResultVars();
					if(varNames.size() != 1) {
						throw new IllegalArgumentException("SPARQL SELECT rule must return exactly one variable");
					}
					int added = 0;
					String varName = varNames.get(0);
					while(rs.hasNext()) {
						QuerySolution qs = rs.next();
						RDFNode object = qs.get(varName);
						if(object != null) {
							Statement s = inf.createStatement((Resource)focusNode, predicate, object);
							if(!inf.contains(s)) {
								added++;
								inf.add(s);
							}
						}
					}
					sum += added;
				}
			}
		}
		return sum;
	}

	
	@Override
	public Property getKeyProperty() {
		return SH.select;
	}
}
