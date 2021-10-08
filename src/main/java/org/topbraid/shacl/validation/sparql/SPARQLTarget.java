package org.topbraid.shacl.validation.sparql;

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
import org.topbraid.shacl.targets.Target;
import org.topbraid.shacl.validation.SHACLException;
import org.topbraid.shacl.vocabulary.SH;

public class SPARQLTarget implements Target {
	
	private Query query;
	
	private SHParameterizableTarget parameterizableTarget;
	
	
	SPARQLTarget(Resource executable, SHParameterizableTarget parameterizableTarget) {
		
		this.parameterizableTarget = parameterizableTarget;

		String sparql = JenaUtil.getStringProperty(executable, SH.select);
		if(sparql == null) {
			throw new SHACLException("Missing sh:select at " + executable);
		}
		try {
			query = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes(sparql, executable));
		}
		catch(Exception ex) {
			try {
				query = ARQFactory.get().createQuery(SPARQLSubstitutions.withPrefixes("SELECT ?this WHERE {" + sparql + "}", executable));
			}
			catch(QueryParseException ex2) {
				throw new SHACLException("Invalid SPARQL target (" + ex2.getLocalizedMessage() + ")");
			}
		}
	}

	
	@Override
	public void addTargetNodes(Dataset dataset, Set<RDFNode> results) {
		QuerySolutionMap bindings = null;
		if(parameterizableTarget != null) {
			bindings = new QuerySolutionMap();
			parameterizableTarget.addBindings(bindings);
		}
		try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
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
		}
	}

	
	@Override
	public boolean contains(Dataset dataset, RDFNode node) {
		QuerySolutionMap bindings = new QuerySolutionMap();
		bindings.add(SH.thisVar.getVarName(), node);
		if(parameterizableTarget != null) {
			parameterizableTarget.addBindings(bindings);
		}
		if(query.isAskType()) {
			try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
			    return qexec.execAsk();
			}
		}
		else {
			try(QueryExecution qexec = SPARQLSubstitutions.createQueryExecution(query, dataset, bindings)) {
			    ResultSet rs = qexec.execSelect();
			    return rs.hasNext();
			}
		}
	}
}
