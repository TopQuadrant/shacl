package org.topbraid.shacl.rules;

import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.shacl.engine.Shape;
import org.topbraid.shacl.validation.sparql.SPARQLSubstitutions;
import org.topbraid.shacl.vocabulary.SH;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.progress.ProgressMonitor;
import org.topbraid.spin.util.JenaUtil;

public class SPARQLRule extends Rule {
	
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
				Model constructed = qexec.execConstruct();
				for(Statement s : constructed.listStatements().toList()) {
					ruleEngine.infer(s.asTriple(), this, shape);
				}
			}
		}
	}
	
	
	public Query getQuery() {
		return query;
	}
	
	
	@Override
    public String toString() {
		String label = getLabel();
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
